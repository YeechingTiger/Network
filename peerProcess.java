import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by xinghe on 2017/4/8.
 */


public class peerProcess {

    private int numOfPN;
    private int unchokingInterval;
    private int opUnchIntval;
    private int fileSize;
    private int pieceSize;
    int id;
    int portnumber;
    int port;
    String ip_address;
    FileChunk fileChunk;
    int fileExist;
    Peer fileShareChannel;
    int peerId;

    private ArrayList <Peer> peerlist = new ArrayList <> ();
    Peer op = null;
    private int ntime;
    private int opntime;
    private boolean informflag = false;
    private int count = 0;

    public static void main (String[] args) throws Exception {
        new peerProcess (args[ 0 ]);
    }

    public peerProcess (String ID) throws Exception {

        int peerId = Integer.parseInt(ID);
        BufferedReader read = new BufferedReader(new FileReader (new File("Common.cfg")));
        String[] arr = read.readLine().split(" ");
        numOfPN = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        unchokingInterval = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        opUnchIntval = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        String fileName = arr[1];
        arr = read.readLine().split(" ");
        fileSize = Integer.parseInt(arr[1]);
        arr = read.readLine().split(" ");
        pieceSize = Integer.parseInt(arr[1]);
        
        ntime = unchokingInterval;
        opntime = opUnchIntval;

        Path wiki_path = Paths.get("/cise/homes/chilee/Desktop/Network/", "PeerInfo.cfg");
        Charset charset = Charset.forName("ISO-8859-1");
        List<String> lines = Files.readAllLines(wiki_path, charset);
        String[] stringarr;

        for(int i = 0;i < lines.size(); i++)
        {
            stringarr = lines.get(i).split(" ");
            id = Integer.parseInt(stringarr[0]);
	    if(id == peerId)
                fileExist = Integer.parseInt(stringarr[3]);
        }
	fileChunk = new FileChunk(peerId, fileName, fileExist, fileSize, pieceSize);
        //Set the values readed from peerInfo.cfg and create socket(XH)
	ServerSocket serverSocket = null;
        for(int i = 0; i < lines.size(); i++)
        {
            
	    stringarr = lines.get(i).split(" ");
            id = Integer.parseInt(stringarr[0]);
            ip_address = stringarr[1];
            port = Integer.parseInt(stringarr[2]);

            //Get the portnumber of this peerId
            if(id == peerId)
            {
                portnumber = port;
		serverSocket = new ServerSocket(portnumber);
                System.out.println(portnumber);
            }
            else if(id < peerId)
            {
                Socket socket = new Socket(ip_address,port);

		    fileShareChannel = new Peer(peerId, id, socket, fileSize, pieceSize, fileChunk, this);
			peerlist.add(fileShareChannel);
			fileChunk.log.cnted(id);

                System.out.println(ip_address + " " + port);
                count++;
            }
            else if(id > peerId)
            {
		        System.out.println(portnumber);
                Socket socket = serverSocket.accept();
                fileShareChannel = new Peer(peerId, id, socket, fileSize, pieceSize, fileChunk, this);
                peerlist.add(fileShareChannel);
                fileChunk.log.cnct(id);
                System.out.println("ID > PEERID");
                count++;
            }

        }



        for ( Peer aPeerlist : peerlist ) {
            new Thread (aPeerlist).start ();
        }

        while ( true ) {
            Thread.sleep (cycleTimer ());
	    System.out.println(op);	
System.out.println(peerlist.size());

            if (checklist ()) {
                Thread.sleep (1000);
                System.out.println ("Transfer is completed");
                break;
            }

            if (ntime < 0.1) {
                System.out.println ("check quit------------------------:" + count + "/" + peerlist.size ());
                System.out.println ("check bitfield:" + fileChunk.checkBitFd () + " total write piece:" + fileChunk.count);
                System.out.println ("the piece not received is:" + fileChunk.checkBitFdInt ());
                for ( int i = 0; i < peerlist.size (); i++ )
                    System.out.println ("The size is:" + peerlist.size () + " choke check:" + peerlist.get (i).tarid + " is choked?:" + peerlist.get (i).chokestate);
                if (peerlist.size () != 0) {
                    ArrayList <Integer> l = new ArrayList <Integer> ();
                    if (peerlist.size () <= numOfPN) {
                        Iterator <Peer> iterator = peerlist.iterator ();
                        while ( iterator.hasNext () ) {
                            Peer aPeerlist = iterator.next ();
                            if (aPeerlist.chokestate != -1) {
                                aPeerlist.unchoke ();
                                fileChunk.log.unchokingLog (aPeerlist.tarid);
                                l.add (aPeerlist.tarid);
                                System.out.println (peerId + " unchoke " + aPeerlist.tarid);
                            }
                        }
                    } else {
                        int i = 0;
                        while ( i < peerlist.size () ) {
                            if (i < numOfPN) {
                                if (peerlist.get (i).chokestate != -1) {
                                    System.out.println ("Processing unchoke neighbors ");
                                    peerlist.get (i).unchoke ();
                                    fileChunk.log.unchokingLog (peerlist.get (i).tarid);
                                    l.add (peerlist.get (i).tarid);
                                    System.out.println (peerId + " unchoke " + peerlist.get (i).tarid);
                                }
                            } else {
                                if (peerlist.get (i).chokestate != 1) {
                                    System.out.println ("Processing choke neighbors ");
                                    peerlist.get (i).choke ();
                                    fileChunk.log.chokingLog (peerlist.get (i).tarid);
                                    System.out.println (peerId + " choke " + peerlist.get (i).tarid);
                                }
                            }
                            i++;
                        }
                    }
                    if (l.size () != 0) {
                        fileChunk.log.changeOfPreNbLog (l);
                    }
                }
                ntime = unchokingInterval;
                listmaxify (peerlist);
            }
            if (opntime < 0.1) {
                System.out.println ("Processing OPunchoking neighbors");
                chooseop ();
                opntime = opUnchIntval;
            }
        }
    }


