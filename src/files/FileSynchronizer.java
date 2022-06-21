package files;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import files.entity.Commands;
import files.entity.LogInfo;
import files.entity.SyncEnum;
import files.entity.SyncParameters;
import files.resume.ResumePointLog;
import files.resume.ShutDownLogHandler;
import files.service.DirWatchService;
import files.service.PrintService;
import files.service.UpdateService;
import utils.*;

import java.io.*;
import java.util.Scanner;

public class FileSynchronizer {
    private final long MAX_SIZE = SyncParameters.PART_SIZE;
    private long partSize = MAX_SIZE;

    private static Configurations conf = new Configurations();

    private static final BasicAWSCredentials credentials = new BasicAWSCredentials(conf.accessKey, conf.secretKey);
    private static final ClientConfiguration ccfg = new ClientConfiguration().withUseExpectContinue(false);

    private static final AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(conf.serviceEndpoint, conf.signingRegion);

    private static final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withClientConfiguration(ccfg)
            .withEndpointConfiguration(endpoint)
            .withPathStyleAccessEnabled(true)
            .build();

    private static long pos;  //偏移量
    private static long contentLength;  //文件总长度
    private static String eTag;  //文件id
    /**
     * 同步到本地目录
     */
    public void syncForInit() {

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(conf.bucketName);
        ObjectListing objectListing;
        S3Object o = null;
        S3ObjectInputStream s3is = null;
        RandomAccessFile raf = null;

        boolean hasResume = false;
        LogInfo lastProgressInfo = ResumePointLog.readLog(conf.lastTransferLogDir);
        if(lastProgressInfo != null){
            hasResume = true;
            System.out.println("[Advice] - resume last progress: ");
        }

        File dir = new File(conf.workDirectory);
        if(!dir.exists()){
            dir.mkdir();
        }
        do {
            objectListing = s3.listObjects(listObjectsRequest);

            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                eTag = objectSummary.getETag();
                String keyName = objectSummary.getKey();
                if (keyName.endsWith("/")) {
                    continue;
                }
                File file = new File(dir, keyName);
                File fileParent = file.getParentFile();
                while (!fileParent.exists()) {
                    fileParent.mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    final GetObjectRequest downloadRequest = new GetObjectRequest(conf.bucketName, keyName);
                    ObjectMetadata oMetaData = s3.getObjectMetadata(conf.bucketName, keyName);
                    contentLength = oMetaData.getContentLength();

                    if(eTag != null && file.length() == contentLength){
                        //System.out.println("skip.");
                        continue;
                    }

                    raf = new RandomAccessFile(file,"rw");
                    pos = 0;
                    boolean resume = false;
                    if(hasResume){
                            if(eTag.equals(lastProgressInfo.geteTag())
                                    && lastProgressInfo.getContentLength() == contentLength){
                                pos = lastProgressInfo.getPos();
                                resume = true;
                            }
                    }
                    long start = pos / MAX_SIZE + 1;

                    for (long i = start; pos < contentLength; i++) {
                        partSize = MAX_SIZE;
                        partSize = Math.min(partSize, contentLength - pos);

                        downloadRequest.setRange(pos, pos + partSize);
                        o = s3.getObject(downloadRequest);
                        if(!resume)
                            PrintService.printProgress(start,i,contentLength/MAX_SIZE + 1,keyName);
                        else
                            PrintService.printResume(start,i,contentLength/MAX_SIZE + 1,keyName);

                        s3is = o.getObjectContent();
                        raf.seek(pos);
                        byte[] read_buf = new byte[1 << 10];
                        int read_len = 0;
                        while ((read_len = s3is.read(read_buf)) > 0) {
                            raf.write(read_buf, 0, read_len);
                        }
                        pos += partSize + 1;
                    }
                    System.out.println(".");
                    //System.out.print("\ndownload completed.");
                    //System.out.format("save %s to %s.\n", keyName, dir);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (s3is != null) try { s3is.close(); } catch (IOException e) { }
                    if (raf != null) try { raf.close(); } catch (IOException e) { }
                }

            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
    }

    public static void upload(String dir){
        if(dir == null) return;
        //String relativePath = dir.replace(conf.workDirectory+"\\","");
        //System.out.println(relativePath);
        UpdateService updateService = new UpdateService(SyncEnum.OPERATION_UPLOAD,s3,conf,dir);
        new Thread(updateService).start();
        System.out.println("[Advice] - upload completed - " + dir);
        //System.out.println("file " + dir + " upload completed.");
    }

    public static void delete(String dir){
        if(dir == null) return;
        UpdateService updateService = new UpdateService(SyncEnum.OPERATION_DELETE,s3,conf,dir);
        new Thread(updateService).start();
        System.out.println("[Advice] - delete completed - " + dir);
        //System.out.println("file " + dir + " delete completed.");
    }

    public void clearFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        //System.out.println(args.length);
        if(args.length == 2) {
            //if(args[0].matches("(/([a-zA-Z0-9][a-zA-Z0-9_\\-]{0,255}/)*([a-zA-Z0-9][a-zA-Z0-9_\\-]{0,255})|/)")
            //&& args[1].matches("^[a-zA-Z0-9](?:[a-zA-Z0-9 ._-]*[a-zA-Z0-9])?\\\\.[a-zA-Z0-9_-]+$")) {
                conf.workDirectory = args[0];
                conf.lastTransferLogDir = args[1];
            //}
            //else {
            //    System.out.println("illegal parameters.");
            //    System.exit(-1);
            //}

        }
        else if(args.length == 0){
            System.out.println("using default config.");
        }
        else {
            System.out.println("error! please check parameters.");
            System.exit(-1);
        }
        //SignalHandlerImpl signalHandlerImpl = new SignalHandlerImpl(eTag,pos,contentLength,conf);
        //signalHandlerImpl.registerSignal("TERM");    // 15: 正常终止后台
        //signalHandlerImpl.registerSignal("INT");    // 2: 正常终止前台 CTRL+C
        Thread watchInput = new Thread(()->{
            Scanner sc = new Scanner(System.in);
            while(sc.hasNext()){
                String input = sc.nextLine();
                if(Commands.DISPLAY_TREE.equals(input)){
                    File f=new File(conf.workDirectory);
                    PrintService.tree(f,1);
                }
                else if(Commands.HELP_MENU.equals(input)){
                    PrintService.displayHelpMenu();
                }
                else {
                    PrintService.invalidCommandHandler();
                }
            }
        });
        watchInput.start();
        FileSynchronizer fs = new FileSynchronizer();
        //注册回调线程，处理正常结束的信号
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            new ShutDownLogHandler(conf,eTag,pos,contentLength);
        }));
        fs.syncForInit();

        DirWatchService.watch(conf);


        ResumePointLog.writeLog(new LogInfo(null, 0L, 0L),conf.lastTransferLogDir);
    }

}
