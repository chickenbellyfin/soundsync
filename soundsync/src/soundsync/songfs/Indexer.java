package soundsync.songfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import soundsync.Song;
import soundsync.ui.IndexerDialog;
import com.mpatric.mp3agic.Mp3File;

// akshay-computer (130.215.234.149)
// dan-computer (130.215.234.145)

public class Indexer {
	
	private static long startTime;
	private static int fileCount = 0;
	private static int processedFileCount = 0;
	
	private static File root_folder;
	
	public static FSElement loadFileTree(File root_folder) {
		Indexer.root_folder = root_folder;
		return Indexer.getFile(root_folder, 0);
	}
	
	private static FSElement getFile(File root, int depth) {
		URL url = Indexer.makeURL(root);
		if (root.isFile()) {
			if (Indexer.isMusicFile(root)) {
				String title = "";
				String album = "";
				String artist = "";
				long length = 0;
				
				try {
					Mp3File tmp = new Mp3File(root.getAbsolutePath());
					length = tmp.getLengthInMilliseconds();
					if (tmp.hasId3v2Tag()) {
						album = tmp.getId3v2Tag().getAlbum();
						artist = tmp.getId3v2Tag().getArtist();
						title = tmp.getId3v2Tag().getTitle();
					}
					else if (tmp.hasId3v1Tag()) {
						album = tmp.getId3v1Tag().getAlbum();
						artist = tmp.getId3v1Tag().getArtist();
						title = tmp.getId3v1Tag().getTitle();
					}
				}
				catch (Exception e) {
					System.err.format("Error reading MP3 file: %s%n", e);
					e.printStackTrace();
				}
				
				Indexer.processedFileCount++;
				Indexer.monitor.setProgress(Indexer.processedFileCount);
				long remaining = ((System.currentTimeMillis() - Indexer.startTime) / Indexer.processedFileCount) * (Indexer.fileCount - Indexer.processedFileCount);
				remaining /= 1000;
				Indexer.monitor.setNote(String.format("%d:%02d remaining (%d/%d)", (remaining / 60), (remaining % 60), Indexer.processedFileCount, Indexer.fileCount));
				//monitor.setNote(processedFileCount+"/"+fileCount);
				if (Indexer.monitor.isCanceled()) {
					System.exit(0);
				}
				
				Song song = new Song("", title, artist, album, length);
				if (Indexer.verbose) Indexer.printFSE(root, song, depth);
				return new FSElement(root.getName(), url, null, song);
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
			return new FSElement(root.getName(), url, sub_elements.toArray(new FSElement[] {}), null);
		}
	}
	
	private static void printFSE(File f, Song song, int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print("\t");
		System.out.println(f.getName() + " " + song.getName());
	}
	
	private static void printFSE(File f, URL url, int depth) {
		for (int i = 0; i < depth; i++)
			System.out.print("\t");
		System.out.println(f.getName() + " " + url);
	}
	
	private static URL makeURL(File f) {
		try {
			//System.out.println(f.getAbsolutePath().replaceAll("\\\\", "/"));
			//System.out.println(("\\Q" + root_folder.getAbsolutePath().replaceAll("\\\\", "/")  + "\\E"));
			String p = f.getAbsolutePath().replaceAll("\\\\", "/").replaceFirst(("\\Q" + Indexer.root_folder.getAbsolutePath().replaceAll("\\\\", "/") + "\\E"), "http://" + InetAddress.getLocalHost().getHostAddress());
			//System.out.println(p);
			return new URL(p);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean isMusicFile(File f) {
		String ext = Indexer.fileExt(f).toLowerCase();
		return ext.equals("mp3");
	}
	
	private static String fileExt(File f) {
		return f.getName().substring(f.getName().lastIndexOf('.') + 1);
	}
	
	public static boolean verbose;
	public static ProgressMonitor monitor;
	
	public static File root;
	
	public static void showIndexerDialog() {
		IndexerDialog dialog = new IndexerDialog(new JFrame(), true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		if (dialog.ok) {
			Indexer.root = new File(dialog.getSelectedRoot());
			Indexer.fileCount = Indexer.countFiles(Indexer.root);
			
			Indexer.monitor = new ProgressMonitor(new JFrame(), "Indexing MP3 files...", "Preparing...", 0, Indexer.fileCount);
			
			new IndexerThread().start();
			
		}
	}
	
	static class IndexerThread extends Thread {
		
		@Override
		public void run() {
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(new File(Indexer.root.getAbsolutePath() + "\\index")));
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			Indexer.monitor.setNote(Indexer.processedFileCount + "/" + Indexer.fileCount);
			Indexer.startTime = System.currentTimeMillis();
			FSElement rootElement = Indexer.loadFileTree(Indexer.root);
			try {
				out.writeObject(rootElement);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			System.out.printf("Indexed %d music files in %d ms", Indexer.processedFileCount, (System.currentTimeMillis() - Indexer.startTime));
			System.exit(0);
		}
	}
	
	public static int countFiles(File root) {
		int count = 0;
		if (root.isDirectory()) {
			File[] children = root.listFiles();
			for (File c : children) {
				count += Indexer.countFiles(c);
			}
		}
		else if (root.getName().toLowerCase().endsWith("mp3")) { return 1; }
		return count;
	}
}
