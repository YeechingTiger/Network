/**
 * Created by xinghe on 2017/4/8.
 */
public class ActMsg {

    public int messageType;
    public byte[] messagePayload;
    public int messageLength;

    public ActMsg(int messageType)
    {
        this.messageType = messageType;
        this.messagePayload = null;
        messageLength = 5;
    }

    public ActMsg(int messageType, byte[] messagePayload)
    {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        messageLength = 1 + messagePayload.length;
    }

    public void plusPayload(int piece)
    {
        byte[] message;
        message = intToByte(piece);
        messagePayload = message;
        messageLength = 1 + messagePayload.length;
    }

    public void plusPayload(byte[] messagePayload)
    {
        this.messagePayload = messagePayload;
        messageLength =1 + messagePayload.length;
    }

    public byte[] msgPack()
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

    public boolean[] rdBitfd()
    {
        int size = messagePayload.length*8;
        boolean[] bitField = new boolean[size];
        boolean[] result = new boolean[size];
        int i = 0;
        while (i < size) {
            int sign = ((int)messagePayload[i / 8] >> (i % 8)) & 0x1;
            bitField[i] = sign == 1;
            i++;
        }
        int j = 0;
        i = bitField.length - 1;
        while (i >= 0) {
            result[j++] = bitField[i];
            i--;
        }
        return result;
    }
	public byte[] intToByte(int num)
	{
		byte[] message = new byte[4];
		message[0]=(byte)(num>>24);
		message[1]=(byte)(num>>16);
		message[2]=(byte)(num>>8);
		message[3]=(byte)(num);
		return message;
	}
	
	public int byteToInt(byte[] temp)
	{
		int val=0, i=0;
		for(i=0;i<temp.length;i++){
			int shift= (4 - 1 - i) * 8;
            val +=(temp[i] & 0x000000FF) << shift;
		}
		return val;	
	}
}

	
