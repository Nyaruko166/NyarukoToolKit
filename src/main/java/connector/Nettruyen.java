package connector;

import model.Chapter;
import util.NetworkHelper;
import util.PDFHelper;
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

public class Nettruyen implements SourceConnector {

//    Logger log = LogManager.getLogger(Nettruyen.class);

    @Override
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

    @Override
    public List<Chapter> getChapterList(String url) {
        List<Chapter> lstChapter = new ArrayList<>();

        String html = NetworkHelper.fetchHtml(url);
        Document document = Jsoup.parse(html);
        Elements elements = document.select("ul#asc div.chapter > a");
        log.info("Fetching chapters...");
        for (Element element : elements) {
            lstChapter.add(new Chapter(element.text(), element.attr("href")));
        }
        return lstChapter;
    }

    @Override
    public void downloadManga(String title, List<Chapter> lstChapter) {
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

    @Override
    public void downloadChapter(Chapter chapter, Path chapterPath) {
        log.info("Getting {} data...", chapter.getTitle());

        String html = NetworkHelper.fetchHtml(chapter.getSrc());

        Document readingBox = Jsoup.parse(html);
        Elements readingDetail = readingBox.select("div.page-chapter > img");

        log.info("Downloading {} images...", chapter.getTitle());
        for (Element imgDetail : readingDetail) {
            String imgSrc = imgDetail.attr("data-src");
            //Create path to download chapter image
            Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator + imgSrc.substring(imgSrc.lastIndexOf('/') + 1));

            boolean downloadSuccess = false;
            int attempt = 0;

            // Retry loop may cause soft lock
            while (!downloadSuccess) {
                try {
                    NetworkHelper.downloadImageByte(imgSrc, NetworkHelper.getBaseUrl(chapter.getSrc()), imgPath.toString());
                    downloadSuccess = true; // Mark download as successful
                } catch (IOException e) {
                    attempt++;
                    log.error("Error downloading image {}. Attempt: {}", imgSrc, attempt);
                    log.error(e);
                }
            }
        }
        log.info("Converting {} to PDF", chapter.getTitle());
        PDFHelper.convertSingleChapterToPDF(chapterPath);
    }
}
