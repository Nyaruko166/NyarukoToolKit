import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Test {

    static Gson gson = new Gson();

    static final Logger log = LogManager.getLogger(Test.class);

    public static void main(String[] args) throws IOException, InterruptedException {

    }
//}

//        Scanner scanner = new Scanner(System.in);
//
//        String geminiKey = "Man i love fauna!";
//
//        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";
//
//        String bodyTemplate = "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}";
//        log.info("Enter your question: ");
//        String question = scanner.nextLine();
//        String body = String.format(bodyTemplate, question);
//
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost(apiUrl + geminiKey);
//
//        post.addHeader("Content-Type", "application/json");
//        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
//
//        JsonObject resultObject = new JsonObject();
//        try {
//            CloseableHttpResponse res = client.execute(post);
//            BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
//            Gson gson = new Gson();
//            resultObject = gson.fromJson(rd, JsonObject.class);
//            String answer = resultObject.get("candidates").getAsJsonArray().get(0).getAsJsonObject().get("content").getAsJsonObject().get("parts").getAsJsonArray().get(0).getAsJsonObject().get("text").toString();
//            log.info("Result for " + question + " is " + answer);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        System.out.print("Enter yt link: ");
//                    String ytUrl = in.nextLine();
//
//                    while (ytUrl.isBlank()) {
//                        System.out.println("Go fuck yourself");
//                        System.out.print("Pls enter yt link: ");
//                        ytUrl = in.nextLine();
//                    }
//
//                    String command = "cmd /c start cmd.exe /K \"cd ./libs/ && %s\" && exit";
//                    String customArgs = "yt-dlp.exe --list-subs ";
//
//                    try {
//                        Process process = Runtime.getRuntime().exec(String.format(command, customArgs + ytUrl));
//                        process.waitFor();
//                    } catch (Exception e) {
//                        System.out.println("HEY Buddy ! U r Dog Something Wrong ");
//                        e.printStackTrace();
//                    }

//}

}
