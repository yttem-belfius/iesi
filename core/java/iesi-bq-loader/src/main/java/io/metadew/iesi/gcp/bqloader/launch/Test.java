package io.metadew.iesi.gcp.bqloader.launch;

import io.metadew.iesi.gcp.common.configuration.Configuration;

import java.io.*;

public class Test {
    public static void main( String[] args ) {

        System.setProperty("log4j.configurationFile", "log4j2-gcp.xml");
        System.out.println(Configuration.getInstance().getProperty("iesi.gcp.bql.topic").orElse(""));

    }

}

