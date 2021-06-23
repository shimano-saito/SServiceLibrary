package com.serialportsdk;

import android.os.Message;

import com.smn.bean.BCLProtocol;

public interface SerialPortCallback {
    void serialInitResult(boolean success);
    void serialgoodSelected();
    void serialgoodPaying(BCLProtocol bclProtocol);
    void serialMachineReset(int res);
    void serialOther(Message msg);
}
