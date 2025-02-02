package com.awesome.yunpicturebackend.util;

/**
 * 颜色相似度工具
 */
public class ColorSimilarityUtil {

    /**
     * 计算两个十六进制颜色的相似度
     *
     * @param hexColor1 第一个颜色值，例如 "#FFFFFF"
     * @param hexColor2 第二个颜色值，例如 "#000000"
     * @return 相似度，范围为 0（完全不同）到 1（完全相同）
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        // 转换为 RGB 值
        int[] rgb1 = hexToRgb(hexColor1);
        int[] rgb2 = hexToRgb(hexColor2);

        // 计算欧几里得距离
        double distance = Math.sqrt(
                Math.pow(rgb1[0] - rgb2[0], 2) +
                Math.pow(rgb1[1] - rgb2[1], 2) +
                Math.pow(rgb1[2] - rgb2[2], 2)
        );

        // 最大颜色距离（255,255,255 和 0,0,0）
        double maxDistance = Math.sqrt(Math.pow(255, 2) * 3);

        // 计算相似度（1 - 距离比值）
        return 1 - (distance / maxDistance);
    }

    /**
     * 将十六进制颜色转换为 RGB 数组
     *
     * @param hexColor 十六进制颜色字符串，例如 "#FFFFFF" 或 "0xFFFFFF" 或 "FFF"
     * @return RGB 数组，例如 [255, 255, 255]
     */
    private static int[] hexToRgb(String hexColor) {
        // 处理前缀 "0x" 或 "#"
        if (hexColor.startsWith("0x") || hexColor.startsWith("0X")) {
            hexColor = hexColor.substring(2);
        } else if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        // 补全非标准长度的颜色字符串
        if (hexColor.length() == 3) {
            // 3 位颜色扩展为 6 位
            hexColor = "" + hexColor.charAt(0) + hexColor.charAt(0)
                    + hexColor.charAt(1) + hexColor.charAt(1)
                    + hexColor.charAt(2) + hexColor.charAt(2);
        } else if (hexColor.length() < 6) {
            // 不足 6 位的颜色用 "0" 补全至 6 位
            hexColor = String.format("%-6s", hexColor).replace(' ', '0');
        } else if (hexColor.length() > 6) {
            throw new IllegalArgumentException("颜色值长度超过 6 位，不合法：" + hexColor);
        }

        // 解析 RGB 值
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        return new int[]{r, g, b};
    }

}
