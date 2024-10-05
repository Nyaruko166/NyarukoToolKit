package Utils;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppConfig {

    private String yt_git;

    private String yt_version;

    private String app_name;

    private String working_directory;

    public static AppConfig InitConfig() {

        AppConfig appConfig = AppConfig.builder()
                .yt_git("https://github.com/yt-dlp/yt-dlp/releases/latest")
                .yt_version("2003.6.1")
                .app_name("test")
                .working_directory("")
                .build();

        return appConfig;

    }

}
