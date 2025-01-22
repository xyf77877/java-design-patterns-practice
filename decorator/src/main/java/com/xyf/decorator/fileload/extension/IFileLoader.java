package com.xyf.decorator.fileload.extension;

import java.util.List;

/**
 * 抽象文件加载器
 */
public interface IFileLoader {
    List<String> read();
    void write(List<String> content);
}
