package dmsongfs;

import java.io.Serializable;
import java.net.URL;

public class FSElement implements Serializable {
	
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
	
	@Override
	public String toString() {
		return getName();
	}
	
}
