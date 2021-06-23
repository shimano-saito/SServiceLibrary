package com.vmgateway.ssl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

public class LaneActivity  extends AppCompatActivity {
    private static final String TAG = "Log:LaneActivity";

    private BaseApplication app;
    private AsyncHttpRequest mTask;
    private JSONArray laneInfos;
    private GCCService mService;

    private int sumMax = 8;

    LinearLayout layout;
    LinearLayout layout2;
    LinearLayout layout3;
    LinearLayout layout4;
    LinearLayout layout5;
    LinearLayout layout6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lane);
        Log.d(TAG, "onCreate()");



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
                getLaneInfo();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");



        //アクションバー戻る
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("レーン動作テスト");

        //GCCServiceとbind
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),GCCService.class);
        intent.putExtra("LaneActivity","lanetest");
        bindService(intent, connection , Context.BIND_AUTO_CREATE);

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
        unbindService(connection);

    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        super.finalize();
    }

    //*****サーバーデータを取得、LaneIdを順番に並べる lanes.getLaneInfo().get(0).getLaneId()

    public ArrayList getLaneInfo(){

//        int weightSum[] = {0,0,0,0,0,0,0};
        int lanesSize = laneInfos.length();//使用レーンの数(ボタンの数と同じ)
        int lane;
        int width;
        int springType;
        int pitch;

        Log.d(TAG, "lanesSize : " + lanesSize);
        for(int i = 0 ; i < lanesSize ; i++){
            try {
                lane = (laneInfos.getJSONObject(i).getInt("laneId"));
                width = laneInfos.getJSONObject(i).getInt("width");
                springType = laneInfos.getJSONObject(i).getInt("springType");
                pitch = laneInfos.getJSONObject(i).getInt("pitch");
                Log.d(TAG, "lane : " + lane + " ,width : " + width + " ,springType : " + springType + " ,pitch : " + pitch);

                if(lane/100 == 1){
                    //1列目
//                    int laneMAX = lanes.getLaneInfo().get(i).getWidthSum();
                    layout = initButton(layout,i,lane,width,springType,pitch);
//                    ++weightSum[0];
//                    layout.setWeightSum(sumMax);
                }else if(lane/100 == 2){
                    layout2 = initButton(layout2,i,lane,width,springType,pitch);
//                    ++weightSum[1];
//                    layout2.setWeightSum(sumMax);
                }else if(lane/100 == 3){
                    layout3 = initButton(layout3,i,lane,width,springType,pitch);

//                    ++weightSum[2];

                }else if(lane/100 == 4){
                    layout4 = initButton(layout4,i,lane,width,springType,pitch);
//                    ++weightSum[3];

                }else if(lane/100 == 5){
                    layout5 = initButton(layout5,i,lane,width,springType,pitch);

//                    ++weightSum[4];

                }else if(lane/100 == 6){
                    layout6 = initButton(layout6,i,lane,width,springType,pitch);

//                    ++weightSum[5];

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        Log.d("layout.getTag",layout.getTag(101).toString());
        /*
        int weightSum = 8;
        layout.setWeightSum(weightSum);
        layout2.setWeightSum(weightSum);
        layout3.setWeightSum(weightSum);
        layout4.setWeightSum(weightSum);
        layout5.setWeightSum(weightSum);
        layout6.setWeightSum(weightSum);
*/
        return null;
    }

    //button tag設定
    public LinearLayout initButton(LinearLayout lay,int i,int lane,int width,int springType,int pitch){

        Button btn = new Button(this);
        btn.setTag(String.valueOf(lane));

        btn.setText(String.format(Locale.US,
                "LaneId: " + lane
                        + "\nwidth" + width
                        + "\nSpringType " + springType
                        +  "\npitch: " + pitch));

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
        Log.d(TAG, "button Set "  + lane);
        //Listnerセット
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ViewからTagを取り出す
                TextView textview = findViewById(R.id.textView);
                //                   textview.setTextSize(textSize);
                textview.setText(String.format(Locale.US, lane + " が押されました"));
                mService.deliver(lane);

                Log.d(TAG, "GCCService.deliver lane : " + lane);

            }
        });

        return lay;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.v(TAG, "onServiceConnected");
            mService = ((GCCService.laneTestBinder) binder).getService();
        }
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisConnected");
            mService = null;
        }
    };
}
