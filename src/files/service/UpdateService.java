package files.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import utils.Configurations;
import files.entity.SyncEnum;
import files.entity.SyncParameters;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UpdateService implements Runnable{

    private SyncEnum type;
    private AmazonS3 s3;
    private Configurations conf;
    private String filePath;
    private long partSize;

    public UpdateService(SyncEnum type, AmazonS3 s3, Configurations conf,String relativePath) {
        this.type = type;
        this.s3 = s3;
        this.conf = conf;
        this.filePath = relativePath;

    }

    @Override
    public void run() {
        if(type == SyncEnum.OPERATION_UPLOAD){
            putObject();
        }else if(type == SyncEnum.OPERATION_DELETE){
            deleteObject();
        }
    }

    /**
     * 上传，分段
     */
    private void putObject(){

        final String keyName = Paths.get(filePath).getFileName().toString();
        final File file = new File(filePath);
        ArrayList<PartETag> partETags = new ArrayList<PartETag>();

        long contentLength = file.length();
        PrintService.printSize(contentLength);

        String uploadId = null;

        try {
            InitiateMultipartUploadRequest initRequest =
                    new InitiateMultipartUploadRequest(conf.bucketName, keyName);
            uploadId = s3.initiateMultipartUpload(initRequest).getUploadId();
            //System.out.format("Created upload ID was %s\n", uploadId);

            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                partSize = 0;
                partSize = Math.min(SyncParameters.PART_SIZE, contentLength - filePosition);

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(conf.bucketName)
                        .withKey(keyName)
                        .withUploadId(uploadId)
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                PrintService.printProgress(1,i,contentLength/SyncParameters.PART_SIZE + 1,keyName);
                partETags.add(s3.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
                System.out.println(".");
            }

            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(conf.bucketName, keyName, uploadId, partETags);
            s3.completeMultipartUpload(compRequest);
            System.out.println("[Advice] - upload completed.");
        } catch (Exception e) {
            e.printStackTrace();
            if (uploadId != null && !uploadId.isEmpty()) {
                System.out.println("[Error] - Aborting upload");
                s3.abortMultipartUpload(new AbortMultipartUploadRequest(conf.bucketName, keyName, uploadId));
            }
        }
    }

    /**
     * 删除对象
     */
    private void deleteObject(){
        final String keyName = Paths.get(filePath).getFileName().toString();
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(conf.bucketName,keyName);
        s3.deleteObject(deleteObjectRequest);
        System.out.println("[Advice] - delete complete.");
    }

}
