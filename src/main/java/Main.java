import Connector.Nettruyen;
import Model.AppConfig;
import Model.Chapter;
import Util.Color;
import Util.TerminalHelper;
import Util.YtdlUtil;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

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

        firstTimeConfig();
        log.info("Reading config...");
        loadedConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);

        Terminal terminal = TerminalBuilder.builder().system(true).jansi(true).build();
//        terminal.enterRawMode();
        menuConsole(terminal);
//        checkboxConsole(terminal);
    }

    private static void checkForUpdate(Terminal terminal) throws IOException {
        if (YtdlUtil.downloadLastedVersion(loadedConfig) == null) {
            log.info("You're up to date!");
        }
        log.info("Press any key to continue...");
        terminal.flush();
        terminal.reader().read();
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

        terminal.flush();
        log.info("Press any key to continue...");
        terminal.reader().read();
    }

    private static void mangaCrawler(Terminal terminal) throws IOException {
        Nettruyen nettruyen = new Nettruyen();
        String title;
        String mangaUrl;
        do {
            log.debug("Right now we can only download manga from Nettruyen-likes website...");
            log.info("Enter manga url: ");
            mangaUrl = scanner.nextLine();
            title = nettruyen.getMangaTitle(mangaUrl);
        } while (title == null);

        List<Chapter> lstChapter = nettruyen.getChapterList(mangaUrl);

        List<Chapter> selectedChapters = checkboxConsole(terminal, lstChapter);

        log.info("{} chapters selected.", selectedChapters.size());

        for (Chapter x : selectedChapters) {
            log.info(x.getTitle());
        }

        //Choose none = download all
        if (selectedChapters.isEmpty()) {
            nettruyen.downloadManga(title, lstChapter);
        } else {
            nettruyen.downloadManga(title, selectedChapters);
        }

        terminal.flush();
        log.info("Press any key to continue...");
        terminal.reader().read();
    }

    // Method to handle the selected option
    private static void handleMenuSelection(String selectedOption, Terminal terminal) throws IOException {
        switch (selectedOption) {
            case "Check for update":
                checkForUpdate(terminal);
                break;
            case "Download video from youtube":
                downloadYT(terminal);
                break;
            case "Download manga to PDF":
                mangaCrawler(terminal);
                break;
            case "QUIT":
                terminal.writer().println("Goodbye!");
                break;
            default:
                TerminalHelper.printLn(terminal, Color.RED, "Invalid option!");
                break;
        }
    }

    private static void menuConsole(Terminal terminal) {

        List<String> options = Arrays.asList("Check for update", "Download video from youtube", "Download manga to PDF");

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

    private static void printCheckboxMenu(List<String> options, boolean[] selected, int selectedIndex,
                                          Terminal terminal, int startIndex, int endIndex) throws IOException {
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
                    if (!dir.exists() && !dir.isDirectory()) {
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
