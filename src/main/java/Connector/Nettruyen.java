package Connector;

import Model.Chapter;
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

    public static void main(String[] args) {
        Path mangaDir = Paths.get("./Mangas/");

        if (!mangaDir.toFile().exists()) {
            mangaDir.toFile().mkdir();
        }

        Path mangaDownloadPath = null;

        log.info("Getting information...");
        try {
            Document document = Jsoup.connect("https://nettruyenviet.com/truyen-tranh/doc-thoai-cua-nguoi-duoc-si").get();
            String title = document.select("h1.title-detail").text();
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
//            log.info(element.text());
            }

            for (Chapter chapter : lstChapter) {
                log.info("Getting {} data...", chapter.getTitle());
                Document readingBox = Jsoup.connect(chapter.getSrc()).get();
                Elements readingDetail = readingBox.select("div.page-chapter > img");
                Path chapterPath = Paths.get(mangaDownloadPath.toAbsolutePath() + File.separator + chapter.getTitle());
                chapterPath.toFile().mkdir();

                log.info("Downloading {} images...", chapter.getTitle());
                for (Element imgDetail : readingDetail) {
                    String imgSrc = imgDetail.attr("data-src");
                    Path imgPath = Paths.get(chapterPath.toAbsolutePath()
                                             + File.separator + imgSrc.substring(imgSrc.lastIndexOf('/') + 1));
                    FileUtils.copyURLToFile(new URL(imgDetail.attr("data-src")), imgPath.toFile());
                }
                log.info("Downloaded manga: {}", title);
            }
        } catch (IOException e) {
            log.error(e);
        }

        //PDF
        String rootFolderPath = "C:/Users/ADMIN/Documents/Code/SideProject/NyarukoToolKit/Mangas/Độc Thoại Của Người Dược Sĩ";
        String outputFolderPath = mangaDir + File.separator + "PDF Files";

        Path outputPath = Paths.get(outputFolderPath);
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }

        log.info("Creating PDF...");
        File rootFolder = new java.io.File(rootFolderPath);
        File[] chapterFolders = rootFolder.listFiles(java.io.File::isDirectory);

        if (chapterFolders != null) {
            for (java.io.File chapterFolder : chapterFolders) {
                String chapterName = chapterFolder.getName();
                String outputPdfPath = outputFolderPath + java.io.File.separator + chapterName + ".pdf";

                try (FileOutputStream fos = new FileOutputStream(outputPdfPath)) {
                    com.lowagie.text.Document document = new com.lowagie.text.Document();
                    PdfWriter.getInstance(document, fos);
                    document.open();

                    File[] imageFiles = chapterFolder.listFiles((dir, name) ->
                            name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|gif)$"));

                    if (imageFiles != null && imageFiles.length > 0) {
                        // Sort image files numerically within each chapter folder
                        Arrays.sort(imageFiles, Comparator.comparingInt(f -> extractNumber(f.getName())));

                        for (java.io.File imageFile : imageFiles) {
                            Image image = Image.getInstance(imageFile.getAbsolutePath());

                            // Set the page size to match the image size
                            Rectangle pageSize = new Rectangle(image.getWidth(), image.getHeight());
                            document.setPageSize(pageSize);
                            document.newPage();

                            image.setAbsolutePosition(0, 0); // Position the image at the bottom-left corner of the page
                            document.add(image);
                        }
                        document.close();
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
