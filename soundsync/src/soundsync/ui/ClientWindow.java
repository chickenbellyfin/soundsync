package soundsync.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import soundsync.Config;
import soundsync.client.SoundSyncClient;
import soundsync.songfs.FSElement;

public class ClientWindow extends JFrame implements ActionListener, SoundSyncClient.SyncClientListener {
	
	public SoundSyncClient soundClient;
	
	private JLabel user_name;
	public SongController controller;
	public QueueList queue;
	private JButton addButton, removeButton;
	
	public FSElement songList;
	
	private String user_id;
	
	public ClientWindow(String user_id, SoundSyncClient ssc) {
		setTitle("SoundSync Client");
	
		this.user_id = user_id;
		soundClient = ssc;
		ssc.win = this;
		
		setupGUI();
		
		setVisible(true);		
		
		soundClient.startInputProcessor();
	}
	
	private void setupGUI() {
		user_name = new JLabel();
		controller = new SongController(user_id, soundClient);
		queue = new QueueList(user_id);
		addButton = new JButton();
		removeButton = new JButton();		
		user_name.setText("Logged in as: " + user_id);
		user_name.setFont(new Font("Comic Sans MS", Font.PLAIN, 10));
		
		controller.rwd_btn.addActionListener(this);
		controller.fwd_btn.addActionListener(this);
		
//		controller.delete_btn.addActionListener(this);
		
		addButton.setText("Add song...");
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButton.addActionListener(this);
		
		removeButton.setText("Delete songs");
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		removeButton.addActionListener(this);
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		getContentPane().add(user_name, c);
		c.gridx = 0;
		c.gridy = 1;
		getContentPane().add(controller, c);
		c.gridx = 0;
		c.gridy = 2;
		getContentPane().add(queue, c);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		getContentPane().add(addButton, c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(removeButton, c);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		pack();
		setLocationByPlatform(true);
		setResizable(false);
		
		songList = null;
		
		try {
			InputStream remoteIS = new URL("http://" + Config.SERVER_ADDR + "/index").openStream();
			
			ObjectInputStream ois = new ObjectInputStream(remoteIS);
			
			songList = (FSElement)(ois.readObject());
		}
		catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (s == controller.rwd_btn) {
			controller.update(0);
		}
		else if (s == controller.fwd_btn) {
			//nextSong();
		}
		else if (s == addButton) {
			URL[] song_urls = SongSelectorDialog.selectSong(songList);
			System.out.format("Submitting URL%s:%n", song_urls.length > 1 ? "s" : "");
			soundClient.submitSong(song_urls);
//			for (URL url : song_urls) {
//				soundClient.submitSong(url.toString());
//				System.out.format("\t%s%n", url);
//			}
		}
		else if (s == removeButton) {
			int[] selected = queue.table.getSelectedRows();	
			URL[] songsToRemove = new URL[selected.length];
			for(int i = 0; i < selected.length; i++){
				songsToRemove[i] = songList.find(queue.getSong(i)).url;
			}
			soundClient.voteRemove(songsToRemove);
		}
	}
	
	public void nextSong() {
		if (queue.hasSongs()) controller.setSong(queue.getHead(), true);
	}



	@Override
	public void songAdded(String user, String url) {
		// TODO: Add the song
	}

	@Override
	public void syncDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
}
