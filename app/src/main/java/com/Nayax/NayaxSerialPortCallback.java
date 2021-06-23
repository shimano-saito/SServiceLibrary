package com.Nayax;

/*
 * コールバックの定義
 */
public interface NayaxSerialPortCallback {
    void NayaxDeviceConditionChanged( NayaxResult result );
    void NayaxCommanExecuted( NayaxResult result );
}
