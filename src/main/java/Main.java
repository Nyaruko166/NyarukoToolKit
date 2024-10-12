import Connector.Nettruyen;
import Model.AppConfig;
import Util.Color;
import Util.YtdlUtil;
import Util.TerminalHelper;
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
import java.util.*;


public class Main {

    static final Logger log = LogManager.getLogger(Main.class);

    static final File configFile = new File("./libs/config.json");

    static AppConfig loadedConfig = new AppConfig();

    static final Scanner scanner = new Scanner(System.in);

    static final Gson gson = new Gson();

    static KeyMap<String> keyMap = new KeyMap<>();

    public static void main(String[] args) throws IOException {

//        Map<String,String> ytdlArgs = new HashMap<>();
//
//        ytdlArgs.put();

        // Create a key map to bind keys to specific actions
        keyMap.bind("UP", "w");
        keyMap.bind("DOWN", "s");
        keyMap.bind("SPACE", " ");
        keyMap.bind("ENTER", "e");
        //Todo: Check box still not have quit and back yet
        keyMap.bind("QUIT", "q");

        firstTimeConfig();
        log.info("Reading config...");
        loadedConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);

        Terminal terminal = TerminalBuilder.builder().system(true).jansi(true).build();
//        terminal.enterRawMode();

        List<String> mainMenuOptions = Arrays.asList("Check for update", "Download video from youtube", "Download manga to PDF");

        menuConsole(terminal, mainMenuOptions);
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

        loop:
        while (true) {
            log.debug("Right now we can only download manga from Nettruyen-likes website...");
            log.info("Enter manga url: ");
            String mangaUrl = scanner.nextLine();
            switch (nettruyen.downloadManga(loadedConfig.getWorking_directory(), mangaUrl)) {
                case SUCCESS -> {
                    break loop;
                }
                case NOT_FOUND -> {
                    terminal.puts(InfoCmp.Capability.clear_screen);
                    log.error("Manga not found!");
                }
            }
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

    private static void menuConsole(Terminal terminal, List<String> options) {

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

    private static void checkboxConsole(Terminal terminal) {
        // Create a BindingReader to read input
        BindingReader reader = new BindingReader(terminal.reader());

        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        boolean[] selected = new boolean[options.size()]; // Track which options are checked
        int selectedIndex = 0;

        while (true) {

            terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);

            TerminalHelper.printLn(terminal, Color.GREEN, "==> Up/Down with W/S");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Quit/Confirm with Q/E");
            TerminalHelper.printLn(terminal, Color.GREEN, "==> Choose with Spacebar");


            terminal.writer().flush();

            // Display the checkbox menu
            try {
                printCheckboxMenu(options, selected, selectedIndex, terminal);
                // Read user input using BindingReader
                String key = reader.readBinding(keyMap);

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
                    case "SPACE":
                        // Toggle the selected option
                        selected[selectedIndex] = !selected[selectedIndex];
                        break;
                    case "ENTER":
                        handleCheckboxSelection(selected, options, terminal);
                        return;
                    default:
                        break;
                }
            } catch (RuntimeException | IOException e) {
                log.error(e);
            }
        }
    }

    // Handle the final menu selection and output the selected checkboxes
    private static void handleCheckboxSelection(boolean[] selected, List<String> options, Terminal terminal) throws IOException {

        List<String> selectedOptions = new ArrayList<>();

        //Todo handle none options

        terminal.writer().println("\nSelected options:");
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                selectedOptions.add(options.get(i));
            }
        }
        terminal.flush();  // Make sure to flush the output

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

    // Print the menu to the console, showing checkboxes
    private static void printCheckboxMenu(List<String> options, boolean[] selected, int selectedIndex, Terminal terminal) throws IOException {
        for (int i = 0; i < options.size(); i++) {
            String checkbox = selected[i] ? "[x]" : "[ ]";  // Show checked or unchecked box
            if (i == selectedIndex) {
                terminal.writer().println("\u001B[38;5;212m> \u001B[0m" + checkbox + " " + options.get(i));  // Highlight the selected option
            } else {
                terminal.writer().println("  " + checkbox + " " + options.get(i));
            }
        }
        terminal.flush();  // Ensure the menu is flushed and printed immediately
    }

}
