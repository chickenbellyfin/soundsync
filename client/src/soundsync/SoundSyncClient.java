package soundsync;

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

    public static final int PORT = 1980;
    public static final String SERVER_ADDR = "130.215.234.149";
    private static final String GREET = "HI";
    public AudioPlayer audio;
    private String id;
    private Socket server;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isRunning;
    private Runnable inputProcessor = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    //System.out.println("waiting...");
                    String cmd = in.readUTF();
                    System.out.println("COMMAND: "+cmd);
                    doCommand(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
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
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        
        audio = new AudioPlayer();

    }

    public boolean connect(String user, String pass) {
        int tries = 3;
        boolean connected = false;

        do {
            if (server == null) {
                try {
                    server = new Socket(SERVER_ADDR, PORT);
                    in = new DataInputStream(server.getInputStream());
                    out = new DataOutputStream(server.getOutputStream());

                    out.writeUTF(user);
                    out.writeUTF(pass);
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

    private void doCommand(String cmd) {
        if(cmd.equals("PING")){
            sendServerMessage("PING");            
        } else if (cmd.startsWith("PLAY:")) {
            long startTime = Long.parseLong(cmd.substring(5));
            playAt(startTime);
        } else if (cmd.equalsIgnoreCase("STOP")) {
            audio.stop();
        } else if (cmd.startsWith("LOAD:")) {
            long time = audio.loadSong(cmd.substring("LOAD:".length())); //its probably a url
            sendServerMessage("READY:" + time);
        } else if(cmd.equals("TIME")){
            sendServerMessage(""+System.currentTimeMillis());
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
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SoundSyncClient client = new SoundSyncClient();
        System.out.println(client.connect("qwer"+System.currentTimeMillis(), "tyuiop"));
    }
}
