package Connector;

import Model.Chapter;
import Util.ApiHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mangadex {

    static Logger log = LogManager.getLogger(Mangadex.class);

    static final String MANGADEX_API = "https://api.mangadex.org";

    Gson gson = new Gson();

    public static void main(String[] args) {
        Mangadex mangadex = new Mangadex();
        String testUUID = "2442e655-dcfe-4ed2-99de-d5b212836b64";
//        mangadex.getTitleFromUUID("");
        List<Chapter> lstChapters = mangadex.getChaptersFromUUID(testUUID);
        for (Chapter chapter : lstChapters) {
            System.out.println(chapter.getTitle() + " - " + chapter.getSrc());
        }
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
        //@formatter:off
        //Navigate through fking nested json response
        return jsonBody.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                .get("title").getAsJsonObject().get("en").getAsString();
        //@formatter:on
    }

    public List<Chapter> getChaptersFromUUID(String uuid) {
        String getChaptersApi = "%s/manga/%s/feed";
        String baseUrl = getChaptersApi.formatted(MANGADEX_API, uuid);
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

    private String getUUIDFromURL(String url) {
        Pattern pattern = Pattern.compile("title/([a-f0-9\\-]+)/");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
