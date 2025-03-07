package util;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NetworkHelper {

    static Logger log = LogManager.getLogger(NetworkHelper.class);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .addInterceptor(new UserAgentInterceptor())
//            .addInterceptor(new RateLimitInterceptor())
            .build();

    // Method to fetch HTML content from a URL
    public static String fetchHtml(String url) {

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
//                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Referer", Objects.requireNonNull(getBaseUrl(url)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Failed to fetch HTML. Response code: " + response.code());
            }
        } catch (IOException e) {
            log.error(e);
        }
        return "";
    }

    // Method to download an image from a get request and response with ByteStream
    public static void downloadImageByte(String imageUrl, String referer, String outputFilePath) throws IOException {
        Request request = new Request.Builder()
                .url(imageUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Referer", referer + "/")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
//                File outputFile = new File(outputFilePath);
//                if (!outputFile.exists()) {
//                    outputFile.mkdir();
//                }
                FileUtils.copyInputStreamToFile(response.body().byteStream(), new File(outputFilePath));
//                saveImage(response.body(), outputFilePath);
            } else {
                throw new IOException("Failed to download image. Response code: " + response.code());
            }
        }
    }

    public static String getBaseUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException e) {
            log.error("Invalid URL format: {}", e.getMessage());
            return null;
        }
    }

    // Interceptor to mimic a modern browser User-Agent
    static class UserAgentInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }
}