    public void listmaxify (ArrayList <Peer> l) {
        Comparator <Peer> c = new Comparator <Peer> () {
            double a, b;
            public int compare (Peer p1, Peer p2) {
                a = p1.getSpeed ();
                b = p2.getSpeed ();
                if (a > b)
                    return 1;
                else if (a < b)
                    return -1;
                else
                    return 0;
            }
        };
        l.sort (c);
    }

    public synchronized void chooseop () throws IOException {
        if (op != null) {
            op.choke ();
            peerlist.add (op);
            op = null;
        }
        int i = 0;
        while ( i < peerlist.size () ) {
            if (peerlist.get (i).chokestate != -1) {
                op = peerlist.get (i);
                op.unchoke ();
                fileChunk.log.changeOfOpUnchokeNbLog (op.tarid);
                fileChunk.log.unchokingLog (op.tarid);
                peerlist.remove (peerlist.get (i));
                System.out.println ("choose op successful:" + op.id + "/" + op.tarid);
                return;
            }
            i++;
        }
    }

    public synchronized boolean checklist () {
        return op == null && peerlist.size () == 0;
    }

    public synchronized void broadcast (int i) throws IOException {
        int j = 0;
        while ( j < peerlist.size () ) {
            peerlist.get (j).have (i);
            System.out.println (peerId + " has sent a message of type 4 to " + peerlist.get (j).tarid);
            j++;
        }
        if (op != null)
            op.have (i);
    }

    public int cycleTimer () {
        int time;
        time = ntime >= opntime ? opntime : ntime;
        ntime = ntime - time;
        opntime = opntime - time;
        return time * 1000;
    }

    public synchronized void quit (Peer p) throws IOException {
        if (!informflag) {
            informflag = true;
            fileChunk.log.completeLog ();
        }
        if (op == p) {
            op = null;
            opntime = 0;
        } else {
            peerlist.remove (p);
            ntime = 0;
        }
        count--;
   }

}


