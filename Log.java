import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Date;

/**
 * Created by xinghe on 2017/4/8.
 */
class Log {

    private int mainId;
    private BufferedWriter logFile;
    private String logContent;
    Log(int mainId) throws IOException {
        this.mainId = mainId;
        String logFileName = "/cise/homes/chilee/Desktop/Network/log_peer_" + mainId + ".log";
        logFile = new BufferedWriter(new FileWriter(logFileName));
    }

    synchronized private String getTime() {
        SimpleDateFormat time;
        time = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");
        return time.format(new Date());
    }

    synchronized void cnct(int connectId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] makes a connection to Peer [" + connectId + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void cnted(int connectId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is connected from Peer [" + connectId + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void changeOfPreNbLog(LinkedList prNbList) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has the preferred neighbors [";
        logFile.write(logContent);
        System.out.println(logContent);
        for (int i = 0; i < prNbList.size(); i++) {
            if (i == prNbList.size() - 1)
                logFile.write(prNbList.get(i) + "].\n");
            else
                logFile.write(prNbList.get(i) + ", ");
        }
    }

    synchronized void changeOfOpUnchokeNbLog(int opUnchokeId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has the optimistically neighbor [" + opUnchokeId + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void unchokingLog(int unchokedById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is unchoked by Peer [" + unchokedById + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void chokingLog(int chokedById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is choked by Peer [" + chokedById + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void haveReceived(int haveById, int index) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'have' message from [" + haveById + "] for the piece [" + index + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void interestedReiceived(int receiveById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'interested' message from [" + receiveById + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void notInterestedReceived(int receiveById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'not interested' message from [" + receiveById + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void pieceDownloadLog(int peerId, int index, int pieceNumber) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has downloaded the piece [" + index + "] from [" + peerId + "]. Now the number of pieces it has is [" + pieceNumber + "].\n";
        logFile.write(logContent);
        System.out.println(logContent);
    }

    synchronized void completeLog() throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has downloaded the complete file.\n";
        logFile.write(logContent);
        System.out.println(logContent);
        logFile.close();
    }
}
