package com.xyf.adapter;

/**
 * 类适配器
 */
public class SDAdapterTF1 extends TFCardImpl implements SDCard{
    @Override
    public String readSD() {
        return readTF();
    }

    @Override
    public void writeSD(String msg) {
        writeTF(msg);
    }
}
