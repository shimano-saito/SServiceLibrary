package serialcommon;

public class SerialMessage {

    private byte command;
    private byte[] data;
    private byte result;
    private boolean isNeedResult;

    public SerialMessage() {}

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getResult() {
        return result;
    }

    public void setResult(byte result) {
        this.result = result;
    }

    public boolean isNeedResult() {
        return isNeedResult;
    }

    public void setNeedResult(boolean needResult) {
        isNeedResult = needResult;
    }
}
