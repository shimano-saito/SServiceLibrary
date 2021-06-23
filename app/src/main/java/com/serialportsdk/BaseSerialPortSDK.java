package com.serialportsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android_serialport_api.SerialPort;
import serialcommon.SerialMessage;

public class BaseSerialPortSDK {

    private static final String TAG = BaseSerialPortSDK.class.getSimpleName();

    protected Context mContext;
    protected SerialPortCallback mCallback;
    protected Handler mMainHandler = new Handler(Looper.getMainLooper());
    protected List<SerialMessage> mSerialMessageList = new ArrayList<SerialMessage>();

    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream = null;
    protected InputStream mInputStream = null;

    protected byte OUT_SF = (byte) 0xC7;
    protected byte IN_SF = (byte) 0xC8;
    protected byte ADDR = 0x40;
    protected byte VER = 0x00;

    // VMC-安卓中控 GCC-售货机
    // VMC -> GCC
    protected final byte VMC_RESET_REQ = (byte) 0x80;
    protected final byte VMC_STATUS_REQ = (byte) 0x81;
    protected final byte VMC_VENDING_REQ = (byte) 0x82;
    protected final byte VMC_VENDINGRESULT_REQ = (byte) 0x83;
    protected final byte VMC_OPENDOOR_REQ = (byte) 0x84;
    protected final byte VMC_CLOSEDOOR_REQ = (byte) 0x85;
    protected final byte VMC_OPENLIGHT_REQ = (byte) 0x86;
    protected final byte VMC_CLOSELIGHT_REQ = (byte) 0x87;
    protected final byte VMC_GETINFO_REQ = (byte) 0x88;
    // GCC -> VMC
    protected final byte VMC_RESET_ACK = (byte) 0x00;
    protected final byte VMC_STATUS_RPT = (byte) 0x01;
    protected final byte VMC_VENDING_ACK = (byte) 0x02;
    protected final byte VMC_VENDINGRESULT_RPT = (byte) 0x03;
    protected final byte VMC_OPENDOOR_RPT = (byte) 0x04;
    protected final byte VMC_CLOSEDOOR_RPT = (byte) 0x05;
    protected final byte VMC_OPENLIGHT_RPT = (byte) 0x06;
    protected final byte VMC_CLOSELIGHT_RPT = (byte) 0x07;
    protected final byte VMC_GETINFO_RPT = (byte) 0x08;

    public void setCallback(SerialPortCallback callback) {
        if (null == callback) {
            Log.e(TAG, "callback null");
            return;
        }
        mCallback = callback;
    }

    public BaseSerialPortSDK(Context context) {
        Log.d(TAG,"bcl debug core-serial type is JP");
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
     * @return
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    public SerialPort getSerialPort() throws SecurityException, IOException,
            InvalidParameterException {
        if (mSerialPort == null) {
            String path = new String();
            int baudrate = 0;
            if ((path.length() == 0) || (baudrate == -1)) {
                path = SerialPortConst.PORTTRADE;
                baudrate = SerialPortConst.DEFINE_BAUDRATE;
            }
            mSerialPort = new SerialPort(new File(path), baudrate, SerialPortConst.DEFINE_PARITY);
        }
        return mSerialPort;
    }

    protected void reset(final int res) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCallback.serialMachineReset(res);
            }
        });
    }

    protected void serialOther(final Message msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCallback.serialOther(msg);
            }
        });
    }

    /** キューにメッセージ追加 **/
    protected void setSerialMessage(SerialMessage serialMessage) {
        if (serialMessage != null) {
            mSerialMessageList.add(serialMessage);
        }
    }

    /** キュースタックの最上位データを取得 **/
    protected SerialMessage getSerialMessageFromList() {
        if (mSerialMessageList.size() != 0) {
            return mSerialMessageList.get(0);
        }
        return null;
    }

    /** キュースタックの最上位データを削除 **/
    protected void removeSerialMessageFromList() {
        if (mSerialMessageList.size() != 0) {
            mSerialMessageList.remove(0);
        }
    }

    /** キュー内の指定されたコマンドワードのすべてのデータを削除 **/
    protected void removeSerialMessageFromList(byte command) {
        Iterator<SerialMessage> iterator = mSerialMessageList.iterator();
        while (iterator.hasNext()) {
            SerialMessage serialMessage = iterator.next();
            if (serialMessage != null && serialMessage.getCommand() == command) {
                iterator.remove();
            }
        }
    }

    protected void notificationMachine(byte command, byte[] datas) {
        int dataSize = datas.length + 7;
        byte[] bytes = new byte[dataSize];
        bytes[0] = OUT_SF;
        bytes[1] = (byte) (dataSize - 2);
        bytes[2] = ADDR;
        bytes[3] = VER;
        bytes[4] = command;
        for (int i = 0; i < datas.length; i++) {
            bytes[i + 5] = datas[i];
        }
        byte[] bytesCrc = new byte[dataSize - 2];
        for (int i = 0; i < (bytes.length - 2); i++) {
            bytesCrc[i] = bytes[i];
        }
        short crc = CrcCheck(bytesCrc, bytesCrc.length);
        bytes[bytes.length - 2] = (byte) ((crc>>8)&0xff);
        bytes[bytes.length - 1] = (byte) (crc&0xff);
        onDataOutput(bytes);
    }

    private void onDataOutput(final byte[] buffer) {
        try {
            mOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * CRC
     */
    protected short CrcCheck(byte[] bytes, int len) {
        short i, j;
        short crc = 0;
        short current;
        short tmp;

        for (i = 0; i < len; i++)
        {
            tmp = bytes[i];
            current = (short)(tmp << 8);
            for (j = 0; j < 8; j++)
            {
                tmp = (short)(crc ^ current);
                if (tmp < 0)
                    crc = (short)(((short)(crc << 1)) ^ 0x1021);
                else
                    crc <<= 1;
                current <<= 1;
            }
        }

        return crc;
    }

}
