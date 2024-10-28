package Model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppConfig {

    private String yt_git;

    private String yt_dl_url;

    private String yt_version;

    private String discord_token;

    private String working_directory;

    public static AppConfig configTemplate() {

        AppConfig appConfig = AppConfig.builder()
                .yt_git("https://github.com/yt-dlp/yt-dlp/releases/latest")
                .yt_dl_url("https://github.com/yt-dlp/yt-dlp/releases/download/")
                .yt_version("2003.6.1")
                .working_directory("")
                .build();
        return appConfig;

    }
}
