package Connector;

import Model.Chapter;
import Model.MangadexCover;
import Util.ApiHelper;
import Util.PDFHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mangadex implements SourceConnector {

//    static Logger log = LogManager.getLogger(Mangadex.class);

    static final String MANGADEX_API = "https://api.mangadex.org";
    static final String MANGADEX_DEFAULT_DOWNLOAD_API = "https://uploads.mangadex.org";
    static final String MANGADEX_GET_MANGA_API = MANGADEX_API + "/manga/%s";
    static final String MANGADEX_GET_CHAPTERS_API = MANGADEX_API + "/manga/%s/feed";
    static final String MANGADEX_GET_CHAPTER_IMAGES_API = MANGADEX_API + "/at-home/server/%s";
    static final String MANGADEX_GET_COVERS_API = MANGADEX_API + "/cover";
    static final String MANGADEX_IMAGES_DOWNLOAD_API = "%s/data/%s/%s";
    static final String MANGADEX_COVERS_DOWNLOAD_API = MANGADEX_DEFAULT_DOWNLOAD_API + "/covers/%s/%s";

    @Override
    public String getMangaTitle(String mangaURL) {
        log.info("Getting information...");
        String url = MANGADEX_GET_MANGA_API.formatted(getUUIDFromURL(mangaURL));

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
        String baseUrl = MANGADEX_GET_CHAPTERS_API.formatted(getUUIDFromURL(mangaURL));

        Map<String, String> params = new HashMap<>();
        params.put("translatedLanguage[]", "vi");
        params.put("order[chapter]", "asc");

        String url = ApiHelper.urlParamBuilder(baseUrl, params);
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

        Path mangaDownloadPath = Paths.get(mangaDir + File.separator + title);
        log.info("Fetching manga: {}", title);
//        if (!mangaDownloadPath.toFile().exists()) {
//            mangaDownloadPath.toFile().mkdir();
//            log.info("Created folder at: {}", mangaDownloadPath.toString());
//        }

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
        log.info("Manga location: {}", mangaDownloadPath);
    }

    @Override
    public void downloadChapter(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());

        String url = MANGADEX_GET_CHAPTER_IMAGES_API.formatted(chapter.getSrc());

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
                downloadUrl = new URL(MANGADEX_IMAGES_DOWNLOAD_API.formatted(baseUrl, hash, imgId));
            } catch (MalformedURLException e) {
                log.error("Malformed URL: {}", e.getMessage());
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

    private List<MangadexCover> getAllCovers(String mangaURL) {
        List<MangadexCover> lstCover = new ArrayList<>();

        String mangaId = getUUIDFromURL(mangaURL);

        Map<String, String> params = new HashMap<>();
        params.put("limit", "50");
        params.put("manga[]", mangaId);
        params.put("order[volume]", "asc");

        String url = ApiHelper.urlParamBuilder(MANGADEX_GET_COVERS_API, params);
        log.info("Getting covers...");
        String responseJson = ApiHelper.getRequest(url);
        JsonObject jsonBody = gson.fromJson(responseJson, JsonObject.class);
        //Again, it's time to navigate through fk nested json
        JsonArray dataArray = jsonBody.getAsJsonArray("data");
        int nullCounter = 0;
        for (JsonElement jsonElement : dataArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            //Yes null again, now it is Volume so imma do null + n
            String title;
            JsonElement volumeJson = jsonObject.get("attributes").getAsJsonObject().get("volume");
            if (volumeJson.isJsonNull()) {
                title = "CoverNull " + nullCounter;
                nullCounter++;
            } else {
                title = "Vol " + volumeJson.getAsString();
            }
            String coverFileName = jsonObject.get("attributes").getAsJsonObject().get("fileName").getAsString();
            lstCover.add(new MangadexCover(title, coverFileName));
        }
        return lstCover;
    }

    public void downloadMangaCovers(String title, String mangaUrl) {

        String mangaId = getUUIDFromURL(mangaUrl);
        Path coverDownloadPath = Paths.get(mangaDir + File.separator + title + File.separator + "Covers");
        List<MangadexCover> lstCover = getAllCovers(mangaUrl);

        if (!coverDownloadPath.toFile().exists()) {
            coverDownloadPath.toFile().mkdirs();
        }

        log.info("Downloading manga covers...");
        for (MangadexCover cover : lstCover) {

            boolean downloadSuccess = false;
            int attempt = 0;

            try {
                URL downloadUrl = new URL(MANGADEX_COVERS_DOWNLOAD_API.formatted(mangaId, cover.getFileName()));
                File downloadPath = new File(coverDownloadPath.toFile(),
                        cover.getTitle() + cover.getFileName().substring(cover.getFileName().lastIndexOf("."))); //substring to get file ext

                while (!downloadSuccess) {
                    try {
                        assert downloadUrl != null;
                        log.info("Downloading {}'s cover", cover.getTitle());
                        FileUtils.copyURLToFile(downloadUrl, downloadPath);
                        downloadSuccess = true; // Mark download as successful
                    } catch (IOException e) {
                        attempt++;
                        log.error("Error downloading image {}. Attempt: {}", downloadUrl, attempt);
                        log.error(e);
                    }
                }
            } catch (MalformedURLException e) {
                log.error("Malformed URL: {}", e.getMessage());
            }
        }
        log.info("Downloaded all covers.");
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
