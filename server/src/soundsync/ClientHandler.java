package soundsync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler {

    public String id;
    private SoundSyncServer server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean loaded = false;
    private boolean isRunning = false;
    
    public long ping;
    public long lag;
    
    private Runnable inputProcessor = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    String cmd = in.readUTF();
                    System.out.println(cmd);
                    doCommand(cmd);
                } catch(Exception e){
                	e.printStackTrace();
                	disconnect();
                }
            }
        }
    };

//    private Runnable outputProcessor = new Runnable(){
//        @Override
//        public void run(){
//        }
//    };
    public ClientHandler(String id, Socket s, SoundSyncServer srv) throws Exception {
        this.id = id;

        this.socket = s;
        this.server = srv;

        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void doCommand(String cmd) {
        if (cmd.startsWith("READY:")) {
            long trackTimeMillis = Long.parseLong(cmd.substring("READY:".length()));
            loaded = true;
            server.clientLoaded(trackTimeMillis);
        } else if (cmd.startsWith("ADD:")){
        	submitSong(cmd.substring(4));
        }
    }
    
    public void pingTest(){
        int tests = 100;
        long totalPing = 0;
        for(int i = 0; i < tests; i++){
            try{
                out.writeUTF("PING");
                out.flush();
                long sTime = System.currentTimeMillis();
                in.readUTF();
                totalPing += System.currentTimeMillis()-sTime;
            } catch (Exception e){
            	e.printStackTrace();
            	disconnect();
            }
        } 
        
        ping = totalPing/tests;
        System.out.println(id+" ping "+ping);
        
        try{
            out.writeUTF("TIME");
            lag = System.currentTimeMillis() - (Long.parseLong(in.readUTF())+ping/2);
            System.out.println(id+" lag "+lag);
        } catch(Exception e){
            e.printStackTrace();
            disconnect();
        }
    }

    public void sendLoad(String url) {
        loaded = false;
        send("LOAD:"+url);                
        System.out.println(id + " sendLoad");

    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }

    }

    public void start() {
        if (!isRunning) {            
            isRunning = true;
            new Thread(inputProcessor).start();
            //new Thread(outputProcessor).start();
        }
    }
    
    private void submitSong(String loc){
    	server.addSong(loc, id);
    }
    
    private void disconnect(){
    	isRunning = false;
    	
    	try{
    		socket.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	server.removeClient(this);    	
    }
}