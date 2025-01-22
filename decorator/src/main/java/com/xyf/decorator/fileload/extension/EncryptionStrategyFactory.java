package com.xyf.decorator.fileload.extension;

import org.springframework.stereotype.Component;

import static com.xyf.decorator.fileload.extension.EncryptionType.AES;

@Component
public class EncryptionStrategyFactory {
    private static final EncryptionType encryptionType = AES;

    public static builder builder() {
        return new builder();
    }

    public static class builder {
        private EncryptionType encryptionType;

        public IEncryptionStrategy build() {
            return switch (encryptionType) {
                case AES -> new AESIEncryptionStrategy();
                case BASE64 -> new Base64EncryptionStrategy();
                default -> null;
            };
        }

        public builder setEncryptionType(EncryptionType encryptionType) {
            this.encryptionType = encryptionType;
            return this;
        }
    }
}
