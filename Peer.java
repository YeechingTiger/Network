import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by xinghe on 2017/4/8.
 */


public class Peer implements Runnable{

    Socket s = null;
    int tarid;
    int id;

    private BitSet tarbitfield;
    private int blength;
    private FileChunk fm;
    public peerProcess pp;

    private OutputStream outs = null;
    private InputStream ins = null;
    ByteArrayInputStream bins = null;
    ByteArrayOutputStream bouts = null;

    int chokestate=0;
    int bechokestate=0;
    int requestate=0;
    int interestate=0;
    int sendstate =-1;
    int havestate=0;
    int mstate=0;
    int renum=-1;
    ArrayList<Integer> havelist = new ArrayList <> ();

    int piecesize;
    double speed=0;

    public Peer(int id, int tarid, Socket s, int filesize, int chunksize, FileChunk fm, peerProcess pp) throws IOException
    {
        this.id=id;
        this.tarid=tarid;
        this.s=s;
        this.fm=fm;
        this.pp=pp;
        this.piecesize=chunksize;
        blength=Helper.upperNum(filesize,chunksize);
	System.out.println(blength);	
        tarbitfield=new BitSet(blength);
        System.out.println("Check target bitfield:"+checkBitFd(tarbitfield));
    }

    @Override
    public void run() {
        try {
            ins = s.getInputStream ();
            outs = s.getOutputStream ();

            HandShakeMessage hshake = new HandShakeMessage (id);
            byte[] sendmsg = hshake.handShakeMessageToByte ();
            sendFile (sendmsg);
            fm.log.cnct (tarid);

            byte[] rev = new byte[32];
            ins.read (rev, 0, 32);
            HandShakeMessage revmsg = new HandShakeMessage (rev);
            if (revmsg.handShake_Header.equals ("P2PFILESHARINGPROJ"))
                if (revmsg.peerId == tarid) {
                    System.out.println ("Handshake with " + tarid + " is successful");
                    fm.log.cnted (tarid);
                } else
                    System.out.println ("Handshake failed");

            byte[] sendbf = fm.bitfdToByte ();
            ActMsg bfmsg = new ActMsg (5, sendbf);
            sendFile (bfmsg.msgPack ());
            while (true) {
                checkstate ();
                boolean flag1 = fm.checkBitFd ();
                boolean flag2 = checkBitFd (tarbitfield);

                if (fm.checkBitFd () && checkBitFd (tarbitfield) && havestate != 1) {
                  System.out.println(tarbitfield.get(1)); 
		  Thread.sleep (1000);
                    pp.quit (this);
                    System.out.println (id + " has quit" + tarid);
                    break;
                }

                if (ins.available () == 0) {
                    Thread.sleep (50);
                    continue;
                }

                byte[] revm = receiveFile ();
                ActMsg m = byteToMsg (revm);
                System.out.println (id + " received a message of type " + m.messageType + " from " + tarid);

                if (bechokestate == 1 && renum != -1) {
                    requestate = -1;
                    fm.requestField[renum] = 0;
                    renum = -1;
                }

                if (renum != -1 && m.messageType == 0) {
                    requestate = -1;
                    System.out.println ("The request number is:" + renum);
                    fm.requestField[renum] = 0;
                    renum = -1;
                }

                if (m.messageType == 0) {
                    fm.log.chokingLog (tarid);
                    bechokestate = 1;
                    System.out.println (id + " is choked by " + tarid);

                } else if (m.messageType == 1) {
                    unchokeProcess ();

                } else if (m.messageType == 2) {
                    interestProcess ();
                    fm.log.interestedReiceived (tarid);

                } else if (m.messageType == 3) {
                    notinterestProcess ();
                    fm.log.notInterestedReceived (tarid);

                } else if (m.messageType == 4) {
                    haveProcess (m);

                } else if (m.messageType == 5) {
                    bitfieldProcess (m);

                } else if (m.messageType == 6) {
                    requestProcess (m);

                } else if (m.messageType == 7) {
                    pieceProcess (m);

                }
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public byte[] receiveFile() throws IOException{
        byte[] length = new byte[4];
        byte[] outA;
        int rev, total=0;
        while(true)
        {
            if ((total < 4)) {
                rev = ins.read (length, total, 4 - total);
                total = total + rev;
            } else {
                break;
            }
        }
        int flength = Helper.byteToInt(length);
        System.out.println("received a message of length "+flength +"/"+tarid);
        outA = new byte[flength];
        total=0;
        
        while(total<flength)
        {
            rev = ins.read(outA, total, flength-total);
            total = total + rev;
        }
        byte[] out = new byte[length.length+outA.length];
        for(int i=0;i<4;i++)
            out[i]=length[i];
        for(int i=4;i<4+outA.length;i++)
            out[i]=outA[i-4];
        System.out.println("The length of message received is " + out.length);
        return out;
    }

    public void sendFile(byte[] buf) throws IOException{
        outs.write(buf);
        outs.flush();
    }


    public void unchokeProcess() throws IOException
    {
        int rpiece;
        while(true)
        {
            rpiece = getPiece(tarbitfield);
            if(rpiece==-1)
                break;
            else if(fm.requestField[rpiece]==0)
                break;
        }
        if (rpiece == -1) {
            ActMsg m = new ActMsg(3);
            byte[] temp=m.msgPack();
            sendFile(temp);
        } else {
            ActMsg m=new ActMsg(6);
            m.plusPayload(rpiece);
            renum=rpiece;
            byte[] temp = m.msgPack();
            sendFile(temp);
            requestate=1;
            bechokestate=-1;
            fm.requestField[rpiece]=1;
            System.out.println(id + " send a message of type 6 to "+ tarid+" requesting piece# "+rpiece);
        }
        fm.log.unchokingLog(tarid);
    }

    public void interestProcess()
    {
        interestate =1;
        System.out.println(id+ " received a message of type 2 from "+tarid);
    }

    public void notinterestProcess()
    {
        interestate=-1;
        System.out.println(id+" received a message of type 3 from "+tarid);
    }

    public void haveProcess(ActMsg m) throws IOException
    {
        int pieceNum=Helper.byteToInt(m.messagePayload);
	System.out.println(pieceNum);
        fm.log.haveReceived(tarid, pieceNum);
        tarbitfield.flip(pieceNum);

        System.out.println("Check own bitfield at num#:"+pieceNum+" is:"+fm.getBitFd(pieceNum));
        if (fm.getBitFd (pieceNum)) {
            ActMsg sm = new ActMsg(3);
            byte[] temp=sm.msgPack();
            sendFile(temp);
        } else {
            ActMsg sm = new ActMsg(2);
            byte[] temp=sm.msgPack();
            sendFile(temp);
        }

    }

    public void requestProcess(ActMsg m)throws IOException
    {
        int piecenum = Helper.byteToInt(m.messagePayload);
        System.out.println(tarid +" is requesting piece# "+piecenum +" from " +id);
        if(chokestate!=1&& !tarbitfield.get (piecenum))
        {
            ActMsg ms = new ActMsg(7);
            ms.plusPayload(fm.getPiece(piecenum));
            byte[] temp=ms.msgPack();
            sendFile(temp);
            System.out.println(id + " send a message of type 7 to "+ tarid +" with piece# "+ piecenum);
        }
    }

    public void pieceProcess(ActMsg m) throws IOException
    {
        byte[] fileindex= new byte[4];
        byte[] filecontent= new byte[m.messagePayload.length-4];
        int i,index;
        i=0;
        while (i<4) {
            fileindex[i]=m.messagePayload[i];
            i++;
        }
        index = Helper.byteToInt(fileindex);
        if(index!=renum)
            System.out.println("This is not the piece requested, which is "+renum);
        System.out.println(id+"received a piece# "+index +" from "+tarid);
        System.out.println(id+" has the piece#?:"+ fm.getBitFd(index));
        System.out.println(id+" is in requestate:" + requestate);
        if(!fm.getBitFd (index))
        {
            int j;
            for(j=0, i=4;i<m.messagePayload.length;i++)
                filecontent[j++]=m.messagePayload[i];
            fm.wrtPiece(filecontent, index);
            fm.setBitFd(index, true);
            fm.log.pieceDownloadLog(tarid, index);
            pp.broadcast(index);
            renum=-1;
            requestate=-1;
            fm.requestField[index]=2;
        }
        if(requestate!=1)
        {
            int piece;
            while(true)
            {
                piece = getPiece(tarbitfield);
                if (piece == -1 || fm.requestField[piece] == 0) break;
            }
            renum=piece;
            if (piece == -1) {
                ActMsg mb =new ActMsg(3);
                byte[] temp =m.msgPack();
                sendFile(temp);
                requestate=-1;
                System.out.println(id+" send a message of type 3 to "+tarid);
            } else {
                ActMsg ms = new ActMsg(6);
                ms.plusPayload(piece);
                byte[] temp = ms.msgPack();
                sendFile(temp);
                requestate=1;
                fm.requestField[piece]=1;
                System.out.println(id+" send a request to " + tarid+" requesting piece# "+piece);
            }
        }
        addSpeed();
    }



    public void bitfieldProcess(ActMsg m) throws IOException
    {
        tarbitfield = rdBitfd(m.messagePayload);
        System.out.println("check target bitfield:"+checkBitFd(tarbitfield));
        int index = getPiece(tarbitfield);
        if (index < 0) {
            ActMsg sm = new ActMsg(3);
            byte[] temp= sm.msgPack();
            sendFile(temp);
            System.out.println(id + " send a message of type 3 to "+tarid );
        } else {
            ActMsg sm = new ActMsg(2);
            byte[] temp= sm.msgPack();
            sendFile(temp);
            System.out.println(id + " send a message of type 2 to "+tarid );
        }
    }

    public BitSet rdBitfd(byte[] payload)
    {
        return BitSet.valueOf(payload);
    }

    public ActMsg byteToMsg(byte[] temp)
    {
        int mtype,i;
        byte[] payload=new byte[temp.length-5];

        mtype=(int)temp[4];
        System.out.println("The type of received message is " + mtype);

        for(i=5;i<temp.length;i++)
            payload[i-5]=temp[i];

        return(new ActMsg(mtype, payload));
    }

    public int getPiece(BitSet bitfield)
    {
        int base=new Random ().nextInt(blength);
        int i;
        for(i=0; i<blength; i++)
            if(bitfield.get ((base + i) % blength) && !fm.getBitFd ((base + i) % blength))
                return (base+i)%blength;
        System.out.println("Condition of number choose "+(base+i)%blength);
        System.out.println("bitfield check:"+fm.checkBitFd()+"/"+checkBitFd(tarbitfield));
        return -1;
    }

    public synchronized void unchoke()
    {
        chokestate=-1;
        sendstate=2;
    }

    public synchronized void choke()
    {
        chokestate=1;
        sendstate=1;
    }

    public synchronized double getSpeed()
    {
        double cs=speed;
        speed=0;
        return cs;
    }

    public synchronized void addSpeed()
    {
        speed=speed+piecesize;
    }

    public synchronized boolean interested()
    {
        return interestate == 1;
    }

    public synchronized void have(int i) throws IOException
    {
        havelist.add(i);
        havestate=1;
    }

    private synchronized void checkstate() throws IOException
    {
        switch (sendstate) {
            case 0:
                ;
                break;
            case 1: {
                ActMsg m = new ActMsg (0);
                byte[] s = m.msgPack ();
                sendFile (s);
                sendstate = 0;
                System.out.println (id + " has send a message of type 0 to " + tarid);
                break;
            }
            case 2: {
                ActMsg m = new ActMsg (1);
                byte[] s = m.msgPack ();
                sendFile (s);
                sendstate = 0;
                System.out.println (id + " has send a message of type 1 to " + tarid);
                break;
            }
        }

        switch (havestate) {
            case 1:
                if (havelist.size () != 0) {
                    do {
                        ActMsg m = new ActMsg (4);
                        m.plusPayload (havelist.get (0));
                        byte[] temp = m.msgPack ();
                        sendFile (temp);
                        System.out.println (id + " has sent a message of type 4 to " + tarid + " piece#:" + havelist.get (0));
                        havelist.remove (0);
                        System.out.println (id + " remaining havelist size:" + havelist.size ());
                    } while (havelist.size () != 0);
                }
                havestate = -1;
                break;
        }
    }

    public boolean checkBitFd(BitSet bitfield)
    {
        for(int i=0;i<blength;i++)
            if(!bitfield.get (i))
                return false;
        return true;
    }


}
