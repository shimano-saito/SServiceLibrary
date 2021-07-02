package com.vmgateway.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileModel {

    private static String TAG = new Object(){}.getClass().getEnclosingClass().getName();

    public FileModel(){

    }

    // ファイルデータの読み込み
    public static String GetFileData(String path )
    {

        Log.d(TAG, "GetFileData"  );

//        String text = "";
        final StringBuffer sb = new StringBuffer("");

        try {

            if( ! path.isEmpty() )
            {

                String filepath = Environment.getExternalStoragePublicDirectory("") + path;

                Log.d(TAG, "filepath" + filepath);

                File file = new File(filepath);

                if (file.exists()) {
                    // ファイルがある
                    Log.d(TAG, "file.exists TRUE");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));

                    String tmp;

                    while ((tmp = reader.readLine()) != null) {
                        sb.append(tmp);
                    }

                    reader.close();

                } else {
                    // ファイルがない
                    Log.d(TAG, "file.exists FALSE");

                }

            }

        }
        catch (IOException e)
        {

            e.printStackTrace();

        }
        catch( Exception e )
        {

            e.printStackTrace();

        }

        return  sb.toString();

    }
    public static File[] getFileList(String path, String code )
    {

        try{

            String filepath = Environment.getExternalStoragePublicDirectory("") + "/DCIM/Items/";// + path;

            Log.d(TAG,   filepath);

//            File directory = new File(filepath);



//            return  new File(filepath).listFiles();
//            String[] fileNames = directory.list();
            File[]  directory = new File(filepath).listFiles( new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {

                    Log.d(TAG, name  );

                    if(name.indexOf(code) != -1){
                        return  true;
                    }else {
                        return false;
                    }
                }
            });

            Log.d(TAG, String.format("サイズ:%d", directory.length));

            java.util.Arrays.sort(directory, new java.util.Comparator<File>() {
                public int compare(File file1, File file2){
                    return file1.getName().compareTo(file2.getName());
                }
            });

            return directory;



        }
        catch( Exception e )
        {
            Log.d(TAG, "例外エラー"  );
            e.printStackTrace();

        }

        return  null;

    }

