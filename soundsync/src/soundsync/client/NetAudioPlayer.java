/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundsync.client;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;

/**
 * 
 * @author Akshay
 */
public class NetAudioPlayer {
	
	public Clip clip;
	
	private boolean muted = false;
	
	ArrayList<Clip> queue;
	
	public NetAudioPlayer() {
		queue = new ArrayList<Clip>();
	}
	
	public void play() {
		
		try {
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
			e.printStackTrace();
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
	
	public void setMute(boolean isMuted) {
		muted = isMuted;
		try {
			BooleanControl bc = (BooleanControl)clip.getControl(BooleanControl.Type.MUTE);
			if (bc != null) {
				bc.setValue(muted); // true to mute the line, false to unmute
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isMuted() {
		return muted;
	}
	
	public long loadSong(String loc) {
		try {
			System.out.println("loading " + loc);
			long sTime = System.currentTimeMillis();
			
			URL url = new URL(loc.replaceAll(" ", "%20"));
			
			InputStream s = url.openStream();
			
//            ArrayList<Byte> soundArray = new ArrayList<Byte>();
//            int n = 0;
//            while ((n = s.read()) != -1) {
//                soundArray.add((byte) n);
//            }
//
//            byte[] sound = new byte[soundArray.size()];
//            for (int i = 0; i < sound.length; i++) {
//                sound[i] = soundArray.get(i);
//            }           
//
//            InputStream soundStream = new ByteArrayInputStream(sound);
			
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(/*soundStream*/s);
			AudioFormat baseFormat = audioStream.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
			AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
			
			Clip newClip = AudioSystem.getClip();
			
			newClip.open(audioStream2);
			
			queue.add(newClip);
			
			System.out.println("loaded (" + (System.currentTimeMillis() - sTime) + "ms)");
			//clip.start();
			
			return newClip.getMicrosecondLength() / 1000;
			//sendServerMessage(clip.getMicrosecondLength() + "#" + loc);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
}
