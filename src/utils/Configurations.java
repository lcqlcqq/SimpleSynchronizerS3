package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configurations {
    public String workDirectory;
    public String bucketName;
    public String accessKey;
    public String secretKey;
    public String serviceEndpoint;
    public String signingRegion;
    public String lastTransferLogDir;

    public Configurations() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        System.out.print("[Advice] - preparing configurations ...");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        try {
            inputStream.close();
            System.out.println(" completed! ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        workDirectory = properties.getProperty("workDirectory");
        bucketName = properties.getProperty("bucketName");
        accessKey = properties.getProperty("accessKey");
        secretKey = properties.getProperty("secretKey");
        serviceEndpoint = properties.getProperty("serviceEndpoint");
        signingRegion = properties.getProperty("signingRegion");
        lastTransferLogDir = properties.getProperty("lastTransferLogDir");
    }

}
