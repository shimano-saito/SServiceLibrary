package com.vmgateway.ssl;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class StockDetailsActivity extends AppCompatActivity {
    private static final String TAG = "Log:SubActivity";

    private JSONObject jsonLaneData;
    private String laneInfos;
    private int itemMax;
    private int setAmount;
    private int position = 0;
    TextView subtextAmount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        // タイトルバーを非表示にする（setContentViewの前に呼ぶ）
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
//        actionBar.setDisplayHomeAsUpEnabled(true);


        View view = (View)findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拡大した画像をタップするともとの画面に戻る
                JSONObject jsonLaneData = new JSONObject();

                try {
                    jsonLaneData.put("amount",setAmount)
                            .put("position",position);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("setData", jsonLaneData.toString());
                Log.d(TAG,"setData  " + jsonLaneData.toString());
                setResult(RESULT_OK, intent);
//                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        Intent intent = getIntent();
        laneInfos = intent.getStringExtra("jsonLaneData");
        Log.d(TAG,"laneInfos  " + laneInfos);
        try {
            jsonLaneData = new JSONObject(laneInfos);

            TextView subtextName = findViewById(R.id.textname);
            subtextName.setText(jsonLaneData.getString("name"));
            subtextName.setTextSize(54.0f);

            TextView subtextItemId = findViewById(R.id.textitemid);
            subtextItemId.setText(String.valueOf(jsonLaneData.getInt("itemId")));
            subtextItemId.setTextSize(90.0f);

            TextView subtextLane = findViewById(R.id.textlane);
            subtextLane.setText(String.valueOf(jsonLaneData.getInt("laneId")));
            subtextLane.setTextSize(90.0f);

            subtextAmount = findViewById(R.id.textamount);
            subtextAmount.setText(String.valueOf(jsonLaneData.getInt("amount")));
            subtextAmount.setTextSize(120.0f);
            setAmount = jsonLaneData.getInt("amount");

            itemMax = jsonLaneData.getInt("pitch");
            position = jsonLaneData.getInt("position");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ImageView plus = findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick  plus ");
                //pitchより大きくならない
                if(setAmount < itemMax){
                    setAmount++;
                    subtextAmount.setText(String.valueOf(setAmount));

                }else{
                    Log.d(TAG,"補給数量最大です ");

                }

            }
        });

        ImageView minus = findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick  minus ");
                //0未満にならない
                if(setAmount > 0){
                    setAmount--;
                    subtextAmount.setText(String.valueOf(setAmount));

                }else{
                    Log.d(TAG,"補給数量最小です ");

                }

            }
        });

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
