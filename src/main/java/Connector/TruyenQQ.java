package Connector;

import Model.Chapter;
import Util.Config;
import Util.PDFHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TruyenQQ {

    static Logger log = LogManager.getLogger(TruyenQQ.class);

    public List<Chapter> getChapterListQQ(String url) {
        List<Chapter> lstChapter = new ArrayList<>();
        try {
            log.info("Fetching chapters...");
            Document document = Jsoup.connect(url).userAgent("Chrome").get();
            Elements elements = document.select(".content .name-chap > a");
            for (Element element : elements) {
                lstChapter.add(new Chapter(element.text(), getBaseUrlQQ(url) + element.attr("href")));
            }
        } catch (IOException e) {
            log.error(e);
        }
        return lstChapter;
    }

    public void downloadMangaQQ(String title, List<Chapter> lstChapter) {

        Path mangaDir = Paths.get(Config.getInstance().getProperty().getWorking_directory() + "/Mangas/");

        Path mangaDownloadPath = null;

//        log.info("Getting information...");
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

            downloadChapterQQ(chapter, chapterPath);

            if (PDFHelper.isFolderEmpty(chapterPath.toString())) {
                log.error("Failed to download {} ?!", chapter.getTitle());
                log.warn("Retry to download...");
                downloadChapterQQ(chapter, chapterPath);
            }
        }
        log.info("Downloaded manga: {}", title);
        PDFHelper.convertToPDF(mangaDownloadPath.toString());
    }

    private void downloadChapterQQ(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());
        try {
            Document readingBox = Jsoup.connect(chapter.getSrc()).userAgent("Chrome").get();
            Elements readingDetail = readingBox.select("div[class=page-chapter] > img");

            log.info("Downloading {} images...", chapter.getTitle());
            for (Element imgDetail : readingDetail) {
                String imgSrc = imgDetail.attr("src");
                //Create path to download chapter image
                String fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1, imgSrc.lastIndexOf("?"));
                Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator + fileName);
                try {
                    FileUtils.copyURLToFile(new URL(imgSrc), imgPath.toFile());
                } catch (IOException e) {
                    log.error("Error when download the img {}", imgSrc);
                    log.error(e);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String getMangaTitleQQ(String url) {
        log.info("Getting information...");
        String title = null;
        try {
            Document document = Jsoup.connect(url).get();
            title = document.select("div[class=book_other] > h1").text();
            if (title.isBlank()) {
                return null;
            }
        } catch (IOException e) {
            log.error(e);
        }
        return title;
    }

    public String getBaseUrlQQ(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            log.error("Invalid URL format: {}", e.getMessage());
            return null;
        }
    }

}
