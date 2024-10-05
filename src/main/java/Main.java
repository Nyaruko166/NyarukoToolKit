import Utils.AppConfig;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static File configFile = new File("./libs/config.json");

    static Gson gson = new Gson();

    public static void main(String[] args) {

        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .jansi(true)
                    .build();
            terminal.enterRawMode();
            BindingReader bindingReader = new BindingReader(terminal.reader());

            List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3", "Exit");
            int selectedIndex = 0;

            KeyMap<String> keyMap = new KeyMap<>();
            keyMap.bind("UP", "w");          // W key for moving up
            keyMap.bind("DOWN", "s");        // S key for moving down
            keyMap.bind("QUIT", "q");        // Q key to exit
            keyMap.bind("ENTER", "e");      // Enter key

            while (true) {

                terminal.puts(InfoCmp.Capability.clear_screen);
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
                        terminal.writer().println("Exiting...");
                        terminal.flush();
//                        terminal.close();
                        return;
                    case "ENTER":
                        if (options.get(selectedIndex).equals("Exit")) {
                            terminal.writer().println("Exiting...");
                            terminal.flush();
                            return;
                        }
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

    // Method to handle the selected option
    private static void handleMenuSelection(String selectedOption, Terminal terminal) throws IOException {
        switch (selectedOption) {
            case "Option 1":
                method1(terminal);
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
                terminal.writer().println("Invalid option!");
                break;
        }
    }

    // Dummy method 1
    private static void method1(Terminal terminal) throws IOException {
        terminal.writer().println("You selected Option 1");
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

    public static void junk() {
        try {

            if (!configFile.exists()) {
                log.info("Init config file...");
                FileUtils.writeStringToFile(configFile, gson.toJson(AppConfig.InitConfig()), "UTF-8");
            }

            AppConfig appConfig = gson.fromJson(new FileReader(configFile), AppConfig.class);

            StringBuilder strYtUrl = new StringBuilder("https://github.com/yt-dlp/yt-dlp/releases/download/");
            String lastedTag = "";
            Document document = Jsoup.connect(appConfig.getYt_git()).get();
            Elements elements = document.getElementsByClass("Box-body");
            for (int i = 0; i < elements.size(); i++) {
                log.info("Checking for yt-dl update...");
                lastedTag = elements.get(i).getElementsByTag("h1").text().replaceAll("[^\\d.]", "");
                strYtUrl.append(lastedTag).append("/yt-dlp.exe");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            Date lastedDate = dateFormat.parse(lastedTag);
            Date lastedLocal = dateFormat.parse(appConfig.getYt_version());

            if (lastedDate.after(lastedLocal)) {
                log.info("Found new yt-dl version!!");
//                System.out.println("Downloading yt-dl...");
                FileUtils.copyURLToFile(new URL(strYtUrl.toString()), new File("./libs/yt-dlp.exe"));
                appConfig.setYt_version(lastedTag);
                FileUtils.writeStringToFile(configFile, gson.toJson(appConfig), "UTF-8");
                log.info("Update yt-dl completed!");
            }


        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printMenu(List<String> options, int selectedIndex) {
        for (int i = 0; i < options.size(); i++) {
            if (i == selectedIndex) {
                System.out.println("> " + options.get(i));
            } else {
                System.out.println("  " + options.get(i));
            }
        }
    }

}
