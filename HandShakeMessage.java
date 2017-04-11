/**
 * Created by xinghe on 2017/4/8.
 */
class HandShakeMessage {
    String handShake_Header;
    int peerId;
    private String handShakeMessage;

    //Send a handshake message

    HandShakeMessage(int peerId) {
        handShake_Header = "P2PFILESHARINGPROJ";
        this.peerId = peerId;
        handShakeMessage = handShake_Header + "0000000000" + peerId;
    }

    //Receive a handshake message and process it to get peerid and header

    HandShakeMessage(byte[] receivedHandSM) {
        String stringMessage = new String (receivedHandSM);
        StringBuffer stringBuffer;
        stringBuffer = new StringBuffer (stringMessage);
        handShake_Header = stringBuffer.substring (0, 18);
        peerId = Integer.parseInt (stringBuffer.substring (28, 32));
    }

    //Convert a handShake message to byte to send

    byte[] handShakeMessageToByte() {
        byte[] message;
        message = handShakeMessage.getBytes ();
        return message;
    }
}