package soundsync.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import soundsync.Command;
import soundsync.Config;
import soundsync.Song;
import soundsync.ui.ClientWindow;

/**
 * 
 * @author Akshay
 */
public class SoundSyncClient {
	
	public interface SyncClientListener {
		
		public void songAdded(String user, String url);
		
		public void syncDisconnected();
	}
	
	public ClientWindow win;
	public NetAudioPlayer mAudio;
	private String mId;
	private Socket mServer;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean mIsRunning;
	
	private SyncClientListener mListener;
	
	private Thread mWindowUpdater = new Thread() {
		
		public void run() {
			while (mIsRunning) {
				win.controller.update((int)(mAudio.getTimeMillis()));
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
	
	// private Runnable outputProcessor = new Runnable() {
	// @Override
	// public void run() {
	// }
	// };
	public SoundSyncClient() {
		win = null;
		mAudio = new NetAudioPlayer();
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
	
	public void submitSong(String url) {
		sendServerMessage(Command.formatCmd(Command.SERVER_ADD, url));
	}
	
	private void doCommand(String s) {
		System.out.format("Client %s processing command: \"%s\"%n", mId, s);
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
					win.nextSong();
					break;
				
				case Command.CLIENT_STOP:
					mAudio.stop();
					break;
				
				case Command.CLIENT_LOAD: {
					String url = "";
					for (int i = 1; i < parts.length; i++)
						url += parts[i];
					boolean loaded = mAudio.loadSong(url);
					long time = win.songList.find(url).getSong().getLength();
					sendServerMessage(Command.formatCmd(Command.SERVER_READY, (loaded ? time : -1)));
					break;
				}
				
				case Command.CLIENT_CLEAR_QUEUE:
					mAudio.queue.clear();
					break;
				
				case Command.CLIENT_ADD: {
					String url = "";
					for (int i = 2; i < parts.length; i++)
						url += parts[i];
					Song song = win.songList.find(url).getSong();
					song.setOwner(parts[1]);
					win.queue.addSong(song);
					break;
				}
				
				case Command.CLIENT_REMOVE: {
					String url = "";
					for (int i = 1; i < parts.length; i++)
						url += parts[i];
					Song song = win.songList.find(url).getSong();
					win.queue.removeSong(song);
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
				mWindowUpdater.start();
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
		
		if (mListener != null) {
			mListener.syncDisconnected();
		}
		
	}
}
