package soundsync.songfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import soundsync.server.SoundSyncServer;

// akshay-computer (130.215.234.149)

public class Indexer {
	
	public static FSElement loadFileTree(File root_folder) {
		return Indexer.getFile(root_folder, 0);
	}
	
	private static FSElement getFile(File root, int depth) {
		URL url = Indexer.makeURL(root);
		if (root.isFile()) {
			if (Indexer.isMusicFile(root)) {
				if (Indexer.verbose) Indexer.printFSE(root, url, depth);
				return new FSElement(root.getName(), url, null);
			}
			else return null;
		}
		else {
			if (Indexer.verbose) Indexer.printFSE(root, url, depth);
			File[] subs = root.listFiles();
			ArrayList<FSElement> sub_elements = new ArrayList<FSElement>();
			for (File sub : subs) {
				FSElement e = Indexer.getFile(sub, depth + 1);
				if (e != null) sub_elements.add(e);
			}
			return new FSElement(root.getName(), url, sub_elements.toArray(new FSElement[] {}));
		}
	}
	
	private static void printFSE(File f, URL url, int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print("\t");
		System.out.println(f.getName() + " " + url);
	}
	
	private static URL makeURL(File f) {
		try {
			String p = f.getAbsolutePath().replaceAll("\\\\", "/");
			p = p.substring(p.indexOf('.') + 1);
			return new URL("http://" + SoundSyncServer.SERVER_ADDR + p);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean isMusicFile(File f) {
		String ext = Indexer.fileExt(f).toLowerCase();
		return ext.equals("mp3") || ext.equals("ogg") || ext.equals("wav") || ext.equals("m4a");
	}
	
	private static String fileExt(File f) {
		return f.getName().substring(f.getName().lastIndexOf('.') + 1);
	}
	
	public static boolean verbose;
	
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.err.println("Usage: soundsync.Main index OUTPUT ROOT [-verbose]");
			System.exit(1);
		}
		
		Indexer.verbose = args.length == 3;
		
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(args[0])));
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FSElement root = Indexer.loadFileTree(new File(args[1]));
		
		try {
			out.writeObject(root);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
