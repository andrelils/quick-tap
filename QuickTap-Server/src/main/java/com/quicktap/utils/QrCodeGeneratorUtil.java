package com.quicktap.utils;

import org.springframework.stereotype.Component;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 二维码生成工具
 */
@Component
public class QrCodeGeneratorUtil {

    /**
     * 生成二维码并返回 Base64 编码的图片
     */
    public String generateQrCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 256, 256);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(pngData);

            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败", e);
        }
    }

    /**
     * 生成唯一的二维码编码
     */
    public String generateQrCodeId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
