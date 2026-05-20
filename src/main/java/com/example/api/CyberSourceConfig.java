package com.example.api;

import com.cybersource.authsdk.core.MerchantConfig;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;

public class CyberSourceConfig {

    public static MerchantConfig getConfig() throws Exception {
        Properties props = new Properties();

        // Try multiple locations in order
        String[] locations = {
                "cfg/cybersource.properties",                          // when run via bin/q2
                "src/dist/cfg/cybersource.properties",                 // when run via Gradle
                System.getProperty("user.home") +
                        "/IdeaProjects/transaction/jPOS-template/src/dist/cfg/cybersource.properties"
        };

        File cfgFile = null;
        for (String path : locations) {
            File f = new File(path);
            if (f.exists()) {
                cfgFile = f;
                break;
            }
        }

        if (cfgFile == null) {
            throw new RuntimeException(
                    "cybersource.properties not found. Tried:\n" +
                            "  cfg/cybersource.properties\n" +
                            "  src/dist/cfg/cybersource.properties\n" +
                            "Current working dir: " + new File(".").getAbsolutePath()
            );
        }

        System.out.println("Loading config from: " + cfgFile.getAbsolutePath());
        props.load(new FileInputStream(cfgFile));

        Properties merchantProp = new Properties();
        merchantProp.setProperty("authenticationType", "http_signature");
        merchantProp.setProperty("merchantID",         props.getProperty("cybersource.merchant.id"));
        merchantProp.setProperty("merchantKeyId",      props.getProperty("cybersource.key.id"));
        merchantProp.setProperty("merchantsecretKey",  props.getProperty("cybersource.secret.key"));
        merchantProp.setProperty("runEnvironment",     props.getProperty("cybersource.run.environment"));
        merchantProp.setProperty("logEnabled",         "true");
        merchantProp.setProperty("logDirectory",       "log/");
        merchantProp.setProperty("logFilename",        "cybersource.log");

        return new MerchantConfig(merchantProp);
    }
}