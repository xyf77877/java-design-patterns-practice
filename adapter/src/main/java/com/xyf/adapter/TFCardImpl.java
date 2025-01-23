package com.xyf.adapter;

public class TFCardImpl implements TFCard{
    @Override
    public String readTF() {
        System.out.println("TF卡读取数据");
        return "TF卡数据";
    }

    @Override
    public void writeTF(String msg) {
        System.out.println("TF卡写入数据");
    }
}
