package com.xyf.adapter;

public class SDCardImpl implements SDCard{
    @Override
    public String readSD() {
        System.out.println("读取SD卡数据");
        return "SD卡数据";
    }

    @Override
    public void writeSD(String msg) {
        System.out.println("写入SD卡数据");
    }
}
