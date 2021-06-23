package com.smn.bean;

public class BCLProtocol {

    private String money;          //支付金额
    private String cabinetNo;      //柜号
    private String cartNo;         //货道号
    private String qpMoney;        //银联用支付金额
    private String payMode;        //支付方式
    private byte[] tradeNo;        //交易序号

    private int coffee;     // 咖啡量
    private int sugar;      // 糖量
    private int milk;       // 牛奶量

    //bar code
    private String mSKU;
    private String mBevityCode;
    private String mGoodsName;
    private String mSpec;
    private String mPicPath;
    private int    mCompleteNum;
    private int    mOrder;
    private String mMD5;

    private boolean isEmpty;
    private boolean isBreakDown;// 坏道

    public BCLProtocol(){}

    public BCLProtocol(String cabinetNo,String cartNo,String sku,String bevityCode,String goodsName,
                       String spec,String picPath,String money,int completeNum,int order,String md5){
        this.cabinetNo = cabinetNo;
        this.cartNo = cartNo;
        this.mSKU = sku;
        this.mBevityCode = bevityCode;
        this.mGoodsName = goodsName;
        this.mSpec = spec;
        this.mPicPath = picPath;
        this.money = money;
        this.mCompleteNum = completeNum;
        this.mOrder = order;
        this.mMD5 = md5;
    }

    public BCLProtocol(String cabinetNo, String cartNo, String sku, String bevityCode, String
            goodsName, String spec, String picPath, String money, int completeNum, int order,
                       String md5, boolean isEmpty) {
        this(cabinetNo, cartNo, sku, bevityCode, goodsName, spec, picPath, money, completeNum,
                order, md5);
        this.isEmpty = isEmpty;
    }

    public BCLProtocol(String cabinetNo, String cartNo, String sku, String bevityCode, String
            goodsName, String spec, String picPath, String money, int completeNum, int order,
                       String md5, boolean isEmpty, boolean isBreakDown) {
        this(cabinetNo, cartNo, sku, bevityCode, goodsName, spec, picPath, money, completeNum,
                order, md5, isEmpty);
        this.isBreakDown = isBreakDown;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getSKU() {
        return mSKU;
    }

    public void setSKU(String SKU) {
        mSKU = SKU;
    }

    public String getBevityCode() {
        return mBevityCode;
    }

    public void setBevityCode(String bevityCode) {
        mBevityCode = bevityCode;
    }

    public String getGoodsName() {
        return mGoodsName;
    }

    public void setGoodsName(String goodsName) {
        mGoodsName = goodsName;
    }

    public String getSpec() {
        return mSpec;
    }

    public void setSpec(String spec) {
        mSpec = spec;
    }

    public String getPicPath() {
        return mPicPath;
    }

    public void setPicPath(String picPath) {
        mPicPath = picPath;
    }

    public int getCompleteNum() {
        return mCompleteNum;
    }

    public void setCompleteNum(int completeNum) {
        mCompleteNum = completeNum;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public String getMD5() {
        return mMD5;
    }

    public void setMD5(String MD5) {
        mMD5 = MD5;
    }


    /**
     * @return the money
     */
    public String getMoney() {
        return money;
    }
    /**
     * @param money the money to set
     */
    public void SetMoney(String money) {
        this.money = money;
    }
    /**
     * @return the cabinetNo
     */
    public String getCabinetNo() {
        return cabinetNo;
    }
    /**
     * @param cabinetNo the cabinetNo to set
     */
    public void setCabinetNo(String cabinetNo) {
        this.cabinetNo = cabinetNo;
    }
    /**
     * @return the cartNo
     */
    public String getCartNo() {
        return cartNo;
    }
    /**
     * @param cartNo the cartNo to set
     */
    public void setCartNo(String cartNo) {
        this.cartNo = cartNo;
    }
    /**
     * @return the qpMoney
     */
    public String getQpMoney() {
        return qpMoney;
    }
    /**
     * @param qpMoney the qpMoney to set
     */
    public void setQpMoney(String qpMoney) {
        this.qpMoney = qpMoney;
    }
    /**
     * @return the payMode
     */
    public String getPayMode() {
        return payMode;
    }
    /**
     * @param payMode the payMode to set
     */
    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }


    /**
     * @return the tradeNo
     */
    public byte[] getTradeNo() {
        return tradeNo;
    }
    /**
     * @param tradeNo the tradeNo to set
     */
    public void setTradeNo(byte[] tradeNo) {
        this.tradeNo = tradeNo;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isBreakDown() {
        return isBreakDown;
    }

    public void setBreakDown(boolean breakDown) {
        isBreakDown = breakDown;
    }

    public int getCoffee() {
        return coffee;
    }

    public void setCoffee(int coffee) {
        this.coffee = coffee;
    }

    public int getSugar() {
        return sugar;
    }

    public void setSugar(int sugar) {
        this.sugar = sugar;
    }

    public int getMilk() {
        return milk;
    }

    public void setMilk(int milk) {
        this.milk = milk;
    }

}
