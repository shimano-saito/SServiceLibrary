package com.Nayax;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android_serialport_api.SerialPort;

/*
 * VPOS ( MDB ) とのやりとりをするクラス
 */
public class NayaxSerialPort {
    private static final String TAG = NayaxSerialPort.class.getSimpleName();
    /*
     * 画面のコンテキスト
     */
    private Context mContext;
    /*
     * コールバック
     */
    private NayaxSerialPortCallback mCallback;
    /*
     * コールバック用ハンドラー
     */
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    /*
     * 送信コマンドバッファ
     */
    private final List<NayaxPacket> mSerialMessageList = new ArrayList<NayaxPacket>();
    /*
     * シリアルポート
     */
    private SerialPort mSerialPort = null;
    /*
     * 送信用ストリーム
     */
    private OutputStream mOutputStream = null;
    /*
     * 受信用ストリーム
     */
    private InputStream mInputStream = null;
    /*
     * 送受信用スレッド
     */
    private WriteThread writeThread = null;

    private boolean threadflag = false;


    /*
     * コンストラクタ
     *
     * @param   Context context      コールバック登録するコンテキスト
     */
    public NayaxSerialPort( Context context ) {
        Log.d(TAG,"Nayax debug core-serial type is JP");
        this.mContext = context;
        if (mSerialPort == null) {
            try {
                mSerialPort = getSerialPort();
            } catch (InvalidParameterException e) {
                Log.d(TAG, "InvalidParameterException");
            } catch (SecurityException e) {
                Log.d(TAG, "SecurityException");
            } catch (IOException e) {
                Log.d(TAG, "IOException");
            }
        }
        try {
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException");
        } catch (InvalidParameterException e) {
            Log.d(TAG, "InvalidParameterException");
        }

    }
    /**
     * open SerialPort
     *
     * @return シリアルポートオブジェクト
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            mSerialPort = new SerialPort(new File( NayaxSerialPortConst.PORTTRADE ), NayaxSerialPortConst.DEFINE_BAUDRATE, NayaxSerialPortConst.DEFINE_PARITY);
        }
        return mSerialPort;
    }
    /*
     * コールバックの設定
     */
    public void setCallback( NayaxSerialPortCallback callback ) {
        if ( null == callback ) {
            Log.e( TAG, "callback null" );
            return;
        }
        mCallback = callback;
    }
    /*
     * スレッドの開始
     */
    public void threadStart() {
        try {
            writeThread = new WriteThread();
            writeThread.start();
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException");
        } catch (InvalidParameterException e) {
            Log.d(TAG, "InvalidParameterException");
        }
    }
    /*
     * 実際の通信スレッド
     */
    private class WriteThread extends Thread {
        private final int MAXNUB = 512;
        private final byte[] mBuffer = new byte[ MAXNUB ];
        private int mDataLength = 0;
        private int mIndex = 0;

