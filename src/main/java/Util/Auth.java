package Util;

import Model.YoutubeToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 *
 */
public class Auth {

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final String YOUTUBE_KEY_PATH = "./libs/yt_cred.json";
    private static final String TOKEN_PATH = "./libs/yt_token.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Gson gson = new Gson();
    static Logger log = LogManager.getLogger(Auth.class);

    public static Credential authUser() {
        List<String> scope = List.of(YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE);
        try {
            FileDataStoreFactory fileFactory = new FileDataStoreFactory(new File("./libs"));
            GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, getClientSecrets(), scope)
                    .setAccessType("offline").setApprovalPrompt("force")
                    .setDataStoreFactory(fileFactory)
                    .build();
            Credential credential = new AuthorizationCodeInstalledApp(authFlow, new LocalServerReceiver()).authorize("user");
            File ytTokenJson = new File(TOKEN_PATH);
            if (!ytTokenJson.exists()) {
                YoutubeToken youtubeToken = YoutubeToken.builder()
                        .access_token(credential.getAccessToken())
                        .refresh_token(credential.getRefreshToken())
                        .expired_date(getExpirationDate(credential.getExpiresInSeconds())).build();
                FileUtils.writeStringToFile(ytTokenJson, gson.toJson(youtubeToken), "UTF-8");
            }
            return credential;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TokenResponse getRefreshedToken(String refreshToken) throws IOException {
        List<String> scope = List.of(YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE);

        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(HTTP_TRANSPORT, JSON_FACTORY,
                refreshToken, getClientSecrets().getInstalled().getClientId(), getClientSecrets().getInstalled().getClientSecret())
                .setScopes(scope).setGrantType("refresh_token").execute();
        return tokenResponse;
    }

    @SneakyThrows
    public static AccessToken getAccessToken() {
        Credential credential = authUser();
        YoutubeToken youtubeToken = gson.fromJson(new FileReader(TOKEN_PATH), YoutubeToken.class);
        if (credential.getExpiresInSeconds() <= 0 && isExpired(youtubeToken.getExpired_date())) {
            log.info("Refreshing token...");
            TokenResponse tokenResponse = getRefreshedToken(youtubeToken.getRefresh_token());
            youtubeToken.setAccess_token(tokenResponse.getAccessToken());
//            youtubeToken.setRefresh_token(tokenResponse.getRefreshToken());
            youtubeToken.setExpired_date(getExpirationDate(tokenResponse.getExpiresInSeconds()));
            FileUtils.writeStringToFile(new File(TOKEN_PATH), gson.toJson(youtubeToken), "UTF-8");
        }

        return AccessToken.newBuilder().setTokenValue(youtubeToken.getAccess_token()).build();
    }

    private static GoogleClientSecrets getClientSecrets() {
        try {
            return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(YOUTUBE_KEY_PATH)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Converts expiration time in seconds to human-readable date
    private static String getExpirationDate(Long expiresInSeconds) {
        Instant expirationInstant = Instant.now().plusSeconds(expiresInSeconds);
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(expirationInstant, ZoneId.systemDefault());
        return expirationDateTime.format(formatter);
    }

    // Checks if the token has expired using the expiration date string
    private static boolean isExpired(String expirationDate) {
        try {
            LocalDateTime expirationDateTime = LocalDateTime.parse(expirationDate, formatter);
            Instant expirationInstant = expirationDateTime.atZone(ZoneId.systemDefault()).toInstant();
            return Instant.now().isAfter(expirationInstant);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + expirationDate);
            return true; // Consider expired if the date format is invalid
        }
    }

}
