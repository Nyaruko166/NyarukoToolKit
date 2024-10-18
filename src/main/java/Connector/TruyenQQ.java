package Connector;

import Model.Chapter;
import Util.Config;
import Util.NetworkHelper;
import Util.PDFHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TruyenQQ {

    static Logger log = LogManager.getLogger(TruyenQQ.class);

    public List<Chapter> getChapterListQQ(String url) {
        List<Chapter> lstChapter = new ArrayList<>();
        log.info("Fetching chapters...");

        String html = NetworkHelper.fetchHtml(url);

        Document document = Jsoup.parse(html);
        Elements elements = document.select(".content .name-chap > a");
        for (Element element : elements) {
            String title = element.text().replaceAll("Chương (\\d+)", "Chapter $1");
            lstChapter.add(new Chapter(title, NetworkHelper.getBaseUrl(url) + element.attr("href")));
        }
        return lstChapter;
    }

    public void downloadMangaQQ(String title, List<Chapter> lstChapter) {

        Path mangaDir = Paths.get(Config.getInstance().getProperty().getWorking_directory() + "/Mangas/");

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

            downloadChapterQQ(chapter, chapterPath);

            do {
                log.error("Failed to download {}?!", chapter.getTitle());
                log.warn("Retry to download...");
                downloadChapterQQ(chapter, chapterPath);
            } while (PDFHelper.isFolderEmpty(chapterPath.toString()));
        }
        log.info("Downloaded manga: {}", title);
        PDFHelper.convertToPDF(mangaDownloadPath.toString());
    }

    private void downloadChapterQQ(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());

        String html = NetworkHelper.fetchHtml(chapter.getSrc());
        Document document = Jsoup.parse(html);

        Elements readingDetail = document.select("div[class=page-chapter] > img");

        log.info("Downloading {} images...", chapter.getTitle());
        for (Element imgDetail : readingDetail) {
            String imgSrc = imgDetail.attr("src");
//            log.info("Img link {}", imgSrc);
            //Create path to download chapter image
            String fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1, imgSrc.lastIndexOf("?"));
            Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator + fileName);
//            System.out.println(imgPath);
            try {
                NetworkHelper.downloadImageByte(imgSrc, NetworkHelper.getBaseUrl(chapter.getSrc()), imgPath.toString());
            } catch (IOException e) {
                log.error("Error when download the img {}", imgSrc);
                log.error(e);
            }
        }
    }

    public String getMangaTitleQQ(String url) {
        log.info("Getting information...");
        String html = NetworkHelper.fetchHtml(url);
        Document document = Jsoup.parse(html);
        String title = document.select("div[class=book_other] > h1").text();
        if (title.isBlank()) {
            return null;
        }
        return title;
    }
}
