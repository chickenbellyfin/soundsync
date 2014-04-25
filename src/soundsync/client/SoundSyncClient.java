package soundsync.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URL;

import soundsync.Command;
import soundsync.Config;
import soundsync.Song;
import soundsync.songfs.FSElement;
import soundsync.ui.ClientFrame;
import soundsync.ui.SongQueueTableModel;
import soundsync.ui.SongSelectorDialog;

/**
 * 
 * @author Akshay
 */
public class SoundSyncClient {
	
	
	//public ClientWindow win;
	
	private ClientFrame mFrame;
	private SongQueueTableModel mQueue;
	private FSElement mSongIndex;
	
	public NetAudioPlayer mAudio;
	private String mId;
	private Socket mServer;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean mIsRunning;
	
	private String mNextSong;
	
	
	private Thread mTimeUpdater = new Thread() {
		
		@Override
		public void run() {
			while (mIsRunning) {
				try{
					Thread.sleep(500);
				}catch(Exception e){}
				mFrame.updateTime((int)(mAudio.getTimeMillis()));

			}
		}
	};
	
	private Thread mInputProcessor = new Thread() {
		
		@Override
		public void run() {
			while (mIsRunning) {
				try {
					String cmd = in.readUTF();
					doCommand(cmd);
				}
				catch (IOException e) {
					System.err.format("Connection error: %s%n", e);
					e.printStackTrace();
					disconnect();
				}
			}
		}
	};
	

	public SoundSyncClient() {		
		mAudio = new NetAudioPlayer();
		
		try {
			InputStream remoteIS = new URL("http://" + Config.SERVER_ADDR + "/index").openStream();
			
			ObjectInputStream ois = new ObjectInputStream(remoteIS);
			
			mSongIndex = (FSElement)(ois.readObject());
//			mInputProcessor.start();
//			mTimeUpdater.start();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		setupGUI();
	}
	
	private void setupGUI() {
		mFrame = new ClientFrame();
		mQueue = new SongQueueTableModel();
		mFrame.songTable.setModel(mQueue);

		mFrame.addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URL[] song_urls = SongSelectorDialog.selectSong(mSongIndex);
				System.out.format("Submitting URL%s:%n",
						song_urls.length > 1 ? "s" : "");
				submitSong(song_urls);
			}
		});

		mFrame.removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selected = mFrame.songTable.getSelectedRows();
				URL[] songsToRemove = new URL[selected.length];
				for (int i = 0; i < selected.length; i++) {
					songsToRemove[i] = mSongIndex.find(mQueue.getSong(selected[i])).url;
				}
				voteRemove(songsToRemove);
			}
		});
		
		mFrame.mute.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				mAudio.setMute(mFrame.mute.isSelected());;			
			}			
		});
	}	
	
	
	public boolean connect(String serverAddr, String user) {
		mId = user;
		System.out.format("Attempting to connect to \"%s\" as \"%s\"...%n", serverAddr, user);
		
		int tries = 3;
		boolean connected = false;
		
		do {
			System.out.format("%d tries left%n", tries);
			if (mServer == null) {
				try {
					mServer = new Socket(serverAddr, Config.PORT);
					out = new DataOutputStream(mServer.getOutputStream());
					in = new DataInputStream(mServer.getInputStream());
					out = new DataOutputStream(mServer.getOutputStream());
					out.writeUTF(user);
					out.writeUTF(Command.PROTOCOL_VERSION);
					out.flush();
					String authResult = in.readUTF();
					
					if (authResult.equalsIgnoreCase(Command.GOOD)) {
						connected = true;
						mServer.setSoTimeout(0);
					}
					else if (authResult.equalsIgnoreCase(Command.BAD)) {
						mServer = null;
					}
				}
				catch (Exception e) {
					System.err.format("Connection error: %s%n", e);
					e.printStackTrace();
					disconnect();
				}
			}
			tries--;
			
		}
		while (!connected && tries > 0);
		
		if (connected) {
			System.out.format("Connected!%n");
			//mIsRunning = true;
			//mInputProcessor.start();
		}
		else {
			System.out.format("Connection failed%n");
		}
		
		return connected;
	}
	
	public void submitSong(URL[] url) {
		sendServerMessage(Command.format(Command.SERVER_ADD, (Object[])url));
	}
	
	public void voteRemove(URL[] url){
		sendServerMessage(Command.format(Command.SERVER_REMOVE, (Object[])url));
	}
	
	private void doCommand(String s) {
		System.out.format("(%s) %s %n", mId, s);
		String[] parts = s.split(Command.CMD_DELIM_REGEX);
		
		try {
			String cmd = parts[0];
			
			switch (cmd) {
			
				case Command.PING:
					sendServerMessage(Command.PING); // ping the server back
					break;
				
				case Command.CLIENT_TIME:
					sendServerMessage("" + System.currentTimeMillis());
					break;
				
				case Command.CLIENT_PLAY:
					long startTime = Long.parseLong(parts[1]);
					playAt(startTime);
					mFrame.setSong(mSongIndex.find(mNextSong).getSong());
					//win.nextSong(); TODO: fix this line
					break;
				
				case Command.CLIENT_STOP:
					mAudio.stop();
					break;
				
				case Command.CLIENT_LOAD: {
					mNextSong = "";
					for (int i = 1; i < parts.length; i++)
						mNextSong += parts[i];
					boolean loaded = mAudio.loadSong(mNextSong);
					long time = mSongIndex.find(mNextSong).getSong().getLength();
					sendServerMessage(Command.format(Command.SERVER_READY, (loaded ? time : -1)));
					break;
				}
				
				case Command.CLIENT_CLEAR_QUEUE:
					mAudio.queue.clear();
					break;
				
				case Command.CLIENT_ADD: {
					for (int i = 2; i < parts.length; i++){
						Song song = mSongIndex.find(parts[i]).getSong();
						song.setOwner(parts[1]);
						mQueue.addSong(song);
					}
					break;
				}
				
				case Command.CLIENT_REMOVE: {
					for (int i = 1; i < parts.length; i++){
						Song song = mSongIndex.find(parts[i]).getSong();
						mQueue.removeSong(song);
					}
					break;
				}
				
			}
		}
		catch (Exception e) {
			System.err.format("Client %s: Error parsing command \"%s\": %s%n", mId, s, e);
			e.printStackTrace();
		}
		
	}
	
	public void playAt(final long time) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
//				if (System.currentTimeMillis() > time) { // anyone with a offset of over 500ms needs this
//					mAudio.setPan((System.currentTimeMillis() - time) * 1000);
//					
//					mAudio.play();
//				} else {
				while (System.currentTimeMillis() < time);
				mAudio.play();
			}
			//}
		}).start();
	}
	
	public void sendServerMessage(String message) {
		try {
			System.out.format("Sending \"%s\" to server%n", message);
			out.writeUTF(message);
			out.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	public void startInputProcessor() {
		if (!mIsRunning) {
			mIsRunning = true;
			mTimeUpdater.start();
			mInputProcessor.start();
		}
	}
	
	private void disconnect() {
		mIsRunning = false;
		if (mServer != null) try {
			mServer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
