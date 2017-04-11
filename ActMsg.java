/**
 * Created by xinghe on 2017/4/8.
 */
public class ActMsg {

    int messageType;
    byte[] messagePayload;
    private int messageLength;

    ActMsg(int messageType)
    {
        this.messageType = messageType;
        this.messagePayload = null;
        messageLength = 5;
    }

    ActMsg(int messageType, byte[] messagePayload)
    {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        messageLength = 1 + messagePayload.length;
    }

    void plusPayload(int piece)
    {
        byte[] message;
        message = Helper.intToByte(piece);
        messagePayload = message;
        messageLength = 1 + messagePayload.length;
    }

    void plusPayload(byte[] messagePayload)
    {
        this.messagePayload = messagePayload;
        messageLength =1 + messagePayload.length;
    }

    byte[] msgPack()
    {
        byte[] message = new byte[messageLength+4];
        message[0] = (byte)(messageLength>>24);
        message[1] = (byte)(messageLength>>16);
        message[2] = (byte)(messageLength>>8);
        message[3] = (byte)(messageLength);
        message[4] = (byte)messageType;
        int i;
        int j = 0;
        if(messagePayload != null)
        {
            i = 5;
            while (i < messageLength + 4) {
                message[i] = messagePayload[j++];
                i++;
            }
        }
        return message;
    }

}

	
