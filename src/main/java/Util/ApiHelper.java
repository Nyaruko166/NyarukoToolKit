package Util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiHelper {

    static Logger log = LogManager.getLogger(ApiHelper.class);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
//            .followRedirects(true)
            .build();

    public static String getRequest(String url) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Print the response from the MangaDex API
                return response.body().string();
            } else {
                log.error("Failed to fetch data: {}", response.message());
            }
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

}
