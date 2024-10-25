package Util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTubeScopes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Shared class used by every sample. Contains methods for authorizing a user and caching credentials.
 */
public class Auth {

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final String YOUTUBE_KEY_PATH = "./libs/yt_cred.json";

    public static Credential authUser() throws IOException {
        List<String> scope = List.of(YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, getSecRead());
        FileDataStoreFactory fileFactory = new FileDataStoreFactory(new File("./libs"));

        GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scope).setDataStoreFactory(fileFactory)
                .build();

        return new AuthorizationCodeInstalledApp(authFlow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * @return the YouTube access token
     * @throws IOException if the auth flow throws an IOException
     */
    public static String getToken() throws IOException {
        return authUser().getAccessToken();
    }

    /**
     * Gets user custom file if present, else default file
     */
    public static Reader getSecRead() throws IOException {
        Path userSecFile = Paths.get(YOUTUBE_KEY_PATH);
        if (Files.exists(userSecFile, LinkOption.NOFOLLOW_LINKS)) {
            return new InputStreamReader(new FileInputStream(new File(userSecFile.toUri())));
        } else {
            return new InputStreamReader(new FileInputStream(YOUTUBE_KEY_PATH));
        }
    }
}
