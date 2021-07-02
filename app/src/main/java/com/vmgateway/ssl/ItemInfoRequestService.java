package com.vmgateway.ssl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class ItemInfoRequestService extends Service {
    private static final String TAG = "Log:ItemInfoRequestService";

    private BaseApplication app;
    private Messenger mServiceMessenger;
    private Messenger mSelfMessenger;

    private AsyncHttpRequest mTask;
    private JSONArray jsonItems = null;//サーバーデータ
    private JSONArray jsonLanes = null;//サーバーデータ
    private JSONArray jsonProductData = null;//サーバーデータをDispPriority順に並び替えた

    private SllConst s;
   /*
    //テスト的に作成、WeakReferenceからメンバにアクセスすれば、必要ないはず
    public void setmSelfMessenger(Messenger mSelfMessenger) {
        this.mSelfMessenger = mSelfMessenger;
    }
    //テストとして作成。同上
    public Messenger getmSelfMessenger (){
        return this.mSelfMessenger;
    }
    */
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

            JSONObject setting = null;

            ItemInfoRequestService myService = (ItemInfoRequestService)refContext.get();
            myService.mSelfMessenger=msg.replyTo;

/*
            setting = myService.settingCheck();//settingファイルがあるか確認、ない場合は作成する
            if( setting == null){
                //settingChack err
                Log.d(TAG, "settingCheck err");
            }
*/
 //           try {
                //applicationにvmId　password　vmPasswordをセット
//                myService.app.setVmId(setting.getString("vmId"));
//                myService.app.setPassword(setting.getString("password"));
//                myService.app.setVmPassword(setting.getString("vmPassword"));

                Log.d(TAG, "setting 　" +" vmId: "+ myService.app.getVmId() + ", password: "  + myService.app.getPassword() + ", vmPassword: " + myService.app.getVmPassword());

//            } catch (JSONException e) {
 //               e.printStackTrace();
//            }

            try {
                //Thread.sleep(1000);
//                JSONArray jsonArray = new JSONArray(msg.getData().getString("json"));
                Log.d(TAG,"what  " + msg.what);
                //what=1:商品データ要求
                if(msg.what == myService.s.ITEM_DATA){//1
                    //サーバ接続
//                    myService.mTask = new AsyncHttpRequest(myService);
                    myService.mTask = new AsyncHttpRequest(myService.app);
                    myService.mTask.execute();
                    myService.mTask.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);
                            //データ取得後
                            Log.d(TAG, "AsyncHttpRequest Finish  OK");

                            JSONArray itemInfos = myService.app.getJsonItems();
                            Log.d(TAG,itemInfos.toString());

                            //UI側に送信するデータ
                            int amount = 0;
                            try {
                                JSONArray jsonArray = new JSONArray();
                                for(int i = 0; i < itemInfos.length(); i++){
                                    amount = 0;
                                    //TESTサーバー情報取得　大文字 本番は小文字にい統一
                                    Log.d(TAG,"name  " + itemInfos.getJSONObject(i).getString("name"));
                                    Log.d(TAG,"code  " + itemInfos.getJSONObject(i).getString("code"));
                                    Log.d(TAG,"price  " + itemInfos.getJSONObject(i).getInt("price"));
                                    Log.d(TAG,"itemId  " + itemInfos.getJSONObject(i).getInt("itemId"));
                                    Log.d(TAG,"description  " + itemInfos.getJSONObject(i).getString("description"));
//                                    Log.d(TAG,"data  " + itemInfos.getJSONObject(i).getString("data"));
                                    //在庫数各Laneの合計
                                    for(int x = 0 ; x < itemInfos.getJSONObject(i).getJSONArray("laneItems").length(); x++){
                                        Log.d(TAG,"laneId  " + itemInfos.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getString("laneId"));
                                        amount = amount + itemInfos.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("amount");
                                    }
                                    Log.d(TAG,"amount  " + amount);//合計在庫

                                    //Activityに送るJSONデータ 小文字
                                    jsonArray.put(new JSONObject()
                                            .put("name",itemInfos.getJSONObject(i).getString("name"))
                                            .put("code",itemInfos.getJSONObject(i).getString("code"))
                                            .put("price",itemInfos.getJSONObject(i).getInt("price"))
                                            .put("itemId",itemInfos.getJSONObject(i).getInt("itemId"))
                                            .put("description",itemInfos.getJSONObject(i).getString("description"))
                                            .put("data",itemInfos.getJSONObject(i).getString("data"))
                                            .put("amount",amount));

                                }

                                //取得したデータを送信する
                                Message msg2 = Message.obtain(null, myService.s.ITEM_DATA );//1
                                msg2.replyTo = myService.mSelfMessenger;

                                Bundle bundle =new Bundle();
                                bundle.putString("itemInfos",jsonArray.toString());
                                msg2.setData(bundle);

                                myService.mSelfMessenger.send(msg2);
                                Log.d(TAG,"mSelfMessenger.send " + msg2.getData().toString());

                            } catch (JSONException | RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public ItemInfoRequestService() {
        Log.d(TAG, "MyService()");
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
        mServiceMessenger = new Messenger(new RequestHandler(getApplicationContext().getMainLooper(),this));

        s = new SllConst();
        app = (BaseApplication)getApplication();
        app.setServiceResult(null);//払出err 初期化


        app.setPassword("password");
        app.setVmId("VM0001");
        app.setVmPassword("VM0001");

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

    //設定ファイルがなかったら作成
    public JSONObject settingCheck() {
        Log.d(TAG, "settingCheck()");
        String path;
        String fileName;
        String strFile;
        String text;
        File file;

        //pathの設定
        fileName = "setting.txt";
        path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString();
        strFile = path + "/" + fileName;
        file = new File(path + "/" + fileName);

        Log.d(TAG, "file  " + strFile);

        String state = Environment.getExternalStorageState();
        Log.d(TAG, "state  " + state);

        if(Environment.MEDIA_MOUNTED.equals(state)){
            try {
                if(!file.exists()){
                    Log.d(TAG, "File 存在しません  ");
                    //File作成
                    //FileOutputStream(パス, true：ファイル末尾に書き込み、false：上書き)
                    FileOutputStream fileOutputStream = new FileOutputStream(strFile, false);
                    JSONObject jsonobject = new JSONObject();
                    jsonobject.put("vmId", "VM0001")
                            .put("password", "VM0001")
                            .put("vmPassword", "shimano");

                    fileOutputStream.write((jsonobject.toString()).getBytes());
                    Log.d(TAG, "jsonobject  " + jsonobject.toString());

                    fileOutputStream.close();
                }

                //ファイルの読み込み
                text = readFile(strFile);
                JSONObject jsonobjectText = new JSONObject(text);
                Log.d(TAG, "setting  " + text);

                return jsonobjectText;
            } catch (FileNotFoundException | JSONException e) {
             e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    private String readFile(String file){
        String text = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            String lineBuffer;
            while (true){
                lineBuffer = reader.readLine();
                if (lineBuffer != null){
                    text += lineBuffer;
                }
                else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

}