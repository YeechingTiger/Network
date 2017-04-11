import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by xing he on 2017/3/8.Project for Computer Networks Course.
 */
public class peerProcess {
    private int numOfPN;
    private int unChokingInterval;
    private int opUnChokingInterval;
    private FileChunk fileChunk;
    private int fileExist;
    private LinkedList <Peer> fileShareChannelList = new LinkedList <> ();
    private Peer op = null;
    private int nTime;
    private int opnTime;
    private boolean aBoolean = false;
    public LinkedList <Peer> tempList = new LinkedList <> ();
    public static void main (String[] args) throws Exception {
        new peerProcess (args[ 0 ]);
    }

    private peerProcess(String ID) throws Exception {

        readFile(ID);
        startThread();

        while ( true ) {

            Thread.sleep (cycleTimer());
            if (checkList()) {
                Thread.sleep (1024);
                fileChunk.log.completeLog();
                break;
            }

            if (nTime < 0.1) {
                if (fileShareChannelList.size () != 0) {
                    LinkedList <Integer> l = new LinkedList <> ();
                    chokeJudge(l);
                    if (l.size () != 0) {
                        fileChunk.log.changeOfPreNbLog (l);
                    }
                }
                nTime = unChokingInterval;
                sortList(fileShareChannelList);
            }
            if (opnTime < 0.1) {
                chooseOp();
                opnTime = opUnChokingInterval;
            }
        }
    }


    private void sortList(LinkedList<Peer> fileShareChannelList) {
        Comparator <Peer> comparator = new Comparator <Peer> () {
            int speed1, speed2;
            public int compare (Peer p1, Peer p2) {
                speed1 = p1.getSpeed ();
                speed2 = p2.getSpeed ();
                if (speed1 > speed2)
                    return 1;
                else if (speed1 < speed2)
                    return -1;
                else
                    return 0;
            }
        };
        fileShareChannelList.sort (comparator);
    }

    private synchronized void chooseOp() throws IOException {
        if (op != null) {
            op.choke (true);
            fileShareChannelList.add (op);
            op = null;
        }
        int i = 0;
        while ( i < fileShareChannelList.size () ) {
            if (fileShareChannelList.get (i).stateArray[0] != -1) {
                op = fileShareChannelList.get (i);
                op.choke (false);
                fileChunk.log.changeOfOpUnchokeNbLog (op.targetId);
                fileChunk.log.unchokingLog (op.targetId);
                fileShareChannelList.remove (fileShareChannelList.get (i));
                return;
            }
            i++;
        }
    }

    private synchronized boolean checkList() {
        return op == null && fileShareChannelList.size () == 0;
    }

    synchronized void shareInfo(int i) throws IOException {
        int j = 0;
        while ( j < fileShareChannelList.size () ) {
            fileShareChannelList.get (j).have (i);
            j++;
        }
        if (op != null)
            op.have (i);
    }

    private int cycleTimer() {
        int time;
        time = nTime >= opnTime ? opnTime : nTime;
        nTime = nTime - time;
        opnTime = opnTime - time;
        return time * 1000;
    }

    synchronized void quit(Peer p) throws IOException {
        if (!aBoolean) {
            aBoolean = true;
        }
        if (op == p) {
            op = null;
            opnTime = 0;
        } else {
            fileShareChannelList.remove (p);
            nTime = 0;
        }
    }

   private void readFile(String ID) throws Exception{
        //Read file ****************************************************************************
       int peerId = Integer.parseInt(ID);
        BufferedReader read = new BufferedReader(new FileReader (new File("Common.cfg")));
        String[] arr = read.readLine().split(" ");
        numOfPN = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        unChokingInterval = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        opUnChokingInterval = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        String fileName = arr[1];
        arr = read.readLine().split(" ");
       int fileSize = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
       int pieceSize = Integer.parseInt(arr[1]);
        nTime = unChokingInterval;
        opnTime = opUnChokingInterval;
        Path wiki_path = Paths.get("/cise/homes/chilee/Desktop/Network/", "PeerInfo.cfg");
        Charset charset = Charset.forName("ISO-8859-1");
        List<String> lines = Files.readAllLines(wiki_path, charset);
        String[] stringArray;

       int id;
       for(int i = 0; i < lines.size() && lines.get(i) != null && !Objects.equals(lines.get(i), ""); i++)
        {
            stringArray = lines.get(i).split(" ");
            id = Integer.parseInt(stringArray[0]);
            if(id == peerId) {
                fileExist = Integer.parseInt(stringArray[3]);
            }
        }
        fileChunk = new FileChunk(peerId, fileName, fileExist, fileSize, pieceSize);
        //Set the values readed from peerInfo.cfg and create socket(XH)
        ServerSocket serverSocket = null;
        for (String line : lines) {
            stringArray = line.split(" ");
            id = Integer.parseInt(stringArray[0]);
            String ipAddress = stringArray[1];
            int port = Integer.parseInt(stringArray[2]);
            //Get the portNumber of this peerId
            Peer fileShareChannel;
            if (id == peerId) {
                serverSocket = new ServerSocket(port);
            } else if (id < peerId) {
                Socket socket = new Socket(ipAddress, port);
                fileShareChannel = new Peer(peerId, id, socket, fileSize, pieceSize, fileChunk, this);
                fileShareChannelList.add(fileShareChannel);
                tempList.add(fileShareChannel);
                fileChunk.log.cnted(id);
            } else if (id > peerId) {
                Socket socket = null;
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                }
                fileShareChannel = new Peer(peerId, id, socket, fileSize, pieceSize, fileChunk, this);
                fileShareChannelList.add(fileShareChannel);
                tempList.add(fileShareChannel);
                fileChunk.log.cnct(id);
            }
        }
        //Read file over ****************************************************************************
    }

    private void chokeJudge(LinkedList<Integer> list) throws IOException {
        if (fileShareChannelList.size () <= numOfPN) {
            for (Peer aPeerlist : fileShareChannelList) {
                if (aPeerlist.stateArray[0] != -1) {
                    aPeerlist.choke(false);
                    fileChunk.log.unchokingLog(aPeerlist.targetId);
                    list.add(aPeerlist.targetId);
                }
            }
        } else {
            int i = 0;
            while ( i < fileShareChannelList.size () ) {
                if (i < numOfPN) {
                    if (fileShareChannelList.get (i).stateArray[0] != -1) {
                        fileShareChannelList.get (i).choke (false);
                        fileChunk.log.unchokingLog (fileShareChannelList.get (i).targetId);
                        list.add (fileShareChannelList.get (i).targetId);
                    }
                } else {
                    if (fileShareChannelList.get (i).stateArray[0] != 1) {
                        fileShareChannelList.get (i).choke (true);
                        fileChunk.log.chokingLog (fileShareChannelList.get (i).targetId);
                    }
                }
                i++;
            }
        }
    }

    private void startThread() {
        for ( Peer channelList : fileShareChannelList ) {
            new Thread (channelList).start ();
        }
    }

}


