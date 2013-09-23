package soundsync.songfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

// akshay-computer (130.215.234.149)

public class Main {
	
	public static FSElement loadFileTree(File root_folder) {
		return getFile(root_folder, 0);
	}
	
	private static FSElement getFile(File root, int depth) {
		URL url = makeURL(root);
		if (root.isFile()) {
			if (isMusicFile(root)) {
				if (verbose) printFSE(root, url, depth);
				return new FSElement(root.getName(), url, null);
			}
			else return null;
		}
		else {
			if (verbose) printFSE(root, url, depth);
			File[] subs = root.listFiles();
			ArrayList<FSElement> sub_elements = new ArrayList<FSElement>();
			for (File sub : subs) {
				FSElement e = getFile(sub, depth + 1);
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
			return new URL("http://130.215.234.149" + p);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean isMusicFile(File f) {
		String ext = fileExt(f).toLowerCase();
		return ext.equals("mp3") || ext.equals("ogg") || ext.equals("wav") || ext.equals("m4a");
	}
	
	private static String fileExt(File f) {
		return f.getName().substring(f.getName().lastIndexOf('.') + 1);
	}
	
	public static boolean verbose;
	
	public static void main(String[] args) {
		
		if (args.length != 2 && args.length != 3) {
			System.err.println("Usage: dmsongfs.Main song_list_file_path song_files_root_path [-verbose]");
			System.exit(1);
		}
		
		verbose = args.length == 3;
		
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(new File(args[0])));
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FSElement root = loadFileTree(new File(args[1]));
		
		try {
			out.writeObject(root);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
