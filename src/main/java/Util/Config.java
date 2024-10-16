package Util;

import Model.AppConfig;
import com.google.gson.Gson;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Config {

    private static Config instance;

    private AppConfig appConfig;

    private Config() {
        Gson gson = new Gson();
        try {
            File configFile = new File("./libs/config.json");
            appConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);
        } catch (FileNotFoundException e) {
            Log.error(e);
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public AppConfig getProperty() {
        return appConfig;
    }

}
