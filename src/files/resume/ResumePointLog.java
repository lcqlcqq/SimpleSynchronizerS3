package files.resume;

import files.entity.LogInfo;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ResumePointLog {
    public static void writeLog(LogInfo obj, String logDir) {
        File file =new File(logDir);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            System.out.println("\nsaving resume point...");
            objOut.flush();
            objOut.close();
            out.close();
        } catch (IOException e) {
        }
    }
    public static LogInfo readLog(String logDir){
        File file = new File(logDir);
        FileInputStream in;
        LogInfo res = null;
        try{
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            res = (LogInfo) objIn.readObject();
            System.out.println("\npreparing resume log...");
            objIn.close();
            in.close();

        } catch (Exception e){
        }
        return res;
    }
    public static void writePauseLog(List<LogInfo> obj, String logDir) {
        File file =new File(logDir);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            System.out.println("writing log for pause...");
            objOut.flush();
            objOut.close();
            out.close();
        } catch (IOException e) {
        }
    }
    public static List<LogInfo> readPauseLog(String logDir){
        File file = new File(logDir);
        FileInputStream in;
        List<LogInfo> res = null;
        try{
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            res = (List<LogInfo>) objIn.readObject();
            System.out.println("reading log for pause...");
            objIn.close();
            in.close();

        } catch (Exception e){
        }
        return res;
    }


}
