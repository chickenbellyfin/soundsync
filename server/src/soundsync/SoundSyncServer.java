/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundsync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Akshay
 */
public class SoundSyncServer implements Runnable{

    public final int PORT = 1980;
    
    public static final String M_PLAY = "PLAY:";
    public static final String M_STOP = "STOP";
    public static final String M_LOAD = "LOAD:";
    
    
    private ServerSocket serverSocket;
    private HashMap<String, ClientHandler> clientList;
    
    private ServerFrame frame;
    
    private String currentStream = "";
    private DefaultListModel songList;
    
    private boolean startNextTrack = true;
    private long trackStartTime;
    private long trackLength;
    private int loadCount = 0;
    
    private boolean isRunning = false;
   
    


    public SoundSyncServer() {
        try{
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(0);
        } catch(Exception e){
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        setupGUI();
        
        clientList = new HashMap<String, ClientHandler>();


    }
    
    @Override
    public void run(){
        isRunning = true;
        
        connectClients:while(isRunning){
            try{
                Socket newClient = serverSocket.accept();
                newClient.setSoTimeout(0);
                DataInputStream tmpIn = new DataInputStream(newClient.getInputStream());
                DataOutputStream tmpOut = new DataOutputStream(newClient.getOutputStream());
                
                String user = tmpIn.readUTF();
                
                if(authenticate(user)){
                    tmpOut.writeUTF("GOOD");
                } else {
                    tmpOut.writeUTF("BAD");
                }
                tmpOut.flush();
                
                ClientHandler newClientHandler = new ClientHandler(user, newClient, this);
                String newClientAddr = newClient.getInetAddress().getHostAddress();
                clientList.put(user, newClientHandler);
                newClientHandler.pingTest();
                newClientHandler.start();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private boolean authenticate(String user){
        return true;
    }
    
    public void clientLoaded(long time){
        trackLength = time;
        loadCount++;
        if(loadCount == clientList.size()){
            loadCount = 0;
            sendPlay();
        }
    }
    
    private void setupGUI(){
        frame = new ServerFrame();
        songList = (DefaultListModel) frame.queue.getModel();      
        
        frame.playButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                 sendLoad("http://130.215.234.149/Akshay/Psy/GS.mp3");
            }        
        });
       
    }
    
    private void sendLoad(final String url){
        new Thread(new Runnable(){
            @Override
            public void run(){
                for(ClientHandler h:clientList.values()){
                    try{
                        h.sendLoad(url);
                    }catch(Exception e){}
                }                           
            }
        }).start();        
    }
    
    private void sendPlay(){
        System.out.println("sendPlay");
        new Thread(new Runnable(){
            @Override
            public void run(){
                trackStartTime = System.currentTimeMillis() + 500;
                for(ClientHandler h:clientList.values()){
                    try{
                        h.send(M_PLAY+(trackStartTime-h.lag));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }                           
            }
        }).start();
        
    }

           



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SoundSyncServer().run();
    }
}
