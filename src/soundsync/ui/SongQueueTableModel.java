package soundsync.ui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import soundsync.Song;

public class SongQueueTableModel extends AbstractTableModel {
	
	public static final String[] column_names = new String[] { "Name", "Length", "Artist", "Album", "Owner"};
	
	private ArrayList<Song> songs;
	
	public SongQueueTableModel(){
		songs = new ArrayList<Song>();
	}
	
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
		synchronized (songs) {
			try {
				return getSongInfo(songs.get(rowIndex), columnIndex);
			}
			catch (Exception e) {
				return null;
			}
		}
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
			default:
				return null;
		}
	}
	
	@Override
	public String getColumnName(int col) {
		return column_names[col];
	}
	
	public void addSong(Song song) {
		synchronized (songs) {
			songs.add(song);
			fireTableDataChanged();
		}
	}
	
	public void removeSong(Song song) {
		synchronized (songs) {
			songs.remove(song);
			fireTableDataChanged();
		}
	}
	
	public Song getSong(int i){
		return songs.get(i);
	}
	
	public boolean hasSongs(){
		return !songs.isEmpty();
	}

}
