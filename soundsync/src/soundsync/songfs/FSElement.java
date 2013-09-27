package soundsync.songfs;

import java.io.Serializable;
import java.net.URL;
import soundsync.Song;

public class FSElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4281554220143405377L;

	private String name;
	public URL url;

	private Song song;

	private FSElement[] children;

	FSElement(String name, URL url, FSElement[] children, Song song) {
		this.name = name;
		this.url = url;
		this.children = children;
		this.song = song;
	}

	public String getName() {
		return name;
	}

	public URL getURL() {
		return url;
	}

	public FSElement[] getChildren() {
		return children;
	}

	public void print() {
		print(0);
	}

	private void print(int depth) {
		System.out.println(getName());
		if (children != null)
			for (FSElement c : children) {
				for (int i = 0; i < depth; i++)
					System.out.print("\t");
				c.print(depth + 1);
			}
	}

	public FSElement find(String url) {
		if (this.url.toString().equals(url)) {
			return this;
		} else {
			if (children == null)
				return null;
			for (FSElement e : children) {
				FSElement tmp;
				if ((tmp = e.find(url)) != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	public FSElement find(Song song){
		if(this.song == song){
			return this;
		} else {
			if(children == null){
				return null;
			}
			for (FSElement e : children) {
				FSElement tmp;
				if ((tmp = e.find(song)) != null) {
					return tmp;
				}
			}
		}
		return null;
	}

	public Song getSong() {
		return song;
	}

	@Override
	public String toString() {
		return getName();
	}

}
