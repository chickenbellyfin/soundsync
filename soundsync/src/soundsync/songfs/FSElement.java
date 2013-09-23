package soundsync.songfs;

import java.io.Serializable;
import java.net.URL;
import soundsync.ui.Song;

public class FSElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4281554220143405376L;
	
	private String name;
	private URL url;
	private FSElement[] children;
	
	FSElement(String name, URL url, FSElement[] children) {
		this.name = name;
		this.url = url;
		this.children = children;
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
		if (children != null) for (FSElement c : children) {
			for (int i = 0; i < depth; i++)
				System.out.print("\t");
			c.print(depth + 1);
		}
	}
	
	public FSElement find(String url) {
		return null;
	}
	
	public Song getSong() {
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
