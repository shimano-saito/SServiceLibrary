package serialcommon;

public class SerialMessageFilter {

    private byte[] commandList;

    public byte[] getCommandList() {
        return commandList == null ? null : commandList.clone();
    }

    public void setCommandList(byte[] commandList) {
        this.commandList = (commandList == null ? null : commandList.clone());
    }

    public SerialMessageFilter() {}

}
