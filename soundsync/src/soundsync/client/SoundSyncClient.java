package soundsync.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.UIManager;
import soundsync.Command;
import soundsync.ui.LoginWindow;

/**
 * 
 * @author Akshay
 */
public class SoundSyncClient {
	
	public interface SyncClientListener {
		
		public void syncDisconnected();
	}
	
	public static final int PORT = 1980;
	
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
					String cmd = in.readUTF();
					doCommand(cmd);
				}
				catch (IOException e) {
					System.err.format("Connection error: %s%n", e);
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
		id = user;
		System.out.format("Attempting to connect to \"%s\" as \"%s\"...%n", serverAddr, user);
		
		int tries = 3;
		boolean connected = false;
		
		do {
			System.out.format("%d tries left%n", tries);
			if (server == null) {
				try {
					server = new Socket(serverAddr, SoundSyncClient.PORT);
					in = new DataInputStream(server.getInputStream());
					out = new DataOutputStream(server.getOutputStream());
					
					out.writeUTF(user);
					out.writeUTF(Command.PROTOCOL_VERSION);
					out.flush();
					String authResult = in.readUTF();
					
					if (authResult.equalsIgnoreCase(Command.GOOD)) {
						connected = true;
						server.setSoTimeout(0);
					}
					else if (authResult.equalsIgnoreCase(Command.BAD)) {
						server = null;
					}
				}
				catch (Exception e) {
					System.err.format("Connection error: %s%n", e);
					disconnect();
				}
			}
			tries--;
			
		}
		while (!connected && tries > 0);
		
		if (connected) {
			System.out.format("Connected!%n");
			isRunning = true;
			new Thread(inputProcessor).start();
			//new Thread(outputProcessor).start();
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
		System.out.format("Client %s processing command: \"%s\"%n", id, s);
		String[] parts = s.split(Command.CMD_DELIM_REGEX);
		
		try {
			String cmd = parts[0];
			
			switch (cmd) {
				case Command.PING:
					sendServerMessage(Command.PING); //ping the server back
					break;
				case Command.CLIENT_TIME:
					sendServerMessage("" + System.currentTimeMillis());
					break;
				case Command.CLIENT_PLAY:
					long startTime = Long.parseLong(parts[1]);
					playAt(startTime);
					break;
				case Command.CLIENT_STOP:
					audio.stop();
					break;
				case Command.CLIENT_LOAD:
					String url = "";
					for (int i = 1; i < parts.length; i++)
						url += parts[i];
					long time = audio.loadSong(url); //its probably a url
					sendServerMessage(Command.formatCmd(Command.SERVER_READY, "" + time));
					break;
				case Command.CLIENT_CLEAR_QUEUE:
					audio.queue.clear();
					break;
			}
		}
		catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			System.err.format("Client %s: Error parsing command \"%s\": %s%n", id, s, e);
		}
		
	}
	
	public void playAt(final long time) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (System.currentTimeMillis() > time) { // anyone with a offset of over 500ms needs this
					audio.clip.setMicrosecondPosition((System.currentTimeMillis() - time) * 1000);
					audio.play();
					System.out.println("audio.play()");
				}
				else {
					while (System.currentTimeMillis() < time);
					audio.play();
					System.out.println("Client play");
				}
			}
		}).start();
	}
	
	public void sendServerMessage(String message) {
		try {
			System.out.format("Sending \"%s\" to client %s%n", message, id);
			out.writeUTF(message);
			out.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	private void disconnect() {
		isRunning = false;
		if (server != null) try {
			server.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (listener != null) {
			listener.syncDisconnected();
		}
		
	}
	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		new LoginWindow();
	}
}
