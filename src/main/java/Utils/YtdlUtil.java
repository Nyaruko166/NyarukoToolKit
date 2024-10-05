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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class YtdlUtil {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

    Logger log = LogManager.getLogger(YtdlUtil.class);

    public String fetchLastedYtdl(AppConfig appConfig) {

        try {
            log.info("Getting lasted version info...");
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

    public AppConfig downloadLastedVersion(AppConfig appConfig) {
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

}
