package com.xyf.decorator.fileload;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 加解密装饰器
 */
public class EncryptionDataLoaderDecorator implements IFileLoader {

    private final IFileLoader dataLoader;

    // 注入被装饰的 DataLoader
    public EncryptionDataLoaderDecorator(IFileLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    public List<String> read() {
        List<String> encryptedData = dataLoader.read();
        if (encryptedData != null) {
            return decrypt(encryptedData);  // 解密
        }
        return null;
    }

    @Override
    public void write(List<String> data) {
        List<String> encryptedData = encrypt(data);  // 加密
        dataLoader.write(encryptedData);
    }

    private List<String> encrypt(List<String> data) {
        // 简单使用 Base64 加密（可替换为更复杂的加密算法）
        List<String> collect = data.stream().map(datum -> Base64.getEncoder().encodeToString(datum.getBytes())).collect(Collectors.toList());
        return collect;
    }

    private List<String> decrypt(List<String> encryptedData) {
        return encryptedData.stream().map(datum -> Base64.getDecoder().decode(datum)).map(bytes -> new String(bytes)).collect(Collectors.toList());
    }
}
