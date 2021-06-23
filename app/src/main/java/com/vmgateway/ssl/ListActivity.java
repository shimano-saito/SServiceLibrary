package com.vmgateway.ssl;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "Log:List_Activity";

    private BaseApplication app;
    private AsyncHttpRequest mTask;
    private JSONArray laneItems;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Log.d(TAG, "onCreate()");



    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        //アクションバー
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("商品一覧");

        app = (BaseApplication) getApplication();

        mTask = new AsyncHttpRequest(app);
        mTask.execute();
        mTask.setOnCallBack(new AsyncHttpRequest.CallBackTask() {
            @Override
            public void CallBack(String result) {
                super.CallBack(result);
                //データ取得後
                Log.d(TAG, "AsyncHttpRequest Finish  OK");
                laneItems = app.getJsonItems();
                Log.d(TAG,laneItems.toString());

                // ListViewのインスタンスを生成
                listView = findViewById(R.id.listView);
                BaseAdapter adapter = new ProductListAdapter(getApplicationContext(),
                        R.layout.list_items, laneItems);
                // ListViewにadapterをセット
                listView.setAdapter(adapter);
                Log.d(TAG, "listView.setAdapter(adapter)  OK");
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
}


//***************************************************
//ListView レイアウト BaseAdapter継承してカスタムする
//***************************************************
class ProductListAdapter extends BaseAdapter {
    public static final String TAG = "ProductListAdapter";

    private Context context;
    private LayoutInflater inflater;
    private int layoutID;
    private JSONArray itemList;

    private int amount;
//    private ArrayList<String> adNamelist;

    class ViewHolder {
        LinearLayout linearLaneId;
        TextView textId;
        TextView textName;
        TextView textPrice;
        TextView textAmount;

//        ImageView img;
    }

    ProductListAdapter(Context context, int itemLayoutId, JSONArray items) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        layoutID = itemLayoutId;
        itemList = items;

    }

    //*****注意点*****
    //listViewのlayout_widthとlayout_heightはwrap_contentだとListView全体のサイズが分からないため、
    //要素数以上に呼ばれることがある
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //ViewHolderを使うことでgetViewの引数が再利用され処理が早くなる
        //findViewByIdで取得した参照をViewHolderクラスに保持してView.tagに格納して再利用
        Log.d(TAG, "getView()");
        amount = 0;
        TextView textLaneId = null;
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(layoutID, null);
            holder = new ViewHolder();
//            holder.img = convertView.findViewById(R.id.img_item);
            holder.textId = convertView.findViewById(R.id.textId);
            holder.textName = convertView.findViewById(R.id.textName);
            holder.textPrice = convertView.findViewById(R.id.textPrice);
            holder.textAmount = convertView.findViewById(R.id.textAmount);
            holder.linearLaneId = convertView.findViewById(R.id.linearlaneid);

            convertView.setTag(holder);
        } else {
            //再利用時
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.textId.setText(itemList.getJSONObject(position).getString("name"));
            holder.textName.setText("ID:" + itemList.getJSONObject(position).getString("itemId"));
            holder.textPrice.setText("￥" + itemList.getJSONObject(position).getString("price"));
//            holder.textPrice.setText(itemList.getJSONObject(position).getString("Amount"));


            
            for (int i = 0; i < itemList.getJSONObject(position).getJSONArray("laneItems").length(); i++) {
                Log.d(TAG, "laneId  " + itemList.getJSONObject(position).getJSONArray("laneItems").getJSONObject(i).getString("laneId"));

                textLaneId = new TextView(context);
                textLaneId.setText(itemList.getJSONObject(position).getJSONArray("laneItems").getJSONObject(i).getString("laneId"));
                holder.linearLaneId.addView(textLaneId, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                amount = amount + itemList.getJSONObject(position).getJSONArray("laneItems").getJSONObject(i).getInt("amount");
            }
            holder.textAmount.setText("在庫：" + Integer.toString(amount));

            Log.d(TAG, "name : " + itemList.getJSONObject(position).getString("name")
                    + ", itemId :" + itemList.getJSONObject(position).getInt("itemId")
                    + ", price :" + itemList.getJSONObject(position).getInt("price")
                    + ", amount :" + amount);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return itemList.length();
    }

    @Override
    public Object getItem(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}