import Model.Chapter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Test {

    static Gson gson = new Gson();

    static final Logger log = LogManager.getLogger(Test.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        Path mangaDir = Paths.get("./Mangas/");

        if (!mangaDir.toFile().exists()) {
            mangaDir.toFile().mkdir();
        }

        log.info("Getting information...");
        Document document = Jsoup.connect("aaaaaaa").get();
        String title = document.select("h1.title-detail").text();
        log.info("Fetching manga: {}", title);
        Path mangaDownloadPath = Paths.get(mangaDir + File.separator + title);
        if (!mangaDownloadPath.toFile().exists()) {
            mangaDownloadPath.toFile().mkdir();
            log.info("Created folder at: {}", mangaDownloadPath.toString());
        }

        Elements elements = document.select("ul#asc div.chapter > a");
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
        }

        log.info("Downloaded manga: {}", title);

        //PDF
        String rootFolderPath = mangaDownloadPath.toString();
        String outputFolderPath = mangaDir + File.separator + "PDF Files";

        Path outputPath = Paths.get(outputFolderPath);
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdir();
        }

        log.info("Creating PDF...");
        File rootFolder = new File(rootFolderPath);
        File[] chapterFolders = rootFolder.listFiles(File::isDirectory); // Get all directories (chapters) in the root folder

        if (chapterFolders != null) {
            for (File chapterFolder : chapterFolders) {
                String chapterName = chapterFolder.getName();
                String outputPdfPath = outputFolderPath + File.separator + chapterName + ".pdf";

                try (PDDocument pdDocument = new PDDocument()) {
                    File[] imageFiles = chapterFolder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|gif)$"));

                    if (imageFiles != null && imageFiles.length > 0) {
                        // Sort image files numerically based on their names
                        Arrays.sort(imageFiles, new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                int num1 = extractNumber(f1.getName());
                                int num2 = extractNumber(f2.getName());
                                return Integer.compare(num1, num2);
                            }

                            private int extractNumber(String fileName) {
                                try {
                                    String number = fileName.replaceAll("\\D+", ""); // Extract digits from the file name
                                    return Integer.parseInt(number);
                                } catch (NumberFormatException e) {
                                    return 0; // Return 0 if no number is found
                                }
                            }
                        });

                        // Add images to the PDF in the sorted order
                        for (File imageFile : imageFiles) {
                            PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), pdDocument);
                            PDRectangle pageSize = new PDRectangle(image.getWidth(), image.getHeight());
                            PDPage page = new PDPage(pageSize);
                            pdDocument.addPage(page);

                            try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                                contentStream.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
                            }
                        }
                        pdDocument.save(outputPdfPath);
                        log.info("Created PDF for {}!", chapterName);
                    } else {

                        log.info("Can't find image in folder {}", chapterName);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            }
        } else {
            log.error("Can't find chapter folder in root folder!");
        }
    }
//}

//        Scanner scanner = new Scanner(System.in);
//
//        String geminiKey = "Man i love fauna!";
//
//        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";
//
//        String bodyTemplate = "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}";
//        log.info("Enter your question: ");
//        String question = scanner.nextLine();
//        String body = String.format(bodyTemplate, question);
//
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost(apiUrl + geminiKey);
//
//        post.addHeader("Content-Type", "application/json");
//        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
//
//        JsonObject resultObject = new JsonObject();
//        try {
//            CloseableHttpResponse res = client.execute(post);
//            BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
//            Gson gson = new Gson();
//            resultObject = gson.fromJson(rd, JsonObject.class);
//            String answer = resultObject.get("candidates").getAsJsonArray().get(0).getAsJsonObject().get("content").getAsJsonObject().get("parts").getAsJsonArray().get(0).getAsJsonObject().get("text").toString();
//            log.info("Result for " + question + " is " + answer);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        System.out.print("Enter yt link: ");
//                    String ytUrl = in.nextLine();
//
//                    while (ytUrl.isBlank()) {
//                        System.out.println("Go fuck yourself");
//                        System.out.print("Pls enter yt link: ");
//                        ytUrl = in.nextLine();
//                    }
//
//                    String command = "cmd /c start cmd.exe /K \"cd ./libs/ && %s\" && exit";
//                    String customArgs = "yt-dlp.exe --list-subs ";
//
//                    try {
//                        Process process = Runtime.getRuntime().exec(String.format(command, customArgs + ytUrl));
//                        process.waitFor();
//                    } catch (Exception e) {
//                        System.out.println("HEY Buddy ! U r Dog Something Wrong ");
//                        e.printStackTrace();
//                    }

//}

}
