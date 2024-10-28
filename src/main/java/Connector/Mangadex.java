package Connector;

import Model.Chapter;
import Util.ApiHelper;
import Util.Config;
import Util.PDFHelper;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mangadex implements SourceConnector {

//    static Logger log = LogManager.getLogger(Mangadex.class);

    static final String MANGADEX_API = "https://api.mangadex.org";
    static final String MANGADEX_GET_MANGA_API = "%s/manga/%s";
    static final String MANGADEX_GET_CHAPTERS_API = "%s/manga/%s/feed";
    static final String MANGADEX_GET_CHAPTER_IMAGES_API = "%s/at-home/server/%s";
    static final String MANGADEX_GET_IMAGES_DOWNLOAD_API = "%s/data/%s/%s";

    @Override
    public String getMangaTitle(String mangaURL) {
        log.info("Getting information...");
        String url = MANGADEX_GET_MANGA_API.formatted(MANGADEX_API, getUUIDFromURL(mangaURL));

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

    @Override
    public List<Chapter> getChapterList(String mangaURL) {
        String baseUrl = MANGADEX_GET_CHAPTERS_API.formatted(MANGADEX_API, getUUIDFromURL(mangaURL));
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

    @Override
    public void downloadManga(String title, List<Chapter> lstChapter) {

        Path mangaDownloadPath = null;

        mangaDownloadPath = Paths.get(mangaDir + File.separator + title);
        log.info("Fetching manga: {}", title);
        if (!mangaDownloadPath.toFile().exists()) {
            mangaDownloadPath.toFile().mkdir();
            log.info("Created folder at: {}", mangaDownloadPath.toString());
        }

        for (Chapter chapter : lstChapter) {
            //Create folder to store chapter image
            Path chapterPath = Paths.get(mangaDownloadPath.toAbsolutePath() + File.separator + chapter.getTitle());
            chapterPath.toFile().mkdir();

            downloadChapter(chapter, chapterPath);

            while (PDFHelper.isFolderEmpty(chapterPath.toString())) {
                log.error("Failed to download {}?!", chapter.getTitle());
                log.warn("Retry to download...");
                downloadChapter(chapter, chapterPath);
            }
        }
        log.info("Downloaded manga: {}", title);
    }

    @Override
    public void downloadChapter(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());

        String url = MANGADEX_GET_CHAPTER_IMAGES_API.formatted(MANGADEX_API, chapter.getSrc());

        String responseBody = ApiHelper.getRequest(url);
        JsonObject jsonBody = gson.fromJson(responseBody, JsonObject.class);

        String baseUrl = jsonBody.get("baseUrl").getAsString();
        JsonObject chapterData = jsonBody.get("chapter").getAsJsonObject();
        String hash = chapterData.get("hash").getAsString();
        JsonArray imgIdArray = chapterData.get("data").getAsJsonArray();
//        System.out.println("Base URL: " + baseUrl);
//        System.out.println("Hash: " + hash);

        log.info("Downloading {} images...", chapter.getTitle());
        for (JsonElement jsonElement : imgIdArray) {
            boolean downloadSuccess = false;
            int attempt = 0;

            String imgId = jsonElement.getAsString();
            URL downloadUrl = null;
            try {
                downloadUrl = new URL(MANGADEX_GET_IMAGES_DOWNLOAD_API.formatted(baseUrl, hash, imgId));
            } catch (MalformedURLException e) {
                log.error("Malformed URL: {}", e);
            }
            String fileName = imgId.replaceAll("^(\\d+)-.*(\\.[^.]+)$", "$1$2");
            File downloadPath = new File(chapterPath + File.separator + fileName);
            // Retry loop may cause soft lock
            while (!downloadSuccess) {
                try {
                    assert downloadUrl != null;
                    FileUtils.copyURLToFile(downloadUrl, downloadPath);
                    downloadSuccess = true; // Mark download as successful
                } catch (IOException e) {
                    attempt++;
                    log.error("Error downloading image {}. Attempt: {}", downloadUrl, attempt);
                    log.error(e);
                }
            }
        }

        log.info("Converting {} to PDF", chapter.getTitle());
        PDFHelper.convertSingleChapterToPDF(chapterPath);
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
