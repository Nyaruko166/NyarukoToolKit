import Connector.Mangadex;
import Connector.Nettruyen;
import Connector.TruyenQQ;
import Model.AppConfig;
import Model.Chapter;
import Model.VideoIndex;
import Util.*;
import Util.Color;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Main {

    static final Logger log = LogManager.getLogger(Main.class);

    static final File configFile = new File("./libs/config.json");

    static AppConfig loadedConfig = new AppConfig();

    static final Scanner scanner = new Scanner(System.in);

    static final Gson gson = new Gson();

    static KeyMap<String> keyMap = new KeyMap<>();

    private static final int PAGE_SIZE = 10;

    public static void main(String[] args) throws IOException {

        // Create a key map to bind keys to specific actions
        keyMap.bind("UP", "w");
        keyMap.bind("DOWN", "s");
        keyMap.bind("SPACE", " ");
        keyMap.bind("ENTER", "e");
        keyMap.bind("NEXT", "d");
        keyMap.bind("PREVIOUS", "a");
        //Todo: Check box still not have quit and back yet
        keyMap.bind("QUIT", "q");

        Terminal terminal = TerminalBuilder.builder().system(true).jansi(true).build();

//        if (true) {
//            String path = "D:\\Videos\\Outplayed\\GTFO\\GTFO_10-29-2024_23-14-26-774\\index.json";
//            Desktop desktop = Desktop.getDesktop();
//            desktop.open(new File(path));
//            uploadToYoutube(terminal);
//            System.exit(0);
//        }

        firstTimeConfig();
        log.info("Reading config...");
        loadedConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);

//        terminal.enterRawMode();
        menuConsole(terminal);
//        checkboxConsole(terminal);
    }

    private static void checkForUpdate(Terminal terminal) throws IOException {
        if (YtdlUtil.downloadLastedVersion(loadedConfig) == null) {
            log.info("You're up to date!");
        }
        TerminalHelper.anyKeyToCont(terminal);
    }


    private static void downloadYT(Terminal terminal) throws IOException {

        while (true) {

            log.info("Enter video url: ");
            String videoUrl = scanner.nextLine();
            String title = YtdlUtil.fetchTitle(videoUrl);
            if (title != null) {
                Path path = Paths.get(loadedConfig.getWorking_directory() + "\\Videos\\" + title);
                Files.createDirectories(path);
                log.info("Created folder at {}", path.toString());
                YtdlUtil.downloadVideo(videoUrl, path.toString());
                break;
            }

            terminal.puts(InfoCmp.Capability.clear_screen);
            log.error("Please enter a valid video url!");

        }

        TerminalHelper.anyKeyToCont(terminal);
    }

    private static void uploadToYoutube(Terminal terminal) throws IOException {
        Path videoFolder;
        //Quick validate shit
        while (true) {
            TerminalHelper.printLn(terminal, Color.GREEN, "Enter path to video folder:");
            videoFolder = Path.of(scanner.nextLine().replace("\\", "/"));
            if (!videoFolder.toFile().exists() || !videoFolder.toFile().isDirectory()) {
                log.error("Please enter a valid path to video folder!");
            } else {
                break;
            }
        }

        File noUpload = new File(videoFolder + "/.noupload");
        if (noUpload.exists()) {
            log.info("Found .noupload, this folder already uploaded to youtube or user don't want to upload!");
            TerminalHelper.anyKeyToCont(terminal);
            return;
        }

        //Todo handle not found index.json or make new one in cli or some shit
        File indexFile = new File(videoFolder + "/index.json");

        if (!indexFile.exists()) {
            List<VideoIndex> lstIndex = new ArrayList<>();
            File[] fileArray = videoFolder.toFile().listFiles((dir, name) -> name.endsWith(".mp4")); //Get only .mp4
            for (File file : fileArray) {
                lstIndex.add(new VideoIndex("", "/" + file.getName()));
            }
            JsonArray jsonArray = gson.toJsonTree(lstIndex).getAsJsonArray();
            FileUtils.writeStringToFile(indexFile, jsonArray.toString(), "UTF-8");
        }

        log.info("Created index.json file, Opening index.json");
        log.info("Insert your title for each clip");
        Desktop.getDesktop().open(indexFile);
        TerminalHelper.anyKeyToCont(terminal);
        Boolean result = true;
        Boolean state = true;
        do {
            result = yesNoConsole(terminal, "Have you name all the clips yet?");
            if (result == null) {
                indexFile.delete();
                log.info("Deleted index.json file");
                TerminalHelper.anyKeyToCont(terminal);
                //sus
                break;
            }
            //Yes no question
            if (result) {
                JsonArray jsonArray = gson.fromJson(new FileReader(indexFile), JsonArray.class);
                for (JsonElement jsonElement : jsonArray) {
                    if (jsonElement.getAsJsonObject().get("title").getAsString().isBlank()) {
                        log.error("Title is blank, please double check this path {}",
                                jsonElement.getAsJsonObject().get("path").getAsString());
                        TerminalHelper.anyKeyToCont(terminal);
                        break;
                    } else {
                        log.info("Please close index.json file editor window...");
                        state = false;
                    }
                }
            } else {
                continue;
            }
        } while (state);

        if (result == null) {
            if (indexFile.exists()) {
                FileUtils.deleteQuietly(indexFile);
            }
            return;
        }

        log.info("Found index.json");
        JsonArray jsonArray = gson.fromJson(new FileReader(indexFile), JsonArray.class);
        for (JsonElement element : jsonArray) {
            VideoIndex videoIndex = gson.fromJson(element, VideoIndex.class);
            YoutubeUtil.uploadVideo(videoIndex.getTitle(), new File(videoFolder + videoIndex.getPath()));
        }

        //Mark as done
        log.info("Created .nopload file");
        noUpload.createNewFile();

        TerminalHelper.anyKeyToCont(terminal);
    }

    private static void mangaCrawler(Terminal terminal) throws IOException {
        Nettruyen nettruyen = new Nettruyen();
        TruyenQQ truyenQQ = new TruyenQQ();
        Mangadex mangadex = new Mangadex();
        String mangaUrl;
        String title = "";
        String host = "";

        do {

            log.debug("Right now we can only download manga from Nettruyen-likes website...");
            log.info("Enter manga url: ");
            mangaUrl = scanner.nextLine();
            if (mangaUrl.contains("truyenqq")) {
                title = truyenQQ.getMangaTitle(mangaUrl);
                host = "truyenqq";
            } else if (mangaUrl.contains("nettruyen")) {
                host = "nettruyen";
                title = nettruyen.getMangaTitle(mangaUrl);
            } else if (mangaUrl.contains("mangadex")) {
                host = "mangadex";
                title = mangadex.getMangaTitle(mangaUrl);
            }

        } while (title == null || title.isBlank());

        switch (host) {
            case "mangadex" -> {
                List<Chapter> lstChapter = mangadex.getChapterList(mangaUrl);
                List<Chapter> selectedChapters = checkboxConsole(terminal, lstChapter);

                mangadex.downloadMangaCovers(title, mangaUrl);

                //Choose none = download all
                if (selectedChapters.isEmpty()) {
                    log.info("Download all chapters.");
                    mangadex.downloadManga(title, lstChapter);
                } else {
                    log.info("{} chapters selected.", selectedChapters.size());
                    mangadex.downloadManga(title, selectedChapters);
                }
            }

            case "truyenqq" -> {
                List<Chapter> lstChapter = truyenQQ.getChapterList(mangaUrl);
                List<Chapter> selectedChapters = checkboxConsole(terminal, lstChapter);
                log.info("{} chapters selected.", selectedChapters.size());

                //Choose none = download all
                if (selectedChapters.isEmpty()) {
                    truyenQQ.downloadManga(title, lstChapter);
                } else {
                    truyenQQ.downloadManga(title, selectedChapters);
                }
            }

            case "nettruyen" -> {
                List<Chapter> lstChapter = nettruyen.getChapterList(mangaUrl);
                List<Chapter> selectedChapters = checkboxConsole(terminal, lstChapter);
                log.info("{} chapters selected.", selectedChapters.size());

                //Choose none = download all
                if (selectedChapters.isEmpty()) {
                    nettruyen.downloadManga(title, lstChapter);
                } else {
                    nettruyen.downloadManga(title, selectedChapters);
                }
            }
        }

    }

    // Method to handle the selected option
    private static void handleMenuSelection(String selectedOption, Terminal terminal) throws IOException {
        switch (selectedOption) {
            case "Check for update" -> checkForUpdate(terminal);
            case "Download video from youtube" -> downloadYT(terminal);
            case "Download manga to PDF" -> mangaCrawler(terminal);
            case "Upload video to YT and send to Discord" -> uploadToYoutube(terminal);
            case "QUIT" -> terminal.writer().println("Goodbye!");
            default -> TerminalHelper.printLn(terminal, Color.RED, "Invalid option!");
        }
    }

    private static Boolean yesNoConsole(Terminal terminal, String log) {

        List<String> options = Arrays.asList("Yes", "No");

        BindingReader bindingReader = new BindingReader(terminal.reader());

        int selectedIndex = 0;

        while (true) {
            terminal.puts(InfoCmp.Capability.clear_screen);
            if (log != null) TerminalHelper.printLn(terminal, Color.CYAN, log);
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Up/Down with W/S");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Cancel/Confirm with Q/E");

            printMenu(options, selectedIndex);

            String key = bindingReader.readBinding(keyMap);

            if (key == null) {
                continue; // Skip if no valid key pressed
            }

            switch (key) {
                case "UP":
                    selectedIndex = (selectedIndex > 0) ? selectedIndex - 1 : options.size() - 1;
                    break;
                case "DOWN":
                    selectedIndex = (selectedIndex < options.size() - 1) ? selectedIndex + 1 : 0;
                    break;
                case "QUIT":
                    TerminalHelper.printLn(terminal, Color.RED, "Deleting index.json...");
                    terminal.flush();
                    return null;
                case "ENTER":
                    terminal.writer().println("Selected: " + options.get(selectedIndex));
                    terminal.flush();
                    if (options.get(selectedIndex).equalsIgnoreCase("Yes")) return true;
                    return false;
                default:
                    break;
            }
        }

    }

    private static void menuConsole(Terminal terminal) {

        List<String> options = Arrays.asList("Check for update", "Download video from youtube", "Download manga to PDF", "Upload video to YT and send to Discord");

        try {
            BindingReader bindingReader = new BindingReader(terminal.reader());

            int selectedIndex = 0;

            while (true) {

                terminal.puts(InfoCmp.Capability.clear_screen);
                TerminalHelper.printLn(terminal, Color.GREEN, "==> Up/Down with W/S");
                TerminalHelper.printLn(terminal, Color.GREEN, "==> Quit/Confirm with Q/E");

                printMenu(options, selectedIndex);

                String key = bindingReader.readBinding(keyMap);

                if (key == null) {
                    continue; // Skip if no valid key pressed
                }

                switch (key) {
                    case "UP":
                        selectedIndex = (selectedIndex > 0) ? selectedIndex - 1 : options.size() - 1;
                        break;
                    case "DOWN":
                        selectedIndex = (selectedIndex < options.size() - 1) ? selectedIndex + 1 : 0;
                        break;
                    case "QUIT":
                        TerminalHelper.printLn(terminal, Color.RED, "Exiting...");
                        terminal.flush();
                        return;
                    case "ENTER":
                        handleMenuSelection(options.get(selectedIndex), terminal);
                        terminal.writer().println("Selected: " + options.get(selectedIndex));
                        terminal.flush();
                        break;
                    default:
                        break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> checkboxConsole(Terminal terminal, List<T> options) {
        BindingReader reader = new BindingReader(terminal.reader());
        boolean[] selected = new boolean[options.size()]; // Track which options are checked
        int totalPages = (int) Math.ceil((double) options.size() / PAGE_SIZE);
        int selectedIndex = 0;
        int currentPage = 0;

        while (true) {
            terminal.puts(InfoCmp.Capability.clear_screen);

            // Display instructions
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Navigate with W/S");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Toggle selection with Spacebar");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Next Page/Previous Page with D/A");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Quit/Confirm with Q/E");
            TerminalHelper.printLn(terminal, Color.BLUE, "If you don't select anything, the script will download all the chapters.");
            TerminalHelper.printLn(terminal, Color.CYAN, "------------------------------------------------------------------------");

            terminal.writer().flush();

            // Calculate the start and end indexes for the current page
            int startIndex = currentPage * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, options.size());

            // Display the checkbox menu for the current page
            try {
                List<String> optionsString = options.stream().map(Object::toString).toList();
                printCheckboxMenu(optionsString, selected, selectedIndex, terminal, startIndex, endIndex);
                TerminalHelper.printLn(terminal, Color.CYAN, String.format("Page %d of %d", currentPage + 1, totalPages));
                String key = reader.readBinding(keyMap);

                if (key == null) {
                    continue;
                }

                switch (key) {
                    case "UP":
                        selectedIndex = (selectedIndex > startIndex) ? selectedIndex - 1 : endIndex - 1;
                        break;
                    case "DOWN":
                        selectedIndex = (selectedIndex < endIndex - 1) ? selectedIndex + 1 : startIndex;
                        break;
                    case "NEXT":
                        if (endIndex < options.size()) {
                            currentPage++;
                            selectedIndex = currentPage * PAGE_SIZE;
                        }
                        break;
                    case "PREVIOUS":
                        if (currentPage > 0) {
                            currentPage--;
                            selectedIndex = currentPage * PAGE_SIZE;
                        }
                        break;
                    case "SPACE":
                        selected[selectedIndex] = !selected[selectedIndex];
                        break;
                    case "QUIT":
                    case "ENTER":
                        return handleCheckboxSelection(selected, options);
                    default:
                        break;
                }
            } catch (RuntimeException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printCheckboxMenu(List<String> options, boolean[] selected, int selectedIndex, Terminal terminal, int startIndex, int endIndex) throws IOException {
        for (int i = startIndex; i < endIndex; i++) {
            String checkboxMarker = selected[i] ? "[x] " : "[ ] ";
            String selectionMarker = (i == selectedIndex) ? "-> " : "   ";
            TerminalHelper.printLn(terminal, Color.YELLOW, selectionMarker + checkboxMarker + options.get(i));
        }
        terminal.flush();
    }

    private static <T> List<T> handleCheckboxSelection(boolean[] selected, List<T> options) {
        List<T> selectedOptions = new ArrayList<>();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                selectedOptions.add(options.get(i));
            }
        }
        return selectedOptions;
    }

    private static void firstTimeConfig() {
        if (!configFile.exists()) {
            log.info("Init config file...");
            try {
                String dirUrl;
                AppConfig appConfigTemp = AppConfig.configTemplate();
                while (true) {
                    System.out.println("Choose working directory: ");
                    dirUrl = scanner.nextLine().replace("\\", "/");
                    File dir = new File(dirUrl);
                    if (!dir.exists() || !dir.isDirectory()) {
                        log.error("Enter valid working directory!");
                    } else {
                        break;
                    }
                }
                AppConfig appConfig = YtdlUtil.downloadLastedVersion(appConfigTemp);
                if (appConfig == null) {
                    appConfig = appConfigTemp;
                }
                appConfig.setWorking_directory(dirUrl);
                File mangaDir = new File(dirUrl + "/Mangas");
                if (!mangaDir.exists()) {
                    mangaDir.mkdir();
                }
                FileUtils.writeStringToFile(configFile, gson.toJson(appConfig), "UTF-8");
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private static void printMenu(List<String> options, int selectedIndex) {
        for (int i = 0; i < options.size(); i++) {
            if (i == selectedIndex) {
                System.out.println("\u001B[38;5;212m> \u001B[0m" + options.get(i));
            } else {
                System.out.println("  " + options.get(i));
            }
        }
    }
}
