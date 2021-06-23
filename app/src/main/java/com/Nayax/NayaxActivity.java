package com.Nayax;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

//VMC FreezerTest
public class NayaxActivity extends AppCompatActivity implements NayaxSerialPortCallback {
    public static final String TAG = NayaxActivity.class.getSimpleName();

    //SerialPortSDK spsdk;
    NayaxSerialPort nayax;

    Button InitBtn;
    Button EnableBtn;
    Button MdbBtn;
    Button DisableBtn;
    Button CardBtn;
    Button CardPay1Btn;
    Button CardResultBtn;
    Button CardPay2Btn;
    Button CardCancelBtn;
    Button NextAct;

    ArrayList<Button> buttonList = new ArrayList<>();

    public int Lane = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nayax);

        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setText(textView.getText(), TextView.BufferType.EDITABLE);
        textView.setText("");

        InitBtn = findViewById(R.id.Init);
        InitBtn.setBackgroundColor(Color.parseColor("#afeeee"));
        InitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Init が押されました\n");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"Init が押されました\n");
                nayax.Init();
            }
        });
        EnableBtn = findViewById(R.id.Enable);
        EnableBtn.setBackgroundColor(Color.parseColor("#66cdaa"));
        EnableBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Enable が押されました\n");
               TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"Enable が押されました\n");
                nayax.Enable();
            }
        });

        MdbBtn = findViewById(R.id.MDB);
        MdbBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"MdbBoardQuery が押されました");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"MdbBoardQuery が押されました\n");
                nayax.MdbBoardQuery();
            }
        });
        DisableBtn = findViewById(R.id.Disable);
        DisableBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Disable が押されました");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"Disable が押されました\n");
                nayax.Disable();
            }
        });
        CardBtn = findViewById(R.id.Card1);
        CardBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"CardQuery が押されました");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"CardQuery が押されました\n");
                nayax.CardReaderQuery();
            }
        });
        CardPay1Btn = findViewById(R.id.CardPay1);
        CardPay1Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"CardPay1 が押されました");
                TextView textview = findViewById(R.id.textView);
                EditText e = findViewById(R.id.editTextNumber);
                CharSequence val = (CharSequence) e.getText();
                //
                if( val != null && TextUtils.isEmpty( val ) == false ) {
                    int am = Integer.parseInt( val.toString());
                    ((Editable) textview.getText()).insert(0,"CardPay1 が押されました。決済要求額　"+ am + " 円\n");
                    nayax.PaymentRequest((byte) 2, (byte) 3, am);
                }
                else {
                    ((Editable) textview.getText()).insert(0,"金額を指定してください。\n");
                }
            }
        });
        CardResultBtn = findViewById(R.id.cardResult);
        CardResultBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"cardResultSend が押されました");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"cardResultSend が押されました\n");
                nayax.PaymentStatus();
            }
        });
        CardPay2Btn = findViewById(R.id.cardPay2);
        CardPay2Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"CardPay2 が押されました");
                TextView textview = findViewById(R.id.textView);
                //
                EditText e = findViewById(R.id.editTextNumber);
                CharSequence val = (CharSequence) e.getText();
                //
                if( val != null && TextUtils.isEmpty( val ) == false ) {
                    int am = Integer.parseInt( val.toString());
                    ((Editable) textview.getText()).insert(0,"CardPay2 が押されました。結果　"+ am + " をセット\n");
                    //nayax.PaymentRequest2( (byte) 2, (byte) 3, (byte) 0 );
                    nayax.PaymentRequest2( (byte) 2, (byte) 3, (byte) am );
                }
                else {
                    ((Editable) textview.getText()).insert(0,"結果を指定してください。\n");
                }
            }
        });
        CardCancelBtn = findViewById(R.id.cardCancel);
        CardCancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"cardCancel が押されました");
                TextView textview = findViewById(R.id.textView);
                ((Editable) textview.getText()).insert(0,"cardCancel が押されました\n");
                nayax.PaymentCancel();
            }
        });
        //
        NextAct = findViewById((R.id.nextAc));
        NextAct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(TAG,"NextAct が押されました");
//                Intent next = new Intent(getApplication(), NextActivity2.class);
//                startActivity(next);
            }
        });
        //
        //spsdk = new SerialPortSDK(this, null);
        //spsdk.setCallback(this);
        nayax = new NayaxSerialPort(this );
        nayax.setCallback( this );
        nayax.threadStart();
    }
    @Override
    public void NayaxDeviceConditionChanged(NayaxResult result) {
        TextView textview = (TextView) findViewById(R.id.textView);
        //
        if( result.getMT() == 0x91 && result.getOPE() == 0x01 ) {
            String m = "Status changed  Card Enable " + result.getCardEnable() + " Card Error " + result.getCardError() + " Card Cost " + result.getCardCost();
            ((Editable) textview.getText()).insert(0, m +"\n");
            Log.i( TAG, m );
        }
        else {
            String m ="Illegal parameter MT -> "+ result.getMT() +" OPE -> "+ result.getOPE();
            ((Editable) textview.getText()).insert(0,m +"\n");
            Log.e( TAG, m );
        }
    }

    @Override
    public void NayaxCommanExecuted(NayaxResult result) {
        TextView textview = (TextView)findViewById(R.id.textView);
        //
        int mt = result.getMT();
        int ope = result.getOPE();
        //
        Log.d( TAG, String.valueOf(mt));
        switch ( mt ){
            case 0x91:
                switch ( ope ){
                    case  0x01:
                        // ハートビート
                        Log.e(TAG, "illegal ope "+ ope );
                        break;
                    case 0:
                    case 0x04:
                        // MDBデバイス情報
                        String m0 = "MDB Serial Number " + result.getmDBdeviceSerial() + "  Version " + result.getmDBdeviceVersion();
                        ((Editable) textview.getText()).insert(0,m0 +"\n");
                        Log.i( TAG, m0 );
                        break;
                    case 0x06:
                        // カードリーダー情報
                        String m1 ="CardReader Type " + result.getCardType() + "  Serial Number " + result.getCardSerial()
                                + "  Model Number" + result.getCardModelNumber() +"  Version " + result.getCardVervion()
                                + "  Maker " + result.getCardMaker();
                        ((Editable) textview.getText()).insert(0,m1 +"\n");
                        Log.i( TAG, m1 );
                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ ope );
                }
                break;
            case 0x92:
                // コマンド実行結果
                switch ( ope ) {
                    case 0x01:
                        // 初期化
                    case 0x02:
                        // 許可/禁止
                    case 0x0a:
                        // カードリーダー料金設定 ?
                        Log.i( TAG, "0a" );
                    case 0x0c:
                        // カードリーダー出金
                        Log.i( TAG, "0c" );
                    case 0x0e:
                        // カードリーダー支払キャンセル
                        String m0 = "Command Result " + result.getCardReaderAck();
                        ((Editable) textview.getText()).insert(0,m0 +"\n");
                        Log.i( TAG, m0 );
                        break;
                    case 0x0b:
                        // カードリーダー料金照会
                        String m1 = "Command Result " + result.getCardReaderAck() + "  CardReader Cabinet " + result.getCardCabinet()
                                +"  Lane " + result.getCardLane() +"  Status " + result.getCardStatus();
                        ((Editable) textview.getText()).insert(0,m1 +"\n");
                        Log.i( TAG, m1 );
                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ ope );
                }
                break;
            default:
                Log.e(TAG, "illegal MT "+ mt );
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        nayax.serialPortClose();
        Log.d(TAG, "serialPortClose()");
        try {
            nayax.threadStop();
            Log.d(TAG, "threadStop()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
