package util;

import com.google.gson.Gson;
import model.AppConfig;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    private final String configPath = "./libs/config.json";

    Logger log = LogManager.getLogger(Config.class);
    Gson gson = new Gson();

    private static Config instance;

    private AppConfig appConfig;

    private Config() {
        try {
            File configFile = new File(configPath);
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

        if (appConfig.getDiscord_bot_api() == null || appConfig.getDiscord_bot_api().isBlank()) {
            log.error("Please put discord_bot_api into \"./libs/config.json\".\"");
            System.exit(1);
        }

        return appConfig;
    }

    public void updateConfig(AppConfig appConfig) {
        try {
            FileUtils.writeStringToFile(new File(configPath), gson.toJson(appConfig), "UTF-8");
        } catch (IOException e) {
            log.error("Error writing to config file {}", e);
        }
    }

}
