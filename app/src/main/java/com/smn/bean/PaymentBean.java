/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.smn.bean;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PaymentBean implements Cloneable, Parcelable {

    private String sn;
    private String outTradeNo = "";
    private String productCode;
    private String subjectSound;
    private String subjectOther;
    private String totalFee;
    private String itBPay;
    private String dynamicIdType;
    private String qRId = "";
    private String timer;
    private String deliverResult = "";
    private String payMode = "";
    private String money_QuickPass = "";
    private String quickpass_tradeNo = "";
    private byte[] tradeData = new byte[22];
    private String dynamicId = "";
    private String balanceOfAccount;
    private String money = "";
    private String androidpn_status = "";
    private String activitiesMode;
    private String activitiesCode;
    private String goodsId;
    private String cabinetNoStr;
    private String cartNoStr;

    private String terminalNum = "";
    private String userId = "";
    private String sequence = "";
    private int tax = 0;
    private int totalCount;
    private String totalMoneyTaxIn = "";
    private String totalMoneyTaxEx = "";
    private String backscancode = "";
    private String payType = "";
    private String payName = "";
    private String appType = "";
    private int resultStatistic = 2;
    private ArrayList<Goods> goods;
    private String sign = "";

    private int boxSize;

    public int getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(int boxSize) {
        this.boxSize = boxSize;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public String getTotalMoneyTaxIn() {
        return totalMoneyTaxIn;
    }

    public void setTotalMoneyTaxIn(String totalMoneyTaxIn) {
        this.totalMoneyTaxIn = totalMoneyTaxIn;
    }

    public String getTotalMoneyTaxEx() {
        return totalMoneyTaxEx;
    }

    public void setTotalMoneyTaxEx(String totalMoneyTaxEx) {
        this.totalMoneyTaxEx = totalMoneyTaxEx;
    }

    public String getTerminalNum() {
        return terminalNum;
    }

    public void setTerminalNum(String terminalNum) {
        this.terminalNum = terminalNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getBackscancode() {
        return backscancode;
    }

    public void setBackscancode(String backscancode) {
        this.backscancode = backscancode;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public int getResultStatistic() {
        return resultStatistic;
    }

    public void setResultStatistic(int resultStatistic) {
        this.resultStatistic = resultStatistic;
    }

    public ArrayList<Goods> getGoods() {
        return goods;
    }

    public void setGoods(ArrayList<Goods> goods) {
        this.goods = goods;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    private String goodsName;
    private String sku;

    public String getBevityCode() {
        return bevityCode;
    }

    public void setBevityCode(String bevityCode) {
        this.bevityCode = bevityCode;
    }

    private String bevityCode;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSubjectSound() {
        return subjectSound;
    }

    public void setSubjectSound(String subjectSound) {
        this.subjectSound = subjectSound;
    }

    public String getSubjectOther() {
        return subjectOther;
    }

    public void setSubjectOther(String subjectOther) {
        this.subjectOther = subjectOther;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }

    public String getItBPay() {
        return itBPay;
    }

    public void setItBPay(String itBPay) {
        this.itBPay = itBPay;
    }

    public String getDynamicIdType() {
        return dynamicIdType;
    }

    public void setDynamicIdType(String dynamicIdType) {
        this.dynamicIdType = dynamicIdType;
    }


    public String getDynamicId() {
        return dynamicId;
    }

    public void setDynamicId(String dynamicId) {
        this.dynamicId = dynamicId;
    }

    public String getQrId() {
        return qRId;
    }

    public void setQrId(String qrid) {
        this.qRId = qrid;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public String getDeliverResult() {
        return deliverResult;
    }

    public void setDeliverResult(String deliverResult) {
        this.deliverResult = deliverResult;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getMoney_QuickPass() {
        return money_QuickPass;
    }

    public void setMoney_QuickPass(String money_QuickPass) {
        this.money_QuickPass = money_QuickPass;
    }

    public String getQuickpass_tradeNo() {
        return quickpass_tradeNo;
    }

    public void setQuickpass_tradeNo(String quickpass_tradeNo) {
        this.quickpass_tradeNo = quickpass_tradeNo;
    }

    public byte[] getTradeData() {
        return tradeData;
    }

    public void setTradeData(byte[] tradeData) {
        this.tradeData = tradeData;
    }

    public String getBalanceOfAccount() {
        return balanceOfAccount;
    }

    public void setBalanceOfAccount(String balanceOfAccount) {
        this.balanceOfAccount = balanceOfAccount;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getAndroidpn_Status() {
        return androidpn_status;
    }

    public void setAndroidpn_Status(String androidpn_status) {
        this.androidpn_status = androidpn_status;
    }

    public String getactivitiesMode() {
        return activitiesMode;
    }

    public void setactivitiesMode(String activitiesMode) {
        this.activitiesMode = activitiesMode;
    }

    public String getactivitiesCode() {
        return activitiesCode;
    }

    public void setactivitiesCode(String activitiesCode) {
        this.activitiesCode = activitiesCode;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String activitiesCode) {
        this.goodsId = activitiesCode;
    }

    public String getCabinetNoStr() {
        return cabinetNoStr;
    }

    public void setCabinetNoStr(String cabinetNoStr) {
        this.cabinetNoStr = cabinetNoStr;
    }

    public String getCartNoStr() {
        return cartNoStr;
    }

    public void setCartNoStr(String cartNoStr) {
        this.cartNoStr = cartNoStr;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.outTradeNo);
        dest.writeString(this.productCode);
        dest.writeString(this.subjectSound);
        dest.writeString(this.subjectOther);
        dest.writeString(this.totalFee);
        dest.writeString(this.itBPay);
        dest.writeString(this.dynamicIdType);
        dest.writeString(this.qRId);
        dest.writeString(this.timer);
        dest.writeString(this.deliverResult);
        dest.writeString(this.payMode);
        dest.writeString(this.money_QuickPass);
        dest.writeString(this.quickpass_tradeNo);
        dest.writeByteArray(this.tradeData);
        dest.writeString(this.dynamicId);
        dest.writeString(this.balanceOfAccount);
        dest.writeString(this.money);
        dest.writeString(this.androidpn_status);
        dest.writeString(this.activitiesMode);
        dest.writeString(this.activitiesCode);
        dest.writeString(this.goodsId);
        dest.writeString(this.cabinetNoStr);
        dest.writeString(this.cartNoStr);
        dest.writeString(this.terminalNum);
        dest.writeString(this.userId);
        dest.writeString(this.sequence);
        dest.writeInt(this.tax);
        dest.writeInt(this.totalCount);
        dest.writeString(this.totalMoneyTaxIn);
        dest.writeString(this.totalMoneyTaxEx);
        dest.writeString(this.backscancode);
        dest.writeString(this.payType);
        dest.writeString(this.payName);
        dest.writeString(this.appType);
        dest.writeInt(this.resultStatistic);
        dest.writeTypedList(this.goods);
        dest.writeString(this.sign);
        dest.writeInt(this.boxSize);
        dest.writeString(this.goodsName);
        dest.writeString(this.sku);
        dest.writeString(this.bevityCode);
    }

    public PaymentBean() {
    }

    protected PaymentBean(Parcel in) {
        this.sn = in.readString();
        this.outTradeNo = in.readString();
        this.productCode = in.readString();
        this.subjectSound = in.readString();
        this.subjectOther = in.readString();
        this.totalFee = in.readString();
        this.itBPay = in.readString();
        this.dynamicIdType = in.readString();
        this.qRId = in.readString();
        this.timer = in.readString();
        this.deliverResult = in.readString();
        this.payMode = in.readString();
        this.money_QuickPass = in.readString();
        this.quickpass_tradeNo = in.readString();
        this.tradeData = in.createByteArray();
        this.dynamicId = in.readString();
        this.balanceOfAccount = in.readString();
        this.money = in.readString();
        this.androidpn_status = in.readString();
        this.activitiesMode = in.readString();
        this.activitiesCode = in.readString();
        this.goodsId = in.readString();
        this.cabinetNoStr = in.readString();
        this.cartNoStr = in.readString();
        this.terminalNum = in.readString();
        this.userId = in.readString();
        this.sequence = in.readString();
        this.tax = in.readInt();
        this.totalCount = in.readInt();
        this.totalMoneyTaxIn = in.readString();
        this.totalMoneyTaxEx = in.readString();
        this.backscancode = in.readString();
        this.payType = in.readString();
        this.payName = in.readString();
        this.appType = in.readString();
        this.resultStatistic = in.readInt();
        this.goods = in.createTypedArrayList(Goods.CREATOR);
        this.sign = in.readString();
        this.boxSize = in.readInt();
        this.goodsName = in.readString();
        this.sku = in.readString();
        this.bevityCode = in.readString();
    }

    public static final Creator<PaymentBean> CREATOR = new Creator<PaymentBean>() {
        @Override
        public PaymentBean createFromParcel(Parcel source) {
            return new PaymentBean(source);
        }

        @Override
        public PaymentBean[] newArray(int size) {
            return new PaymentBean[size];
        }
    };
}
