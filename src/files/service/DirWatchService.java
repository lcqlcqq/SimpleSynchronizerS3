package files.service;

import com.sun.nio.file.SensitivityWatchEventModifier;
import files.FileSynchronizer;
import utils.Configurations;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class DirWatchService {
    private static Configurations conf;
    private static String dir;
    public static void watch(Configurations cf) {
        conf = cf;
        dir = conf.workDirectory;
        try (WatchService watchService = FileSystems.getDefault().newWatchService()){
            Path path = Paths.get(dir);
            //监听文件增删改事件
            path.register(watchService, new WatchEvent.Kind[]{
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                    },
                    SensitivityWatchEventModifier.HIGH);
            File f = new File(dir);
            File[] tempList = f.listFiles();
            for (int i=0; i<tempList.length; i++){
                if(tempList[i].isDirectory()){
                    Path subPath = Paths.get(tempList[i].getPath());
                    subPath.register(watchService, new WatchEvent.Kind[]{
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_DELETE
                            },
                            SensitivityWatchEventModifier.HIGH);
                }
            }
            while (true) {
                //返回排队的 key。如果没有排队的密钥可用，则此方法等待。
                WatchKey key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;

                    Path filename = watchEventPath.context();


                    Path fileDir = (Path) key.watchable();
                    Path fullDir = fileDir.resolve(watchEventPath.context());
                    File nf = new File(String.valueOf(fileDir));
                    System.out.println("变化对象是 " + filename);
                    //if(nf.isDirectory()) continue;
                    System.out.println("[FileWatchService] - filename: " + fullDir + " ; EVENT: " + kind.name().split("_")[1]);

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("[Advice] - begin uploading: " + filename);
                        FileSynchronizer.upload(String.valueOf(fullDir));
                        //System.out.println(filename + "被创建");
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("[Advice] - begin modification: " + filename);
                        FileSynchronizer.delete(String.valueOf(fullDir));
                        FileSynchronizer.upload(String.valueOf(fullDir));
                        //System.out.println(filename + "被修改");
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("[Advice] - begin deleting: " + filename);
                        FileSynchronizer.delete(String.valueOf(fullDir));
                        //System.out.println(filename + "被删除");
                    }

                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }




}