        @Override
        public void run() {
            threadflag = true;
            Log.d(TAG, "VpcSerialSDK ReadThread run");
            super.run();
            while ( threadflag ) {
                try {
                    sleep( 1000 );
                    NayaxPacket send = null;
                    if( mSerialMessageList.size() > 0 ){
                        // 送信データ有
                        send = mSerialMessageList.get( 0 );
                        mSerialMessageList.remove( 0 );
                    }
                    else {
                        // データがないのでハートビート
                        send = new NayaxPacket( (byte) 0x21, new byte[]{ 0x01 } );
                    }
                    trancateInsupStream();
                    clearBuffer();
                    Log.i( TAG, "Nayax send -> "+ send.toString() );
                    mOutputStream.write( send.getPacketData() );
                    readAndGo( send );
                    //
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e( TAG, "WriteThread IOException " + e);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e( TAG, "Exception IOException " + e);
                    return;
                }
            }
        }
        /*
         * 受信データの解析といろいろ
         *
         * @param   NayaxPacket send 送信データ 整合性のチェックに使用する
         */
        private void readAndGo( NayaxPacket send ){
            while ( threadflag ) {
                try {
                    if ( mInputStream.available() <= 0 ) {
                        // タイムアウトを考慮すべし
                        sleep( 1 );
                        continue;
                    }
                    // SF
                    int readed = mInputStream.read();
                    switch ( mIndex ){
                        case 0:
                            // SF
                            if( readed != NayaxPacket.RecvSF ){
                                // だめ
                                Log.e( TAG, "illegal recieve SF -> "+ readed );
                            }
                            mBuffer[ mIndex++ ] = (byte) readed;
                            break;
                        case 1:
                            // LEN
                            if( readed < 5 || 127 < readed ) {
                                // おかしい
                                clearBuffer();
                                Log.e( TAG, "illegal LEN -> "+ readed );
                                continue;
                            }
                            mBuffer[ mIndex++ ] = (byte) readed;
                            mDataLength = ( readed + 1 );
                            break;
                        default:
                            // ADDR SN MT DATA CRC
                            mBuffer[ mIndex++ ] = (byte) readed;
                            //
                            if( mIndex == mDataLength ){
                                // 終了
                                try {
                                    Parse( send, new NayaxPacket( Arrays.copyOfRange( mBuffer, 0, mDataLength ) ) );
                                }
                                catch ( Exception e ) {
                                    e.printStackTrace();
                                }
                                clearBuffer();
                                return;
                            }
                            break;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    Log.e( TAG, "readAndGo IOException " + e);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e( TAG, "readAndGo Exception " + e);
                }
            }
        }
        /*
         * ハートビートの状態保持
         * 　こいつと比較して差分があったらイベントするのだ
         */
        private byte[] heartBreatCondition;
        /*
         * パースの実態
         *
         * @param   NayaxPacket send    送信データ 整合性のチェックに使用する
         * @param   NayaxPacket recv    受信データ
         * @throw   RuntimeException    受信データエラー
         */
        private void Parse( final NayaxPacket send, final NayaxPacket recv ){
            Log.i( TAG, "Nayax recv -> "+ recv.toString() );
            // エラーチェックいる ??
            int sendMT = send.getMT();
            int recvMT = recv.getMT();
            if( recvMT - sendMT != 0x70 ){
                // あれれ
                throw new RuntimeException( "missing recv command "+ ( sendMT + 0x070 ) +" -> "+ recvMT );
            }
            if ( recvMT == 0x91 && recv.getData()[ 0 ] == 0x01 ){
                // ハートビートだ
                byte[] d = recv.getData();
                if( !Arrays.equals( d, heartBreatCondition ) ){
                    // 変化したので通知
                    heartBreatCondition = d.clone();
                    final NayaxResult res = new NayaxResult( recv );
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG,"Status changed.");
                            mCallback.NayaxDeviceConditionChanged( res );
                        }
                    } );
                }
            }
            else {
                //　コマンド実行結果
                final NayaxResult res = new NayaxResult( recv );
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.NayaxCommanExecuted( res );
                    }
                } );
            }
        }
        /*
         * 入力ストリームのゴミデータの処理
         *
         * @throw   IOException
         */
        private void trancateInsupStream() throws IOException {
            while ( mInputStream.available() > 0 ){
                int b = mInputStream.read();
                Log.d( TAG, String.format("drop data %02x", b ) );
            }
        }
        /*
         * 受信バッファのクリア
         */
        private void clearBuffer() {
            for (int i = 0; i < MAXNUB; i++) {
                mBuffer[i] = 0;
            }
            mIndex = 0;
            mDataLength = 0;
        }
    }
    /*
     * MDBボード情報の取得
     */
    public void MdbBoardQuery(){
        mSerialMessageList.add( new NayaxPacket( (byte) 0x21,  new byte[]{ (byte) 0x04 } ) );
    }
    /*
     * カードデバイス情報の取得
     */
    public void CardReaderQuery(){
        mSerialMessageList.add( new NayaxPacket( (byte) 0x21,  new byte[]{ (byte) 0x06 } ) );
    }
    /*
     * 初期化
     */
    public void Init(){
        // 暫定今回はカードリーダーだけ
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  new byte[]{ (byte) 0x01, (byte) 0x04, (byte) 0x02, (byte) 0x02 } ) );
    }
    /*
     * Enable 今はカードリーダーだけ
     */
    public void Enable(){
        // 暫定今回はカードリーダーだけ
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  new byte[]{ (byte) 0x02, (byte) 0x04, (byte) 0x01 } ) );
    }
    /*
     * Disnable 今はカードリーダーだけ
     */
    public void Disable(){
        // 暫定今回はカードリーダーだけ
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  new byte[]{ (byte) 0x02, (byte) 0x04, (byte) 0x02 } ) );
    }
    /*
     * 決済要求
     *
     * @param   byte cab    キャビネット番号
     * @param   byte col    カラム番号
     * @param   int amount  決済額
     */
    public void PaymentRequest( byte cab, byte col, int amount ){
        byte[] d = new byte[ 7 ];
        d[ 0 ] = (byte) 0x0a;
        d[ 1 ] = cab;
        d[ 2 ] = col;
        ByteBuffer bytes = ByteBuffer.allocate( 4 ).putInt( amount );
        d[ 3 ] = bytes.get( 0 );
        d[ 4 ] = bytes.get( 1 );
        d[ 5 ] = bytes.get( 2 );
        d[ 6 ] = bytes.get( 3 );
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  d ) );
    }
    /*
     * 決済状態の取得
     *
     * @return      status
     *              0 アイドル
     *              1 決済中
     *              2 不明
     *              4 払出待ち      こいつを確認出来たら PaymentRequest2()するのだ
     *              8 キャンセル
     */
    public void PaymentStatus(){
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  new byte[]{ (byte) 0x0b } ) );
    }
    /*
     * 払出状態通知
     *
     * @param   byte cab        キャビネット番号
     * @param   byte col        カラム番号
     * @param   byte result     払出結果　　1 成功      他　失敗
     */
    public void PaymentRequest2( byte cab, byte col, byte result ){
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22, new byte[]{ (byte) 0x0c, cab, col, result , 0 } ) );
    }
    /*
     * 決済キャンセル
     */
    // 自販機からはキャンセルしない　Nayaxにまかせる
    public void PaymentCancel(){
        mSerialMessageList.add( new NayaxPacket( (byte) 0x22,  new byte[]{ (byte) 0x0e } ) );
    }

    //SerialPortClose追記
    public void serialPortClose(){
        mSerialPort.close();
    }
    //thread stop
    public void threadStop() throws InterruptedException {
        threadflag = false;
        //
        /*readAndGo() で止まらない事象があったので使用しない
        writeThread.interrupt();
        Log.d(TAG,"interrupt " + writeThread.interrupted());
        Log.d(TAG,"interrupt " + writeThread.isInterrupted());
        */
    }
}