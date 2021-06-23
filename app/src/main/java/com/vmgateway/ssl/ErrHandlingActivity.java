package com.vmgateway.ssl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class ErrHandlingActivity extends AppCompatActivity{
    private static final String TAG = "Log:ErrActivity";

    BaseApplication app;

    //払出結果
    private String  resultItem;
    private JSONArray jsonArrayResult;  //払出結果
    private LinearLayout errList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_err_handling);
        Log.d(TAG, "onCreate()");



    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        //アクションバー非表示
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        app = (BaseApplication) getApplication();
        jsonArrayResult = app.getServiceResult();
        Log.d(TAG, "jsonArrayResult  " + jsonArrayResult.toString());
        errList = findViewById(R.id.errlist);

        TextView text = new TextView(this);

        text.setText("払出エラー");
        text.append("");
        text.append("");

        errList.addView(text);

        String strResult;
        int result = 0;

        try {
            //払出 result  出荷済:3 ,出荷未了:5 ,出荷取り消し:6
            for (int i = 0; i < jsonArrayResult.length(); i++) {
                TextView textResult = new TextView(this);
                textResult.setText("\n\n");//改行
                textResult.setTextColor(Color.parseColor("#FFEB3B"));
                textResult.setTextSize(24.0f);

                result = jsonArrayResult.getJSONObject(i).getInt("status");

                switch (result) {
                    case 3:
                        Log.d(TAG, "出荷済  " + result);
                        strResult = "出荷済";

                        break;
                    case 5:
                        Log.d(TAG, "出荷未了  " + result);
                        strResult = "出荷未了";

                        break;
                    case 6:
                        Log.d(TAG, "出荷取り消し  " + result);
                        strResult = "出荷取り消し";

                        break;
                    default:
                        Log.e(TAG, "その他 result  " + result);
                        strResult = "other";

                        break;
                }

                textResult.append(jsonArrayResult.getJSONObject(i).getString("name") + "\n");
                textResult.append("Lane  " + jsonArrayResult.getJSONObject(i).getString("laneId") + " , " + strResult);
                errList.addView(textResult);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button backButton = findViewById(R.id.backbutton2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                app = (BaseApplication)getApplication();
                app.setServiceResult(null);

                //Intent生成　name,code,price,descriptionList,渡す
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
