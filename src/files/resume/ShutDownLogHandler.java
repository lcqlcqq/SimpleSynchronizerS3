package files.resume;

import files.entity.LogInfo;
import utils.Configurations;

public class ShutDownLogHandler{
    //收到结束信号后处理断点，文件信息
    public ShutDownLogHandler(Configurations conf, String eTag, Long pos, Long contentLength){
        ResumePointLog.writeLog(new LogInfo(eTag,pos,contentLength),conf.lastTransferLogDir);
        System.out.println("Synchronizer unexpectedly exit.");
    }
}
