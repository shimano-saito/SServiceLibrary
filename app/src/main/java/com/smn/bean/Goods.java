package com.smn.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 购物车中的单个商品
 */
public class Goods implements Cloneable, Parcelable {

    private int subSeq;
    private int count;
    private String goodsName;
    private String goodsPriceTaxEx;
    private String goodsPriceTaxIn;
    private String productCode;
    private String cabNo;
    private String channelNo;
    private String dummy1;
    private String box;
    private String diopter;
    private int result = -1;

    public int getSubSeq() {
        return subSeq;
    }

    public void setSubSeq(int subSeq) {
        this.subSeq = subSeq;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsPriceTaxEx() {
        return goodsPriceTaxEx;
    }

    public void setGoodsPriceTaxEx(String goodsPriceTaxEx) {
        this.goodsPriceTaxEx = goodsPriceTaxEx;
    }

    public String getGoodsPriceTaxIn() {
        return goodsPriceTaxIn;
    }

    public void setGoodsPriceTaxIn(String goodsPriceTaxIn) {
        this.goodsPriceTaxIn = goodsPriceTaxIn;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getCabNo() {
        return cabNo;
    }

    public void setCabNo(String cabNo) {
        this.cabNo = cabNo;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    public String getDummy1() {
        return dummy1;
    }

    public void setDummy1(String dummy1) {
        this.dummy1 = dummy1;
    }

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getDiopter() {
        return diopter;
    }

    public void setDiopter(String diopter) {
        this.diopter = diopter;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.subSeq);
        dest.writeInt(this.count);
        dest.writeString(this.goodsName);
        dest.writeString(this.goodsPriceTaxEx);
        dest.writeString(this.goodsPriceTaxIn);
        dest.writeString(this.productCode);
        dest.writeString(this.cabNo);
        dest.writeString(this.channelNo);
        dest.writeString(this.dummy1);
        dest.writeString(this.box);
        dest.writeString(this.diopter);
        dest.writeInt(this.result);
    }

    public Goods() {
    }

    protected Goods(Parcel in) {
        this.subSeq = in.readInt();
        this.count = in.readInt();
        this.goodsName = in.readString();
        this.goodsPriceTaxEx = in.readString();
        this.goodsPriceTaxIn = in.readString();
        this.productCode = in.readString();
        this.cabNo = in.readString();
        this.channelNo = in.readString();
        this.dummy1 = in.readString();
        this.box = in.readString();
        this.diopter = in.readString();
        this.result = in.readInt();
    }

    public static final Creator<Goods> CREATOR = new Creator<Goods>() {
        @Override
        public Goods createFromParcel(Parcel source) {
            return new Goods(source);
        }

        @Override
        public Goods[] newArray(int size) {
            return new Goods[size];
        }
    };
}
