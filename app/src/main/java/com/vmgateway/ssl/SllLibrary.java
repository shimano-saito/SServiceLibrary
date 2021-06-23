package com.vmgateway.ssl;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SllLibrary {
    private static final String TAG = "Log:SllLibrary";
    //orderにLaneItems:[laneId,Amount]をくっつける resultもくっつける
    public JSONArray addLaneId(JSONArray uiOrder, JSONArray item){
        JSONArray newArrayOrders;
        JSONObject newObjectOrder;

        /** LaneItems:[laneId,Amount]追加 (JSONArray)  */
        try{
            for(int i= 0; i < uiOrder.length() ; i++){
                for(int x = 0; x < item.length() ; x++){

//                     Log.d(TAG, "order : " + order.getJSONObject(i).getInt("itemId") + "  item : " + item.getJSONObject(x).getInt("ItemId"));
                    if(uiOrder.getJSONObject(i).getInt("itemId") == item.getJSONObject(x).getInt("itemId")){

                        //laneIdは複数ある場合がある
                        Log.d(TAG, "length : " + item.getJSONObject(x).getJSONArray("laneItems").length());
                        JSONArray laneItems = new JSONArray();
                        for(int y = 0; y < item.getJSONObject(x).getJSONArray("laneItems").length() ; y++){
                            int lane = item.getJSONObject(x).getJSONArray("laneItems").getJSONObject(y).getInt("laneId");//レーン
                            int laneAmount = item.getJSONObject(x).getJSONArray("laneItems").getJSONObject(y).getInt("amount");//レーン毎の在庫

                            laneItems.put(new JSONObject().put("laneId",lane).put("amount",laneAmount).put("spareAmount",laneAmount));//計算用：spareAmount
                            uiOrder.getJSONObject(i).put("laneItems",laneItems);
//                             Log.d(TAG, "order : " + order.toString());
                        }
                    }
                }
                uiOrder.getJSONObject(i).put("salesItemId",0);//初期値
                uiOrder.getJSONObject(i).put("result",false);//初期値
            }
            Log.d(TAG, "uiOrder : " + uiOrder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**  払い出すlaneIdを決める　在庫が多いレーンから払出 */
        int largeAmount = 0;
        newArrayOrders = new JSONArray();
        try{
            for(int i= 0; i < uiOrder.length() ; i++) {
                //一番多い在庫数を確認 largeAmount
                for (int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++) {
                    if(largeAmount <= uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount")){
                        largeAmount = uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount");
                    }
                }
                //在庫数が多いlaneIdを抜き出し、serviceで使用するorderを生成
                //同一商品がある場合はspareAmountを引いて確認　　int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++
                for(int y = 0 ; y < uiOrder.getJSONObject(i).getInt("quantity") ; y++){
                    for(int x = 0; x < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); x++){
                        if(largeAmount == uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount")){
                            //在庫多数があった
                            newArrayOrders.put(new JSONObject()
                                    .put("name",uiOrder.getJSONObject(i).getString("name"))
                                    .put("itemId",uiOrder.getJSONObject(i).getInt("itemId"))
                                    .put("price",uiOrder.getJSONObject(i).getInt("price"))
                                    .put("laneId",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("laneId"))//払出laneId確定
                                    .put("amount",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("amount"))//amount階層あげる
                                    .put("spareAmount",uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).getInt("spareAmount"))
                            );
                            uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(x).put("spareAmount",largeAmount - 1);//払出予定なので予備在庫１つ減らす
                            //一番多い在庫数を確認 largeAmount が変化する場合があるので再度確認
                            largeAmount = 0 ;
                            for (int z = 0; z < uiOrder.getJSONObject(i).getJSONArray("laneItems").length(); z++) {
                                if(largeAmount <= uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(z).getInt("spareAmount")){
                                    largeAmount = uiOrder.getJSONObject(i).getJSONArray("laneItems").getJSONObject(z).getInt("spareAmount");
                                }
                            }
                            break;
                        }
                    }
                }
            }
            Log.d(TAG, "uiOrder : " + uiOrder.toString());
            Log.d(TAG, "newArrayOrders : " + newArrayOrders.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newArrayOrders;
    }


}
