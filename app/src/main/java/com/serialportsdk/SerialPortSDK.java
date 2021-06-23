package com.serialportsdk;

import android.content.Context;
import android.util.Log;

import com.smn.bean.BCLProtocol;
import com.smn.bean.PaymentBean;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import serialcommon.IMachineService;
import serialcommon.SerialMessage;
import serialcommon.SerialMessageFilter;

@SuppressWarnings("ConstantConditions")
public class SerialPortSDK extends BaseSerialPortSDK {
    private static final String TAG = SerialPortSDK.class.getSimpleName();

    private static final byte VENDOUT_IND = (byte)0xF4;
    private static final byte BX_POLL = 0x56;
    private static final byte BX_5C = (byte) 0x5C;
    public static final byte BX_X5D= 0x5D;

    //払出結果
    private static final int DEFAULT = 6;//未処理　0
    private static final int SUCCESS = 3;//出荷成功 1
    private static final int UNFINISHED = 5;//出荷失敗 5
    private static final int VENDING_REQ_FAILURE = 10;//出荷REQ受付失敗 4

    private boolean isOpenDoor = true;
    private boolean isNoBusiness = true;
    private boolean mIsInit = false;
    private boolean mIsOrders =false;
    private boolean mIsGoodsSelected = false;
    private boolean mIsVending = false;
    private boolean mIsPaying = false;
    private boolean mIsSerialPortOk = false;

    private IMachineService mIMachineService;

    private SerialMessageFilter mSerialMessageFilter = new SerialMessageFilter();
    private byte mCurCab = -1;
    private byte mCurColumn = -1;

    // sub-transaction waiting list
    private ArrayList<PaymentBean> mSubPBWaitingList = new ArrayList<PaymentBean>(5);
    //Is sub-transaction processing
    private boolean mIsSTProcession;

    private ReadThread mReadThread = null;
    private WriteThread mWriteThread = null;

    public boolean isGoodsSelected() {
        return mIsGoodsSelected;
    }

    public void setGoodsSelected(boolean goodsSelected) {
        mIsGoodsSelected = goodsSelected;
    }

    public boolean isPaying() {
        return mIsPaying;
    }

    public void setPaying(boolean paying) {
        mIsPaying = paying;
    }

    private boolean threadflag = false;

    public SerialPortSDK(Context context, String portAdr) {
        super(context);
        threadStart();
    }

    /** 启动写入、读取线程 **/
    public void threadStart() {
        try {
            mReadThread = new ReadThread();
            mReadThread.start();
            mWriteThread = new WriteThread();
            mWriteThread.start();
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException");
        } catch (InvalidParameterException e) {
            Log.d(TAG, "InvalidParameterException");
        }
    }

    private boolean mStatus = false;
    private int mStatusNum = 0;
    private int mPoolingTime = 500;

