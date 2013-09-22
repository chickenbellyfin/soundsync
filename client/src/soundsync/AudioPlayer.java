/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package soundsync;

import java.io.ByteArrayInputStream;
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
public class AudioPlayer {
    
    
    public Clip clip;
    private byte[] sound = new byte[1];
    
    
    private boolean muted = false;

    public void play() {
        try {
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            clip.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMute(boolean isMuted) {
        muted = isMuted;
        BooleanControl bc = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
        if (bc != null) {
            bc.setValue(muted); // true to mute the line, false to unmute
        }
    }
    
    public boolean isMuted(){
        return muted;
    }
    
    public long loadSong(String loc) {        
        try {
            System.out.println("loading " + loc);
            long sTime = System.currentTimeMillis();

            URL url = new URL(loc);
            InputStream s = url.openStream();

            ArrayList<Byte> soundArray = new ArrayList<Byte>();
            int n = 0;
            while ((n = s.read()) != -1) {
                soundArray.add((byte) n);
            }


            sound = new byte[soundArray.size()];
            for (int i = 0; i < sound.length; i++) {
                sound[i] = soundArray.get(i);
            }

           

            InputStream soundStream = new ByteArrayInputStream(sound);

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundStream);
            AudioFormat baseFormat = audioStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(decodedFormat, audioStream);

            if(clip != null){
                clip.stop();
            }
            clip = AudioSystem.getClip();
            clip.open(audioStream2);
            
             System.out.println("loaded (" + (System.currentTimeMillis() - sTime) + "ms)");
            //clip.start();
            
            return clip.getMicrosecondLength()/1000;
            //sendServerMessage(clip.getMicrosecondLength() + "#" + loc);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}
