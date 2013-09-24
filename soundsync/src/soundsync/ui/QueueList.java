package soundsync.ui;

import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import soundsync.Song;

public class QueueList extends JPanel {
	
	private class SongQueueTableModel extends AbstractTableModel {
		
		public String[] column_names = new String[] { "Name", "Length", "Artist", "Album", "Owner", "X" };
		
		@Override
		public int getRowCount() {
			return songs.size();
		}
		
		@Override
		public int getColumnCount() {
			return column_names.length;
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return getSongInfo(songs.get(rowIndex), columnIndex);
		}
		
		private Object getSongInfo(Song song, int id) {
			switch (id) {
				case 0:
					return song.getName();
				case 1:
					return song.formatLength();
				case 2:
					return song.getArtist();
				case 3:
					return song.getAlbum();
				case 4:
					return song.getOwner();
				case 5:
					return 0;
				default:
					return null;
			}
		}
		
		@Override
		public String getColumnName(int col) {
			return column_names[col];
		}
		
	}
	
	String user_id;
	
	private JTable table;
	private JScrollPane scrollpane;
	
	private ArrayList<Song> songs;
	
	public QueueList(String user_id) {
		this.user_id = user_id;
		table = new JTable(new SongQueueTableModel());
		table.getColumnModel().getColumn(5).setMinWidth(20);
		table.getColumnModel().getColumn(5).setMaxWidth(20);
		scrollpane = new JScrollPane();
		songs = new ArrayList<Song>();
		
		scrollpane.setViewportView(table);
		table.setFillsViewportHeight(true);
		
		this.add(scrollpane);
	}
	
	public void addSong(Song song) {
		songs.add(song);
		((SongQueueTableModel)table.getModel()).fireTableDataChanged();
		// TODO: update table when you add a song
	}
	
	public void removeSong(Song song){
		songs.remove(song);
		((SongQueueTableModel)table.getModel()).fireTableDataChanged();
	}
	
	public Song popHead() {
		Song s = songs.remove(0);
		((SongQueueTableModel)table.getModel()).fireTableRowsDeleted(0, 1);
		return s;
	}
	
	// TODO: check owner of songs before deleting
	public void deleteSelected() {
		ArrayList<Song> to_be_removed = new ArrayList<Song>();
		for (int c : table.getSelectedRows()) {
			Song s = songs.get(c);
			if (s.getOwner().equals(user_id)) {
				to_be_removed.add(s);
				((SongQueueTableModel)table.getModel()).fireTableRowsDeleted(c, c + 1);
			}
		}
		songs.removeAll(to_be_removed);
	}
	
	public boolean hasSongs() {
		return !songs.isEmpty();
	}
	
}
