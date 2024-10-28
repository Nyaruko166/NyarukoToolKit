package Model;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YoutubeToken {

    private String access_token;

    private String refresh_token;

    private String expired_date;
}