//    public static String GetStremData(URL url)throws IOException{
//
//        Log.d(TAG, "GetStremData"  );
//
//        final StringBuffer sb = new StringBuffer("");
//
//        try
//        {
//
//            if(url != null)
//            {
//
//                String encoding = "UTF-8";
//                final int TIMEOUT_MILLIS = 10000;
//
//                HttpURLConnection httpConn = null;
//
//                try
//                {
//                    //URL url = new URL(str.trim());
//                    httpConn = (HttpURLConnection) url.openConnection();
//                    httpConn.setConnectTimeout(TIMEOUT_MILLIS);// 接続にかかる時間
//                    httpConn.setReadTimeout(TIMEOUT_MILLIS);// データの読み込みにかかる時間
//                    httpConn.setRequestMethod("GET");// HTTPメソッド
//                    httpConn.setUseCaches(false);// キャッシュ利用
//                    httpConn.setDoOutput(false);// リクエストのボディの送信を許可(GETのときはfalse,POSTのときはtrueにする)
//                    httpConn.setDoInput(true);// レスポンスのボディの受信を許可
//                    // HTTPヘッダをセット
//                    httpConn.setRequestProperty("Authorization", "Bearer "+ CommonModel.instance.getAccessToken() );
//                    httpConn.connect();
//
//                    final int responseCode = httpConn.getResponseCode();
//
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//
//                        InputStream is = null;
//                        InputStreamReader isr = null;
//                        BufferedReader br = null;
//
//                        try
//                        {
//
//                            is = httpConn.getInputStream();
//                            isr = new InputStreamReader(is, encoding);
//                            br = new BufferedReader(isr);
//                            String line = null;
//                            while ((line = br.readLine()) != null) {
//                                sb.append(line);
//                            }
//
//                            Log.d(TAG, "DATA:" + sb.toString()  );
////                            Gson gson = new Gson();
//
//                        }
//                        catch (Exception e)
//                        {
//                            e.printStackTrace();
//                        }
//                        finally {
//
//                            //finally return の後に動作する部分
//// fortify safeかつJava1.6 compliantなclose処理
//                            if (br != null) {
//                                try {
//                                    br.close();
//                                } catch (IOException e) {
//                                }
//                            }
//                            if (isr != null) {
//                                try {
//                                    isr.close();
//                                } catch (IOException e) {
//                                }
//                            }
//                            if (is != null) {
//                                try {
//                                    is.close();
//                                } catch (IOException e) {
//                                }
//                            }
//
//                        }
//
//                    } else {
//                        // If responseCode is not HTTP_OK
//                        Log.d("connect", "responseCode = " + responseCode);
//                    }
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                } catch (IOException e){
//                    e.printStackTrace();
//                }finally{
//
//                    if (httpConn != null) {
//                        httpConn.disconnect();
//                    }
//
//                }
//
//            }
//
//        }
//        catch( Exception e )
//        {
//
//            e.printStackTrace();
//
//        }
//
//        return  sb.toString();
//
//    }

    // Jsonの文字列をシアライズして各変数に配置する

//    public static boolean ConvertItemList(String json){
//
//        boolean Status = false;
//
//        try
//        {
//
//
//            if(! json.isEmpty()) {
//                // データを取得出来た
//                Log.d(TAG, "データ取得成功"  );
//                Log.d( TAG,  json );
//                Gson gson = new Gson();
//
//                Log.d(TAG, "Jsonシアライズ"  );
//                ItemsModel items =  gson.fromJson( json, ItemsModel.class );
//
//                Log.d(TAG, "items.getItemInfos()"  );
//                List<ItemInfo> itemInfoList =  items.getItemInfos();
//
//                if(itemInfoList != null){
//
//                    Log.d(TAG, String.format("itemInfoList Not NULL Size:%d", itemInfoList.size() ));
//
//                    itemInfoList.forEach( i -> {
//
//                        Log.d(TAG,  String.format("i.getId():%d", i.getId()));
//                        Log.d(TAG,  String.format("laneItems Size():%d", i.getlaneItems().size()));
//
//                        if(0 < i.getlaneItems().size())
//                        {
//                            // レーン情報がある
//                            Log.d(TAG, "レーン情報があるので表示"  );
//                            ItemModel item = new ItemModel();
//
//                            item.setId(i.getId());
//                            item.set_janCode(i.getJanCode());
//                            item.setCode(i.getCode());
//                            item.setName(i.getName());
//                            item.setDescription(i.getDescription());
//
//                            Log.d(TAG, "ファイル一覧"  );
//
//                            CommonModel.instance.getPaths().forEach( p ->{
//                                if( p.getType().equals("Images")){
//                                    Log.d(TAG, String.format("Path:%s", p.getPath()));
//                                    Log.d(TAG, String.format("Code:%s", item.getCode()));
//
//                                    File[] str =FileModel.getFileList(p.getPath(), item.getCode());
//
//                                    Log.d(TAG, String.format("ファイルサイズ%d", str.length)  );
//
//                                    if(str != null){
//
//                                        for(int x =0; x < str.length; x++){
//
//                                            Log.d(TAG, str[x].getPath()  );
//                                            item._ImageList.add(str[x].getPath());
//
//                                        }
//
//                                    }
//
//                                    return;
//
//                                }
//                            });
//
//                            item.setPrice(i.getPrice());
//                            item.set_laneItems(i.getlaneItems());
//
//                            Log.d(TAG, "i.getData().trim()"  );
//                            String datatemp = i.getData().trim();
//
//                            Log.d(TAG, datatemp  );
//
//                            // 1行ごとにタグ除去
//                            Pattern pattern = Pattern.compile("\\\\n");
//                            Matcher matcher = pattern.matcher(datatemp);
//                            String plainText = matcher.replaceAll("\n");
//
//                            DatasModel datasModel = gson.fromJson(datatemp, DatasModel.class);
//
//                            Log.d(TAG, "CommonModel.instance.addItemList(item)"  );
//
//                            item.setTopNames(datasModel.getTop().getNames());
//
//                            item.setDetailNames(datasModel.getDetails().getNames());
//
//                            List<LanguageTextModel> descriptions = datasModel.getDetails().getDescriptions();
//
//                            if(descriptions != null){
//
//                                Log.d(TAG, "descriptions not null"  );
//
//
//                            }else{
//
//                                Log.d(TAG, "descriptions null"  );
//
//                                LanguageTextModel data = new LanguageTextModel();
//
//                                data.setLanguage("ja-JP");
//                                data.setText(datasModel.getDetails().getDescription());
//
//                                descriptions = new ArrayList<>();
//                                descriptions.add(data);
//
//                            }
//
//                            item.setDescriptions(descriptions);
//
//
//                            CommonModel.instance.addItemList(item);
//
//                        }else{
//                            Log.d(TAG, "レーン情報がないので表示しない"  );
//                        }
//
//                    });
//
//                    if( 0 < itemInfoList.size()){
//                        Status = true;
//                    }
//
//                }
//                else
//                {
//
//                    Log.d(TAG, "itemInfoList NULL" );
//
//                }
//
//            }
//
//
//
//        }
//        catch( Exception e )
//        {
//            Log.d(TAG, "例外エラー"  );
//            e.printStackTrace();
//
//        }
//
//        return Status;
//
//    }
    public static Drawable ResizeImage(String img, int size )
    {

        Drawable drawable;

        try
        {

            if(!img.isEmpty()){

                Log.d(TAG, img);
                Log.d(TAG, String.format("size:%d", size));

                drawable = Drawable.createFromPath(img);

                //画像のあるパスからdrawableを生成
                Bitmap orgBitmap = ((BitmapDrawable)drawable).getBitmap();

                int swidth = 0;
                int sheight = 0;
                if( orgBitmap.getHeight() < orgBitmap.getWidth()){
                    // 横幅が大きい場合

                    swidth = size;
                    sheight = (int)( orgBitmap.getHeight() / ( orgBitmap.getWidth() / size ));

                }else{
                    // 縦幅が大きい場合

                    sheight = size;
                    swidth = (int)( orgBitmap.getWidth() / ( orgBitmap.getHeight() / size ));

                }

                //DrawableからBitmapインスタンスを取得
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(orgBitmap, swidth, sheight, false);
                //100x100の大きさにリサイズ
                drawable = new BitmapDrawable(Resources.getSystem(), resizedBitmap);

                return drawable;

            }

        }
        catch( Exception e )
        {

            Log.d(TAG, "例外エラー"  );
            e.printStackTrace();

        }

        return null;

    }

}
