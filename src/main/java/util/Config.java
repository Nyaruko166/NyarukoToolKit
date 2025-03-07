package util;

import model.AppConfig;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.utils.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Config {

    Logger log = LogManager.getLogger(Config.class);

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

        if (appConfig.getDiscord_bot_api() == null || appConfig.getDiscord_bot_api().isBlank()) {
            log.error("Please put discord_bot_api into \"./libs/config.json\".\"");
            System.exit(1);
        }

        return appConfig;
    }

}
