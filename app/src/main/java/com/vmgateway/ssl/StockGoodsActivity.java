package com.vmgateway.ssl;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

import static com.vmgateway.ssl.SllConst.LANE_MAX;

public class StockGoodsActivity extends AppCompatActivity {
    private static final String TAG = "Log:StockGoodsActivity";

    private BaseApplication app;
    private AsyncHttpRequest mTask;
    private JSONArray laneInfos;
    private GCCService mService;
    private EditText editAmount;//在庫入力


    private int sumMax = LANE_MAX;

    private LinearLayout layout;
    private LinearLayout layout2;
    private LinearLayout layout3;
    private LinearLayout layout4;
    private LinearLayout layout5;
    private LinearLayout layout6;

    //ボタンモード amount入力　有効/無効
    private boolean inputMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_goods);
        Log.d(TAG, "onCreate()");

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        //アクションバーに戻る機能
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("商品補給");


//        editAmount = findViewById(R.id.editAmount);
//        editAmount.setFocusable(false);//入力無効
        layout = findViewById(R.id.layoutL1);
        layout2 = findViewById(R.id.layoutL2);
        layout3 = findViewById(R.id.layoutL3);
        layout4 = findViewById(R.id.layoutL4);
        layout5 = findViewById(R.id.layoutL5);
        layout6 = findViewById(R.id.layoutL6);



        app = (BaseApplication) getApplication();

        mTask = new AsyncHttpRequest(app);
        mTask.execute();
        mTask.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
            @Override
            public void CallBack(String result) {
                super.CallBack(result);
                //データ取得後
                Log.d(TAG, "AsyncHttpRequest Finish  OK");
                laneInfos = app.getJsonLanes();
                Log.d(TAG,laneInfos.toString());
                //****LaneInfoを取得し、LaneIdを順番に並べる
                getStockLaneInfo(laneInfos);

            }
        });


        Button maxButton = findViewById(R.id.maxbtn);
        maxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //amountをMAXにする　pichと同じにする

            }
        });

        Button enterButton = findViewById(R.id.sendbtn);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            /**サーバーに送信*/

            }
        });

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

    public void getStockLaneInfo(JSONArray jsonLane){

//        int weightSum[] = {0,0,0,0,0,0,0};
        int laneslength = jsonLane.length();//使用レーンの数(ボタンの数と同じ)
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
                laneId = (jsonLane.getJSONObject(i).getInt("laneId"));
                width = jsonLane.getJSONObject(i).getInt("width");
                springType = jsonLane.getJSONObject(i).getInt("springType");
                pitch = jsonLane.getJSONObject(i).getInt("pitch");
                amount = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("amount");
                itemId = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("itemId");
                setAmount = jsonLane.getJSONObject(i).getJSONObject("laneItem").getInt("amount");

//                laneInfos.getJSONObject(i).put("setAmount",setAmount);

                Log.d(TAG, "lane : " + laneId + " ,width : " + width + " ,springType : " + springType + " ,pitch : " + pitch + " ,itemId : " + itemId + " ,amount : " + amount);

                if(laneId/100 == 1){
                    //1列目
//                    int laneMAX = lanes.getLaneInfo().get(i).getWidthSum();
                    layout = initButton(layout,i,laneId,width,springType,pitch,itemId,amount,setAmount);
//                    ++weightSum[0];
//                    layout.setWeightSum(sumMax);
                }else if(laneId/100 == 2){
                    layout2 = initButton(layout2,i,laneId,width,springType,pitch,itemId,amount,setAmount);
//                    ++weightSum[1];
//                    layout2.setWeightSum(sumMax);
                }else if(laneId/100 == 3){
                    layout3 = initButton(layout3,i,laneId,width,springType,pitch,itemId,amount,setAmount);

//                    ++weightSum[2];

                }else if(laneId/100 == 4){
                    layout4 = initButton(layout4,i,laneId,width,springType,pitch,itemId,amount,setAmount);
//                    ++weightSum[3];

                }else if(laneId/100 == 5){
                    layout5 = initButton(layout5,i,laneId,width,springType,pitch,itemId,amount,setAmount);

//                    ++weightSum[4];

                }else if(laneId/100 == 6){
                    layout6 = initButton(layout6,i,laneId,width,springType,pitch,itemId,amount,setAmount);

//                    ++weightSum[5];

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


//        return null;
    }

    //button tag設定
    public LinearLayout initButton(LinearLayout lay,int position,int laneId,int width,int springType,int pitch,int itemId,int amount,int setAmount){

        Button btn = new Button(this);
        btn.setTag(String.valueOf(laneId));

        btn.setText(String.format(Locale.US,
                        "itemId: " + itemId
                        + "\nLaneId: " + laneId
                        +  "\namount: " + setAmount));

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
                //ViewからTagを取り出す
                TextView textview = findViewById(R.id.textView);
//                textview.setText(String.format(Locale.US, amount + " が押されました"));
                String textAmount = null;

                Log.d(TAG, "laneId: " + laneId + " , amount: " + amount  + " , position: " + position);
                if(inputMode == false){
                    //false状態から押されたのでtrue
                    inputMode = true;
                    try {
                        Log.d(TAG, "textAmount  " + laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("amount"));
                        textAmount = Integer.toString(laneInfos.getJSONObject(position).getJSONObject("laneItem").getInt("amount"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    editAmount.setText(textAmount);
//                    editAmount.setFocusableInTouchMode(true);//入力有効
                    Log.d(TAG, "inputMode: true");

                }else{
//                    textAmount = editAmount.getText().toString();
                    try {
                        laneInfos.getJSONObject(position).put("amount",textAmount);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //true状態から押されたのでfalse
                    inputMode =false;
//                    editAmount.setFocusable(false);//入力無効
                    Log.d(TAG, "inputMode: false");
                    //入力された在庫数を反映

                }

            }
        });

        return lay;
    }


}