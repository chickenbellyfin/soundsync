package soundsync;

import javax.swing.UIManager;
import soundsync.ui.LoginWindow;

public class Main {
	
	// sync actions by sending exact time to execute in message, should be a little more than max latency from current time
	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		new LoginWindow();
	}
	
}
