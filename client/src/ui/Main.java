package ui;

import javax.swing.UIManager;

public class Main {
	
	// sync actions by sending exact time to execute in message, should be a little more than max latency from current time
	
	public static void main(String[] args) {
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		LoginWindow loginWnd = new LoginWindow();
	}
	
}
