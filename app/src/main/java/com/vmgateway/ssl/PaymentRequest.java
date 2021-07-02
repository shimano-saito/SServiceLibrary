package com.vmgateway.ssl;


import android.os.AsyncTask;
import android.util.Log;
/*
import com.google.gson.Gson;
import com.smn.main.serverdata.Items;
import com.smn.main.serverdata.Lanes;
import com.smn.main.serverdata.MyToken;
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

//******Http通信　非同期クラス res/xml下にnetwork_security_config.xml追加。アクセス許可******
//Manifestにネットワーク許可追記、res/xmlフォルダ作成しnetwork_security_config作成　manifestに追記
public class PaymentRequest extends AsyncTask<String, Void, String> {
    public static final String TAG = PaymentRequest.class.getSimpleName();

    private BaseApplication myApp;
//    private SharedPreferences dataStore;

//    private QRPaymentService mService;
    private CallBackTask callbacktask;
    private String sendData;
    private String receiveData;

    private String url;
//    private String url = "https://dev-vma.s-lab.app/api/trade/qr";//QR決済

    public PaymentRequest(BaseApplication app,String data,String u) {
        myApp = app;
        sendData = new String(data);
        url = u;
    }
    /*
    public PaymentRequest(QRPaymentService service,String data) {
        mService = service;
        sendData = new String(data);
    }
     */
    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground");

        String token = null;
//        myApp = (BaseApplication) mService.getApplication();
        token = myApp.getToken();
        Log.d(TAG,"token  " + token);

        if(token == "noData"){
            try {
                token = GetToken();
                Log.d(TAG,"retry token");//取得したtoken log
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"QRPayRequest token  " + token);//取得したtoken log
        Log.d(TAG,"sendData  " + sendData);//

        //QR 決済リクエスト
        try {
            receiveData = GetDataStream( token, url,sendData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        try{
            for(int i = 0 ; i < 2 ; i++){
                //送信するbody追加する
                receiveData = GetDataStream( token, url,sendData);
                if(receiveData == null){
                    token = GetToken();
                    Log.d(TAG,"retry token" + token);//取得したtoken log
                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        Log.d(TAG,"receiveData  " + receiveData);//

        return receiveData;
    }

    //token 取得
    public static String GetToken() throws IOException {

        String encoding = "UTF-8";
        String uToken = "https://dev-vma.s-lab.app/token";//トークン取得API
        String key = "grant_type=password&username=VM0001&password=VM0001";//本番テスト name,passwordは自販機枚に違う

        final int TIMEOUT_MILLIS = 10000;
        int responseCode = 0;

        final StringBuffer sb = new StringBuffer("");
        HttpURLConnection httpConn = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;

        try{
            URL url = new URL(uToken);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(TIMEOUT_MILLIS);// 接続にかかる時間
            httpConn.setReadTimeout(TIMEOUT_MILLIS);// データの読み込みにかかる時間
            httpConn.setRequestMethod("POST");// HTTPメソッド
            httpConn.setUseCaches(false);// キャッシュ利用
            httpConn.setDoOutput(true);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
            httpConn.setDoInput(true);// レスポンスのボディの受信を許可
            //ヘッダset
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("charset", "utf-8");
//            httpConn.setRequestProperty("Content-Length", "57");

            //データ送信
            OutputStream os = httpConn.getOutputStream();
            final boolean autoFlash = true;
            PrintStream ps = new PrintStream(os, autoFlash, encoding);
            ps.print(key);
            ps.close();

            responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                //データ取得成功
                is = httpConn.getInputStream();
                isr = new InputStreamReader(is, encoding);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Log.i("HTTP", sb.toString());
                String token = sb.toString();
                Log.d(TAG, "token : " + token);
//                Gson gson = new Gson();
//                MyToken token = gson.fromJson(sb.toString(), MyToken.class);
                return (token);//OK
            } else {
                // If responseCode is not HTTP_OK
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            // fortify safeかつJava1.6 compliantなclose処理
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }

//        Integer res = Integer.valueOf(responseCode);
//        return res.toString();//HTTP コードをStringで返す
        return (null);//NG
    }

    private static String GetDataStream(String token, String u,String data) throws IOException {
        String encoding = "UTF-8";
        final int TIMEOUT_MILLIS = 10000;
        int responseCode = 0;

        final StringBuffer sb = new StringBuffer("");
        HttpURLConnection httpConn = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;

        try {
            URL url = new URL(u);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(TIMEOUT_MILLIS);// 接続にかかる時間
            httpConn.setReadTimeout(TIMEOUT_MILLIS);// データの読み込みにかかる時間
            httpConn.setRequestMethod("POST");// HTTPメソッド
            httpConn.setUseCaches(false);// キャッシュ利用
            httpConn.setDoOutput(true);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
            httpConn.setDoInput(true);// レスポンスのボディの受信を許可
            // HTTPヘッダをセット
            //ヘッダset
            httpConn.setRequestProperty("Content-Type","application/json");
            httpConn.setRequestProperty("charset", "utf-8");
            httpConn.setRequestProperty("Authorization", "Bearer "+ token);

            httpConn.connect();
            //データ送信
            OutputStream os = httpConn.getOutputStream();
            final boolean autoFlash = true;
            PrintStream ps = new PrintStream(os, autoFlash, encoding);
            ps.print(data);
            ps.close();

            responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                is = httpConn.getInputStream();
                isr = new InputStreamReader(is, encoding);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Log.i("HTTP", sb.toString());
//                Gson gson = new Gson();
                return (sb.toString());
            } else {
                // If responseCode is not HTTP_OK
                Log.d("GetDataStream", "responseCode = " + responseCode);
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }finally{
            //finally return の後に動作する部分
// fortify safeかつJava1.6 compliantなclose処理
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
//        Integer res = Integer.valueOf(responseCode);
//        return res.toString();//HTTP コードをStringで返す
        return null;
    }
    //非同期処理終了後　結果をメインスレッドに返す
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callbacktask.CallBack(result);
    }
    public void setOnCallBack(CallBackTask _cbj) {
        callbacktask = _cbj;
    }
    public static class CallBackTask {
        public void CallBack(String result) {
        }
    }
}
