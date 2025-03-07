package util;

import handler.FileUploadProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class YoutubeUtil {

    static Logger log = LogManager.getLogger(YoutubeUtil.class);

    private static final YouTube youtubeService = getService();

    //Todo set title according to game, make terminal clip choice? maybe
//    public static void main(String[] args) {
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-56-40-500.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-45-49-999.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-40-57-372.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-49-43-315.mp4");
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("videoId", "QJxneXLXlzY");
//        ApiHelper.postRequest("http://localhost:8080/discord/send-clip", ApiHelper.requestBodyBuilder(jsonObject.toString()));
//
//    }

    private static YouTube getService() {
        // Create the credentials object
        Credentials credentials = GoogleCredentials.create(Auth.getAccessToken());
        // Build the Youtube service object
        assert credentials != null;
        return new YouTube.Builder(
                Auth.HTTP_TRANSPORT,
                Auth.JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Youtube-Uploader")
                .build();
    }

    public static void uploadVideo(String title, File videoFile) {

        String[] nameArray = videoFile.getName().split("_");

        String newDate = LocalDate.parse(nameArray[1], DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Video video = new Video();

        VideoStatus videoStatus = new VideoStatus();
        videoStatus.setPrivacyStatus("unlisted");
        video.setStatus(videoStatus);

        VideoSnippet videoSnippet = new VideoSnippet();
        videoSnippet.setTitle(title);
        videoSnippet.setDescription("Game: %s - %s".formatted(nameArray[0], newDate));
        video.setSnippet(videoSnippet);

        FileContent mediaContent = new FileContent("video/*", videoFile);

        try {
            YouTube.Videos.Insert request = youtubeService.videos().insert(List.of("snippet", "status"), video, mediaContent);
            request.getMediaHttpUploader().setDirectUploadEnabled(false);
            request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
            request.getMediaHttpUploader().setProgressListener(new FileUploadProgressListener());
            log.info("Uploading video: Title - {} || File Name - {}", title, videoFile.getName());
            Video response = request.execute();
            String videoId = response.getId();
            log.info("Video uploaded successfully: {}", videoId);
            log.info("Link: https://www.youtube.com/watch?v={}", videoId);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("videoId", videoId);
            String url = Config.getInstance().getProperty().getDiscord_bot_api() + "/discord/send-clip";
            String res = ApiHelper.postRequest(url, ApiHelper.requestBodyBuilder(jsonObject.toString()));
            log.info(res);
            log.warn("Sleep for 5s...");
            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            log.error(e);
            System.exit(0);
        }
    }

}
