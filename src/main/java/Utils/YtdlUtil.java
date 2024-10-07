package Utils;

import Model.AppConfig;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YtdlUtil {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

    static Logger log = LogManager.getLogger(YtdlUtil.class);

    public static String fetchLastedYtdl(AppConfig appConfig) {

        try {
            log.info("Getting latest version info...");
            Document document = Jsoup.connect(appConfig.getYt_git()).get();
            Elements elements = document.getElementsByClass("Box-body");
            for (int i = 0; i < elements.size(); i++) {
                return elements.get(i).getElementsByTag("h1").text().replaceAll("[^\\d.]", "");
            }
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    public static AppConfig downloadLastedVersion(AppConfig appConfig) {
        StringBuilder downloadUrl = new StringBuilder(appConfig.getYt_dl_url());
        String lastedTag = fetchLastedYtdl(appConfig);

        try {
            Date lastedDate = dateFormat.parse(lastedTag);
            Date lastedLocal = dateFormat.parse(appConfig.getYt_version());

            if (lastedDate.after(lastedLocal)) {
                log.info("Found new yt-dl version!!");
                downloadUrl.append(lastedTag).append("/yt-dlp.exe");
                FileUtils.copyURLToFile(new URL(downloadUrl.toString()), new File("./libs/yt-dlp.exe"));
                appConfig.setYt_version(lastedTag);
                log.info("Download yt-dl completed!");
                return appConfig;
            }
        } catch (ParseException | IOException e) {
            log.error(e);
        }
        return null;
    }

    public static String fetchTitle(String downloadUrl) {

        if (ytUrlValidate(downloadUrl)) {
            try {
                log.info("Getting information...");
                Document document = Jsoup.connect(downloadUrl).get();
                return document.title().replaceAll(" - YouTube$", "");
            } catch (IOException e) {
                log.error(e);
            }
        }

        return null;

    }

    public static void downloadVideo(List<String> selectedOptions) {

    }

    private static Boolean ytUrlValidate(String downloadUrl) {

        String regex = "^https?://(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{11})(?:&[\\w-]+=[\\w-]+)*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(downloadUrl);
        return matcher.matches();

    }
}
