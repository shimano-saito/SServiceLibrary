package com.vmgateway.ssl;


import android.os.AsyncTask;
import android.util.Log;
/*
import com.google.gson.Gson;
import com.smn.main.serverdata.Items;
import com.smn.main.serverdata.Lanes;
import com.smn.main.serverdata.MyToken;
*/
import com.vmgateway.model.FileModel;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

//******Http通信　非同期クラス res/xml下にnetwork_security_config.xml追加。アクセス許可******
//Manifestにネットワーク許可追記、res/xmlフォルダ作成しnetwork_security_config作成　manifestに追記
public class AsyncHttpRequest extends AsyncTask<String, Void, String> {
    public static final String TAG = AsyncHttpRequest.class.getSimpleName();
    private BaseApplication myApp;
    private ItemInfoRequestService mService;
    private CallBackTask callbacktask;

//    private String uToken = "https://dev-vma.s-lab.app/token";//トークン取得API
    private String uItems = "https://dev-vma.s-lab.app/api/data/items";//商品情報取得API
    private String uLanes = "https://dev-vma.s-lab.app/api/data/lanes";//レーン情報取得API
//    String u = "http://52.197.207.196/SaitouAndroidTest/token";//テストサーバー
//    String uItems = "http://52.197.207.196/SaitouAndroidTest/api/v2/data/Items/5";//テストサーバー
//    String uLanes = "http://52.197.207.196/SaitouAndroidTest/api/v2/data/Lanes/5";//テストサーバー


    public AsyncHttpRequest(BaseApplication app) {
        myApp = app;
    }
    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground");

        JSONObject jsonToken;
        String token = null;
        String items = null;
        String lanes = null;
        JSONArray jsonItems;
        JSONArray jsonLanes;

        SllConst s = new SllConst();

        /**password id取得*/
//        String pass = myApp.getPassword();
//        String vmId = myApp.getVmId();
//        String key = "grant_type="+pass+"&username="+vmId+"&password="+vmId;
//        Log.d(TAG, "grant_type="+pass+"&username="+vmId+"&password="+vmId);
//        String key = "grant_type=password&username=VM0001&password=VM0001";//本番テスト name,passwordは自販機枚に違う

        String text = FileModel.GetFileData( s.SETTING_PATH);
        try {
            JSONObject jo = new JSONObject(text);
            Log.d(TAG, "&username="+jo.getString("username")+"&password="+jo.getString("password"));
            String key = "grant_type=password"+"&username="+jo.getString("username")+"&password="+jo.getString("password");

//        SettingModel setting =  gson.fromJson( text, SettingModel.class );
            Log.d(TAG,"settingFile : " + text);
//        Log.d(TAG, "&username="+setting.getUsername()+"&password="+setting.getPassword());


            jsonToken = new JSONObject(GetToken(key));
            token = jsonToken.getString("access_token");
            Log.d(TAG, token);//取得したtoken log

            /**情報取得*/
            items = GetDataStream( token, s.URL_ITEMS);
            lanes = GetDataStream( token, s.URL_LANES);
            Log.d(TAG, "items" + items);//取得したtoken log
            Log.d(TAG, "lanes" + lanes);//取得したtoken log

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {

            JSONObject joItems = new JSONObject(items);
            jsonItems = joItems.getJSONArray("itemInfos");
            JSONObject joLanes = new JSONObject(lanes);
            jsonLanes = joLanes.getJSONArray("laneInfos");
            myApp.setJsonItems(jsonItems);
            myApp.setJsonLanes(jsonLanes);
            myApp.setToken(token);
            Log.d(TAG, "itemInfos  " + jsonItems.toString());
            Log.d(TAG, "laneInfos  " + jsonLanes.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //token 取得
    public static @Nullable String GetToken(String key) throws IOException {
//        String uToken = "https://dev-vma.s-lab.app/token";//トークン取得API

        String encoding = "UTF-8";

        final int TIMEOUT_MILLIS = 10000;
        int responseCode;
        final StringBuffer sb = new StringBuffer("");
        HttpURLConnection httpConn = null;
        BufferedReader br = null;
        InputStream is = null;
        InputStreamReader isr = null;

        SllConst s = new SllConst();

        try {
            URL url = new URL(s.URL_TOKEN);
//            URL url = new URL(uToken);
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

//            httpConn.connect();
            //データ送信
            OutputStream os = httpConn.getOutputStream();
            final boolean autoFlash = true;
            PrintStream ps = new PrintStream(os, autoFlash, encoding);
            ps.print(key);
            ps.close();

//            final int responseCode = httpConn.getResponseCode();
            responseCode = httpConn.getResponseCode();
            Log.d(TAG, "responseCode  " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
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
        }catch (Exception e){
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
        return (null);//NG
    }

    private static String GetDataStream(String token, String u) throws IOException {
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
            httpConn.setRequestMethod("GET");// HTTPメソッド
            httpConn.setUseCaches(false);// キャッシュ利用
            httpConn.setDoOutput(false);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
            httpConn.setDoInput(true);// レスポンスのボディの受信を許可
            // HTTPヘッダをセット
            httpConn.setRequestProperty("Authorization", "Bearer "+token);
            httpConn.connect();

//            final int responseCode = httpConn.getResponseCode();
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
                Log.d("connect", "responseCode = " + responseCode);
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
        return null;
//        Integer res = Integer.valueOf(responseCode);
//        return res.toString();//HTTP コードをStringで返す
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
