import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

/**
 * Created by xinghe on 2017/4/8.
 */

public class FileChunk {

    public int peerID;
    public int chunkSize;
    public String filename;
    public int fileSize;
    public int exist;
    public Log log;
    public RandomAccessFile raf;
    public BitSet bitField;
    public int[] requestField;
    public int bLength;
    public int count = 0;

    public FileChunk (int peerID, String filename, int exist, int filesize, int chunksize) throws IOException {
        this.peerID = peerID;
        this.filename = "/cise/homes/chilee/Desktop/Network/peer_"+ peerID + "/" + filename;;
        this.exist = exist;
        this.fileSize = filesize;
        this.chunkSize = chunksize;

        File place = new File("/cise/homes/chilee/Desktop/Network/peer_"+ peerID +"/");
        if (! place.exists()) {
            place.mkdirs();
        }

        this.log = new Log(peerID);
        System.out.println(peerID + "/" + exist);
        raf = new RandomAccessFile(this.filename, "rw");
        bLength = Helper.upperNum(filesize, chunksize);
        bitField = new BitSet(bLength);
        requestField = new int[bLength];

        int i;
        if (exist == 1) {
            bitField.set(0, bLength);
            System.out.println("bitset set all bits to be true/" + checkBitFd());
        }
        i = 0;
        while (i < bLength) {
            requestField[i] = 0;
            i++;
        }
    }

    public byte[] bitfdToByte() {
        return bitField.toByteArray();
    }

    public boolean checkBitFd() {
        for (int i = 0; i < bLength; i++)
            if (! bitField.get(i))
                return false;
        return true;
    }

    public int checkBitFdInt() {
        for (int i = 0; i < bLength; i++)
            if (! bitField.get(i))
                return i;
        return - 1;
    }

    public void wrtPiece(byte[] content, int index) throws IOException {
        raf.seek((long) index * chunkSize);
        raf.write(content);
        count++;
        System.out.println(peerID + " Has written total piece number " + count);
    }


    public byte[] getPiece(int index) throws IOException {
        int pLength;
        if (index == bLength - 1)
            pLength = fileSize - (bLength - 1) * chunkSize;
        else
            pLength = chunkSize;
        byte[] buf = new byte[pLength];
        byte[] piece = Helper.intToByte(index);
        raf.seek((long) index * chunkSize);
        raf.read(buf);
        byte[] temp = new byte[buf.length + piece.length];
        int i;
        for (i = 0; i < piece.length; i++)
            temp[i] = piece[i];
        int j = 0;
        for (i = piece.length; i < temp.length; i++)
            temp[i] = buf[j++];
        return temp;
    }


    public boolean getBitFd(int i) {
        return bitField.get(i);
    }

    public void setBitFd(int i, boolean b) {
        bitField.set(i, b);
    }

}
