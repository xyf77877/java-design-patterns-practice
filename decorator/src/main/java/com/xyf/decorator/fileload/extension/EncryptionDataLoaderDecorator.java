package com.xyf.decorator.fileload.extension;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 加解密装饰器
 */
public class EncryptionDataLoaderDecorator implements IFileLoader {

    /**
     * 被装饰的 DataLoader
     */
    private final IFileLoader dataLoader;
    /**
     * 加密类型
     */
    private final EncryptionType encryptionType;

    // 注入被装饰的 DataLoader
    public EncryptionDataLoaderDecorator(IFileLoader dataLoader,EncryptionType encryptionType) {
        this.dataLoader = dataLoader;
        this.encryptionType = encryptionType;
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
        IEncryptionStrategy build = EncryptionStrategyFactory.builder().setEncryptionType(encryptionType).build();
        return data.stream().map(build::encrypt).collect(Collectors.toList());
    }

    private List<String> decrypt(List<String> encryptedData) {
        IEncryptionStrategy build = EncryptionStrategyFactory.builder().setEncryptionType(encryptionType).build();
        return encryptedData.stream().map(build::decrypt).collect(Collectors.toList());
    }
}
