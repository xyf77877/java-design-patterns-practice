package com.xyf.adapter;

/**
 * 对象适配器
 */
public class SDAdapterTF2 implements SDCard{
    private TFCard tfCard;

    public SDAdapterTF2(TFCard tfCard) {
        this.tfCard = tfCard;
    }
    @Override
    public String readSD() {
        return tfCard.readTF();
    }

    @Override
    public void writeSD(String msg) {
        tfCard.writeTF(msg);
    }
}
