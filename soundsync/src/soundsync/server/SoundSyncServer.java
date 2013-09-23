/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundsync.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import static soundsync.Command.*;

/**
 * 
 * @author Akshay
 */
public class SoundSyncServer implements Runnable {
	
	public final int PORT = 1980;
		
	public static final int COL_URL = 1;
	
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
	
	private boolean isRunning = false;
	
	public SoundSyncServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			serverSocket.setSoTimeout(0);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		timer = new Timer();
		
		setupGUI();
		
		clientList = new HashMap<String, ClientHandler>();
		
	}
	
	@Override
	public void run() {
		isRunning = true;
		
		while (isRunning) {
			try {
				Socket newClient = serverSocket.accept();
				newClient.setSoTimeout(0);
				DataInputStream tmpIn = new DataInputStream(newClient.getInputStream());
				DataOutputStream tmpOut = new DataOutputStream(newClient.getOutputStream());
				
				String user = tmpIn.readUTF();
				String userVersion = tmpIn.readUTF();
				System.out.println(user + " is trying to connect with protocol "+userVersion);
				
				if (userVersion.equals(PROTOCOL_VERSION)) {
					tmpOut.writeUTF(GOOD);
				}
				else {
					System.out.printf("%s is had outdated client (user: %d, current:%s)", user, userVersion, PROTOCOL_VERSION);
					tmpOut.writeUTF(BAD);
				}
				tmpOut.flush();
				
				ClientHandler newClientHandler = new ClientHandler(user, newClient, this);
				//String newClientAddr = newClient.getInetAddress().getHostAddress();
				clientList.put(user, newClientHandler);
				newClientHandler.pingTest();
				newClientHandler.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void clientLoaded(long time) {
		trackLength = time;
		loadCount++;
		if (loadCount == clientList.size()) {
			loadCount = 0;
			sendPlay();
		}
	}
	
	private void setupGUI() {
		frame = new ServerFrame();
		songTable = (DefaultTableModel)frame.songList.getModel();
		
		frame.playButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = frame.songList.getSelectedRow();
				if (selectedRow >= 0) {
					broadcast(CLIENT_CLEAR_QUEUE);
					sendLoad((String)songTable.getValueAt(selectedRow, COL_URL));
				}
			}
		});
		
	}
	
	private void sendLoad(final String url) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for (ClientHandler h : clientList.values()) {
					try {
						h.sendLoad(url);
					}
					catch (Exception e) {}
				}
			}
		}).start();
	}
	
	private void sendPlay() {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
//                for(ClientHandler h:clientList.values()){
//                    try{
//                    	System.out.println("start pingtest");
//                        h.pingTest();
//                        System.out.println("end pt");
//                    }catch(Exception e){
//                        e.printStackTrace();
//                    }
//                } 
				trackStartTime = System.currentTimeMillis() + 500;
				for (ClientHandler h : clientList.values()) {
					try {
						h.send(CLIENT_PLAY + (trackStartTime - h.lag));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
		
	}
	
	private void broadcast(String b) {
		for (ClientHandler h : clientList.values()) {
			try {
				h.send(b);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void addSong(String song, String user) {
		for(int i = 0; i < songTable.getRowCount(); i++){
			
		}
		songTable.addRow(new Object[] { "", song, user });
		frame.adjuster.adjustColumns();
		frame.repaint();
		
		broadcast(CLIENT_ADD+user+"#"+song);
	}
	
	public void removeClient(ClientHandler h) {
		clientList.remove(h.id);
	}
	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SoundSyncServer server = new SoundSyncServer();
		server.run();
	}

}
