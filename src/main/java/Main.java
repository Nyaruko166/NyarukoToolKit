import Model.AppConfig;
import Utils.Color;
import Utils.YtdlUtil;
import Utils.TerminalHelper;
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
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Main {

    static final Logger log = LogManager.getLogger(Main.class);

    static final File configFile = new File("./libs/config.json");

    static AppConfig configLoaded = new AppConfig();

    static final Scanner scanner = new Scanner(System.in);

    static final Gson gson = new Gson();

    static YtdlUtil ytdlUtil = new YtdlUtil();

    public static void main(String[] args) throws IOException {

//        firstTimeConfig();
//        log.info("Reading config...");
//        configLoaded = gson.fromJson(new FileReader(configFile), AppConfig.class);

        Terminal terminal = TerminalBuilder.builder().system(true).jansi(true).build();
        terminal.enterRawMode();
//        menuConsole(terminal);
        checkboxConsole(terminal);
    }

    private static void checkboxConsole(Terminal terminal) {
        // Create a BindingReader to read input
        BindingReader reader = new BindingReader(terminal.reader());

        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4");
        boolean[] selected = new boolean[options.size()]; // Track which options are checked
        int selectedIndex = 0;

        // Create a key map to bind keys to specific actions
        KeyMap<String> keyMap = new KeyMap<>();
        keyMap.bind("UP", "w");
        keyMap.bind("DOWN", "s");
        keyMap.bind("SPACE", " ");
        keyMap.bind("ENTER", "e");

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

    // Handle the final menu selection and output the selected checkboxes
    private static void handleCheckboxSelection(boolean[] selected, List<String> options, Terminal terminal) throws IOException {

        terminal.writer().println("\nSelected options:");
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                terminal.writer().println("- " + options.get(i));  // Print checked options
            }
        }
        terminal.flush();  // Make sure to flush the output

    }

    // Method to handle the selected option
    private static void handleMenuSelection(String selectedOption, Terminal terminal) throws IOException {
        switch (selectedOption) {
            case "Check for update":
                checkForUpdate(terminal);
                break;
            case "Option 2":
                method2(terminal);
                break;
            case "Option 3":
                method3(terminal);
                break;
            case "Exit":
                terminal.writer().println("Goodbye!");
                break;
            default:
                TerminalHelper.printLn(terminal, Color.RED, "Invalid option!");
                break;
        }
    }

    private static void menuConsole(Terminal terminal) {

        try {
            BindingReader bindingReader = new BindingReader(terminal.reader());

            List<String> options = Arrays.asList("Check for update", "Option 2", "Option 3");
            int selectedIndex = 0;

            KeyMap<String> keyMap = new KeyMap<>();
            keyMap.bind("UP", "w");          // W key for moving up
            keyMap.bind("DOWN", "s");        // S key for moving down
            keyMap.bind("QUIT", "q");        // Q key to exit
            keyMap.bind("ENTER", "e");      // Enter key


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

    private static void checkForUpdate(Terminal terminal) throws IOException {
        if (ytdlUtil.downloadLastedVersion(configLoaded) == null) {
            log.info("You're up to date!");
            log.info("Press any key to continue...");
        }
        terminal.flush();
        terminal.reader().read(); // Wait for any key press to return to the menu
    }

    // Dummy method 2
    private static void method2(Terminal terminal) throws IOException {
        terminal.writer().println("You selected Option 2");
        terminal.flush();
        terminal.reader().read(); // Wait for any key press to return to the menu
    }

    // Dummy method 3
    private static void method3(Terminal terminal) throws IOException {
        terminal.writer().println("You selected Option 3");
        terminal.flush();
        terminal.reader().read(); // Wait for any key press to return to the menu
    }

    private static void firstTimeConfig() {
        if (!configFile.exists()) {
            log.info("Init config file...");
            try {
                String dirUrl;
                AppConfig appConfig = AppConfig.configTemplate();
                while (true) {
                    System.out.println("Choose working directory: ");
                    dirUrl = scanner.nextLine();
                    File dir = new File(dirUrl);
                    if (!dir.exists() && !dir.isDirectory()) {
                        log.error("Enter valid working directory!");
                    } else {
                        break;
                    }
                }
                appConfig = ytdlUtil.downloadLastedVersion(appConfig);
                appConfig.setWorking_directory(dirUrl);
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