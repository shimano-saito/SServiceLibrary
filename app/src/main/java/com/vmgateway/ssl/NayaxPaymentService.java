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

import com.Nayax.NayaxResult;
import com.Nayax.NayaxSerialPort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import com.Nayax.NayaxSerialPortCallback;

import static java.lang.Thread.sleep;

public class NayaxPaymentService extends Service implements NayaxSerialPortCallback{
    private static final String TAG = "Log:NayaxPaymentService";
    private BaseApplication app;
    public NayaxSerialPort nayaxSrerialport;

    private Messenger mServiceMessenger;
    private Messenger mSelfMessenger;

    private JSONArray myServiceOrders;//NayaxPaymentService tradeId,tradeItemId除いたorder部分

    //Nayax read / write
//    private ReadThread readThread = null;
    private WriteThread writeThread = null;
    private int totalPay;
    private int state = 0;
    private int START_STATE = 0;
    private int COST_RES_STATE = 1;//MDB_CARD_COST_REQ送信している
    private int CARD_TOUCH_STATE = 2;
    private int VEND_RES_STATE = 3;//MDB_CARD_VEND_RES_REQ送信している
    private int INIT_STATE = 4;//INIT送信している
    private int initCount = 0;

    private AsyncHttpRequest mTask;
    private PaymentRequest nayaxTask;

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

    @Override
    public void NayaxDeviceConditionChanged(NayaxResult result) {
        if( result.getMT() == 0x91 && result.getOPE() == 0x01 ) {
            String m = "Status changed  Card Enable " + result.getCardEnable() + " Card Error " + result.getCardError() + " Card Cost " + result.getCardCost();

            Log.i( TAG, m );
        }
        else {
            String m ="Illegal parameter MT -> "+ result.getMT() +" OPE -> "+ result.getOPE();

            Log.e( TAG, m );
        }
    }

