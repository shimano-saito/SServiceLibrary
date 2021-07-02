package com.vmgateway.ssl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.serialportsdk.SerialPortCallback;
import com.serialportsdk.SerialPortSDK;
import com.smn.bean.BCLProtocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class GCCService extends Service implements SerialPortCallback{
    private static final String TAG = "Log:GCCService";

    //払出結果　サーバーAPIに数値合わせている
    private static final int DEFAULT = 6;//未処理　0
    private static final int SUCCESS = 3;//出荷成功 1
    private static final int UNFINISHED = 5;//出荷失敗 5
    private static final int VENDING_REQ_FAILURE = 10;//出荷REQ受付失敗 4

    //LaneActivitiからローカルで呼び出す際に必要
    private laneTestBinder mBinder;
    private SllConst s;

    private BaseApplication app;
    private PaymentRequest payTask;

    private Messenger mServiceMessenger;
    private Messenger mSelfMessenger;

    private JSONObject orders;  //オーダー GCCService内で使用するオーダー、払出したら減らす
//    private JSONObject serviceOrders;  //オーダー BaseApplicationで持ってるグローバル

//    private JSONArray jsonItems;//サーバから取得したデータ laneId有り
//    private JSONArray deliverItems;//ordersにlaneId、在庫を付け足した

    private JSONArray resultArray;//Activityに返すデータ
    private JSONObject resultJson;//Activityに返す商品毎データ

    private JSONObject jsonObject;//商品情報
    private int orderQty; //オーダー数
    private int restQty;  //未払出数
    private int laneNow;//動作中のレーン
    private int amountNow;
    private int gccResult;//出荷結果

    private int sendCount = 0;

    //タイマーも動かしてみた
//    Timer timer = new Timer();

   public SerialPortSDK spsdk;

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
        private SerialPortSDK spsdk;
        private Context context;
        //private Messenger mSelfMessenger;

        RequestHandler(Looper looper, Context context) {
            super(looper);
            refContext = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handle request=" + msg);
            int laneId = 0;
            int amount = 0;
            //whatは4
            GCCService myService = (GCCService)refContext.get();
            myService.app = (BaseApplication) myService.getApplication();
            myService.mSelfMessenger = msg.replyTo;
            //払出要求
            Log.d(TAG, "msg.what  " + msg.what);
            //orderはappから取得する。UIからは払出要求 what 5 だけ来る
             try {
                 if(msg.what == myService.s.VENDING ){//5
                     //グローバル変数からローカル変数にコピー
                     myService.orders = new JSONObject(myService.app.getserviceOrders().toString());
                     Log.d(TAG, "orders  " + myService.orders.toString());
                     Log.d(TAG, "serviceOrders  " + myService.app.getserviceOrders().toString());

                     laneId = myService.orders.getJSONArray("orders").getJSONObject(0).getInt("laneId");

                     /** 払出 */
                     //社内に6段目しかないので一桁目のlane排出
                     laneId = laneId % 100;//TEST
                     laneId = 600 + laneId;//TEST
                     //社内のレーンは600番台なので+500する、本番時には元に戻す
                     myService.spsdk.deliver(laneId);//払出指示、結果はserialMachineReset()に返ってくる
                     Log.d(TAG, "laneId  " + laneId);

                 }else if(msg.what == myService.s.VENDING_TEST){//0
                     //モーター回すだけのインターフェースをここに設ける
                     laneId = msg.getData().getInt("laneId");
                     Log.d(TAG, "laneId: " + laneId);

                     myService.spsdk.deliver(laneId);
                 }else{
                     Log.e(TAG, "handleMessage 予期せぬerr  ");

                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
    }

    public GCCService() {
        Log.d(TAG, "GCCService()");

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        resultArray = new JSONArray();
    }

    /** プロセス間通信と同プロジェクトからbindする場合、戻り値が異なる (通常払出とメンテモードの払出の違い)*/
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");

        s = new SllConst();
        //SerialPortSDK
        spsdk = new SerialPortSDK(this, null);
        spsdk.setCallback(this);

        //メンテのLaneActivityから呼ばれた場合
        if(intent.getStringExtra("LaneActivity") != null){
            //LaneActivityから呼ばれた時は"lanetest",通常はnull。nullじゃなければここに入る
            Log.d(TAG, "LaneActivity");
            mBinder = new laneTestBinder();
            return mBinder;//メンテモード
        }

     // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        mServiceMessenger = new Messenger(new RequestHandler(getApplicationContext().getMainLooper(),this));

        //どっちから呼び出されるかで分けられないか？どうやって共存させる？
        return mServiceMessenger.getBinder();//プロセス間通信
//        return mBinder;//LaneActivityから呼び出すときはこっち
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.d(TAG, "onUnbind()");

        //SerialPort threadを閉じる
        //ここで閉じた方が安定しそう
        spsdk.serialPortClose();
        Log.d(TAG, "serialPortClose()");
        try {
            spsdk.threadStop();
            Log.d(TAG, "threadStop()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    //***********SerialPortCallback**************
    @Override
    public void serialInitResult(boolean success) {
        Log.d(TAG, "serialInitResult() serial Open OK");

    }

    @Override
    public void serialgoodSelected() {
        Log.d(TAG, "serialgoodSelected()");

    }

    @Override
    public void serialgoodPaying(BCLProtocol bclProtocol) {
        Log.d(TAG, "serialgoodPaying()");

    }

    @Override
    public void serialMachineReset(int res) {
        //1商品ごと結果をactivityに返す
        //払出 res  出荷済:3 ,出荷未了:5 ,出荷取り消し:6
        Boolean finishFlag = false;
        gccResult = res;
        String strSendServerData = null;
        int failures = 0;
        Log.d(TAG, "serialMachineReset()  " + gccResult);

        if(gccResult == SUCCESS){
            /**出荷成功*/
            Log.d(TAG, "払出成功");
            //DOOR,LED制御は実装していない。resの引数6以降がコマンドの結果　1が払い出し成功
            //orderにresultを付け加える
            try{
                /** サーバーに送信するDATA*/
                JSONObject sendServerData = new JSONObject();//払出で使用する注文データ
                sendServerData
                        .put("tradeItemId",orders.getJSONArray("orders").getJSONObject(0).getInt("tradeItemId"))
                        .put("itemId",orders.getJSONArray("orders").getJSONObject(0).getInt("itemId"))
                        .put("price",orders.getJSONArray("orders").getJSONObject(0).getInt("price"))
                        .put("laneId",orders.getJSONArray("orders").getJSONObject(0).getInt("laneId"))
                        .put("status",gccResult)
                        .put("from",orders.getJSONArray("orders").getJSONObject(0).getInt("amount"))
                        .put("to",orders.getJSONArray("orders").getJSONObject(0).getInt("spareAmount") - 1);
                Log.d(TAG, "serverDataSend  " + sendServerData.toString());
                strSendServerData = sendServerData.toString();
            }catch (Exception e) {
                e.printStackTrace();
            }
            //サーバーに送信
            SllConst s = new SllConst();
            payTask = new PaymentRequest(app, strSendServerData,s.URL_TRADE);//
            payTask.execute();
            payTask.setOnCallBack(new PaymentRequest.CallBackTask() {
                @Override
                public void CallBack(String serverResult) {
                    super.CallBack(serverResult);
                    Log.d(TAG, "serverResult " + serverResult);

                    //払出結果とは別にサーバー通信 の結果を送る？


                    Bundle bundle;
                    int laneId = 0;
                    try{
                        /** UIに送信*/
                        JSONObject sendUiData = new JSONObject();//払出で使用する注文データ
                        sendUiData
//                                .put("name",orders.getJSONArray("orders").getJSONObject(0).getString("name"))
                                .put("itemId",orders.getJSONArray("orders").getJSONObject(0).getInt("itemId"))
                                .put("laneId",orders.getJSONArray("orders").getJSONObject(0).getInt("laneId"))
                                .put("result",gccResult)
                                .put("finish",finishFlag);
                        Log.d(TAG, "sendUiData  " + sendUiData.toString());
                        //UIに送信
                        Message sendMsg = Message.obtain(null, s.VENDING );//5 resの部分は内容を示す値がいいかも？払出結果、コマンド結果、終了など
                        bundle = new Bundle();
                        bundle.putString("result", String.valueOf(sendUiData));
                        sendMsg.setData(bundle);
                        mSelfMessenger.send(sendMsg);

                        //１商品払出、送信したので払出したordersの商品を削除
                        orders.getJSONArray("orders").remove(0);
                        Log.d(TAG, "orders after " + orders.toString());

                        /** 払出　成功*/
                        //注文が残っていたら払出
                        if(0 < orders.getJSONArray("orders").length()){
                            laneId = orders.getJSONArray("orders").getJSONObject(0).getInt("laneId");

                            /** 払出 */
                            //社内に6段目しかないので一桁目のlane排出
                            laneId = laneId % 100;//TEST
                            laneId = 600 + laneId;//TEST
                            spsdk.deliver(laneId);//払出指示、結果はserialMachineReset()に返ってくる
                            Log.d(TAG, "laneId  " + laneId);

                        }else {
                            //注文がすべて払い出されたら払出完了をUIに送る
                            /** 終了 */
                            Message sendFinishMsg = Message.obtain(null, s.VENDING_END);//6
                            mSelfMessenger.send(sendFinishMsg);
                            Log.d(TAG, "Finish  " + sendFinishMsg.what);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }else{
            /**払出失敗*/
            //注文が残っていたら残りは未払出としてUIに送信
            try {
                int len = orders.getJSONArray("orders").length();//送信完了したら　1商品削除する
                sendCount = orders.getJSONArray("orders").length();
                Log.d(TAG, "払出失敗　商品残  " + len);

                JSONArray resultJdonArray = new JSONArray();

                for(int i = 0; i < len ; i++) {
                    Bundle bundle;
                    /** サーバーに送信する DATA*/
                    /**PaymentRequestが終了してしまう前にfor文が周ってしまう*/
                    JSONObject sendServerData = new JSONObject();//払出で使用する注文データ
                    sendServerData
                            .put("tradeItemId", orders.getJSONArray("orders").getJSONObject(0).getInt("tradeItemId"))
                            .put("itemId", orders.getJSONArray("orders").getJSONObject(0).getInt("itemId"))
                            .put("price", orders.getJSONArray("orders").getJSONObject(0).getInt("price"))
                            .put("laneId", orders.getJSONArray("orders").getJSONObject(0).getInt("laneId"))
                            .put("status", gccResult)
                            .put("from", orders.getJSONArray("orders").getJSONObject(0).getInt("amount"))
                            .put("to", orders.getJSONArray("orders").getJSONObject(0).getInt("spareAmount"));//払い出せていないのでマイナスにしない
                    Log.d(TAG, "serverDataSend  " + sendServerData.toString());
                    String strSendFailServerData = sendServerData.toString();

                    /** UIに送信*/
                    JSONObject sendUiData = new JSONObject();//払出で使用する注文データ
                    sendUiData
//                            .put("name",orders.getJSONArray("orders").getJSONObject(0).getString("name"))
                            .put("itemId",orders.getJSONArray("orders").getJSONObject(0).getInt("itemId"))
                            .put("laneId",orders.getJSONArray("orders").getJSONObject(0).getInt("laneId"))
                            .put("status",gccResult)
                            .put("finish",finishFlag);
                    Log.d(TAG, "sendUiData  " + sendUiData.toString());
                    //UIに送信
                    Message sendMsg = Message.obtain(null, s.VENDING );//5 resの部分は内容を示す値がいいかも？払出結果、コマンド結果、終了など
                    bundle = new Bundle();
                    bundle.putString("result", String.valueOf(sendUiData));
                    sendMsg.setData(bundle);
                    try {
                        mSelfMessenger.send(sendMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

//                    gccResult = DEFAULT;//

                    /** service appにerr保存*/
                    resultJdonArray.put(new JSONObject()
                            .put("tradeItemId", orders.getJSONArray("orders").getJSONObject(0).getInt("tradeItemId"))
//                            .put("name",orders.getJSONArray("orders").getJSONObject(0).getString("name"))
                            .put("itemId", orders.getJSONArray("orders").getJSONObject(0).getInt("itemId"))
                            .put("price", orders.getJSONArray("orders").getJSONObject(0).getInt("price"))
                            .put("laneId", orders.getJSONArray("orders").getJSONObject(0).getInt("laneId"))
                            .put("status", gccResult)
                            .put("from", orders.getJSONArray("orders").getJSONObject(0).getInt("amount"))
                            .put("to", orders.getJSONArray("orders").getJSONObject(0).getInt("spareAmount")));

                    app.setServiceResult(resultJdonArray);//結果保存

                    //１商品送信したので払出したordersの商品を削除
                    orders.getJSONArray("orders").remove(0);
                    Log.d(TAG, "orders after " + orders.toString());

                    /** サーバーに送信*/

                    payTask = new PaymentRequest(app, strSendFailServerData, s.URL_TRADE);//
                    payTask.execute();

                    payTask.setOnCallBack(new PaymentRequest.CallBackTask() {
                        @Override
                        public void CallBack(String serverResult) {
                            super.CallBack(serverResult);
                            Log.d(TAG, "serverResult " + serverResult + " ,sendCount " + sendCount);
                            sendCount--;
                            //払出結果とは別にサーバー通信 の結果を送る？

                            try{
                                //１商品送信したので払出したordersの商品を削除
//                                orders.getJSONArray("orders").remove(0);
//                                Log.d(TAG, "orders after " + orders.toString());

                                //注文 ordersがなくなったら終了
//                                if(0 >= orders.getJSONArray("orders").length()) {
                                if(0 >= sendCount) {
                                    //注文がすべて払い出されたら払出完了をUIに送る
                                    /**送信完了*/
                                    Message sendFinishMsg = Message.obtain(null, s.VENDING_END);//6
                                    try {
                                        mSelfMessenger.send(sendFinishMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "Finish  " + sendFinishMsg.what);
                                }

                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serialOther(Message msg) {
        Log.d(TAG, "serialOther()");

    }

    //LaneActivityから呼び出されるclass
    public class laneTestBinder extends Binder {
        GCCService getService(){
            return GCCService.this;
        }
    }
    public String deliver(int lane){
        String re = "";
        spsdk.deliver(lane);//払出指示、結果はserialMachineReset()に返ってくる
        Log.d(TAG, "laneId  " + lane);
        return re;
    }
}