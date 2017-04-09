/**
 * Created by xinghe on 2017/4/8.
 */
public class HandShakeMessage {
    String handShake_Header;
    int peerId;
    private String handShakeMessage;

    //Send a handshake message

    public HandShakeMessage (int peerId) {
        handShake_Header = "P2PFILESHARINGPROJ";
        this.peerId = peerId;
        handShakeMessage = handShake_Header + "0000000000" + peerId;
    }

    //Receive a handshake message and process it to get peerid and header

    public HandShakeMessage (byte[] receivedHandSM) {
        String stringmessage = new String (receivedHandSM);
        StringBuffer stringBuffer = new StringBuffer (stringmessage);
        handShake_Header = stringBuffer.substring (0, 18);
        peerId = Integer.parseInt (stringBuffer.substring (28, 32));
    }

    //Convert a handShake message to byte to send

    public byte[] handShakeMessageToByte () {
        byte[] message = new byte[ 32 ];
        message = handShakeMessage.getBytes ();
        return message;
    }
}