import java.awt.*;
import java.io.IOException;

public class Console {

    public static void main(String[] args) throws IOException {
        java.io.Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            //Replace Main.class, Main.main()
            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "color a && java -jar \"" + filename + "\""});
        } else {
            Main.main(new String[0]);
            System.out.println("Program has ended, please type 'exit' or 'CTRL + D' to close the console");
        }
    }

}
