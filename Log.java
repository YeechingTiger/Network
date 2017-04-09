import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by xinghe on 2017/4/8.
 */
public class Log {

    private int mainId;
    private BufferedWriter logFile;
    private String logContent;
    public Log(int mainId) throws IOException {
        this.mainId = mainId;
        String logFileName = "/cise/homes/chilee/Desktop/Network/log_peer_" + mainId + ".log";
        logFile = new BufferedWriter(new FileWriter(logFileName));
    }

    synchronized private String getTime() {
        SimpleDateFormat time;
        time = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");
        return time.format(new Date());
    }

    synchronized public void cnct(int connectId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] makes a connection to Peer [" + connectId + "].\n";
        logFile.write(logContent);
    }

    synchronized public void cnted(int connectId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is connected from Peer [" + connectId + "].\n";
        logFile.write(logContent);
    }

    synchronized public void changeOfPreNbLog(ArrayList prNbList) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has the preferred neighbors [";
        logFile.write(logContent);
        for (int i = 0; i < prNbList.size(); i++) {
            if (i == prNbList.size() - 1)
                logFile.write(prNbList.get(i) + "].\n");
            else
                logFile.write(prNbList.get(i) + ", ");
        }
    }

    synchronized public void changeOfOpUnchokeNbLog(int opUnchokeId) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has the optimistically neighbor [" + opUnchokeId + "].\n";
        logFile.write(logContent);
    }

    synchronized public void unchokingLog(int unchokedById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is unchoked by Peer [" + unchokedById + "].\n";
        logFile.write(logContent);
    }

    synchronized public void chokingLog(int chokedById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] is choked by Peer [" + chokedById + "].\n";
        logFile.write(logContent);
    }

    synchronized public void haveReceived(int haveById, int index) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'have' message from [" + haveById + "] for the piece [" + index + "].\n";
        logFile.write(logContent);
    }

    synchronized public void interestedReiceived(int receiveById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'interested' message from [" + receiveById + "].\n";
        logFile.write(logContent);
    }

    synchronized public void notInterestedReceived(int receiveById) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] received the 'not interested' message from [" + receiveById + "].\n";
        logFile.write(logContent);
    }

    synchronized public void pieceDownloadLog(int peerId, int index) throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has downloaded the piece [" + index + "] from [" + peerId + "].\n";
        logFile.write(logContent);
    }

    synchronized public void completeLog() throws IOException {
        logContent = getTime() + "Peer [" + mainId + "] has downloaded the complete file.\n";
        logFile.write(logContent);
        logFile.close();
    }
}
