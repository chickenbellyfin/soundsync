package soundsync.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import soundsync.Command;


/**
 * 
 * @author Akshay
 */
public class SoundSyncServer implements Runnable {
	
	public static final int PORT = 1980;
	public static final String SERVER_ADDR = "130.215.234.149";
	
	public static final int PLAY_DELAY = 500;
	public static final int COL_URL = 1;
	public static final int COL_USER = 2;
	
	private ServerSocket serverSocket;
	private HashMap<String, ClientHandler> clientList;
	
	private ServerFrame frame;
	
	private String currentStream = "";
	private DefaultTableModel songTable;
	
	private boolean startNextTrack = true;
	private long trackStartTime;
	private long trackLength;
	private int loadCount = 0;
	
	private Timer timer;
	
	private boolean connecIsRunning = false;
	private boolean playIsRunning = false;
	
	private boolean playNextFlag = false;
	private boolean queueNextFlag = false;
	
	private String currentSong = "";
	private String nextSong = "";
	
	
	
	
	private Thread playingThread = new Thread(){
		@Override
		public void run(){
			playIsRunning = true;
			long queueTime = 0;
			long trackStopTime = 0;	
			
			while(playIsRunning){
				
				while(true){
					try{
						Thread.sleep(100);
						if(System.currentTimeMillis() > queueTime){
							break;
						}
					} catch(Exception e){}
				}					
				
				sendNextLoad();//TODO what to send???);
				queueNextFlag = false;
				
				while(true){
					try{
						Thread.sleep(50);
						if(System.currentTimeMillis() >= trackStopTime && loadCount >= clientList.size()){
							loadCount = 0;
							break;
						}
					} catch (Exception e){}
				}
				
				trackStartTime = System.currentTimeMillis() + SoundSyncServer.PLAY_DELAY;
				 trackStopTime = trackStartTime + trackLength;
				queueTime = trackStopTime - 10*1000;
				
				sendPlay(trackStartTime);

				playNextFlag = false;
			}			
		}
	};
	
	public SoundSyncServer() {
		try {
			serverSocket = new ServerSocket(SoundSyncServer.PORT, 0, InetAddress.getLocalHost());
			System.out.format("Starting server at %s:%05d%n", serverSocket.getInetAddress().getHostAddress(), SoundSyncServer.PORT);
			serverSocket.setSoTimeout(0);
		}
		catch (Exception e) {
			System.err.format("Error starting server: %s%n", e);
			System.exit(1);
		}
		
		timer = new Timer();
		
		setupGUI();
		
		clientList = new HashMap<String, ClientHandler>();
		
	}
	
	/**
	 * Listens for and handles incoming connections
	 */
	@Override
	public void run() {
		connecIsRunning = true;
		
		while (connecIsRunning) {
			try {
				Socket newClient = serverSocket.accept();
				newClient.setSoTimeout(0);
				DataInputStream tmpIn = new DataInputStream(newClient.getInputStream());
				DataOutputStream tmpOut = new DataOutputStream(newClient.getOutputStream());
				
				String user = tmpIn.readUTF();
				String userVersion = tmpIn.readUTF();
				System.out.format("\"%s\" is trying to connect with protocol %s%n", user, userVersion);
				if (userVersion.equals(Command.PROTOCOL_VERSION)) {
					tmpOut.writeUTF(Command.GOOD);
				}
				else {
					System.out.printf("%s is had outdated client (user: %d, current:%s)", user, userVersion, Command.PROTOCOL_VERSION);
					tmpOut.writeUTF(Command.BAD);
				}
				tmpOut.flush();
				
				ClientHandler newClientHandler = new ClientHandler(user, newClient, this);
				//String newClientAddr = newClient.getInetAddress().getHostAddress();
				clientList.put(user, newClientHandler);
				newClientHandler.pingTest();
				
				//add the songs that are already in the queue
				for(int i = 0; i < songTable.getRowCount(); i++){
					newClientHandler.send(Command.formatCmd(Command.CLIENT_ADD, songTable.getValueAt(i, COL_USER), songTable.getValueAt(i, COL_URL)));
				}
				
				newClientHandler.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setTrackLength(long time) {
		trackLength = time;
	}
	
	public void clientLoaded() {
		loadCount++;
	}
	
	private void setupGUI() {
		frame = new ServerFrame();
		songTable = (DefaultTableModel)frame.songList.getModel();		
		
		frame.playButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!playIsRunning){
					playingThread.start();
				} else {
					//TODO: skip the song
				}
			}
		});
		
		frame.skipButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				broadcast(Command.formatCmd(Command.CLIENT_STOP, ""));
				broadcast(Command.formatCmd(Command.CLIENT_CLEAR_QUEUE, ""));
				
			}
		});
		
		frame.removeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				int[] selected = frame.songList.getSelectedRows();
				ArrayList<String> urls = new ArrayList<String>();
				for(int i:selected){
					urls.add((String)songTable.getValueAt(i, COL_URL));
				}
				for (String url:urls) {
					for(int j = 0; j < songTable.getRowCount(); j++){
						if(url.equals(songTable.getValueAt(j, COL_URL))){
							songTable.removeRow(j);			
						}
						broadcast(Command.formatCmd(Command.CLIENT_REMOVE, url));
					}
				}
			}
		});		
	}
	
	private void sendNextLoad() {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				while(songTable.getRowCount() == 0){ //wait for music to be added
					try{
						Thread.sleep(50);
					} catch(Exception e){}
				}
				nextSong = (String) songTable.getValueAt(0, COL_URL);
				songTable.removeRow(0);
				
				for (ClientHandler h : clientList.values()) {
					try {
						h.sendLoad(nextSong);
					}
					catch (Exception e) {}
				}
			}
		}).start();
	}
	
	private void sendPlay(long trackStartTime) {
		System.out.println("SERVER PLAY");
		
		broadcast(Command.formatCmd(Command.CLIENT_REMOVE, currentSong));
		currentSong = nextSong;
		
		for (ClientHandler h : clientList.values()) {
			try {
				if (h.isLoaded()) {
					h.send(Command.formatCmd(Command.CLIENT_PLAY, trackStartTime - h.lag));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	private void broadcast(final String b) {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				for (ClientHandler h : clientList.values()) {
					try {
						h.send(b);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public void addSong(String song, String user) {
		for (int i = 0; i < songTable.getRowCount(); i++) {
			if (songTable.getValueAt(i, SoundSyncServer.COL_URL).equals(song)) { //song is already in the list
				return;
			}
		}
		songTable.addRow(new Object[] { "", song, user });
		frame.adjuster.adjustColumns();
		frame.repaint();		
		broadcast(Command.formatCmd(Command.CLIENT_ADD, user, song));
	}
	
	public void removeClient(ClientHandler h) {
		clientList.remove(h.id);
	}	
}
