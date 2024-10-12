package Connector;

import Model.Chapter;
import Util.StatusCode;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Nettruyen {

    static final Logger log = LogManager.getLogger(Nettruyen.class);

    public StatusCode downloadManga(String workingDir, String url) {
        Path mangaDir = Paths.get(workingDir + "/Mangas/");

        Path mangaDownloadPath = null;

        log.info("Getting information...");
        try {
            Document document = Jsoup.connect(url).get();
            String title = document.select("h1.title-detail").text();
            if (title.isBlank()) {
                return StatusCode.NOT_FOUND;
            }
            mangaDownloadPath = Paths.get(mangaDir + File.separator + title);
            log.info("Fetching manga: {}", title);
            if (!mangaDownloadPath.toFile().exists()) {
                mangaDownloadPath.toFile().mkdir();
                log.info("Created folder at: {}", mangaDownloadPath.toString());
            }

            org.jsoup.select.Elements elements = document.select("ul#asc div.chapter > a");
            List<Chapter> lstChapter = new ArrayList<>();
            log.info("Fetching chapters...");
            for (Element element : elements) {
                lstChapter.add(new Chapter(element.text(), element.attr("href")));
            }

            for (Chapter chapter : lstChapter) {
                log.info("Getting {} data...", chapter.getTitle());
                Document readingBox = Jsoup.connect(chapter.getSrc()).get();
                Elements readingDetail = readingBox.select("div.page-chapter > img");
                //Create folder to store chapter image
                Path chapterPath = Paths.get(mangaDownloadPath.toAbsolutePath() + File.separator + chapter.getTitle());
                chapterPath.toFile().mkdir();

                log.info("Downloading {} images...", chapter.getTitle());
                for (Element imgDetail : readingDetail) {
                    String imgSrc = imgDetail.attr("data-src");
                    //Create path to download chapter image
                    Path imgPath = Paths.get(chapterPath.toAbsolutePath() + File.separator
                                             + imgSrc.substring(imgSrc.lastIndexOf('/') + 1));
                    try {
                        FileUtils.copyURLToFile(new URL(imgDetail.attr("data-src")), imgPath.toFile());
                    } catch (IOException e) {
                        log.error("Error when download the img {}", imgDetail.attr("data-src"));
                        log.error(e);
                    }
                }
            }
            log.info("Downloaded manga: {}", title);
        } catch (IOException e) {
            log.error(e);
        }
        convertToPDF(mangaDownloadPath.toString());
        return StatusCode.SUCCESS;
    }

    private void convertToPDF(String rootFolderPath) {

        String outputFolderPath = rootFolderPath + "/PDF";

        Path outputPath = Paths.get(outputFolderPath);
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }

        log.info("Creating PDF...");
        File rootFolder = new File(rootFolderPath);
        File[] chapterFolders = rootFolder.listFiles(File::isDirectory);

        if (chapterFolders != null) {
            for (File chapterFolder : chapterFolders) {
                String chapterName = chapterFolder.getName();
                String outputPdfPath = outputFolderPath + File.separator + chapterName + ".pdf";

                try (FileOutputStream fos = new FileOutputStream(outputPdfPath)) {
                    com.lowagie.text.Document pdDocument = new com.lowagie.text.Document();
                    PdfWriter.getInstance(pdDocument, fos);
                    pdDocument.open();

                    File[] imageFiles = chapterFolder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|gif)$"));

                    if (imageFiles != null && imageFiles.length > 0) {
                        // Sort image files numerically within each chapter folder
                        Arrays.sort(imageFiles, Comparator.comparingInt(f -> extractNumber(f.getName())));

                        for (File imageFile : imageFiles) {
                            Image image = Image.getInstance(imageFile.getAbsolutePath());

                            // Set the page size to match the image size
                            Rectangle pageSize = new Rectangle(image.getWidth(), image.getHeight());
                            pdDocument.setPageSize(pageSize);
                            pdDocument.newPage();

                            image.setAbsolutePosition(0, 0);
                            pdDocument.add(image);
                        }
                        pdDocument.close();
                        log.info("PDF created for {} successfully!", chapterName);
                    } else {
                        log.info("No image files found in {}", chapterName);
                    }
                } catch (IOException | com.lowagie.text.DocumentException e) {
                    log.error("Error creating PDF for {}: {}", chapterName, e.getMessage());
                }
            }
        } else {
            log.info("No chapter folders found in the specified root folder.");
        }
    }

    // Extract digits from the file name to help with numerical sorting
    private static int extractNumber(String name) {
        try {
            String number = name.replaceAll("\\D+", ""); // Extract digits from the name
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0; // Return 0 if no number is found
        }
    }
}
