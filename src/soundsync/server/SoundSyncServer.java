package soundsync.server;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import soundsync.Command;
import soundsync.Config;
import soundsync.ui.ServerFrame;


/**
 * @author Akshay
 */
public class SoundSyncServer implements Runnable {
	
	public static final int LOAD_TIME = 10*1000;
	public static final int PLAY_DELAY = 500;
	public static final int COL_URL = 0;
	public static final int COL_USER = 1;
	
	private ServerSocket serverSocket;
	private HashMap<String, ClientHandler> clientList;
	private HashMap<String, Integer> removeVotes;
		
	private ServerFrame frame;
	
	private DefaultTableModel songTable;
	
	private long trackStartTime;
	private long trackLength;
	private long queueTime = 0;
	private long trackStopTime = 0;
	private int loadCount = 0;
	
	private boolean skip = false;
	
		
	private boolean connectIsRunning = false;
	private boolean playIsRunning = false;

	private String currentSong = "";
	private String nextSong = "";
	
	
	
	
	private Thread playingThread = new Thread(){
		@Override
		public void run(){
			playIsRunning = true;
			
			while(playIsRunning){
				
				while(!skip && System.currentTimeMillis() < queueTime){
					try{
						Thread.sleep(100);
					} catch(Exception e){}
				}			
				
				if(!skip) {
					sendNextLoad();//TODO what to send???);
				}
				
				frame.skipButton.setEnabled(false);
				
				while(!skip && (System.currentTimeMillis() <= trackStopTime || loadCount < clientList.size())){
					try{
						Thread.sleep(50);
					} catch (Exception e){}
				}
				loadCount = 0;
				
				trackStartTime = System.currentTimeMillis() + SoundSyncServer.PLAY_DELAY;
				 trackStopTime = trackStartTime + trackLength;
				queueTime = trackStopTime - LOAD_TIME;
				if(skip){
					queueTime = 0;
					trackStopTime = 0;
					skip = false;
					frame.skipButton.setEnabled(true);
					continue;
				} else {
					sendPlay(trackStartTime);
				}
				
				frame.skipButton.setEnabled(true);

			}			
		}
	};
	
	private Thread timerThread = new Thread(){
		@Override
		public void run(){
			while(true){
				try{
					Thread.sleep(200);
					long left = Math.max(0, (trackStopTime - System.currentTimeMillis())/1000);
					frame.timeLeft.setForeground(left*1000 < LOAD_TIME ? Color.RED : Color.BLACK);
					
					frame.timeLeft.setText(String.format("%d:%02d", left/60, left%60));
				} catch(Exception e){}
			}
		}		
	};
	
	public SoundSyncServer() {
		try {
			serverSocket = new ServerSocket(Config.PORT, 0, InetAddress.getLocalHost());
			System.out.format("Starting server at %s:%05d%n", serverSocket.getInetAddress().getHostAddress(), Config.PORT);
			serverSocket.setSoTimeout(0);
		}
		catch (Exception e) {
			System.err.format("Error starting server: %s%n", e);
			System.exit(1);
		}
		
		setupGUI();
		
		clientList = new HashMap<String, ClientHandler>();
		removeVotes = new HashMap<String, Integer>();
	}
	
	/**
	 * Listens for and handles incoming connections
	 */
	@Override
	public void run() {
		connectIsRunning = true;
		
		while (connectIsRunning) {
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
					System.out.printf("%s had outdated client (user: %d, current:%s)", user, userVersion, Command.PROTOCOL_VERSION);
					tmpOut.writeUTF(Command.BAD);
				}
				tmpOut.flush();
				
				ClientHandler newClientHandler = new ClientHandler(user, newClient, this);
				//String newClientAddr = newClient.getInetAddress().getHostAddress();
				clientList.put(user, newClientHandler);
				newClientHandler.clockOffsetTest();
				
				//add the songs that are already in the queue
				for(int i = 0; i < songTable.getRowCount(); i++){
					newClientHandler.send(Command.format(Command.CLIENT_ADD, songTable.getValueAt(i, COL_USER), songTable.getValueAt(i, COL_URL)));
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
		String address = "";
		try{
			address = "@ "+InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e){}
		
		frame.setTitle(String.format("SoundSync Server %s", address));
		songTable = (DefaultTableModel)frame.songList.getModel();		
		
		frame.playButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!playIsRunning){
					playingThread.start();
				} else {
					//TODO: skip the song
				}

				timerThread.start();
				frame.playButton.setEnabled(false);
			}
		});
		
		frame.skipButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				broadcast(Command.format(Command.CLIENT_STOP, ""));
				//broadcast(Command.formatCmd(Command.CLIENT_CLEAR_QUEUE, ""));
				skip = true;	
				frame.skipButton.setEnabled(false);
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
					removeSong(url);
				}
				broadcast(Command.format(Command.CLIENT_REMOVE, urls.toArray()));
			}
		});
		
		frame.syncButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				sync();				
			}
		});
		
	}
	
	private void sync(){
		new Thread(){
			@Override
			public void run(){
				frame.syncButton.setEnabled(false);
				Collection<ClientHandler> clients = clientList.values();
				
				for(ClientHandler c:clients){
					c.flagPingTest();
				}
				frame.syncButton.setEnabled(true);
			}			
		}.start();
	}
	
	public boolean voteRemoveSong(String id, String url){
		for(int j = 0; j < songTable.getRowCount(); j++){
			if(url.equals(songTable.getValueAt(j, COL_URL))){
				if(id.equals(songTable.getValueAt(j, COL_USER))){					
					removeSong(url);
					return true;
				} else {					
					if(!removeVotes.containsKey(url)){
						removeVotes.put(url, 0);					
					}					
					removeVotes.put(url, removeVotes.get(url)+1);					
					if(removeVotes.get(url) >= (clientList.size()/2)+1){
						removeSong(url);
						return true;
					}
				}				
			}
		}
		return false;
	}
	
	public void removeSong(String url){
		for(int j = 0; j < songTable.getRowCount(); j++){
			if(url.equals(songTable.getValueAt(j, COL_URL))){
				removeVotes.remove(url);
				songTable.removeRow(j);	
			}
		}				
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
		currentSong = nextSong;
		frame.currentSong.setText(currentSong.substring(currentSong.lastIndexOf("/")+1, currentSong.lastIndexOf(".")));
		for (ClientHandler h : clientList.values()) {
			try {
				if (h.isLoaded()) {
					h.send(Command.format(Command.CLIENT_PLAY, trackStartTime - h.clockOffset));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		

		broadcast(Command.format(Command.CLIENT_REMOVE, currentSong));
	}
	
	public void broadcast(final String b) {
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
	
	public boolean addSong(String song, String user) {
		for (int i = 0; i < songTable.getRowCount(); i++) {
			if (songTable.getValueAt(i, SoundSyncServer.COL_URL).equals(song)) { //song is already in the list
				return false;
			}
		}
		songTable.addRow(new Object[] {song, user });
		//frame.adjuster.adjustColumns();
		songTable.fireTableDataChanged();
		frame.repaint();		
		return true;
	}
	
	public void removeClient(ClientHandler h) {
		clientList.remove(h.id);
	}	
}
