package soundsync;

import java.util.ArrayList;
import soundsync.client.SoundSyncClient;
import soundsync.server.SoundSyncServer;
import soundsync.songfs.Indexer;

public class Main {
	
	/**
	 * SoundSyncServer
	 * Indexer
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean error = false;
		String mode;
		String[] restArgs = new String[] {};
		
		if (args.length > 0) {
			mode = args[0];
			ArrayList<String> argList = new ArrayList<String>();
			for (String s : args) {
				argList.add(s);
			}
			argList.remove(0);
			restArgs = argList.toArray(restArgs);
		}
		else {
			error = true;
		}
		
		if (!error) {
			mode = args[0];
			if (mode.startsWith("s") || mode.startsWith("S")) {
				SoundSyncServer.main(restArgs);
			}
			else if (mode.startsWith("c") || mode.startsWith("C")) {
				SoundSyncClient.main(restArgs);
			}
			else if (mode.startsWith("i") || mode.startsWith("I")) {
				Indexer.main(restArgs);
			}
			else {
				error = true;
			}
			
		}
		
		if (error) {
			System.err.println("USAGE: soundsync [server|client|index] [args]");
			System.exit(1);
		}
		
	}
	
}
