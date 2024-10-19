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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Nettruyen {

    Logger log = LogManager.getLogger(Nettruyen.class);

    public String getMangaTitle(String url) {
        log.info("Getting information...");
        String title = null;
        try {
            Document document = Jsoup.connect(url).get();
            title = document.select("h1.title-detail").text();
            if (title.isBlank()) {
                return null;
            }
        } catch (IOException e) {
            log.error(e);
        }
        return title;
    }

    public List<Chapter> getChapterList(String url) {
        List<Chapter> lstChapter = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("ul#asc div.chapter > a");
            log.info("Fetching chapters...");
            for (Element element : elements) {
                lstChapter.add(new Chapter(element.text(), element.attr("href")));
            }
        } catch (IOException e) {
            log.error(e);
        }
        return lstChapter;
    }

    public void downloadManga(String title, List<Chapter> lstChapter) {

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

            downloadChapter(chapter, chapterPath);

            while (PDFHelper.isFolderEmpty(chapterPath.toString())) {
                log.error("Failed to download {}?!", chapter.getTitle());
                log.warn("Retry to download...");
                downloadChapter(chapter, chapterPath);
            }
        }
        log.info("Downloaded manga: {}", title);
        PDFHelper.convertAllChapterToPDF(mangaDownloadPath.toString());
    }

    private void downloadChapter(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());
        try {
            Document readingBox = Jsoup.connect(chapter.getSrc()).get();
            Elements readingDetail = readingBox.select("div.page-chapter > img");

            log.info("Downloading {} images...", chapter.getTitle());
            for (Element imgDetail : readingDetail) {
                String imgSrc = imgDetail.attr("data-src");
                //Create path to download chapter image
                Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator + imgSrc.substring(imgSrc.lastIndexOf('/') + 1));
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
}
