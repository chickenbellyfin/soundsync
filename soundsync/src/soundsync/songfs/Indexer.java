package soundsync.songfs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import soundsync.Song;
import soundsync.server.SoundSyncServer;
import soundsync.ui.IndexerDialog;

import com.mpatric.mp3agic.Mp3File;

// akshay-computer (130.215.234.149)
// dan-computer (130.215.234.145)

public class Indexer {
	
	private static int fileCount = 0;
	private static int processedFileCount = 0;
	
	public static FSElement loadFileTree(File root_folder) {
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
				
				processedFileCount++;
				monitor.setProgress(processedFileCount);
				monitor.setNote(processedFileCount+"/"+fileCount);
				if(monitor.isCanceled()){
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
		return ext.equals("mp3");
	}
	
	private static String fileExt(File f) {
		return f.getName().substring(f.getName().lastIndexOf('.') + 1);
	}
	
	public static boolean verbose;
	public static ProgressMonitor monitor;
	
	public static File root;
	
	public static void showIndexerDialog(){
		IndexerDialog dialog = new IndexerDialog(new JFrame(), true);
		dialog.setVisible(true);
		System.out.println("dialog closed");
		if(dialog.ok){
			root = new File(dialog.getSelectedRoot());
			
			fileCount = countFiles(root);

			monitor = new ProgressMonitor(new JFrame(), "Indexing MP3 files...", "Preparing...", 0, fileCount);
			
			new IndexerThread().start();
			
		}
	}
	
	static class IndexerThread extends Thread {
		public void run(){
			
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(new File("index")));
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			monitor.setNote(processedFileCount+"/"+fileCount);
			long sTime = System.currentTimeMillis();
			FSElement rootElement = Indexer.loadFileTree(root);
			try {
				out.writeObject(rootElement);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			System.out.printf("Indexed %d music files in %d ms", processedFileCount, (System.currentTimeMillis() - sTime));
			System.exit(0);

		}
	}
	
	public static int countFiles(File root){
		int count = 0;
		if(root.isDirectory()){
			File[] children  = root.listFiles();
			for(File c:children){
				count += countFiles(c);
			}
		} else if(root.getName().toLowerCase().endsWith("mp3")) {
			return 1;
		}
		return count;
	}
	

//	
//	public static void main(String[] args) {
//		long sTime = System.currentTimeMillis();
//		if (args.length < 2) {
//			System.err.println("Usage: soundsync.Main index OUTPUT ROOT [-verbose]");
//			System.exit(1);
//		}
//		
//		Indexer.verbose = args.length == 3;
//		
//		ObjectOutputStream out = null;
//		try {
//			out = new ObjectOutputStream(new FileOutputStream(new File(args[0])));
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//		
//		FSElement root = Indexer.loadFileTree(new File(args[1]));
//		
//		try {
//			out.writeObject(root);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.printf("Indexed %d music files in %d ms", fileCount, (System.currentTimeMillis() - sTime));
//		
//	}
}
