package soundsync.songfs;

import java.io.Serializable;
import java.net.URL;

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
		System.out.println(getName());
		if (children != null) {
			for (FSElement c : children) {
				c.print();
			}
		}
	}

	@Override
	public String toString() {
		return getName();
	}

}
