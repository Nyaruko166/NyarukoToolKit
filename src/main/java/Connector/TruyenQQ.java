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

    public static void main(String[] args) {
        TruyenQQ truyenQQ = new TruyenQQ();
        String url = "https://truyenqqto.com/truyen-tranh/yuusha-ga-shinda-1278";
//        String url2 = "https://tintruyen.net/1278/202/36.jpg?d=dfgd6546";
//        String fileName = url2.substring(url2.lastIndexOf("/") + 1, url2.lastIndexOf("?"));
//        System.out.println(fileName);
        String title = truyenQQ.getMangaTitleQQ(url);
        List<Chapter> lstChapter = truyenQQ.getChapterListQQ(url);
//        for (Chapter x : lstChapter) {
//            System.out.println(x.getTitle() + " - " + x.getSrc());
//        }
        truyenQQ.downloadMangaQQ(title, lstChapter);
//        try {
//            NetworkHelper.downloadImageByte(url2, NetworkHelper.getBaseUrl(url), "./libs/" + fileName);
//        } catch (IOException e) {
//            log.error(e);
//        }
    }

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

//            if (PDFHelper.isFolderEmpty(chapterPath.toString())) {
//                log.error("Failed to download {} ?!", chapter.getTitle());
//                log.warn("Retry to download...");
//                downloadChapterQQ(chapter, chapterPath);
//            }
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
