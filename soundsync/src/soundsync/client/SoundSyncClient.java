package soundsync.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;

/**
 *
 * @author Akshay
 */
public class SoundSyncClient {
	
	public interface SyncClientListener {
		public void syncDisconnected();
	}

    public static final int PORT = 1980;
    public static final String SERVER_ADDR = "130.215.234.149";
    private static final String GREET = "HI";
    public NetAudioPlayer audio;
    private String id;
    private Socket server;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isRunning;
    
    private SyncClientListener listener;
    
    private Runnable inputProcessor = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    //System.out.println("waiting...");
                    String cmd = in.readUTF();
                    //System.out.println("COMMAND: "+cmd);
                    doCommand(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
        }
    };

//    private Runnable outputProcessor = new Runnable() {
//        @Override
//        public void run() {
//        }
//    };
    public SoundSyncClient() {
        
        audio = new NetAudioPlayer();

    }

    public boolean connect(String serverAddr, String user) {
        int tries = 3;
        boolean connected = false;

        do {
            if (server == null) {
                try {
                    server = new Socket(serverAddr, PORT);
                    in = new DataInputStream(server.getInputStream());
                    out = new DataOutputStream(server.getOutputStream());

                    out.writeUTF(user);
                    out.flush(); 
                    String authResult = in.readUTF();

                    if (authResult.equalsIgnoreCase("GOOD")) {
                        connected = true;
                        server.setSoTimeout(0);
                    } else if (authResult.equalsIgnoreCase("BAD")) {
                        server = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
            tries--;

        } while (!connected && tries > 0);

        if (connected) {
            isRunning = true;
            new Thread(inputProcessor).start();
            //new Thread(outputProcessor).start();
        }

        return connected;
    }
    
    public void submitSong(String url){
    	sendServerMessage("ADD:"+url);
    }

    private void doCommand(String cmd) {
        if(cmd.equals("PING")){
            sendServerMessage("PING"); //ping the server back           
        } else if (cmd.startsWith("PLAY:")) {
            long startTime = Long.parseLong(cmd.substring(5));
            playAt(startTime);
        } else if (cmd.equalsIgnoreCase("STOP")) {
            audio.stop();
        } else if (cmd.startsWith("LOAD:")) {
            long time = audio.loadSong(cmd.substring("LOAD:".length())); //its probably a url
            sendServerMessage("READY:" + time);
        } else if(cmd.equalsIgnoreCase("TIME")){
            sendServerMessage(""+System.currentTimeMillis());
        } else if(cmd.equalsIgnoreCase("CLEARQUEUE")){
        	audio.queue.clear();
        }
        	
    }

    public void playAt(final long time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                if (System.currentTimeMillis() > time) {
//                    audio.clip.setMicrosecondPosition((System.currentTimeMillis() - time) * 1000);
//                    audio.play();
//                    System.out.println("audio.play()");
//                } else {
                    while (System.currentTimeMillis() < time);
                    audio.play();
                    System.out.println("audio.play()");
                //}
            }
        }).start();
    }


    public void sendServerMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    private void disconnect(){
    	isRunning = false;
    	try{
    		server.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	if(listener != null){
    		listener.syncDisconnected();
    	}
    	
    }
}