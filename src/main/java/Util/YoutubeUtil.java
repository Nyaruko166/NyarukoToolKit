package Util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import Handler.FileUploadProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

public class YoutubeUtil {

    static Logger log = LogManager.getLogger(YoutubeUtil.class);

    private static final YouTube youtubeService = getService();

    //Todo set title according to game, make terminal clip choice? maybe
    public static void main(String[] args) {
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-56-40-500.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-45-49-999.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-40-57-372.mp4");
//        uploadVideo("D:\\Videos\\Outplayed\\GTFO\\GTFO_10-24-2024_22-15-26-647\\GTFO_10-25-2024_0-49-43-315.mp4");

    }

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

    public static void uploadVideo(String videoPath) {
        Video video = new Video();

        VideoStatus videoStatus = new VideoStatus();
        videoStatus.setPrivacyStatus("unlisted");
        video.setStatus(videoStatus);

        VideoSnippet videoSnippet = new VideoSnippet();
        videoSnippet.setTitle("Test Title");
        Calendar calendar = Calendar.getInstance();
        videoSnippet.setDescription("Game abc - " + calendar.getTime());
        videoSnippet.setTags(List.of("Sech", "Test"));
        video.setSnippet(videoSnippet);

        File videoFile = Paths.get(videoPath).toFile();
        FileContent mediaContent = new FileContent("video/*", videoFile);

        try {
            YouTube.Videos.Insert request = youtubeService.videos().insert(List.of("snippet", "status"), video, mediaContent);
            request.getMediaHttpUploader().setDirectUploadEnabled(false);
            request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
            request.getMediaHttpUploader().setProgressListener(new FileUploadProgressListener());
            Video response = request.execute();
            log.info("Video uploaded successfully: {}", response.getId());
            log.info("Link: https://www.youtube.com/watch?v={}", response.getId());
        } catch (IOException e) {
            log.error(e);
        }
    }

}
