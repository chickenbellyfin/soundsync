package soundsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import soundsync.Command;
import soundsync.Config;

public class ClientHandler {

	
	public String id;
	private SoundSyncServer server;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private boolean loaded = false;
	private boolean isRunning = false;
	
	private boolean pingTestFlag = false;
	
	public long clockOffset;
	
	// TODO: have window with tabs for each client's output and client-specific commands
	
	private Runnable inputProcessor = new Runnable() {
		
		@Override
		public void run() {
			while (isRunning) {
				try {
					String cmd = in.readUTF();
					doCommand(cmd);
				}
				catch (IOException e) {
					e.printStackTrace();
					System.err.format("[%s] DISCONNECT : %s %n", id, e);
					disconnect();
				}
				
				if(pingTestFlag) {
					clockOffsetTest();
				}
				
			}
		}
	};
	

	public ClientHandler(String id, Socket s, SoundSyncServer srv) throws Exception {
		this.id = id;
		
		this.socket = s;
		this.server = srv;
		
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}
	
	private void doCommand(String s) {
		System.out.format("[%s] %s %n", id, s);

		String[] parts = s.split(Command.CMD_DELIM_REGEX);

		String cmd = parts[0];
			
		switch (cmd) {
			case Command.SERVER_READY:
				long trackTimeMillis = Long.parseLong(parts[1]);
				if (trackTimeMillis > 0) {
					loaded = true;
					server.setTrackLength(trackTimeMillis);
				}
				server.clientLoaded();
				break;
			case Command.SERVER_ADD: {
				for (int i = 1; i < parts.length; i++) {
					submitSong(parts[i]);
				}
				break;
			}
				
			case Command.SERVER_REMOVE: {
				for (int i = 1; i < parts.length; i++) {
					removeSong(parts[i]);
				}
				break;
			}
		}
		
	}
	
	public void clockOffsetTest() {
		
		long totalPing = 0;
		long ping = 0;
		
//		for (int i = 0; i < 10; i++) {
//			try {
//				send(Command.PING);
//
//				in.readUTF();
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//				disconnect();
//			}
//		}
		
		for (int i = 0; i < Config.LAG_TEST_COUNT; i++) {
			try {
				long sTime = System.currentTimeMillis();
				send(Command.PING);
				in.readUTF();
				totalPing += System.currentTimeMillis() - sTime;
			}
			catch (Exception e) {
				e.printStackTrace();
				disconnect();
			}
		}
		
		ping = totalPing / Config.LAG_TEST_COUNT;
		
		try {
			send(Command.CLIENT_TIME);
			clockOffset = System.currentTimeMillis() - (Long.parseLong(in.readUTF()) + ping / 2);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendLoad(String url) {
		loaded = false;
		send(Command.format(Command.CLIENT_LOAD, url));
	}
	
	public void send(String msg) {
		try {
			System.out.format("[%s]-> %s %n", id, msg);
			out.writeUTF(msg);
			out.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
		
	}
	
	public void flagPingTest(){
		pingTestFlag = true;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public void start() {
		if (!isRunning) {
			isRunning = true;
			new Thread(inputProcessor).start();
			// new Thread(outputProcessor).start();
		}
	}
	
	private void submitSong(String loc) {
		server.addSong(loc, id);
	}
	
	private void removeSong(String url){
		server.voteRemoveSong(id, url);
	}
	
	private void disconnect() {
		System.out.println("disconnect()");
		isRunning = false;
		
		try {
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		server.removeClient(this);
	}
}