package interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RateLimitInterceptor implements Interceptor {

    private static final int MAX_RETRIES = 5;
    private static final int INITIAL_WAIT_TIME_MS = 1000; // 1 second

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int retryCount = 0;
        int waitTime = INITIAL_WAIT_TIME_MS;

        while (retryCount < MAX_RETRIES) {
            Response response = chain.proceed(request);

            // Check if we received a 429 response (Too Many Requests)
            if (response.code() == 429) {
                retryCount++;
                response.close(); // Close the response to avoid leaks

                // Check for the Retry-After header in seconds (if provided by the server)
                String retryAfterHeader = response.header("Retry-After");
                if (retryAfterHeader != null) {
                    try {
                        int retryAfter = Integer.parseInt(retryAfterHeader);
                        waitTime = retryAfter * 1000; // Convert seconds to milliseconds
                    } catch (NumberFormatException e) {
                        // Fallback to the current wait time if the header value is not a valid number
                    }
                }

                try {
                    Thread.sleep(waitTime); // Wait before retrying
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                    throw new IOException("Interrupted while waiting to retry after 429", e);
                }

                waitTime *= 2; // Apply exponential backoff for the next retry
            } else {
                // If the response is successful or a different error, return the response
                return response;
            }
        }

        throw new IOException("Failed to get a successful response after multiple retries due to rate limiting.");
    }

}
