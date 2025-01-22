package com.xyf.decorator.fileload.extension;


import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESIEncryptionStrategy implements IEncryptionStrategy {
    // 密钥长度为16字节 (128位)
    private static final byte[] key = "1234567890123456".getBytes(StandardCharsets.UTF_8);
    // 初始化向量 (IV) 长度也是16字节
    private static final byte[] iv = "6543210987654321".getBytes(StandardCharsets.UTF_8);
    // 使用 CBC 模式和 PKCS5Padding 填充
    private static final AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);



    @Override
    public Enum<EncryptionType> getStrategyType() {
        return EncryptionType.AES;
    }

    @Override
    public String encrypt(String content) {
        if (content != null) {
            byte[] encrypted = aes.encrypt(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        return null;
    }

    @Override
    public String decrypt(String content) {
        if (content != null) {
            byte[] encryptedBytes = Base64.getDecoder().decode(content);
            byte[] decrypted = aes.decrypt(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        return null;
    }

}
