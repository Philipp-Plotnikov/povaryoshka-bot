package core;

import static utilities.CoreUtilities.loadEnvFileToSystemProperties;


public class Main {

    private static void initAppEnv(){
        loadEnvFileToSystemProperties();
    }

    public static void main(String[] args) {
        initAppEnv();
        Application.start();
    }
}