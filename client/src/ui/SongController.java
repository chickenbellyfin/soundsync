package ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class SongController extends JPanel {
	
	String user_id;
	
	JPanel info_panel;
	JPanel options_panel;
	JPanel controls_panel;
	JPanel scrubber_panel;
	JPanel volume_panel;
	
	JLabel prev_song_info;
	JLabel curr_song_name;
	JLabel curr_song_info;
	JButton delete_btn, download_btn;
	JButton rwd_btn, play_btn, fwd_btn;
	JLabel curr_time_label, remaining_time_label;
	JSlider scrubber;
	JSlider volume_slider;
	JButton mute_btn;
	
	private Song song, prev_song;
	private int curr_time;
	
	private boolean muted;
	
	public SongController(String user_id) {
		this.user_id = user_id;
		prev_song_info = new JLabel();
		curr_song_name = new JLabel();
		curr_song_info = new JLabel();
		delete_btn = new JButton();
		download_btn = new JButton();
		rwd_btn = new JButton();
		play_btn = new JButton();
		fwd_btn = new JButton();
		curr_time_label = new JLabel();
		remaining_time_label = new JLabel();
		scrubber = new JSlider();
		volume_slider = new JSlider();
		mute_btn = new JButton();
		
		prev_song_info.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
		curr_song_name.setFont(new Font("Comic Sans MS", Font.PLAIN, 30));
		curr_song_info.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
		
		rwd_btn.setText("<<");
		play_btn.setText(">");
		fwd_btn.setText(">>");
		
		delete_btn.setText("X");
		
		download_btn.setText("<3");
		download_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				downloadSong();
			}
		});
		
		setSong(null, false);
		scrubber.setPreferredSize(new Dimension(300, scrubber.getPreferredSize().height));
		
		setMuted(false);
		
		volume_slider.setMinimum(0);
		volume_slider.setMaximum(100);
		volume_slider.setValue(50);
		mute_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setMuted(!muted);
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		info_panel = new JPanel();
		info_panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		info_panel.add(prev_song_info, c);
		c.gridx = 0;
		c.gridy = 1;
		info_panel.add(curr_song_name, c);
		c.gridx = 0;
		c.gridy = 2;
		info_panel.add(curr_song_info, c);
		
		c = new GridBagConstraints();
		options_panel = new JPanel();
		options_panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		options_panel.add(delete_btn, c);
		c.gridx = 1;
		options_panel.add(download_btn, c);
		
		c = new GridBagConstraints();
		controls_panel = new JPanel();
		controls_panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		controls_panel.add(rwd_btn, c);
		c.gridx = 1;
		controls_panel.add(play_btn, c);
		c.gridx = 2;
		controls_panel.add(fwd_btn, c);
		
		c = new GridBagConstraints();
		scrubber_panel = new JPanel();
		scrubber_panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		scrubber_panel.add(curr_time_label, c);
		c.gridx = 1;
		scrubber_panel.add(scrubber, c);
		c.gridx = 2;
		scrubber_panel.add(remaining_time_label, c);
		
		c = new GridBagConstraints();
		volume_panel = new JPanel();
		volume_panel.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		c.anchor = GridBagConstraints.EAST;
		volume_panel.add(volume_slider, c);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;
		volume_panel.add(mute_btn, c);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(info_panel);
		add(options_panel);
		add(controls_panel);
		add(scrubber_panel);
		add(volume_panel);
	}
	
	public Song getSong() {
		return song;
	}
	
	public void setSong(Song song, boolean save_history) {
		if (save_history) prev_song = this.song;
		this.song = song;
		
		if (song != null) {
			scrubber.setMaximum(song.getLength());
			rwd_btn.setEnabled(isOwner());
			play_btn.setEnabled(isOwner());
			delete_btn.setText(isOwner() ? "X" : "(X)");
			fwd_btn.setEnabled(isOwner());
			scrubber.setEnabled(isOwner());
		}
		else {
			scrubber.setMaximum(0);
		}
		
		update(0);
	}
	
	public void setMuted(boolean muted) {
		this.muted = muted;
		mute_btn.setText("mute " + (muted ? "on" : "off"));
	}
	
	public void update(int time) {
		if (song != null) {
			curr_time = time;
			curr_time_label.setText(Song.formatTime(curr_time));
			remaining_time_label.setText("-" + Song.formatTime(song.getLength() - curr_time));
			curr_song_name.setText(song.getName());
			curr_song_info.setText(song.getArtist() + " - " + song.getAlbum());
		}
		else {
			curr_time = 0;
			curr_time_label.setText("--");
			remaining_time_label.setText("--");
			curr_song_name.setText("<select a song>");
			curr_song_info.setText(" ");
		}
		if (prev_song != null) {
			prev_song_info.setText(prev_song.getInfo());
		}
		else {
			prev_song_info.setText(" ");
		}
		scrubber.setValue(curr_time);
	}
	
	public boolean isOwner() {
		return song.getOwner().equals(user_id);
	}
	
	public void downloadSong() {
		// TODO: download song
	}
}
