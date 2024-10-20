package Connector;

import Util.ApiHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mangadex {

    static Logger log = LogManager.getLogger(Mangadex.class);

    static final String MANGADEX_API = "https://api.mangadex.org";

    Gson gson = new Gson();

    public static void main(String[] args) {
        Mangadex mangadex = new Mangadex();
        mangadex.getTitleFromUUID("2442e655-dcfe-4ed2-99de-d5b212836b64");
    }

    private String getUUIDFromURL(String url) {
        Pattern pattern = Pattern.compile("title/([a-f0-9\\-]+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String getTitleFromUUID(String uuid) {
        String getMangaApi = "%s/manga/%s";
        String url = getMangaApi.formatted(MANGADEX_API, uuid);

        String responseBody = ApiHelper.getRequest(url);
        if (responseBody == null) {
            log.error("Manga not found!");
            return null;
        }

        JsonObject jsonBody = gson.fromJson(responseBody, JsonObject.class);
        //Navigate through fking nested json response
        return jsonBody.get("data").getAsJsonObject().get("attributes")
                .getAsJsonObject().get("title")
                .getAsJsonObject().get("en").getAsString();
    }
}
