package com.xyf.adapter;

public class Test {
    public static void main(String[] args) {
        SDCard sdCard = new SDCardImpl();
        System.out.println(sdCard.readSD());
        sdCard.writeSD("hello world");

        // 类适配器模式
        System.out.println("类适配器模式");
        SDCard adapterTF = new SDAdapterTF1();
        System.out.println(adapterTF.readSD());
        adapterTF.writeSD("hello world");
        // 对象适配器模式
        System.out.println("对象适配器模式");
        SDCard adapterTF2 = new SDAdapterTF2(new TFCardImpl());
        System.out.println(adapterTF2.readSD());
        adapterTF2.writeSD("hello world");
    }
}
