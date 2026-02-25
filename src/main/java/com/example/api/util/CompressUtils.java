package com.example.api.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class CompressUtils {
    private CompressUtils() {
    }

    /**
     * UTF-8 문자열을 GZIP 압축 후 Base64 인코딩한 문자열로 반환한다.
     */
    public static String gzipBase64(String str) {
        if (str == null || str.isEmpty()) return "";
        try {
            byte[] input = str.getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
                gzip.write(input);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }
}
