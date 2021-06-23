package com.vmgateway.ssl;

import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.Nayax.NayaxActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Log:MainActivity";

    private BaseApplication app;
    private EditText editPassword;
    private String vmPassword;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        //アクションバー
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("password");


        app = (BaseApplication) getApplication();

        editPassword = findViewById(R.id.pass);
        textView = findViewById(R.id.textView);
        vmPassword = app.getVmPassword();

        Button enter = findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String nayax = "nayax";

                String editText = editPassword.getText().toString();
                Log.d(TAG, "vmPassword ： " + vmPassword);
                Log.d(TAG, "editText ： " + editText);

                if (vmPassword.equals(editText)) {
                    Log.d(TAG, "password一致");

                    if(app.getServiceResult() == null){
                        //払出errが無ければ通常　管理者モード
                        Intent intent = new Intent(getApplicationContext(), TopActivity.class);
                        startActivity(intent);
                    }else{
                        //払い出し後　errがある状態で　管理者モード
                        Intent intent = new Intent(getApplicationContext(), ErrHandlingActivity.class);
                        startActivity(intent);

                    }


                }else if(nayax.equals(editText)){
                    Log.d(TAG, "nayax モード");
                    Intent intent = new Intent(getApplicationContext(), NayaxActivity.class);
                    startActivity(intent);

                }else{
                    textView.setText("passwordが違います");
                    Log.d(TAG, "passwordが違います");
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