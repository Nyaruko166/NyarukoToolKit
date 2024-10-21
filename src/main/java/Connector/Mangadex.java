package Connector;

import Model.Chapter;
import Util.ApiHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mangadex {

    static Logger log = LogManager.getLogger(Mangadex.class);

    static final String MANGADEX_API = "https://api.mangadex.org";
    static final String MANGADEX_GET_MANGA_API = "%s/manga/%s";
    static final String MANGADEX_GET_CHAPTERS_API = "%s/manga/%s/feed";
    static final String MANGADEX_GET_CHAPTER_IMAGES_API = "%s/at-home/server/%s";
    static final String MANGADEX_GET_IMAGES_DOWNLOAD_API = "%s/data/%s/%s";

    Gson gson = new Gson();

    public static void main(String[] args) {
        Mangadex mangadex = new Mangadex();
        String testUUID = "2442e655-dcfe-4ed2-99de-d5b212836b64";
        String chapterUUID = "733212b1-8ab5-4383-9dd3-2d99a760ce85";
//        mangadex.getTitleFromUUID("");
//        List<Chapter> lstChapters = mangadex.getChaptersFromUUID(testUUID);
//        for (Chapter chapter : lstChapters) {
//            System.out.println(chapter.getTitle() + " - " + chapter.getSrc());
//        }
        mangadex.downloadChapterFromUUID(chapterUUID);
    }

    public String getTitleFromUUID(String uuid) {
        String url = MANGADEX_GET_MANGA_API.formatted(MANGADEX_API, uuid);

        String responseBody = ApiHelper.getRequest(url);
        if (responseBody == null) {
            log.error("Manga not found!");
            return null;
        }

        JsonObject jsonBody = gson.fromJson(responseBody, JsonObject.class);
        //@formatter:off
        //Navigate through fking nested json response
        return jsonBody.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                .get("title").getAsJsonObject().get("en").getAsString();
        //@formatter:on
    }

    public List<Chapter> getChaptersFromUUID(String uuid) {
        String baseUrl = MANGADEX_GET_CHAPTERS_API.formatted(MANGADEX_API, uuid);
        //@formatter:off
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("translatedLanguage[]", "vi")
                .addQueryParameter("order[chapter]", "asc");
        //@formatter:on

        String url = urlBuilder.build().toString();
        String responseBody = ApiHelper.getRequest(url);
        JsonObject jsonBody = gson.fromJson(responseBody, JsonObject.class);
        JsonArray dataArray = jsonBody.getAsJsonArray("data");
        List<Chapter> lstChapters = new ArrayList<>();
        for (JsonElement jsonElement : dataArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String title;
            JsonElement chapterFromJson = jsonObject.get("attributes").getAsJsonObject().get("chapter");
            //Sometime chapter is null from oneshot or spin off soooooooooooooooooooooooo using title as replacement
            if (chapterFromJson.isJsonNull()) {
                title = jsonObject.get("attributes").getAsJsonObject().get("title").getAsString();
            } else {
                title = "Chapter " + chapterFromJson.getAsString();
            }
            String src = jsonObject.get("id").getAsString();
            lstChapters.add(new Chapter(title, src));
        }
        return lstChapters;
    }

    public void downloadChapterFromUUID(String chapterUUID) {
        String url = MANGADEX_GET_CHAPTER_IMAGES_API.formatted(MANGADEX_API, chapterUUID);

        String responseBody = ApiHelper.getRequest(url);
        JsonObject jsonBody = gson.fromJson(responseBody, JsonObject.class);

        String baseUrl = jsonBody.get("baseUrl").getAsString();
        JsonObject chapterData = jsonBody.get("chapter").getAsJsonObject();
        String hash = chapterData.get("hash").getAsString();
        JsonArray imgIdArray = chapterData.get("data").getAsJsonArray();
        System.out.println("Base URL: " + baseUrl);
        System.out.println("Hash: " + hash);

        log.info("Downloading {} images...", chapterUUID);
        for (JsonElement jsonElement : imgIdArray) {
            String imgId = jsonElement.getAsString();
//            String downloadUrl = String.format("%s/data/%s/%s", baseUrl, hash, imgId);
            URL downloadUrl = null;
            try {
                downloadUrl = new URL(MANGADEX_GET_IMAGES_DOWNLOAD_API.formatted(baseUrl, hash, imgId));
            } catch (MalformedURLException e) {
                log.error("Malformed URL: {}", e);
            }
            String fileName = imgId.replaceAll("^(\\d+)-.*(\\.[^.]+)$", "$1$2");
            File downloadPath = new File("./libs/" + fileName);
            try {
                assert downloadUrl != null;
                FileUtils.copyURLToFile(downloadUrl, downloadPath);
            } catch (IOException e) {
                log.error("Error when download the img {}", downloadUrl);
                log.error(e);
            }
        }

    }

    private String getUUIDFromURL(String url) {
        Pattern pattern = Pattern.compile("title/([a-f0-9\\-]+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
