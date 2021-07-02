package com.vmgateway.ssl;

public class SllConst {
    /**サーバー*/
    public static String URL_TOKEN = "https://dev.vmgateway.com/token";//トークン取得API
    public static String URL_ITEMS = "https://dev.vmgateway.com/api/data/items";//商品情報取得API
    public static String URL_LANES = "https://dev.vmgateway.com/api/data/lanes";//レーン情報取得API
    public static String URL_QR = "https://dev.vmgateway.com/api/trade/qr";//商品情報更新API
    public static String URL_NAYAX = "https://dev.vmgateway.com/api/trade/nayax";//商品情報更新API
    public static String URL_TRADE = "https://dev.vmgateway.com/api/trade/item";//商品情報更新API
    /*
    public static String URL_TOKEN = "https://dev-vma.s-lab.app/token";//トークン取得API
    public static String URL_ITEMS = "https://dev-vma.s-lab.app/api/data/items";//商品情報取得API
    public static String URL_LANES = "https://dev-vma.s-lab.app/api/data/lanes";//レーン情報取得API
     */
//    String u = "http://52.197.207.196/SaitouAndroidTest/token";//テストサーバー
//    String uItems = "http://52.197.207.196/SaitouAndroidTest/api/v2/data/Items/5";//テストサーバー
//    String uLanes = "http://52.197.207.196/SaitouAndroidTest/api/v2/data/Lanes/5";//テストサーバー
    /**設定ファイル　path*/
    public static final String SETTING_PATH = "/DCIM/PicAD/VenderSetting.json";

    /**レーン設定*/
    public static int LANE_MAX = 8;//キャビネットの最大レーン数

    /**プロセス間通信 what*/
    public static int ITEM_DATA = 0x01;
    public static int QR_PAY = 0x02;
    public static int NAYAX_PAY = 0x03;
    public static int NAYAX_RESULT = 0x04;
    public static int VENDING = 0x05;
    public static int VENDING_END = 0x06;
    public static int NAYAX_CANCEL = 0x0a;
    public static int  VENDING_TEST = 0x00;

    //test用で0x20使用

}

