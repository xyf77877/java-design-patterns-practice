package com.xyf.decorator.fileload.extension;

import java.util.Base64;

public class Base64EncryptionStrategy implements IEncryptionStrategy{
    @Override
    public Enum<EncryptionType> getStrategyType() {
        return EncryptionType.BASE64;
    }

    @Override
    public String encrypt(String content) {
        if (content != null) {
            return Base64.getEncoder().encodeToString(content.getBytes());
        }
        return null;
    }

    @Override
    public String decrypt(String content) {
        if (content != null){
            return new String(Base64.getDecoder().decode(content));
        }
        return null;
    }
}
