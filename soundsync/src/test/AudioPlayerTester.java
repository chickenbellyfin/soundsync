package test;

import soundsync.client.NetAudioPlayer;

public class AudioPlayerTester {

	public static void main(String[] args) {
		NetAudioPlayer p = new NetAudioPlayer();
		p.loadSong("http://localhost/Akshay/Psy/PSY - GENTLEMAN M-V.mp3");
		System.out.println("asdf");
		p.play();
		while(true);

	}

}