    @Override
    public void NayaxCommanExecuted(NayaxResult result) {
        int mt = result.getMT();
        int ope = result.getOPE();
        Message sendFinishMsg = null;
        Message sendResultMsg = null;
        boolean nayaxResult;

        try {
            sleep(1000);//コマンド送信間隔は1000ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "state  " + state);
        //
        switch ( mt ){
            case 0x91:
                switch ( ope ){
                    case  0x01:
                        // ハートビート
                        Log.e(TAG, "illegal ope "+ ope );
                        break;
                    case 0:
                    case 0x04:
                        // MDBデバイス情報
                        String m0 = "MDB Serial Number " + result.getmDBdeviceSerial() + "  Version " + result.getmDBdeviceVersion();
                        Log.i( TAG, m0 );
                        break;
                    case 0x06:
                        // カードリーダー情報
                        String m1 ="CardReader Type " + result.getCardType() + "  Serial Number " + result.getCardSerial()
                                + "  Model Number" + result.getCardModelNumber() +"  Version " + result.getCardVervion()
                                + "  Maker " + result.getCardMaker();
                        Log.i( TAG, m1 );
                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ ope );
                }
                break;
            case 0x92:
                // コマンド実行結果
                switch ( ope ) {
                    case 0x01:
                        // 初期化
                        nayaxSrerialport.PaymentStatus();
                        Log.d(TAG, "PaymentStatus()");
                        state = START_STATE;
                        break;
                    case 0x02:
                        // 許可/禁止
                        break;
                    case 0x0a:
                        /** MDB_CARD_COST_RPT*/
                        //カードリーダーに支払金額を送信して受付開始
                        if(result.getCardMaker().equals(0)){
                            //MDB_CARD_COST_REQ 受付失敗
                            Log.d(TAG, "MDB_CARD_COST_REQ 受付失敗");
                            sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                            Log.d(TAG, "sendFinishMsg()");

                            try {
                                mSelfMessenger.send(sendFinishMsg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }else{
                            //NAYAX受付開始　成功 カードタッチ待ち
                            /**UIに Nayax 準備OK 送信する*/
                            try {
                                JSONObject sendUiData = new JSONObject().put("result",true);
                                sendResultMsg = Message.obtain(null, s.NAYAX_PAY);//3
                                Bundle bundle = new Bundle();
                                bundle.putString("result",sendUiData.toString());
                                sendResultMsg.setData(bundle);
                                mSelfMessenger.send(sendResultMsg);

                                Log.d(TAG,"what  " + sendResultMsg.what);
                                Log.d(TAG,"sendResultMsg  " + sendResultMsg.getData().getString("result"));
                            } catch (RemoteException | JSONException e) {
                                e.printStackTrace();
                            }
                            nayaxSrerialport.PaymentStatus();
                            Log.d(TAG, "PaymentStatus()");
                            state = CARD_TOUCH_STATE;

                        }
                        break;
                    case 0x0c:
                        /** MDB_CARD_VEND_RES_RPT */
                        /** 払出完了通知 */
                        JSONObject jsonObjectSendData = new JSONObject();
                        //UIに支払い完了を送信
                        if(result.getCardMaker().equals(0)){
                            //MDB_CARD_VEND_RES_REQ 受付失敗
                            /**Nayax故障の場合に入る*/
                            Log.d(TAG,"Nayax故障の可能性 ");
                            sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);////0x0a
                            Log.d(TAG, "sendFinishMsg()");
                            try {
                                mSelfMessenger.send(sendFinishMsg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "Finis  " + sendFinishMsg.what);

                        }else{
                            //Nayax結果 成功
                            nayaxResult = true;

                            /** サーバーに送信するデータ*/
                            try {
                                jsonObjectSendData
                                        .put("totalPay",totalPay)
                                        .put("result",nayaxResult)
                                        .put("orders",myServiceOrders);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.d(TAG, "jsonObjectSendData : " + jsonObjectSendData.toString());
                            /**支払い完了　決済結果をサーバーに通知*/
                            SllConst s = new SllConst();
                            nayaxTask = new PaymentRequest(app, jsonObjectSendData.toString(),s.URL_NAYAX);//
                            nayaxTask.execute();
                            nayaxTask.setOnCallBack(new PaymentRequest.CallBackTask() {
                                @Override
                                public void CallBack(String receiveResult) {
                                    super.CallBack(receiveResult);
                                    //データ取得後
                                    //サーバー空の結果はresultに文字列で返ってくる
                                    Log.d(TAG, "NayaxPayRequest Finish  OK");
                                    Log.d(TAG, "receiveResult  " + receiveResult);

                                    /** 払出で使用するserviceOrder作成 */
                                    try {
                                        JSONObject jsonResult = new JSONObject(receiveResult);//決済結果 サーバーからのresult ※nullで落ちる
                                        JSONObject serviceOrders = new JSONObject();//払出で使用する注文データ

                                        //tradeId,tradeItemIdを追加する
                                        for(int i = 0; i < myServiceOrders.length(); i++){
                                            myServiceOrders.getJSONObject(i).put("tradeItemId",jsonResult
                                                    .getJSONArray("orders")
                                                    .getJSONObject(i)
                                                    .getInt("tradeItemId"));
                                        }
//                                    serviceOrders.put("tradeId",jsonResult.getInt("tradeId"))
                                        serviceOrders.put("orders",myServiceOrders);

                                        app.setserviceOrders(serviceOrders);//order部分をappに保存
                                        Log.d(TAG, "serviceOrders : " + serviceOrders.toString());

                                    /** 決済結果をUIに送る */
                                        JSONObject sendUiData = new JSONObject().put("result",jsonResult.getBoolean("result"));
                                        Message msg2 = Message.obtain(null, s.NAYAX_RESULT);//4
                                        Bundle bundle = new Bundle();
                                        bundle.putString("result",sendUiData.toString());
                                        msg2.setData(bundle);
                                        mSelfMessenger.send(msg2);
                                        Log.d(TAG,"mSelfMessenger.send.msg2  " + msg2.getData().getString("result"));//

                                    } catch (RemoteException | JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            //***************************************************************
                            /*MOCK  本番は通知のコールバック内　払出で使用するserviceOrder作成
                            try {
                                JSONObject serviceOrders = new JSONObject();//払出で使用する注文データ

                                int tradId = 10;
                                //tradeId,tradeItemIdを追加する
                                for(int i = 0; i < myServiceOrders.length(); i++){
                                    myServiceOrders.getJSONObject(i).put("tradeItemId",tradId + i);
                                }
                                serviceOrders.put("orders",myServiceOrders);

                                app.setserviceOrders(serviceOrders);//order部分をappに保存
                                Log.d(TAG, "serviceOrders : " + serviceOrders.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                                                        //UIに通知
                            sendResultMsg = Message.obtain(null, 4);
                            try {
                                mSelfMessenger.send(sendResultMsg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            */
                        //***************************************************************

                        }


                        break;
                    case 0x0e:
                        /**  キャンセル */
                        //結果をUIに送信
                        // カードリーダー支払キャンセル
                        String m0 = "Command Result キャンセル" + result.getCardReaderAck();
                        Log.i( TAG, m0 );

                        /** UIにキャンセル受付　送信 */
                        sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                        Log.d(TAG, "sendFinishMsg()");
                        try {
                            mSelfMessenger.send(sendFinishMsg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "Finis  " + sendFinishMsg.what);

                        break;
                    case 0x0b:
                        /** MDB_CARD_COST_RES_RPT*/
                        // カードリーダーステータス　送信はポーリングしている
                        //初回のSTART_STATE 状態が0になるまで待つ状態と、0になった後、MDB_CARD_COST_REQを送信した状態がある
                        String m1 = "Command Result " + result.getCardReaderAck() + "  CardReader Cabinet " + result.getCardCabinet()
                                +"  Lane " + result.getCardLane() +"  Status " + result.getCardStatus();
                        Log.d( TAG, m1 );

                        switch ( result.getCardStatus() ) {
                            case 0x00:
                                /**取引無し*/
                                Log.d(TAG, "取引無し"+ result.getCardStatus() );

                                if(state == START_STATE){
                                    /**MDB_CARD_COST_REQ送信*/
                                    //引数  レーン(1,1を入れておく) ,金額
                                    Log.d(TAG, "totalPay : ￥" + totalPay);
                                    if(totalPay > 0) {
//                                        nayaxSrerialport.PaymentRequest((byte) 1, (byte) 1, totalPay);//Mayax端末に金額制限がある 本番
                                        nayaxSrerialport.PaymentRequest((byte) 1, (byte) 1, 1);//test
                                        Log.d(TAG, "PaymentRequest");
                                        state = COST_RES_STATE;
                                        //UIにカード受付準備OK 送信
                                        sendResultMsg = Message.obtain(null, s.NAYAX_PAY);//3
                                        try {
                                            mSelfMessenger.send(sendResultMsg);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }else if(state == COST_RES_STATE){
                                    //MDB_CARD_COST_REQ送信してもまだ取引無しならもう一回再送信
                                    Log.d(TAG, "totalPay : ￥" + totalPay);
                                    if(totalPay > 0) {
//                                        nayaxSrerialport.PaymentRequest((byte) 1, (byte) 1, totalPay);//本番
                                        nayaxSrerialport.PaymentRequest((byte) 1, (byte) 1, 1);//test
                                        //UIにカード受付準備OK 送信
                                        sendResultMsg = Message.obtain(null, s.NAYAX_PAY);//3
                                        try {
                                            mSelfMessenger.send(sendResultMsg);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                        state = COST_RES_STATE;
                                    }
                                }

                                break;
                            case 0x01:
                                /**取引開始*/
                                //成功
                                Log.d(TAG, "成功 "+ result.getCardStatus() );
                                if(state == START_STATE) {
                                    Log.d(TAG, "START_STATE" + "result" + result.getCardStatus() );
                                    //ここに入らないはずなので終了させる
                                    //UIにカードキャンセル 送信
                                    sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                                    Log.d(TAG, "sendFinishMsg()");
                                    try {
                                        mSelfMessenger.send(sendFinishMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    Log.d(TAG, "カードタッチ待ち");
                                    nayaxSrerialport.PaymentStatus();
                                    Log.d(TAG, "PaymentStatus()");
                                }

                                break;
                            case 0x04:
                                //取引中　結果待ち
                                Log.d(TAG, "取引中 "+ result.getCardStatus() );
                                if(state == START_STATE) {
                                    Log.d(TAG, "START_STATE" + "result" + result.getCardStatus() );
                                    //ここに入らないはずなので終了させる
                                    //UIにカードキャンセル 送信
                                    sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                                    Log.d(TAG, "sendFinishMsg()");
                                    try {
                                        mSelfMessenger.send(sendFinishMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }else if(state == CARD_TOUCH_STATE){
                                    /**払出完了を送信*/
                                    //カードリーダーが払出結果待ちになったので完了を送信
                                    // 出荷結果は1。 支払い後払出の為必ず成功したにする
                                    nayaxSrerialport.PaymentRequest2( (byte) 1, (byte) 1, (byte) 1 );
                                    state = VEND_RES_STATE;
                                }

                                break;
                            case 0x05:
                                //取引中　カードリーダー終了待ち
                                Log.e(TAG, "取引中"+ result.getCardStatus() );

                                break;
                            case 0x08:
                                /**取引完了*/
                                Log.d(TAG, "取引完了"+ result.getCardStatus() );

                                if(state == START_STATE){
                                    //START_STATEなのにステータスが0ならなければ　initする　(3回目でinit)
                                    if(initCount > 3){
                                        nayaxSrerialport.Init();
                                        state = INIT_STATE;
                                        initCount = 0;
                                        Log.d(TAG, "nayax.Init()");
                                    }else{
                                        nayaxSrerialport.PaymentStatus();
                                        Log.d(TAG, "PaymentStatus()");
                                    }
                                    initCount++;

                                } else if(state == COST_RES_STATE){
                                    //ユーザーによる端末キャンセル
                                    //UIにカードキャンセル 送信
//                                    writeThread.interrupt();
                                    sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                                    Log.d(TAG, "sendFinishMsg　ユーザーによるキャンセル,タイムアウト");
                                    try {
                                        mSelfMessenger.send(sendFinishMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                } else if(state == CARD_TOUCH_STATE) {
                                    //カードタッチ
                                    sendFinishMsg = Message.obtain(null, s.NAYAX_CANCEL);//0x0a
                                    Log.d(TAG, "sendFinishMsg() カードNG");
                                    try {
                                        mSelfMessenger.send(sendFinishMsg);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }

                                break;
                            case 0x09:
                                //取引タイムオーバー
                                Log.e(TAG, "取引タイムオーバー"+ result.getCardStatus() );

                                break;
                            default:
                                Log.e(TAG, "CardStatus"+ result.getCardStatus() );
                        }

                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ ope );
                }
                break;
            default:
                Log.e(TAG, "illegal MT "+ mt );
        }
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
            Log.d(TAG, "handle request = " + msg);

            NayaxPaymentService myService = (NayaxPaymentService)refContext.get();
            myService.mSelfMessenger=msg.replyTo;
            myService.app = (BaseApplication)myService.getApplication();

            Log.d(TAG,"what  " + msg.what);
            //what=3 注文確認要求 Nayax
            if(msg.what == myService.s.NAYAX_PAY) {//3
                //QRcode受取り
                String qrCode = msg.getData().getString("nayax");
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
                            myService.totalPay = itemsTotalPay;

                            if(itemsTotalPay != uiTotalPay) {
                                Log.d(TAG,"TotalPay  NG " );
                                /**TotalPay NG*/

                            }

                            /**UIから来るデータをserviceで使用する形にする   {name,itemId,price,laneId,amount}    */
                            //UIから来るデータをserviceで使用する形にする関数 払出で使用するデータ作成
                            myService.myServiceOrders = new JSONArray();
                            //orderにLaneItems:[laneId,Amount]をくっつける
                            myService.myServiceOrders = myService.library.addLaneId(jsonArrayOrder,jsonArrayItems);

                            //totalPay qrCode 追加   //JSONObject jsonObject = new JSONObject(json);
                            Log.d(TAG, "serviceOrders : " + myService.myServiceOrders.toString());

                            /**カードリーダー料金照会  MDB_CARD_COST_RES_REQ */
                            myService.nayaxSrerialport.PaymentStatus();
                            Log.d(TAG, "PaymentStatus()");


                            // Nayax 準備 MDB_CARD_COST_RES_REQポーリング開始*/
//                            myService.writeThread.start();
//                            Log.d(TAG, "WriteThread Start");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else if(msg.what == myService.s.NAYAX_CANCEL) {//0x0a
                /** 自販機からはキャンセルしない　キャンセル処理*/
//                myService.nayax.PaymentCancel();
                Log.d(TAG, "nayax.PaymentCancel()  ");

            }else if(msg.what == 0x20){
                //MOCK btn
                /** 削除予定 MOCK  本番は通知のコールバック内　払出で使用するserviceOrder作成 */
                //***************************************************************
                try {
//                    JSONObject jsonResult = new JSONObject(result.toString());//決済結果
                    JSONObject serviceOrders = new JSONObject();//払出で使用する注文データ

                    int tradId = 10;
                    //tradeId,tradeItemIdを追加する
                    for(int i = 0; i < myService.myServiceOrders.length(); i++){
                        myService.myServiceOrders.getJSONObject(i).put("tradeItemId",tradId + i);
                    }
//                    serviceOrders.put("tradeId",31)
                    serviceOrders.put("orders",myService.myServiceOrders);

                    myService.app.setserviceOrders(serviceOrders);//order部分をappに保存
                    Log.d(TAG, "serviceOrders : " + serviceOrders.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //***************************************************************
                /**UIに通知*/
                Message sendFinishMsg = Message.obtain(null, myService.s.NAYAX_RESULT);//4
                try {
                    myService.mSelfMessenger.send(sendFinishMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


            }else{
                Log.d(TAG, "nayax UI command err  ");
            }
        }
    }

    public NayaxPaymentService() {
        Log.d(TAG, "NayaxPaymentService()");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");


    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        mServiceMessenger = new Messenger(new NayaxPaymentService.RequestHandler(getApplicationContext().getMainLooper(),this));


        /** シリアル準備 */
        nayaxSrerialport = new NayaxSerialPort(this );
        nayaxSrerialport.setCallback( this );
        nayaxSrerialport.threadStart();

        s = new SllConst();
        library = new SllLibrary();
//        writeThread = new WriteThread();

        return mServiceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.d(TAG, "onUnbind()");
        //threadを閉じる
        Log.d(TAG, "writeThread.interrupt()");
        //NayaxSerialPort threadを閉じる
        nayaxSrerialport.serialPortClose();
        Log.d(TAG, "serialPortClose()");
        try {
            nayaxSrerialport.threadStop();
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

    private class WriteThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "Nayax WriteThread run");
            super.run();
            while (!isInterrupted()) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /**カードリーダー料金照会  MDB_CARD_COST_RES_REQ */
                nayaxSrerialport.PaymentStatus();
                Log.d(TAG, "PaymentStatus()");
            }
        }
    }
/*
    private class ReadThread extends Thread {

        @Override
        public void run() {
            Log.d(TAG, "Nayax ReadThread run");
            super.run();
            while (!isInterrupted()) {




            }
        }
    }
*/
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
}
