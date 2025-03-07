package model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPallete {

    public MemberPallete(String memberName, String hexColor) {
        this.hexColor = hexColor;
        this.memberName = memberName;
        this.bgrColor = convertHexToABGR(hexColor);
    }

    private String memberName;

    private String hexColor;

    private String bgrColor;

    public String convertHexToABGR(String hexColor) {
        // Kiểm tra và loại bỏ ký tự "#" nếu có
//        int startIndex = in.indexOf("\"");
//        int endIndex = in.lastIndexOf("\"");
//        String hexColor = in.substring(startIndex + 1, endIndex);
        hexColor = hexColor.trim();
        String content = hexColor.replaceAll("<[^>]*>", "");

        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        // Lấy giá trị của màu R, G, B từ hexColor
        int red = Integer.parseInt(hexColor.substring(0, 2), 16);
        int green = Integer.parseInt(hexColor.substring(2, 4), 16);
        int blue = Integer.parseInt(hexColor.substring(4, 6), 16);

        // Chuyển đổi sang chuỗi "&HBBGGRRAA"
        return String.format("{\\c&H00%02X%02X%02X&}", blue, green, red);
    }
}
