package com.xyf.decorator.fileload.extension;

/**
 * 加密策略接口
 */
public interface IEncryptionStrategy {
    Enum<EncryptionType> getStrategyType();
    String encrypt(String content);
    String decrypt(String content);
}
