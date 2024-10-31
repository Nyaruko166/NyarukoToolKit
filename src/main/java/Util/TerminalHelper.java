package Util;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.io.IOException;

public class TerminalHelper {

    public static String colorLine(Terminal terminal, Color color, String content) {

        String colorString = "%s" + content + "\u001B[0m";
        String resultString = "";

        switch (color) {
            case BLACK:
                resultString = String.format(colorString, "\u001B[0;30m");
                break;
            case RED:
                resultString = String.format(colorString, "\u001B[0;31m");
                break;
            case GREEN:
                resultString = String.format(colorString, "\u001B[0;32m");
                break;
            case YELLOW:
                resultString = String.format(colorString, "\u001B[0;33m");
                break;
            case BLUE:
                resultString = String.format(colorString, "\u001B[0;34m");
                break;
            case PURPLE:
                resultString = String.format(colorString, "\u001B[0;35m");
                break;
            case CYAN:
                resultString = String.format(colorString, "\u001B[0;36m");
                break;
            case WHITE:
                resultString = String.format(colorString, "\u001B[0;37m");
                break;
        }

        return AttributedString.fromAnsi(resultString).toAnsi(terminal);
    }

    public static void printLn(Terminal terminal, Color color, String content) {
        terminal.writer().println(colorLine(terminal, color, content));
    }

    public static void anyKeyToCont(Terminal terminal) throws IOException {
        printLn(terminal, Color.CYAN, "Press any key to continue...");
        terminal.flush();
        terminal.reader().read();
    }

}
