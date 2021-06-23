package com.vmgateway.ssl;

import android.app.Application;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;


public class BaseApplication extends Application {

    private SharedPreferences dataStore;
    private String appToken;
    private JSONArray jsonItems;//サーバーデータ
    private JSONArray jsonLanes;//サーバーデータ
//    private JSONArray jsonProductData;//サーバーデータをDispPriority順に並び替えた
    private JSONObject serviceOrders;//注文 カートの中 UIのorderをservice用に作り変えた

    private JSONArray jsonResult;//払出結果

    //設定
    private String vmId = "";
    private String password = "";
    private String vmPassword = "";

    //===============================================================================
    // Protected Method
    //===============================================================================
    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;

    }
    //===============================================================================
    // Static Private Property
    //===============================================================================
    private static BaseApplication _instance = null;

    //===============================================================================
    // Static Public Method
    //===============================================================================
    public static BaseApplication getInstance() {
        return _instance;
    }

    //グローバル変数
    //SharedPreferences
    public void setDataStore(SharedPreferences j){
        dataStore = j;
    }
    public SharedPreferences getDataStore(){
        return dataStore;
    }

    //JSONArray JsonItems
    public void setJsonItems(JSONArray j){
        jsonItems = j;
    }
    public JSONArray getJsonItems(){
        return jsonItems;
    }
    //JSONArray Jsonanes
    public void setJsonLanes(JSONArray l){
        jsonLanes = l;
    }
    public JSONArray getJsonLanes(){
        return jsonLanes;
    }

    /*
    //JSONArray Jsonanes
    public void setJsonProductData(JSONArray p){
        jsonProductData = p;
    }

    public JSONArray getJsonProductData(){
        return jsonProductData;
    }
*/
    public void setToken(String a){
        appToken = a;
    }
    public String getToken(){
        return appToken;
    }

    //JSONArray orders
    public void setserviceOrders(JSONObject o){
        serviceOrders = o;
    }
    public JSONObject getserviceOrders(){
        return serviceOrders;
    }

    //setting
    public void setVmId(String  vmid){
        vmId = vmid;
    }
    public String getVmId(){
        return vmId;
    }

    public void setPassword(String  pass){
        password = pass;
    }
    public String getPassword(){
        return password;
    }

    public void setVmPassword(String  vmpass){
        vmPassword = vmpass;
    }
    public String getVmPassword(){
        return vmPassword;
    }

    public void setServiceResult(JSONArray result){
        jsonResult = result;
    }
    public JSONArray getServiceResult(){
        return jsonResult;
    }
}
