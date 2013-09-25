package soundsync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import soundsync.server.SoundSyncServer;
import soundsync.songfs.Indexer;
import soundsync.ui.LoginWindow;

public class Main {	

	public static Launcher launcher;
	
	public static void main(String[] args) {
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){}
		
		launcher = new Launcher();
		
		launcher.clientButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				startClient();
			}
		});
		
		launcher.serverButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				startServer();
			}
		});
		
		launcher.indexButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				
				startIndexer();
			}
		});		
	}
	
	
	public static void startClient(){
		launcher.dispose();
		new LoginWindow();		
	}
	
	public static void startServer(){
		launcher.dispose();
		SoundSyncServer server = new SoundSyncServer();
		new Thread(server).start();		
	}
	
	public static void startIndexer(){
		
		int needed = JOptionPane.showOptionDialog(new JFrame(), "Are you trying hosting a SoundSync Server?", "Indexer",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if(needed == JOptionPane.YES_OPTION){
			launcher.dispose();
			Indexer.showIndexerDialog();
		} else {
			JOptionPane.showMessageDialog(new JFrame(), "You don't need this.");
			launcher.indexButton.setEnabled(false);			
		}
		
		
	}

	

		
	
	
	 
//	public static void main(String[] args) {
//		boolean error = false;
//		String mode;
//		String[] restArgs = new String[] {};
//		
//		if (args.length > 0) {
//			mode = args[0];
//			ArrayList<String> argList = new ArrayList<String>();
//			for (String s : args) {
//				argList.add(s);
//			}
//			argList.remove(0);
//			restArgs = argList.toArray(restArgs);
//		}
//		else {
//			error = true;
//		}
//		
//		if (!error) {
//			mode = args[0];
//			if (mode.startsWith("s") || mode.startsWith("S")) {
//				SoundSyncServer.main(restArgs);
//			}
//			else if (mode.startsWith("c") || mode.startsWith("C")) {
//				SoundSyncClient.main(restArgs);
//			}
//			else if (mode.startsWith("i") || mode.startsWith("I")) {
//				Indexer.main(restArgs);
//			}
//			else {
//				error = true;
//			}
//			
//		}
//		
//		if (error) {
//			System.err.println("USAGE: soundsync [server|client|index] [args]");
//			System.exit(1);
//		}
//		
//	}
	
}
