package com.vmgateway.ssl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class QRPaymentService extends Service {
    private static final String TAG = "Log:QRPaymentService";
    private BaseApplication app;
    private Messenger mServiceMessenger;
    private Messenger mSelfMessenger;

    private JSONArray myServiceOrders;//QRPaymentService内で使用する tradeId,tradeItemId除いたorder部分

    private AsyncHttpRequest mTask;
    private PaymentRequest qrTask;

    private SllConst s;
    private SllLibrary library;

    //テスト的に作成、WeakReferenceからメンバにアクセスすれば、必要ないはず
    public void setmSelfMessenger(Messenger mSelfMessenger) {
        this.mSelfMessenger = mSelfMessenger;
    }
    //テストとして作成。同上
    public Messenger getmSelfMessenger (){
        return this.mSelfMessenger;
    }


    static class RequestHandler extends Handler {
        private final WeakReference<Context> refContext;
        //private Messenger mSelfMessenger;

        RequestHandler(Looper looper, Context context) {
            super(looper);
            refContext = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handle request=" + msg);

            QRPaymentService myService = (QRPaymentService)refContext.get();
            myService.mSelfMessenger=msg.replyTo;
            myService.app = (BaseApplication)myService.getApplication();

            //Thread.sleep(1000);
//            JSONArray jsonArray = new JSONArray(msg.getData().getString("json"));
            Log.d(TAG,"what  " + msg.what);
            //what=2:注文確認要求 QR
            if(msg.what == myService.s.QR_PAY){//2
                //QRcode受取り
                String qrCode = msg.getData().getString("qrCode");
                Log.d(TAG, "msg= " + qrCode);

                /****サーバーにitemInfos,laneInfos要求****/
                myService.mTask = new AsyncHttpRequest(myService.app);//AsyncHttpRequestの引数をclassにしないと
                myService.mTask.execute();
                myService.mTask.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
                    @Override
                    public void CallBack(String result) {
                        super.CallBack(result);
                        //データ取得後
                        Log.d(TAG, "AsyncHttpRequest Finish  OK");

                        JSONObject jsonObjectQr;
                        JSONObject jsonObjectSendData = null;//サーバー送信データ

                        try {
                            //UI側から来たデータ
                            jsonObjectQr = new JSONObject(qrCode);//JSONObject QRコード+ orderのデータがUIから来る
                            JSONArray jsonArrayOrder = jsonObjectQr.getJSONArray("orders");//UIから来たデータのorder部分
                            Log.d(TAG, "jsonArrayOrder  " + jsonArrayOrder.toString());

                            //UI側 支払金額合計計算
                            int uiTotalPay = 0;

                            //サーバーデータ金額とUI金額チェック
                            //UI order
                            int itemsTotalPay = 0;
                            JSONArray jsonArrayItems = myService.app.getJsonItems();//サーバーデータ
                            for(int i = 0 ; i < jsonArrayOrder.length(); i++){
                                //UI サーバーデータ　itemId一致させる
                                for(int y = 0 ; y < jsonArrayItems.length(); y++){
//                            Log.d(TAG,"itemId 比較   " + jsonArrayOrder.getJSONObject(i).getInt("itemId") + "  " + jsonArrayItems.getJSONObject(y).getInt("itemId"));
                                    if(jsonArrayOrder.getJSONObject(i).getInt("itemId") == jsonArrayItems.getJSONObject(y).getInt("itemId")){
                                        //同じ商品IDの金額を確認
                                        Log.d(TAG,"price 比較   " + jsonArrayOrder.getJSONObject(i).getInt("price") + "  " + jsonArrayItems.getJSONObject(y).getInt("price"));
                                        if(jsonArrayOrder.getJSONObject(i).getInt("price") != jsonArrayItems.getJSONObject(y).getInt("price")){
                                            //単価金額が違う!
                                            Log.d(TAG, "!!!price NG!!!  " + "UI 単価  " + jsonArrayOrder.getJSONObject(i).getInt("price") + ", server 単価  " + jsonArrayItems.getJSONObject(y).getInt("price"));
                                        }
                                        int q = jsonArrayOrder.getJSONObject(i).getInt("quantity");
                                        itemsTotalPay = jsonArrayItems.getJSONObject(y).getInt("price") * jsonArrayOrder.getJSONObject(i).getInt("quantity") + itemsTotalPay;//order数量分かける
                                        uiTotalPay = jsonArrayOrder.getJSONObject(i).getInt("price") * jsonArrayOrder.getJSONObject(i).getInt("quantity") + uiTotalPay;//order数量分かける
                                    }
                                }
                            }
                            Log.d(TAG,"itemsTotalPay  " + itemsTotalPay +" ,uiTotalPay " + uiTotalPay);//UI側　合計金額

                            if(itemsTotalPay != uiTotalPay) {
                                Log.e(TAG,"TotalPay  NG " );
                                /**TotalPay NG*/

                            }

                            /**UIから来るデータをserviceで使用する形にする   {name,itemId,price,laneId,amount}    */
                            //UIから来るデータをserviceで使用する形にする関数 払出で使用するデータ作成
                            jsonObjectSendData = new JSONObject();//サーバー送信データ
                            myService.myServiceOrders = new JSONArray();
                            //orderにLaneItems:[laneId,Amount]をくっつける
                            myService.myServiceOrders = myService.library.addLaneId(jsonArrayOrder,jsonArrayItems);

                            //totalPay qrCode 追加   //JSONObject jsonObject = new JSONObject(json);
                            Log.d(TAG, "serviceOrders : " + myService.myServiceOrders.toString());
                            jsonObjectSendData
                                    .put("totalPay",uiTotalPay)
                                    .put("qrCode",jsonObjectQr.getString("qrCode"))
                                    .put("orders",myService.myServiceOrders);

                            Log.d(TAG, "jsonObjectSendData : " + jsonObjectSendData.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /**サーバーにQRコード送信    */
                        myService.qrTask = new PaymentRequest(myService.app, jsonObjectSendData.toString(),myService.s.URL_QR);//
                        myService.qrTask.execute();
                        myService.qrTask.setOnCallBack(new PaymentRequest.CallBackTask() {
                            @Override
                            public void CallBack(String qrResult) {
                                super.CallBack(qrResult);
                                //データ取得後
                                //サーバー空の結果はresultに文字列で返ってくる
                                Log.d(TAG, "QRPayRequest Finish  OK");
                                Log.d(TAG, "qrResult  " + qrResult);//受信データ String

                                /** 払出で使用するserviceOrder作成 */
                                try {
                                    JSONObject jsonResult = new JSONObject(qrResult);//決済結果
                                    JSONObject serviceOrders = new JSONObject();//払出で使用する注文データ

                                    //tradeId,tradeItemIdを追加する
                                    for(int i = 0; i < myService.myServiceOrders.length(); i++){
                                        myService.myServiceOrders.getJSONObject(i).put("tradeItemId",jsonResult
                                                .getJSONArray("orders")
                                                .getJSONObject(i)
                                                .getInt("tradeItemId"));
                                    }
//                                    serviceOrders.put("tradeId",jsonResult.getInt("tradeId"))
                                    serviceOrders.put("orders",myService.myServiceOrders);

                                    myService.app.setserviceOrders(serviceOrders);//order部分をappに保存
                                    Log.d(TAG, "serviceOrders : " + serviceOrders.toString());
/*
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
*/
                                /** 決済結果をUIに送る */
//                                try {
                                    JSONObject sendUiData = new JSONObject().put("result",jsonResult.getBoolean("result"));
                                    Message msg2 = Message.obtain(null, myService.s.QR_PAY);//2
                                    Bundle bundle = new Bundle();
                                    bundle.putString("result",sendUiData.toString());
                                    msg2.setData(bundle);
                                    myService.mSelfMessenger.send(msg2);
                                    Log.d(TAG,"mSelfMessenger.send.msg2  " + msg2.getData().getString("result"));

                                } catch (RemoteException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }else{
                Log.e(TAG,"what NG QR_PAY(2)が来るべき " + msg.what);

            }
        }
    }

    public QRPaymentService() {
        Log.d(TAG, "QRPaymentService()");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");


        //Timer timer = new Timer();
        /*
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Timer()");
                Messenger msr = getmSelfMessenger();
                if(msr != null){
                    try{
                        msr.send(Message.obtain(null,2));
                    } catch(RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        },0, 10000);

         */
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        mServiceMessenger = new Messenger(new QRPaymentService.RequestHandler(getApplicationContext().getMainLooper(),this));

        s = new SllConst();
        library = new SllLibrary();

        return mServiceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.d(TAG, "onUnbind()");
        /*
        if(timer != null) {
            timer.cancel();
        }

         */
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        super.finalize();
    }

    //orderにLaneItems:[laneId,Amount]をくっつける resultもくっつける
    public JSONArray addLaneId2(JSONArray uiOrder,JSONArray item){
        JSONArray newArrayOrders;
        JSONObject newObjectOrder;

        /** LaneItems:[laneId,Amount]追加 (JSONArray)  */
        try{
            for(int i= 0; i < uiOrder.length() ; i++){
                for(int x = 0; x < item.length() ; x++){

//                     Log.d(TAG, "order : " + order.getJSONObject(i).getInt("itemId") + "  item : " + item.getJSONObject(x).getInt("ItemId"));
                    if(uiOrder.getJSONObject(i).getInt("itemId") == item.getJSONObject(x).getInt("itemId")){

                        //laneIdは複数ある場合がある
                        Log.d(TAG, "length : " + item.getJSONObject(x).getJSONArray("laneItems").length());
                        JSONArray laneItems = new JSONArray();
                        for(int y = 0; y < item.getJSONObject(x).getJSONArray("laneItems").length() ; y++){
                            int lane = item.getJSONObject(x).getJSONArray("laneItems").getJSONObject(y).getInt("laneId");//レーン
                            int laneAmount = item.getJSONObject(x).getJSONArray("laneItems").getJSONObject(y).getInt("amount");//レーン毎の在庫

                            laneItems.put(new JSONObject().put("laneId",lane).put("amount",laneAmount).put("spareAmount",laneAmount));//計算用：spareAmount
                            uiOrder.getJSONObject(i).put("laneItems",laneItems);
//                             Log.d(TAG, "order : " + order.toString());
                        }
                    }
                }
                uiOrder.getJSONObject(i).put("salesItemId",0);//初期値
                uiOrder.getJSONObject(i).put("result",false);//初期値
            }
            Log.d(TAG, "uiOrder : " + uiOrder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**  払い出すlaneIdを決める　在庫が多いレーンから払出 */
        int largeAmount = 0;
        newArrayOrders = new JSONArray();
        try{
            for(int i= 0; i < uiOrder.length() ; i++) {
                //一番多い在庫数を確認 largeAmount
                for (int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++) {
                    if(largeAmount <= uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount")){
                        largeAmount = uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount");
                    }
                }
                //在庫数が多いlaneIdを抜き出し、serviceで使用するorderを生成
                //同一商品がある場合はspareAmountを引いて確認　　int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++
                for(int y = 0 ; y < uiOrder.getJSONObject(i).getInt("quantity") ; y++){
                    for(int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++){
                        if(largeAmount == uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount")){
                        //在庫多数があった
                            newArrayOrders.put(new JSONObject()
                                    .put("name",uiOrder.getJSONObject(i).getString("name"))
                                    .put("itemId",uiOrder.getJSONObject(i).getInt("itemId"))
                                    .put("price",uiOrder.getJSONObject(i).getInt("price"))
                                    .put("laneId",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("laneId"))//払出laneId確定
                                    .put("amount",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("amount"))//amount階層あげる
                                    .put("spareAmount",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount"))
                            );
                            uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).put("spareAmount",largeAmount - 1);//払出予定なので予備在庫１つ減らす
                            //一番多い在庫数を確認 largeAmount が変化する場合があるので再度確認
                            largeAmount = 0 ;
                            for (int z = 0; z < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); z++) {
                                if(largeAmount <= uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(z).getInt("spareAmount")){
                                    largeAmount = uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(z).getInt("spareAmount");
                                }
                            }
                            break;
                        }
                    }
                }
            }
            Log.d(TAG, "uiOrder : " + uiOrder.toString());
            Log.d(TAG, "newArrayOrders : " + newArrayOrders.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newArrayOrders;
    }

    public int deliverLane(JSONArray deliverItems){
        int lane = 0;


        return lane;
    }

}
