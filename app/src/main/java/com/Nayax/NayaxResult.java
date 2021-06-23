package com.Nayax;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * 受信データをパースして結果を保持するクラス
 */
public class NayaxResult {
    private final String TAG = NayaxResult.class.getSimpleName();

    private int mt = 0;
    private int ope = 0;
    private boolean cardEnable = false;
    private boolean cardError = false;
    private int cardCost = 0;
    private String mDBdeviceSerial = "";
    private String mDBdeviceVersion = "";
    private int cardType = 0;
    private String cardSerial ="";
    private String cardModelNumber ="";
    private String cardVervion ="";
    private String cardMaker ="";
    private int cardReaderAck = 0;
    private int commandResult = 0;
    private int cardCabinet = 0;
    private int cardLane = 0;
    private int cardStatus = 0;

    public int getMT(){ return ( mt );}
    public int getOPE(){ return ( ope ); }
    public boolean getCardEnable(){ return ( cardEnable ); }
    public boolean getCardError(){ return ( cardError ); }
    public int getCardCost(){ return ( cardCost ); }
    public String getmDBdeviceSerial(){ return ( mDBdeviceSerial ); }
    public String getmDBdeviceVersion(){ return ( mDBdeviceVersion ); }
    public int getCardType(){ return ( cardType ); };
    public String getCardSerial(){ return ( cardSerial ); }
    public String getCardModelNumber(){ return ( cardModelNumber ); }
    public String getCardVervion(){ return ( cardVervion ); }
    public String getCardMaker(){ return ( cardMaker ); }
    public int getCardReaderAck(){ return ( cardReaderAck ); }
    public int getCommandResult(){ return ( commandResult ); }
    public int getCardCabinet(){ return ( cardCabinet ); }
    public int getCardLane(){ return ( cardLane ); }
    public int getCardStatus(){ return ( cardStatus ); }
    /*
     * コンストラクタ
     *
     * NayaxPacket packet   受信データ
     */
    public  NayaxResult( NayaxPacket packet ){
        mt = packet.getMT();
        byte[] data = packet.getData();
        //
        switch ( mt ){
            case 0x91:
                ope = data[ 0 ];
                switch ( ope ){
                    case  0x01:
                        // ハートビート
                        // 動作状態
                        cardError = ( data[ 23 ] & 0x02 ) == 0x02;
                        cardEnable = ( data[ 23 ] & 0x01 ) == 0;
                        ByteBuffer b1 = ByteBuffer.allocate( 4 ).put( data, 24, 4 );
                        b1.flip();
                        cardCost = b1.getInt();
                        break;
                    case 0:
                    case 0x04:
                        // MDBデバイス情報
                        //mDBdeviceSerial = new String( Arrays.copyOfRange( data, 1, 21 ) );
                        //ByteBuffer b4 = ByteBuffer.allocate( 2 ).put( data, 21, 2 );
                        mDBdeviceSerial = HexToStr( Arrays.copyOfRange( data, 0, 20 ) );
                        ByteBuffer b4 = ByteBuffer.allocate( 2 ).put( data, 20, 2 );
                        b4.flip();
                        mDBdeviceVersion = String.format( "%04x", b4.getShort() );
                        break;
                    case 0x06:
                        // カードリーダー情報
                        cardType = data[ 1 ];
                        if( data.length > 13 ) {
                            cardSerial = HexToStr(Arrays.copyOfRange( data, 2, 14) );
                        }
                        if( data.length > 25 ) {
                            cardModelNumber = HexToStr(Arrays.copyOfRange( data, 14, 26 ));
                        }
                        if( data.length > 27 ) {
                            ByteBuffer b6 = ByteBuffer.allocate(2).put( data, 26, 2 );
                            b6.flip();
                            cardVervion = String.format("%x04", b6.getShort());
                        }
                        if( data.length > 30 ) {
                            cardMaker = String.format("%06x", Arrays.copyOfRange( data, 28, 31 ) );
                        }
                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ data[ 0 ] );
                        throw new RuntimeException("illegal ope "+ data[ 0 ] );
                }
                break;
            case 0x92:
                ope = data[ 0 ];
                // コマンド実行結果
                switch ( ope ) {
                    case 0x01:
                        // 初期化
                    case 0x02:
                        // 許可/禁止
                        cardReaderAck = data[ 3 ];
                        break;
                    case 0x0a:
                        // カードリーダー料金設定 ?
                    case 0x0c:
                        // カードリーダー出金
                    case 0x0e:
                        // カードリーダー支払キャンセル
                        commandResult = data[ 1 ];
                        break;
                    case 0x0b:
                        // カードリーダー料金照会
                        cardCabinet = data[ 1 ];
                        cardLane = data[ 2 ];
                        commandResult = data[ 3 ];
                        cardStatus = data[ 4 ];
                        break;
                    default:
                        Log.e(TAG, "illegal ope "+ data[ 0 ] );
                        throw new RuntimeException("illegal ope "+ data[ 0 ] );
                }
                break;
            default:
                Log.e(TAG, "illegal MT "+ packet.getMT() );
                throw new RuntimeException("illegal MT "+ packet.getMT() );
        }
    }
    /*
     * デバッグ用文字列化
     */
    private String HexToStr( byte[] bytes ){
        StringBuilder sb = new StringBuilder();
        //
        for( byte b: bytes ){
            sb.append( String.format("%02x ", b ) );
        }
        return ( sb.toString() );
    }
}