    private class WriteThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "VpcSerialSDK WriteThread run");
            threadflag = true;
            super.run();
            while (threadflag) {
                try {
                    mPoolingTime = 500;
                    mStatusNum++;
                    Log.d(TAG, "mStatus = " + mStatus);
                    Log.d(TAG, "mIsVending = " + mIsVending);
                    if (!mStatus) {
                        notificationMachine(VMC_STATUS_REQ, new byte[0]);
                        Log.d(TAG, "notification VMC_STATUS_REQ");
                    } else {
                        if (mIsVending) {
                            notificationMachine(VMC_VENDINGRESULT_REQ, new byte[0]);
                            Log.d(TAG, "notification VMC_VENDINGRESULT_REQ");
                            //VMC_VENDINGRESULT_REQの後に1000ms待つのが正解
                                    mPoolingTime = 1000;
                        } else {
                            SerialMessage serialMessage = getSerialMessageFromList();
                            if (serialMessage == null) {
                                notificationMachine(VMC_STATUS_REQ, new byte[0]);
                                Log.d(TAG, "notification VMC_STATUS_REQ");
                            } else {
                                removeSerialMessageFromList();
                                if (serialMessage.getCommand() == VMC_VENDING_REQ) {
                                    //VMC_VENDINGRESULT_REQの後に1000ms待つのが正解
//                                    mPoolingTime = 1000;
                                }
                                if(serialMessage.getCommand() == VMC_RESET_REQ){
                                    //本来はACkがなければ8秒毎にVMC_RESET_REQを送信する
                                    //GCC_RESET_ACKを受信したら10秒待機しVMC_STATUS_REQ送信し状態確認をする
                                    //Testなので1回だけ送信
                                    mPoolingTime = 10000;
                                }
                                notificationMachine(serialMessage.getCommand(), serialMessage.getData());
                                Log.d(TAG, "notification " + serialMessage.getCommand());
                            }
                        }
                    }
                    sleep(mPoolingTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e( TAG, "GCC Serial WriteThread Exception " + e);
                    return;
                }
            }
        }
    }

    private class ReadThread extends Thread {
        private int mNumber = 0;
        private final int MAXNUB = 258;
        private byte[] mBuffer = new byte[MAXNUB];
        private int mDataLength = 0;

        @Override
        public void run() {
            Log.d(TAG, "VpcSerialSDK ReadThread run");
            threadflag = true;
            super.run();
            while (threadflag) {
                try {
                    if (mInputStream.available() <= 0) {
                        continue;
                    }
                    if (mNumber > MAXNUB) {
                        clearBuffer();
                    }

                    byte buffer;
                    buffer = (byte) mInputStream.read();

                    if (mBuffer[0] == 0) {
                        if (buffer == IN_SF) {
                            mBuffer[mNumber] = buffer;
                            mNumber++;
                        }
                    } else {
                        mBuffer[mNumber] = buffer;
                        if (mNumber == 1) {
                            if (buffer < 5 || buffer > 255) {
                                clearBuffer();
                                continue;
                            } else {
                                mDataLength = buffer & 0xff;
                            }
                        }
                        if (mNumber == 3) {
                            //バージョン:テスト機は0x36返ってくる
                            Log.d(TAG, "VmcGccプロトコル Ver = " + buffer);
                            if (buffer != 0x36) {
//                                Log.d(TAG, "clearBuffer = " + buffer);
                                clearBuffer();
                                continue;
                            }
                        }
                        if (mNumber == (mDataLength + 1)) {
                            byte[] bytes = new byte[mDataLength];
                            for (int i = 0; i < mDataLength; i++) {
                                bytes[i] = mBuffer[i];
                            }
                            short crc = CrcCheck(bytes, bytes.length);
                            if (((((crc>>8)&0xff) == (mBuffer[mDataLength]&0xff))
                                    && ((crc&0xff) == (mBuffer[mDataLength+1]&0xff)))
                                    || mBuffer[4] == 0x00) {
                                byte command = mBuffer[4];
                                byte[] datas;
                                if (mDataLength > 5) {
                                    int datasLen = mDataLength - 5;
                                    datas = new byte[datasLen];
                                    for (int i = 0; i < datasLen; i++) {
                                        datas[i] = mBuffer[5 + i];
                                    }
                                } else {
                                    datas = new byte[0];
                                }
                                Log.d(TAG, "SF:" + mBuffer[0] + " , LEN:" + mBuffer[1] +" , ADDR:" + mBuffer[2] +" , Ver:" + mBuffer[3] +" , MT:" + mBuffer[4]);
                                dataParse(command, datas);
                            }
                            clearBuffer();
                            continue;
                        }
                        mNumber++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void clearBuffer() {
            for (int i = 0; i < MAXNUB; i++) {
                mBuffer[i] = 0;
            }
            mNumber = 0;
            mDataLength = 0;
        }
    }

    /**
     * 接收消息解析
     * @param command 命令字
     * @param datas 数据
     */
    protected void dataParse(byte command, byte[] datas) {
        if (!isPorkOK()) {
            mIsSerialPortOk = true;
            // 串口通信正常
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.serialInitResult(true);
                }
            });
        }

        mStatusNum = 0;

        switch (command) {
            case VMC_STATUS_RPT:// 全体のステータス報告
                Log.d(TAG, "GCC_STATUS_RPT = " + datas[8] + datas[7] + datas[6] + datas[5] + datas[4] + datas[3] + datas[2] + datas[1] + datas[0]);
                //ドアerrが出るので0にしている
                datas[8]=0;
                datas[5]=0;
                if (datas[1] == 0x00 && datas[2] == 0x00 && datas[3] == 0x00
                        && datas[4] == 0x00 && datas[4] == 0x00 && datas[5] == 0x00
                        && datas[6] == 0x00 && datas[7] == 0x00) {
                    if (datas[0] == 0x00 && datas[8] == 0x00) {
                        mStatus = true;
                    } else {
                        mStatus = false;
                    }
                } else {
                    mStatus = false;
                    mStatusNum = 10;
                }
                break;
            case VMC_VENDING_ACK:  //出荷注文受信、出荷開始
                Log.d(TAG, "VMC_VENDING_ACK = " + datas[0] + datas[1]);
                if (datas[0] == 0x00) {
                    mIsVending = true;
                } else {
                    if (datas[1] == 0x01) {
                        //出荷REQ受付失敗
                        reset(VENDING_REQ_FAILURE);
                    } else {
                        reset(UNFINISHED);//出荷失敗
                    }
                }
                break;
            case VMC_VENDINGRESULT_RPT://出荷状況報告
                Log.d(TAG, "VMC_VENDINGRESULT_RPT = " + datas[0] + datas[1] + datas[2]);
                if (datas[0] == 0x00) {
                    if (datas[1] == 0x00) {
                        //出荷成功 VMC_VENDINGRESULT_RPT出荷リクエスト出し続けているのでresetしないといけない
                        reset(SUCCESS );
                        mIsVending = false;//出荷リクエストフラグ?
                    } else {
                        reset(UNFINISHED );
                        //出荷失敗
                        //モータ回った後に配送操作中のままになる。出荷状態要求を
                        //出し続けて出荷失敗在庫エラーになるのでmIsVendingを無理やりfalseにする
                        mIsVending = false;
                    }
                }
                break;
            case VMC_RESET_ACK:
                Log.d(TAG, "VMC_RESET_ACK 受信 ");
                reset(14);
                break;
            default:
                Log.d(TAG, "dataParse() Other ");
                //RESET_ACKの100ms後に0xb1(-79)が返ってきている。不明
                break;
        }
    }

    /**
     * 指示VMC选货
     *
     * @param mCurCab 当前柜号
     * @param mCurColumn 当前货道号
     */
    private void goodSelectCtrl(byte mCurCab, byte mCurColumn) {
        mIsInit = true;
        this.mCurCab = mCurCab;
        this.mCurColumn = mCurColumn;

        if (mIsInit && !mIsOrders) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.serialgoodSelected();
                }
            });
        }
    }

    /**
     * 卡更新应答(请求VMC出货)
     * 开门 | 暂停营业 状态不能进行出货操作
     * @param bclProtocol BCL协议卡更新数据
     * @return int
     */
    public int cardUpdate(BCLProtocol bclProtocol) {
        Log.d(TAG, "cardUpdate Start mIsVending = "+mIsVending+"; mIsGoodsSelected = "+mIsGoodsSelected);
        if (mIsVending || !mIsGoodsSelected) return 1;

        SerialMessage message = new SerialMessage();
        message.setCommand(VMC_VENDING_REQ);

        byte[] datas = new byte[2];

        datas[0] = (byte) ((mCurColumn/10)+1);
        datas[1] = (byte) (mCurColumn%10);

        message.setCommand(VMC_VENDING_REQ);
        message.setData(datas);
        message.setNeedResult(false);
        setSerialMessage(message);
        return 0;
    }
    public int deliver(int sNum) {
        Log.d(TAG, "laneId = " + sNum);
        // if (mIsVending || !mIsGoodsSelected) return 1;

        SerialMessage message = new SerialMessage();
//        message.setCommand(VMC_VENDING_REQ);

        byte[] datas = new byte[2];

        datas[0] = (byte) (sNum /100);
        datas[1] = (byte) (sNum %100);

        message.setCommand(VMC_VENDING_REQ);
        message.setData(datas);
        message.setNeedResult(false);
        setSerialMessage(message);
        Log.d(TAG, "datas0 datas1 = " + datas[0] + datas[1]);
        Log.d(TAG, "cu message = " + message);
        return 0;
    }
    //door led 制御
    public void ctrDoorLed(int doorled){
        //DOOROPEN:1,DOORCLOSE:2,LEDOPEN:3,LEDCLOSE:4
        SerialMessage message = new SerialMessage();
        byte[] datas = new byte[1];
        datas[0] = 0;
        switch (doorled) {

            case 1:
                Log.d(TAG, "DOOR OPEN");
                message.setCommand(VMC_OPENDOOR_REQ);
                message.setData(datas);
                message.setNeedResult(false);
                setSerialMessage(message);
                break;
            case 2:
                Log.d(TAG, "DOOR CLOSE");
                message.setCommand(VMC_CLOSEDOOR_REQ);
                message.setData(datas);
                message.setNeedResult(false);
                setSerialMessage(message);

                break;
            case 3:
                Log.d(TAG, "LED OPEN");
                message.setCommand(VMC_OPENLIGHT_REQ);
                message.setData(datas);
                message.setNeedResult(false);
                setSerialMessage(message);

                break;
            case 4:
                Log.d(TAG, "LED CLOSE");
                message.setCommand(VMC_CLOSELIGHT_REQ);
                message.setData(datas);
                message.setNeedResult(false);
                setSerialMessage(message);

                break;
            case 5:
                Log.d(TAG, "RESET");
                mIsVending = false;
                message.setCommand(VMC_RESET_REQ);
                message.setData(datas);
                message.setNeedResult(false);
                setSerialMessage(message);

                break;
            default:
                Log.d(TAG, "ctrDoorLed ERR");

                break;
        }
    }

    /**
     * 卡更新失败应答(扣款失败)
     *
     * @param flag 失败代码
     * @return int
     */
    public int cardDel(int flag) {
        Log.d(TAG, "cardDel Start");
        cancelRequest();
        return 0;
    }

    private void cancelRequest() {
        if (!mIsVending && mIsInit) {
            //出荷REQ受付失敗
            reset(4);
        }
    }

    /**
     * 支付准备Ng
     */
    public void payNgPrepare() {
        Log.d(TAG, "pay Prepare NG");
        cancelRequest();
        reset(0);
    }

    public void resetStatus(boolean resetBuf){
        Log.d(TAG, "resetStatus");
        mIsInit = false;
        mIsOrders = false;
        mIsGoodsSelected = false;
        mIsPaying = false;
        mIsVending = false;
        mCurCab = -1;
        mCurColumn = -1;
    }

    public void vendoutRequest(PaymentBean paymentBean) {
        if (null == paymentBean) {
            return;
        }
        int cabinetNo = Integer.valueOf(paymentBean.getCabinetNoStr());
        int cartNo = Integer.valueOf(paymentBean.getCartNoStr());
        Log.d(TAG, "vendoutRequest cabinetNo: " + cabinetNo + "; cartNo: "+cartNo);
        if (mIsSTProcession) {
            Log.d(TAG, "previous request isn't complete.Add this request to waiting list");
            mSubPBWaitingList.add(paymentBean);
            //wait previous vendout complete
            return;
        } else {
//            mHandlingSubPaymentBean = paymentBean;
        }
        mIsSTProcession = true;
        SerialMessage message = new SerialMessage();
        byte[] vendoutInd = new byte[38];
        //mode
        vendoutInd[0] = 0x02;
        //method
        vendoutInd[1] = 0x02;
        //type
        vendoutInd[2] = 0x31;
        //column
        try {
            vendoutInd[3] = (byte) cartNo;
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return;
        }

        message.setCommand(VENDOUT_IND);
        message.setData(vendoutInd);
        message.setNeedResult(false);
        if (null == mIMachineService) {
            Log.e(TAG, "mIMachineService is null");
        }
        mIMachineService.asyncInvoke(message);
    }

    public void setSelectMode(int flag, int num_hight, int num_low) {
        Log.d(TAG, "setSelectMode column: " + num_low);
        if (flag != 0) {
            if (!mStatus) {
                return;
            }
            if (mIsInit) {
                return;
            }
            removeSerialMessageFromList(VMC_VENDING_REQ);
            removeSerialMessageFromList(VMC_VENDINGRESULT_REQ);
            goodSelectCtrl((byte) num_hight, (byte) num_low);
        }
    }

    public boolean isPorkOK() {
        return mIsSerialPortOk;
    }

    //SerialPortClose追記
    public void serialPortClose(){
        mSerialPort.close();
    }
    //thread stop
    public void threadStop() throws InterruptedException {
        threadflag = false;

        /*Nayaxで止まらない事象があったのでthreadはフラグで止めるやり方にする
        mReadThread.interrupt();
        mWriteThread.interrupt();
        Log.d(TAG,"ReadThread " + mReadThread.isInterrupted());
        Log.d(TAG,"WriteThread " + mWriteThread.isInterrupted());
         */
    }
}
