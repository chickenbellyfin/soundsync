package soundsync;

import java.io.Serializable;

public class Song implements Serializable{
	
	private static final long serialVersionUID = -7767597819769697769L;

	public static String formatTime(long time) {
		return String.format("%d:%02d", time / 60, time % 60);
	}
	
	private String owner;
	private String name, artist, album;
	private long length;
	
	public Song(String owner, String name, String artist, String album, long length) {
		this.owner = owner;
		this.name = name;
		this.artist = artist;
		this.album = album;
		this.length = length;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getName() {
		return name;
	}
	
	public String getArtist() {
		return artist == null || artist == "" ? "Unknown Artist" : artist;
	}
	
	public String getAlbum() {
		return album == null || album == "" ? "Unknown Album" : album;
	}
	
	public String formatLength() {
		return Song.formatTime(length);
	}
	
	public long getLength() {
		return length;
	}
	
	public String getInfo() {
		return getName() + " - " + getArtist();
	}
	
}
