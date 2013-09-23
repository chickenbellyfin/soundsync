package soundsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import soundsync.Command;

public class ClientHandler {
<<<<<<< .mine

	public static String formatCmd(String cmd, Object... args) {
		String s = cmd;
		for (Object arg : args)
			s += Command.CMD_DELIM + arg.toString();
		return s;
	}

=======
	
>>>>>>> .r23
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
					doCommand(cmd);
				} catch (Exception e) {
					e.printStackTrace();
					disconnect();
				}
			}
		}
	};

	// private Runnable outputProcessor = new Runnable(){
	// @Override
	// public void run(){
	// }
	// };
	public ClientHandler(String id, Socket s, SoundSyncServer srv)
			throws Exception {
		this.id = id;

		this.socket = s;
		this.server = srv;

		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public void doCommand(String s) {
		System.out.format("Client %s processing command: \"%s\"%n", id, s);
<<<<<<< .mine
		String[] parts = s.split(Command.CMD_DELIM_REGEX);

=======
		String[] parts = s.split(Command.CMD_DELIM);
		
>>>>>>> .r23
		try {
			String cmd = parts[0];

			switch (cmd) {
			case Command.SERVER_READY:
				long trackTimeMillis = Long.parseLong(parts[1]);
				loaded = true;
				server.clientLoaded(trackTimeMillis);
				break;
			case Command.SERVER_ADD:
				String url = "";
				for (int i = 1; i < parts.length; i++)
					url += parts[i];
				System.out.println("dan thinks the url is " + url);
				submitSong(url);
				break;
			}
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			System.err.format("Client %s: Error parsing command \"%s\": %s%n",
					id, s, e);
		}
	}

	public void pingTest() {
		int tests = 100;

		long totalPing = 0;

		for (int i = 0; i < 10; i++) {
			try {
<<<<<<< .mine
				send(Command.PING);
=======
				out.writeUTF(Command.PING);
				out.flush();
>>>>>>> .r23
				in.readUTF();
			} catch (Exception e) {
				e.printStackTrace();
				disconnect();
			}
		}

		for (int i = 0; i < tests; i++) {
			try {
<<<<<<< .mine
				send(Command.PING);
=======
				out.writeUTF(Command.PING);
				out.flush();
>>>>>>> .r23
				long sTime = System.currentTimeMillis();
				in.readUTF();
				totalPing += System.currentTimeMillis() - sTime;
			} catch (Exception e) {
				e.printStackTrace();
				disconnect();
			}
		}

		ping = totalPing / tests;
		System.out.format("Client %s ping: %d%n", id, ping);

		try {
<<<<<<< .mine
			send(Command.CLIENT_TIME);
			lag = System.currentTimeMillis()
					- (Long.parseLong(in.readUTF()) + ping / 2);
=======
			out.writeUTF(Command.CLIENT_TIME);
			lag = System.currentTimeMillis() - (Long.parseLong(in.readUTF()) + ping / 2);
>>>>>>> .r23
			System.out.format("Client %s lag: %d%n", id, lag);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendLoad(String url) {
		loaded = false;
		send(Command.formatCmd(Command.CLIENT_LOAD, url));
		System.out.format("Client %s sendLoad%n", id);
	}

	public void send(String msg) {
		try {
			System.out.format("Client %s sending \"%s\"%n", id, msg);
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
			// new Thread(outputProcessor).start();
		}
	}

	private void submitSong(String loc) {
		server.addSong(loc, id);
	}

	private void disconnect() {
		isRunning = false;

		try {
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		server.removeClient(this);
	}
}