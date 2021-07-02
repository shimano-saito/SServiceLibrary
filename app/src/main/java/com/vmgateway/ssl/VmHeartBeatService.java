package com.vmgateway.ssl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.serialportsdk.SerialPortSDK;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VmHeartBeatService extends Service {
    private static final String TAG = "Log:VmHeartBeatService";

    public Threadsend send = null;
    public int count = 0;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");


    }

    /**StartServiceで呼びだれた場合実行*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        send = new Threadsend();
        send.start();

        return START_NOT_STICKY;
    }

    public class Threadsend<Sting> extends Thread{
        private Context context;
        public Threadsend(){
            this.context = context;
        }

        @Override
        public void run() {
            super.run() ;
            while(!isInterrupted()){
                Log.d("i = ", String.valueOf(count));
                count++;
                //時刻取得
                String date = getNowDate();

                String VmId = "Vm0001";
                String data = date + VmId;

                try {
                    //ソケットオープン
                    int port = 49205;
                    DatagramSocket sendUdpSocket = new DatagramSocket();

                    InetAddress IPAddress = InetAddress.getByName("54.178.70.127");

                    byte[] strByte = data.getBytes();
                    System.out.println(data);

                    DatagramPacket sendPacket = new DatagramPacket(strByte, strByte.length, IPAddress,port);
                    sendUdpSocket.send(sendPacket);
                    sendUdpSocket.close();

                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**データ*/
    public static String getNowDate(){
        Locale japan = new Locale("ja","JP","JP");
        final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss",japan);
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }


    /**BindServiceで呼びだれた場合実行*/
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");


        return null;
    }
    @Override
    public boolean onUnbind(Intent intent){
        super.onUnbind(intent);
        Log.d(TAG, "onUnbind()");

        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void finalize() throws Throwable {
        Log.d(TAG, "finalize()");
        super.finalize();
    }
}
