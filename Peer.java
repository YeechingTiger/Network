import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Created by xinghe on 2017/4/8.
 */


public class Peer implements Runnable{
    int targetId;
    private int id;
    private BitSet targetBitField;
    private int bitSetLength;
    private FileChunk fileChunk;
    private peerProcess peerProcess;
    InputStream inputStream;
    int[] stateArray;
    private ArrayList<Integer> haveList = new ArrayList <>();
    private FileOperation file;
    private StateMachine stateMachine;
    Peer(int id, int targetId, Socket socket, int fileSize, int chunkSize, FileChunk fileChunk, peerProcess peerProcess) throws IOException
    {
        this.id = id;
        this.targetId = targetId;
        this.fileChunk = fileChunk;
        this.peerProcess = peerProcess;
        bitSetLength = Helper.upperNum(fileSize,chunkSize);
        targetBitField = new BitSet(bitSetLength);
        inputStream = socket.getInputStream ();
        OutputStream outputStream = socket.getOutputStream();
        file = new FileOperation(inputStream, outputStream);
        stateArray = new int[] {0, 0, 0, 0, -1, 0, -1};
        stateMachine = new StateMachine(stateArray, fileChunk, targetId, file, bitSetLength, targetBitField, peerProcess);
    }

    @Override
    public void run() {
        try {
            file.handShake(id, fileChunk, targetId);
            sendBitField();
            do {
                stateChecking();
                if (stateMachine.fileChunk.checkBitFd() && checkBitFd(stateMachine.targetBitField) && stateMachine.stateArray[5] != 1) {
                    Thread.sleep(1000);
                    peerProcess.quit(this);
                    break;
                }

                if (inputStream.available() == 0) {
                    Thread.sleep(50);
                    continue;
                }

                byte[] receiveMessage = file.receiveFile();
                ActMsg msg = Helper.byteToMsg(receiveMessage);
                if (stateArray[1] == 1 && stateArray[6] != -1) {
                    stateArray[2] = -1;
                    fileChunk.requestField[stateArray[6]] = 0;
                    stateArray[6] = -1;
                }

                if (stateArray[6] != -1 && msg.messageType == 0) {
                    stateArray[2] = -1;
                    fileChunk.requestField[stateArray[6]] = 0;
                    stateArray[6] = -1;
                }
                stateMachine.operationChoosing(msg);
            } while (true);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized void choke(boolean flag)
    {
        if (flag) {
            stateArray[0] = 1;
            stateArray[4] = 1;
        }
        else {
            stateArray[0] = -1;
            stateArray[4] = 2;
        }
    }

    synchronized int getSpeed()
    {
        int periodSpeed = stateMachine.speed;
        stateMachine.speed = 0;
        return periodSpeed;
    }



    synchronized void have(int i) throws IOException
    {
        haveList.add(i);
        stateArray[5] = 1;
    }

    private synchronized void stateChecking() throws IOException
    {
        switch (stateArray[4]) {
            case 0:
                break;
            case 1: {
                file.sendFile (0);
                stateArray[4] = 0;
                break;
            }
            case 2: {
                file.sendFile (1);
                stateArray[4] = 0;
                break;
            }
        }
        if (stateArray[5] == 1) {
            if (haveList.size() != 0) {
                do {
                    ActMsg m = new ActMsg(4);
                    m.plusPayload(haveList.get(0));
                    file.sendFile(m);
                    haveList.remove(0);
                } while (haveList.size() != 0);
            }
            stateArray[5] = -1;

        }
    }

    private synchronized boolean checkBitFd(BitSet bitField)
    {
        for(int i=0;i<bitSetLength;i++)
            if(!bitField.get(i))
                return false;
        return true;
    }

    private synchronized void sendBitField() throws IOException {
        byte[] sendBitField = fileChunk.bitfdToByte ();
        ActMsg bitFieldMsg = new ActMsg (5, sendBitField);
        file.sendFile (bitFieldMsg);
    }
}
