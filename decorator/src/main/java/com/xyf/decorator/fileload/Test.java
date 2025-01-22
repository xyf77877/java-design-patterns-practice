package com.xyf.decorator.fileload;

import com.xyf.decorator.fileload.extension.BaseFileLoader;
import com.xyf.decorator.fileload.extension.EncryptionDataLoaderDecorator;
import com.xyf.decorator.fileload.extension.EncryptionType;
import com.xyf.decorator.fileload.extension.IFileLoader;

import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("hello", "world");
        IFileLoader fileLoader = new EncryptionDataLoaderDecorator(new BaseFileLoader(), EncryptionType.AES);
        fileLoader.write(list);
        List<String> read = fileLoader.read();
        System.out.println(read);
    }
}
