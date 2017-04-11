import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Created by xinghe on 2017/4/8.
 */

class FileChunk {

    private int chunkSize;
    private int fileSize;
    Log log;
    private RandomAccessFile randomAccessFile;
    private BitSet bitField;
    int[] requestField;
    private int bitSetLength;
    int count = 0;

    FileChunk(int peerID, String fileName, int exist, int fileSize, int chunkSize) throws IOException {
        String fileName1 = "/cise/homes/chilee/Desktop/Network/peer_" + peerID + "/" + fileName;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        bitSetLength = Helper.upperNum(fileSize, chunkSize);
        File place = new File("/cise/homes/chilee/Desktop/Network/peer_"+ peerID +"/");
        if (! place.exists()) place.mkdirs();

        this.log = new Log(peerID);
        randomAccessFile = new RandomAccessFile(fileName1, "rw");
        bitField = new BitSet(bitSetLength);
        requestField = new int[bitSetLength];

        int i;
        switch (exist) {
            case 1:
                bitField.set(0, bitSetLength);
                count = bitSetLength;
                break;
            default:
                bitField.set(0, bitSetLength, false);
                count = 0;
                break;
        }
        i = 0;
        if (i < bitSetLength) {
            do {
                requestField[i] = 0;
                i++;
            } while (i < bitSetLength);
        }
    }

    byte[] bitfdToByte() {
        return bitField.toByteArray();
    }

    boolean checkBitFd() {
        return IntStream.range(0, bitSetLength).allMatch(i -> bitField.get(i));
    }

    void wrtPiece(byte[] content, int index) throws IOException {
        randomAccessFile.seek((long) index * chunkSize);
        randomAccessFile.write(content);
        count++;
    }


    byte[] getPiece(int index) throws IOException {
        int trueSize;
        trueSize = index == bitSetLength - 1 ? fileSize - index * chunkSize : chunkSize;
        byte[] payload = new byte[trueSize + 4];
        byte[] chunkNeeded = new byte[trueSize];
        byte[] indexByte = Helper.intToByte(index);
        long readPlace = (long) index*chunkSize;
        randomAccessFile.seek(readPlace);
        randomAccessFile.read(chunkNeeded);
        System.arraycopy(indexByte, 0, payload, 0, 4);
        System.arraycopy(chunkNeeded, 0, payload, 4, payload.length - 4);
        return payload;
    }


    boolean getBitFd(int i) {
        return bitField.get(i);
    }

    void setBitFd(int i) {
        bitField.set(i, true);
    }

}
