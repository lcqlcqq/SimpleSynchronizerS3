package files.service;

import files.entity.SyncParameters;

import java.io.File;

public class PrintService {
    public static void printProgress(long start,long i,long m,String keyName){
        int percentage = (int)(i * 100.0/m + 0.5);
        if(i>start){
            for(int j=0; j<=String.valueOf(percentage).length(); j++)
                System.out.print("\b");
        }else{
            System.out.print("[Advice] - downloading: " + keyName + ": ");
        }
        System.out.print(percentage+"%");
    }

    public static void printResume(long start,long i,long m,String keyName){
        int percentage = (int)(i * 100.0/m + 0.5);
        if(i>start){
            for(int j=0; j<=String.valueOf(percentage).length(); j++)
                System.out.print("\b");
        }else{
            System.out.print("[Advice] - resume downloading " + keyName + ": ");
        }
        System.out.print(percentage+"%");
    }

    public static void printSize(long contentLength){
        int sizeUnit = 0;
        double size = contentLength;
        while(size/1024 > 1 && sizeUnit < SyncParameters.ByteUnit.length){
            ++sizeUnit;
            size/=1024;
        }
        System.out.println("[Information] - upload file size = "+ contentLength + SyncParameters.ByteUnit[sizeUnit]);
    }

    public static void tree(File f, int level) {
        String preStr = "";
        for (int i = 0; i < level; i++) {
            if (i == level - 1) {
                preStr = preStr + "└─";
            } else {
                preStr = preStr + "|   ";
            }
        }
        File[] childs = f.listFiles();
        for (int i = 0; i < childs.length; i++) {
            System.out.println(preStr + childs[i].getName());
            if (childs[i].isDirectory()) {
                tree(childs[i], level + 1);
            }
        }
    }

    public static void displayHelpMenu() {
        System.out.println("Synchronizer - command menu: ");
        System.out.println("tree - display the work directory tree.");
    }

    public static void invalidCommandHandler() {
        System.out.println("[Advice] - Invalid command, type 'help' for more information.");
    }


}
