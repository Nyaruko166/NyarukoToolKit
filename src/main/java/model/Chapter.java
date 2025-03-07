package model;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chapter {

    private String title;

    private String src;

    @Override
    public String toString() {
        return title;
    }
}
