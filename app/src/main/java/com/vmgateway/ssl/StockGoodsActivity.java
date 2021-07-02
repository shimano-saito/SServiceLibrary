package com.vmgateway.ssl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class StockGoodsActivity extends AppCompatActivity {
    private static final String TAG = "Log:StockGoodsActivity";

    // どこで呼び出したのかを判別するためのコード
    private static final int RESULTCODE = 1;

    private BaseApplication app;
    private AsyncHttpRequest mTask;
    private PaymentRequest dataSendTask;
    private JSONArray laneInfos;//サーバーから取得データ　在庫変更していく
    private JSONArray oriLaneInfos;//サーバーから取得データ

    private LinearLayout Vlayout;
//    private ArrayList listLayout;
    private LinearLayout[] layout = null;
    private LinearLayout layout2;
    private LinearLayout layout3;
    private LinearLayout layout4;
    private LinearLayout layout5;
    private LinearLayout layout6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_goods);
        Log.d(TAG, "onCreate()");
        //アクションバーに戻る機能
//        ActionBar actionBar = getSupportActionBar();
//
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("商品補給");

        Vlayout = findViewById(R.id.Vlayout);
        layout = new LinearLayout[Vlayout.getChildCount()];

        layout[0] = findViewById(R.id.layoutL1);
        layout[1] = findViewById(R.id.layoutL2);
        layout[2] = findViewById(R.id.layoutL3);
        layout[3] = findViewById(R.id.layoutL4);
        layout[4] = findViewById(R.id.layoutL5);
        layout[5] = findViewById(R.id.layoutL6);


        app = (BaseApplication) getApplication();

        app.setPassword("password");
        app.setVmId("VM0001");
        app.setVmPassword("VM0001");

        mTask = new AsyncHttpRequest(app);
        mTask.execute();
        mTask.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
            @Override
            public void CallBack(String result) {
                super.CallBack(result);
                //データ取得後
                Log.d(TAG, "AsyncHttpRequest Finish  OK");
                laneInfos = app.getJsonLanes();

                try {
                    oriLaneInfos = new JSONArray(laneInfos.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG,laneInfos.toString());
                //****LaneInfoを取得し、LaneIdを順番に並べる
                getStockLaneInfo(laneInfos);

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");


        /**MAX値にする*/
        Button maxbtn = findViewById(R.id.maxbtn);
        maxbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "buy button");
                int position = 0;
                int pitch = 0;
                for(int i = 0; i < Vlayout.getChildCount() ; i++){
                     for(int x = 0; x < layout[i].getChildCount() ; x++){
                         try {
                             pitch = laneInfos.getJSONObject(position).getInt("pitch");
                             laneInfos.getJSONObject(position).getJSONObject("laneItem").put("amount",pitch);//在庫数をMAX(pitchの数)にする

                             Button btn = (Button) layout[i].getChildAt(x);
                             btn.setText(String.format(Locale.US,
                                     laneInfos.getJSONObject(position).getJSONObject("laneItem").getString("name")
                                             + "\n商品Id: " + laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("itemId")
                                             + "\nレーンId: " + laneInfos.getJSONObject(position).getInt("laneId")
                                             + "\n在庫:" + pitch
                             ));
                             position++;
                         } catch (JSONException e) {
                             e.printStackTrace();
                         }

                     }


                }
            }
        });

        /**サーバーに送信*/
        Button sendbtn = findViewById(R.id.sendbtn);
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sendbtn ");
                /** サーバーに送信するDATA*/

                for(int i = 0 ; i < laneInfos.length() ; i++){
                    try{
                        //払出と同じフォーマット
                        JSONObject sendServerData = new JSONObject();//払出で使用する注文データ
                        sendServerData
                                .put("tradeItemId",null)//サーバー側null　OK
                                .put("itemId",laneInfos.getJSONObject(i).getJSONObject("laneItem").getInt("itemId"))
                                .put("price",laneInfos.getJSONObject(i).getJSONObject("laneItem").getInt("price"))
                                .put("laneId",laneInfos.getJSONObject(i).getInt("laneId"))
                                .put("status",0)//商品補充の時は？
                                .put("from",oriLaneInfos.getJSONObject(i).getJSONObject("laneItem").getInt("amount"))//サーバーから取得してきた時の値
                                .put("to",laneInfos.getJSONObject(i).getJSONObject("laneItem").getInt("amount"));//変更後の値
                        Log.d(TAG, "serverDataSend  " + sendServerData.toString());
                        String strSendServerData = sendServerData.toString();


                        //サーバーに送信
                        SllConst s = new SllConst();
                        dataSendTask = new PaymentRequest(app, strSendServerData,s.URL_TRADE);//
                        dataSendTask.execute();
                        dataSendTask.setOnCallBack(new PaymentRequest.CallBackTask() {
                            @Override
                            public void CallBack(String serverResult) {
                                super.CallBack(serverResult);
                                Log.d(TAG, "serverResult " + serverResult);
                                //送信完了
                                try {
                                    JSONObject jsonObject = new JSONObject(serverResult);
                                    if(jsonObject.getBoolean("result") == true){
                                        //成功
                                        Toast ts = Toast.makeText(getApplication(), "送信 成功", Toast.LENGTH_SHORT);
                                        ts.setGravity(Gravity.TOP,0, 200);
                                        ts.show();
                                    }else{
                                        //受信失敗
                                        Toast ts = Toast.makeText(getApplication(), "送信　失敗", Toast.LENGTH_SHORT);
                                        ts.setGravity(Gravity.TOP,0, 200);
                                        ts.show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });



    }

    /**
     * 遷移先から戻ってきたときの処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULTCODE && resultCode == RESULT_OK) {
            Log.d(TAG, "元の画面に戻ってきた");
//            Intent intent = getIntent();
            int num = 0;
            String setData = data.getStringExtra("setData");

            Log.d(TAG,"setData  " + setData);

            LinearLayout lay = new LinearLayout(this);

            try {
                JSONObject jsonSetData = new JSONObject(setData);
                int position = jsonSetData.getInt("position");
                int myPosition = position;
                //positionが何列目なのか判定
                //position ボタンの位置、layout[i].getChildCount()：１段のボタンの数
                for(num = 0; num < Vlayout.getChildCount() ; num++){
                    myPosition = myPosition - layout[num].getChildCount();

                    if(myPosition < 0){
                        lay = layout[num];//ボタンがあるlayout
                        break;
                    }
                }
                myPosition = myPosition + layout[num].getChildCount();//layout[i]の何個目のボタンか、引いた分戻す
                laneInfos.getJSONObject(position).getJSONObject("laneItem").put("amount",jsonSetData.getInt("amount"));
                Log.d(TAG,"laneInfos  " + laneInfos.toString());
                Log.d(TAG,"getChildCount  " + lay.getChildCount());
                Log.d(TAG,"myPosition  " + myPosition);

                Button btn = (Button) lay.getChildAt(myPosition);
//                btn.setText(String.format(Locale.US, "amount" + String.valueOf(jsonSetData.getInt("amount"))));
//                String name = "abc";
                btn.setText(String.format(Locale.US,
                        laneInfos.getJSONObject(position).getJSONObject("laneItem").getString("name")
                        + "\n商品Id: " + laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("itemId")
                        + "\nレーンId: " + laneInfos.getJSONObject(position).getInt("laneId")
//                        +  "\namount: " + jsonSetData.getInt("amount")
                        +"在庫" + jsonSetData.getInt("amount")
                ));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //在庫データを更新したのでUIに反映
//            Log.d(TAG,"button position  " + );

        }
    }

    //アクションバー　戻る機能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getStockLaneInfo(JSONArray jsonLane) {
        Log.d(TAG, "getStockLaneInfo");
        int laneslength = jsonLane.length();//使用レーンの数(ボタンの数と同じ)
        String name;
        int laneId;
        int width;
        int springType;
        int pitch;
        int amount;
        int itemId;
        int setAmount = 0;//セットする在庫数

        Log.d(TAG, "lanesSize : " + laneslength);
        for(int i = 0 ; i < laneslength ; i++){
            try {
                name = jsonLane.getJSONObject(i).getJSONObject("laneItem").getString("name");
                laneId = jsonLane.getJSONObject(i).getInt("laneId");
                width = jsonLane.getJSONObject(i).getInt("width");
                springType = jsonLane.getJSONObject(i).getInt("springType");
                pitch = jsonLane.getJSONObject(i).getInt("pitch");
                amount = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("amount");
                itemId = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("itemId");
                setAmount = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("amount");

//                laneInfos.getJSONObject(i).put("setAmount",setAmount);

                Log.d(TAG, "name : " + name + " ,width : " + width + " ,springType : " + springType + " ,pitch : " + pitch + " ,itemId : " + itemId + " ,amount : " + amount);

                if(laneId/100 == 1){
                    //1列目
//                    int laneMAX = lanes.getLaneInfo().get(i).getWidthSum();
                    layout[0] = initButton(layout[0],i,jsonLane);
//                    ++weightSum[0];
//                    layout.setWeightSum(sumMax);
                }else if(laneId/100 == 2){
                    layout[1] = initButton(layout[1],i,jsonLane);
//                    ++weightSum[1];
//                    layout2.setWeightSum(sumMax);
                }else if(laneId/100 == 3){
                    layout[2] = initButton(layout[2],i,jsonLane);

//                    ++weightSum[2];

                }else if(laneId/100 == 4){
                    layout[3] = initButton(layout[3],i,jsonLane);
//                    ++weightSum[3];

                }else if(laneId/100 == 5){
                    layout[4] = initButton(layout[4],i,jsonLane);

//                    ++weightSum[4];

                }else if(laneId/100 == 6){
                    layout[5] = initButton(layout[5],i,jsonLane);

//                    ++weightSum[5];

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    //button tag設定
//    public LinearLayout initButton(LinearLayout lay,int position,String name,int laneId,int width,int itemId,int amount,int setAmount){
    public LinearLayout initButton(LinearLayout lay, int position, JSONArray jsonLane)  {
        int laneId = 0;
        int width = 0;
        int springType = 0;
        int pitch = 0;
        int amount = 0;
        int itemId = 0;
        int setAmount = 0;
        String name = "";

        try {
            name = jsonLane.getJSONObject(position).getJSONObject("laneItem").getString("name");
            laneId = jsonLane.getJSONObject(position).getInt("laneId");
            width = jsonLane.getJSONObject(position).getInt("width");
            springType = jsonLane.getJSONObject(position).getInt("springType");
            pitch = jsonLane.getJSONObject(position).getInt("pitch");
            amount = jsonLane.getJSONObject(position).getJSONObject("laneItem").getInt("amount");
            itemId = jsonLane.getJSONObject(position).getJSONObject("laneItem").getInt("itemId");
            setAmount = jsonLane.getJSONObject(position).getJSONObject("laneItem").getInt("amount");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        Button btn = new Button(this);
        btn.setTag(String.valueOf(position));

        btn.setText(String.format(Locale.US,
                        name
                               + "\n商品Id: " + itemId
                               + "\nレーン: " + laneId
                               +"\n在庫: " + setAmount
//                             "\nLaneId: " + laneId
//                          +"\n" + setAmount
                        ));


        btn.setBackgroundColor(Color.parseColor("#ffefd5"));

        LinearLayout.LayoutParams buttonLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);//MATCH_PARENT WRAP_CONTENT
        int margins = 5;
        //left top right bottom
        buttonLayoutParams.setMargins(2, margins, 2, margins);
        //button size 割合
        buttonLayoutParams.weight = width;
//            btn.setLayoutParams(buttonLayoutParams);
        //set
        lay.addView(btn,buttonLayoutParams);
        Log.d(TAG, "button Set "  + laneId);
        //Listnerセット
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**未完成　うまく在庫を更新できない　要修正*/
                Log.d(TAG, "onClick   " + " position: " + position);
                //ViewからTagを取り出す
                TextView textview = findViewById(R.id.textname);
//                textview.setText(String.format(Locale.US, amount + " が押されました"));
                JSONObject jsonLaneData = new JSONObject();

                try {

                    jsonLaneData.put("name",laneInfos.getJSONObject(position).getJSONObject("laneItem").getString("name"))
                            .put("itemId",laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("itemId"))
                            .put("laneId",laneInfos.getJSONObject(position).getInt("laneId"))
                            .put("pitch",laneInfos.getJSONObject(position).getInt("pitch"))
                            .put("amount",laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("amount"))
                            .put("position",position);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // 拡大した画像をタップするともとの画面に戻る
                Intent intent = new Intent(getApplicationContext(), StockDetailsActivity.class);
                intent.putExtra("jsonLaneData", jsonLaneData.toString());
                Log.d(TAG,"jsonLaneData  " + jsonLaneData.toString());
                startActivityForResult(intent, RESULTCODE);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);



            }
        });

        return lay;
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        super.finalize();
    }


}
