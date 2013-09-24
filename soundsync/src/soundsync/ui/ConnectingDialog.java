package soundsync.ui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import soundsync.client.SoundSyncClient;

public class ConnectingDialog extends JDialog {
	
	public static void showDialog(final String addr, final String username) {
		if (ConnectingDialog.dlg == null) {
			ConnectingDialog.dlg = new ConnectingDialog(addr, username);
			
			final SwingWorker<SoundSyncClient, Void> worker = new SwingWorker<SoundSyncClient, Void>() {
				
				@Override
				public SoundSyncClient doInBackground() {
					SoundSyncClient ssc = new SoundSyncClient();
					ssc.connect(addr, username); //comment out this line if server isnt't up...will also cause other errors
					return ssc;
				}
				
				@Override
				public void done() {
					try {
						SoundSyncClient client = get();
						new ClientWindow(username, client);
						ConnectingDialog.dlg.connected = true;
						ConnectingDialog.dlg.dispose();
						ConnectingDialog.dlg = null;
					}
					catch (InterruptedException ignore) {}
					catch (java.util.concurrent.ExecutionException e) {
						String why = null;
						Throwable cause = e.getCause();
						if (cause != null) {
							why = cause.getMessage();
						}
						else {
							why = e.getMessage();
						}
						System.err.println("Error connecting: " + why);
					}
				}
			};
			
			worker.execute();
		}
		else throw new IllegalStateException("Connection dialog already being shown!");
	}
	
	public static ConnectingDialog dlg = null;
	
	boolean connected;
	
	JLabel message;
	ImageIcon loading_img;
	JLabel loading_lbl;
	
	private ConnectingDialog(final String addr, final String username) {
		
		setTitle("SoundSync Connect");
		
		connected = false;
		
		message = new JLabel();
		loading_img = new ImageIcon("res/spinner.gif");
		loading_lbl = new JLabel();
		
		message.setText(String.format("Connecting to server at \"%s\" as \"%s\"...", addr, username));
		loading_lbl.setIcon(loading_img);
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 5, 5);
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
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
}
