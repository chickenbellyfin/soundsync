package soundsync.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;

/** 
 * @author Akshay
 */
public class NetAudioPlayer {
	
	private Clip clip;
	
	private boolean muted = false;
	
	ArrayList<Clip> queue;
	
	public NetAudioPlayer() {
		
		try {
			//clip = AudioSystem.getClip();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		queue = new ArrayList<Clip>();
	}
	
	public void play() {
		try {
			System.out.println("Client play");
			if (clip != null) {
				clip.stop();
				clip.drain();
				clip.flush();
				clip.close();
			}
			if (!queue.isEmpty()) {
				clip = queue.remove(0);
			}
			setMute(muted);
			clip.start();
		}
		catch (Exception e) {
			System.err.format("Error playing clip: %s%n", e);
		}
	}
	
	public void stop() {
		try {
			clip.stop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setPan(long micro) {
		if (clip != null) {
			clip.setMicrosecondPosition(micro);
		}
	}
	
	public void setMute(boolean isMuted) {
		muted = isMuted;
		try {
			if (clip != null) {
				BooleanControl bc = (BooleanControl)clip.getControl(BooleanControl.Type.MUTE);
				if (bc != null) {
					bc.setValue(muted); // true to mute the line, false to unmute
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isMuted() {
		return muted;
	}
	
	public long getTimeMillis() {
		if(clip == null)return 0L;
		return clip.getMicrosecondPosition() / 1000L;
	}
	
	public boolean loadSong(String loc) {
		InputStream s = null;
		try {
			System.out.format("Loading %s%n", loc);
			long sTime = System.currentTimeMillis();
			
			URL url = new URL(loc.replaceAll(" ", "%20"));
			
			s = url.openStream();
			
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(/*soundStream*/s);
			AudioFormat baseFormat = audioStream.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
			
			Clip newClip = AudioSystem.getClip();
			newClip.open(audioStream2);
			
			queue.add(newClip);
			
			System.out.format("Loaded (%dms)%n", System.currentTimeMillis() - sTime);
			return true;
		}
		catch (Exception e) {
			System.err.println("Error loading song:");
			e.printStackTrace();
		}
		
		if (s != null) try {
			s.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
