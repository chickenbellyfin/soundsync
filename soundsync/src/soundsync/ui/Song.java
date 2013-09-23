package soundsync.ui;

public class Song {
	
	public static String formatTime(int time) {
		return String.format("%d:%02d", time / 60, time % 60);
	}
	
	private String owner;
	private String name, artist, album;
	private int length;
	
	public Song(String owner, String name, String artist, String album, int length) {
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
	
	public int getLength() {
		return length;
	}
	
	public String getInfo() {
		return getName() + " - " + getArtist();
	}
	
}
