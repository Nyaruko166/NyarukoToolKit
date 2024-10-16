package Util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class PDFHelper {

    static Logger log = LogManager.getLogger(PDFHelper.class);

    public static void convertToPDF(String rootFolderPath) {

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
                if (chapterName.equalsIgnoreCase("pdf")) {
                    continue;
                }
                String outputPdfPath = outputFolderPath + File.separator + chapterName + ".pdf";

                try (FileOutputStream fos = new FileOutputStream(outputPdfPath)) {
                    Document pdDocument = new Document();
                    PdfWriter.getInstance(pdDocument, fos);
                    pdDocument.open();

                    File[] imageFiles = chapterFolder.listFiles((dir, name) -> name.toLowerCase()
                            .matches(".*\\.(jpg|jpeg|png|bmp|gif)$"));

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
                } catch (IOException | DocumentException e) {
                    log.error("Error creating PDF for {}: {}", chapterName, e.getMessage());
                }
            }
        } else {
            log.info("No chapter folders found in the specified root folder.");
        }
    }

    public static boolean isFolderEmpty(String folderPath) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            // Check if the directory has any files or subdirectories
            return folder.list().length == 0;
        } else {
            log.error("The specified path is not a directory.");
            return false;
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
