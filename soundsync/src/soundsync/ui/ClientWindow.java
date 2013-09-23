package soundsync.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import soundsync.client.SoundSyncClient;
import soundsync.songfs.FSElement;

public class ClientWindow extends JFrame implements ActionListener {
	
	private SoundSyncClient soundClient;
	
	private JLabel user_name;
	private SongController controller;
	private QueueList queue;
	private JButton add_song_btn, delete_songs_btn;
	
	private FSElement song_list;
	
	private String user_id;
	
	public ClientWindow(String user_id) {
		setTitle("Dorm Music Client");
		
		this.user_id = user_id;
		
		soundClient = new SoundSyncClient();
		soundClient.connect("130.215.234.149", user_id); //comment out this line if server isnt't up...will also cause other errors
		
		setupGUI();
	}
	
	private void setupGUI() {
		user_name = new JLabel();
		controller = new SongController(user_id, soundClient);
		queue = new QueueList(user_id);
		add_song_btn = new JButton();
		delete_songs_btn = new JButton();
		
		user_name.setText("Logged in as: " + user_id);
		user_name.setFont(new Font("Comic Sans MS", Font.PLAIN, 10));
		
		controller.rwd_btn.addActionListener(this);
		controller.fwd_btn.addActionListener(this);
		
		controller.delete_btn.addActionListener(this);
		
		add_song_btn.setText("add song...");
		add_song_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		add_song_btn.addActionListener(this);
		
		delete_songs_btn.setText("delete songs");
		delete_songs_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		delete_songs_btn.addActionListener(this);
		
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
		getContentPane().add(add_song_btn, c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(delete_songs_btn, c);
		
		pack();
		setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
		song_list = null;
		
		queue.addSong(new Song("daniel", "Cyan", "Arno Cost", "bla", 7 * 60 + 43));
		queue.addSong(new Song("akshay", "Hello Goodbye", "The Beatles", null, 3 * 60 + 14));
		queue.addSong(new Song("daniel", "Seven Nation Army", "The White Stripes", "something", 4 * 60 + 14));
		controller.setSong(queue.popHead(), true);
		
		//File ff = new File("index");
		//System.out.println(ff.getAbsolutePath());
		
		try {
			InputStream remoteIS = new URL("http://130.215.234.149/index").openStream();
			//InputStream localIS = new FileInputStream(new File("index"));
			
			ObjectInputStream ois = new ObjectInputStream(remoteIS);
			
			setSongList((FSElement)(ois.readObject()));
			//ois.close();
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
			if (queue.hasSongs()) controller.setSong(queue.popHead(), true);
		}
		else if (s == controller.delete_btn) {
			if (controller.isOwner()) {
				if (queue.hasSongs()) controller.setSong(queue.popHead(), false);
			}
			else {
				// TODO: vote to delete song
			}
		}
		else if (s == add_song_btn) {
			URL[] song_urls = SongSelectorDialog.selectSong(song_list);
			System.out.println("Selected URL" + (song_urls.length > 1 ? "s" : "") + ":");
			for (URL url : song_urls) {
				soundClient.submitSong(url.toString());
				System.out.println(url);
			}
		}
		else if (s == delete_songs_btn) {
			queue.deleteSelected();
		}
	}
	
	public void setSongList(FSElement fs) {
		song_list = fs;
		fs.print();
		//System.out.println((fs.getChildren()[0]).getChildren()[0].toString());
	}
	
}
