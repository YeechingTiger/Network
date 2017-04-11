import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by xinghe on 2017/3/8.Project for Computer Networks Course.
 */
public class StateMachine {
    int speed = 0;
    int[] stateArray;
    FileChunk fileChunk;
    private int targetId;
    private FileOperation file;
    private int bitSetLength;
    private peerProcess peerProcess;
    public BitSet targetBitField;

    StateMachine(int[] stateArray, FileChunk fileChunk, int targetId, FileOperation file, int bitSetLength, BitSet targetBitField, peerProcess peerProcess) {
        this.stateArray = stateArray;
        this.fileChunk = fileChunk;
        this.targetId = targetId;
        this.file = file;
        this.bitSetLength = bitSetLength;
        this.targetBitField = targetBitField;
        this.peerProcess = peerProcess;
    }


    void operationChoosing(ActMsg msg) throws IOException {
        if (msg.messageType == 0) {
            fileChunk.log.chokingLog(targetId);
            stateArray[1] = 1;
        } else if (msg.messageType == 1) {
            unchokeProcess();

        } else if (msg.messageType == 2) {
            interestProcess(true);
            fileChunk.log.interestedReiceived(targetId);

        } else if (msg.messageType == 3) {
            interestProcess(false);
            fileChunk.log.notInterestedReceived(targetId);

        } else if (msg.messageType == 4) {
            haveProcess(msg);

        } else if (msg.messageType == 5) {
            bitFieldProcess(msg);

        } else if (msg.messageType == 6) {
            requestProcess(msg);

        } else if (msg.messageType == 7) {
            pieceProcess(msg);
        }
    }

    private void unchokeProcess() throws IOException
    {
        int rpiece;
        while(true)
        {
            rpiece = getPiece(targetBitField);
            if(rpiece == -1)
                break;
            else if(fileChunk.requestField[rpiece] == 0)
                break;
        }

        if (rpiece == -1)
        {
            file.sendFile(3);
        }
        else
        {
            ActMsg m=new ActMsg(6);
            m.plusPayload(rpiece);
            stateArray[6] =rpiece;
            file.sendFile(m);
            stateArray[2]= 1;
            stateArray[1] = -1;
            fileChunk.requestField[rpiece]=1;
        }
        fileChunk.log.unchokingLog(targetId);
    }

    private void interestProcess(boolean flag)
    {
        if (flag)
            stateArray[3] = 1;
        else
            stateArray[3] = -1;
    }


    private void haveProcess(ActMsg m) throws IOException
    {
        int pieceNum=Helper.byteToInt(m.messagePayload);
        fileChunk.log.haveReceived(targetId, pieceNum);
        targetBitField.flip(pieceNum);
        if (fileChunk.getBitFd (pieceNum)) {
            file.sendFile(3);
        } else {
            file.sendFile(2);
        }

    }

    private void requestProcess(ActMsg m)throws IOException
    {
        int pieceNumber = Helper.byteToInt(m.messagePayload);
        if(stateArray[0] !=1 && !targetBitField.get (pieceNumber))
        {
            ActMsg ms = new ActMsg(7);
            ms.plusPayload(fileChunk.getPiece(pieceNumber));
            file.sendFile(ms);
        }
    }

    private void pieceProcess(ActMsg m) throws IOException
    {
        byte[] fileIndex= new byte[4];
        byte[] fileContent= new byte[m.messagePayload.length-4];
        int i,index;
        i = 0;
        while (i < 4) {
            fileIndex[i]=m.messagePayload[i];
            i++;
        }
        index = Helper.byteToInt(fileIndex);
        if(!fileChunk.getBitFd (index))
        {
            int j;
            for(j=0, i=4;i<m.messagePayload.length;i++)
                fileContent[j++]=m.messagePayload[i];
            fileChunk.wrtPiece(fileContent, index);
            fileChunk.setBitFd(index);
            fileChunk.log.pieceDownloadLog(targetId, index, fileChunk.count);
            peerProcess.shareInfo(index);
            stateArray[6] = -1;
            stateArray[2]= -1;
            fileChunk.requestField[index]=2;
        }

        if(stateArray[2]!=1)
        {
            int piece;
            while(true)
            {
                piece = getPiece(targetBitField);
                if (piece == -1 || fileChunk.requestField[piece] == 0) break;
            }
            stateArray[6] = piece;
            if (piece == -1) {
                file.sendFile(m);
                stateArray[2] = -1;
            } else {
                ActMsg ms = new ActMsg(6);
                ms.plusPayload(piece);
                file.sendFile(ms);
                stateArray[2] = 1;
                fileChunk.requestField[piece]=1;
            }
        }
        addSpeed();
    }



    private void bitFieldProcess(ActMsg actMsg) throws IOException
    {
        targetBitField = rdBitfd(actMsg.messagePayload);
        int index = getPiece(targetBitField);
        if (index < 0) {
            file.sendFile(3);
        } else {
            file.sendFile(2);
        }
    }

    private BitSet rdBitfd(byte[] payload)
    {
        return BitSet.valueOf(payload);
    }



    private int getPiece(BitSet bitField)
    {
        int base = new Random().nextInt(bitSetLength);
        int i;
        for(i = 0; i < bitSetLength; i++)
            if(bitField.get ((base + i) % bitSetLength) && !fileChunk.getBitFd ((base + i) % bitSetLength))
                return (base+i)% bitSetLength;
        return -1;
    }

    private synchronized void addSpeed()
    {
        speed = speed++;
    }

}
