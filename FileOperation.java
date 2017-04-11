import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by xinghe on 2017/3/8.Project for Computer Networks Course.
 */
class FileOperation {
    private InputStream inputStream;
    private OutputStream outputStream;
    FileOperation(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    synchronized void sendFile(ActMsg actMsg) throws IOException {
        byte[] msg = actMsg.msgPack();
        outputStream.write(msg);
        outputStream.flush();
    }

    synchronized private void sendFile(HandShakeMessage handShakeMessage) throws IOException{
        byte[] msg = handShakeMessage.handShakeMessageToByte();
        outputStream.write(msg);
        outputStream.flush();
    }

    synchronized void sendFile(int code) throws IOException{
        ActMsg actMsg = new ActMsg(code);
        byte[] msg = actMsg.msgPack();
        outputStream.write(msg);
        outputStream.flush();
    }

    synchronized byte[] receiveFile() throws IOException{
        byte[] length = new byte[4];
        byte[] outA;
        int rev, total=0;
        while(true)
        {
            if ((total < 4)) {
                rev = inputStream.read (length, total, 4 - total);
                total = total + rev;
            } else {
                break;
            }
        }
        int fileLength = Helper.byteToInt(length);
        outA = new byte[fileLength];
        total=0;

        while(total<fileLength)
        {
            rev = inputStream.read(outA, total, fileLength-total);
            total = total + rev;
        }
        byte[] out = new byte[length.length+outA.length];
        System.arraycopy(length, 0, out, 0, 4);
        System.arraycopy(outA, 0, out, 4, 4 + outA.length - 4);
        return out;
    }

    synchronized void handShake(int id, FileChunk fileChunk, int targetId) throws IOException {
        HandShakeMessage handshake = new HandShakeMessage (id);
        sendFile(handshake);
        fileChunk.log.cnct (targetId);
        byte[] rev = new byte[32];
        inputStream.read(rev, 0, 32);
        HandShakeMessage revMsg = new HandShakeMessage (rev);
        if (revMsg.handShake_Header.equals ("P2PFILESHARINGPROJ"))
            if (revMsg.peerId == targetId) {
                fileChunk.log.cnted (targetId);
            }
    }
}
