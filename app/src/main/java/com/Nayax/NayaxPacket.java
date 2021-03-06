package com.Nayax;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/*
 * VPOS を MDB ボード経由で通信する送受信データを扱うクラス
 */
public class NayaxPacket {
    private final String TAG = NayaxPacket.class.getSimpleName();
    /*
     * 送信用 SF
     */
    public static final byte SendSF = (byte) 0x0e5;
    /*
     * 受信用 SF
     */
    public static final int RecvSF = 0x0e6;
    /*
     * 通信で使用する SN の生成用
     */
    private static int serialNumber = 1;

    private static Object lockObject = new Object();
    /*
     * 送受信生の全データ
     */
    private byte[] packetData;
    /*
     * データ長
     */
    private byte len;
    /*
     * デバイスアドレス
     */
    private final byte addr = 0x31;
    /*
     * SN
     */
    private byte sn;
    /*
     * MT
     */
    private byte mt;
    /*
     * データ部
     */
    private byte[] data;
    /*
     * Lenの取得
     */
    public int getLEN(){ return ( len & 0x0ff ); }
    /*
     * SNの取得
     */
    public int getSN(){ return ( sn & 0x0ff ); }
    /*
     * MTの取得
     */
    public int getMT(){ return ( mt & 0x0ff ); }
    /*
     * ADDRの取得
     */
    public byte getADDR(){ return ( addr ); }
    /*
     * データの取得
     */
    public byte[] getData(){ return ( data ); }
    /*
     * バイト配列全データの取得
     */
    public byte[] getPacketData(){ return ( packetData ); }
    /*
     * 送信用コンストラクタ
     *
     * byte m       サブコマンド
     * byte[] d     データ 必要ない場合は null
     */
    public NayaxPacket( byte m, byte[] d ){
        // error check and set length
        len = 5;
        if( d != null && d.length > 120 ){
            Log.e( TAG,"data length overflow size == "+ d.length );
            throw new RuntimeException("data length overflow size == "+ d.length );
        }
        else if( d != null ) {
            len += d.length;
        }
        packetData = new byte[ len + 1 ];
        packetData[ 0 ] = SendSF;
        packetData[ 1 ] = (byte) len;
        packetData[ 2 ] = addr;

        // SN　
        // ハートビートと送信コマンド同時に呼び出された場合、serialNumberが被ってしまうことがある
        synchronized (lockObject){
            sn = (byte) serialNumber;
            if( serialNumber == 127 ) {
                serialNumber = 1;
            }
            else {
                serialNumber++;
            }
            packetData[ 3 ] = sn;
        }

        mt = m;
        packetData[ 4 ] = mt;

        if ( len > 5 ) {
            data = d.clone();
            System.arraycopy( d, 0, packetData, 5, d.length );
        }
        else {
            data = new byte[ 0 ];
        }
        // crc
        int crc = 0;
        for( int i = 0; i < len; i++ ){
            crc = ( crc ^ packetData[ i ] );
        }
        packetData[ len ] = (byte) crc;
    }
    /*
     * 受信用コンストラクタ
     *
     * byte[] d     受信データ
     */
    public NayaxPacket( byte[] d ) throws RuntimeException {
        //
        if( d == null || d.length < 6 ){
            Log.e( TAG,"illegal data length" );
            throw new RuntimeException("illegal data length" );
        }
        packetData = d.clone();
        // SF
        int sf = ( packetData[ 0 ] & 0x0ff );
        if( sf != RecvSF ){
            Log.e( TAG, "illegal SF 0xE6 != "+ sf );
            throw new RuntimeException( "illegal header 0xE6 != "+ sf );
        }
        // LEN
        len = packetData[ 1 ];
        int realLen = packetData.length -1;
        if( realLen != len ) {
            Log.e( TAG, "illegal length "+ len +" != "+ realLen );
            throw new RuntimeException( "illegal length "+ len +" != "+ realLen );
        }
        // ADDR
        byte tAddr = packetData[ 2 ];
        if( tAddr != addr ){
            Log.e( TAG, "illegal ADDR "+ addr +" != "+ tAddr );
            throw new RuntimeException( "illegal ADDR "+ addr +" != "+ tAddr );
        }
        // SN
        sn = packetData[ 3 ];
        // MT
        mt = packetData[ 4 ];
        // DATA
        data = new byte[ len -5 ];
        if( len > 5 ){
            System.arraycopy( packetData, 5, data, 0,len - 5 );
        }
        // CRC
        int crc = 0;
        for( int i = 0; i < len; i++ ){
            crc = ( crc ^ packetData[ i ] );
        }
        if( crc != packetData[ len ] ){
            Log.e( TAG, "illegal crc "+ crc +" != "+ packetData[ len ] );
            throw new RuntimeException( "illegal crc "+ crc +" != "+ packetData[ len ] );
        }
    }
    /*
     * デバッグ用　文字列化
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        //
        for( byte b: packetData ){
            sb.append( String.format( "%02x", b ) ).append(" ");
        }
        return ( sb.toString() );
    }
}

