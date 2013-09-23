package soundsync.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class ConnectingDialog extends JDialog {
	
	public static void showDialog(String addr, String username) {
		if (dlg == null) dlg = new ConnectingDialog(addr, username);
		else throw new IllegalStateException("Connection dialog already being shown!");
	}
	
	public static void closeDialog() {
		if (dlg != null) {
			dlg.connected = true;
			dlg.dispose();
			dlg = null;
		}
		else throw new IllegalStateException("No connection dialog open");
	}
	
	public static ConnectingDialog dlg = null;
	
	boolean connected;
	
	JLabel message;
	ImageIcon loading_img;
	JLabel loading_lbl;
	
	private ConnectingDialog(String addr, String username) {
		setTitle("SoundSync Connect");
		
		connected = false;
		
		message = new JLabel();
		loading_img = new ImageIcon("res\\spinner.gif");
		loading_lbl = new JLabel();
		
		message.setText(String.format("Connecting to server at %s as %s...", addr, username));
		loading_lbl.setIcon(loading_img);
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		getContentPane().add(loading_lbl, c);
		c.gridx = 1;
		c.gridy = 0;
		getContentPane().add(message, c);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				if (!connected) System.exit(0);
			}
		});
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
}
