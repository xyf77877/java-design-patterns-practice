package com.xyf.decorator.fileload.extension;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件读取实现类
 */
public class BaseFileLoader implements IFileLoader {
    @Override
    public List<String> read() {
        // 读取文件内容
        List<String> strings = FileUtil.readLines("g:\\a.txt", "UTF-8");
        System.out.println("读取文件内容：" + strings);
        return strings;
    }

    @Override
    public void write(List<String> content) {
        FileUtil.writeLines(content, new File("g:\\a.txt"), "UTF-8");
        System.out.println("写入文件内容：" + content);
    }

    public static void main(String[] args) {
        IFileLoader fileLoader = new BaseFileLoader();
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("world");
        fileLoader.write(list);
        BaseFileLoader baseFileLoader = new BaseFileLoader();
        List<String> read = baseFileLoader.read();
    }
}
