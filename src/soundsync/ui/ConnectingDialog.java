package soundsync.ui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import soundsync.client.SoundSyncClient;

public class ConnectingDialog extends JDialog {
	
	public static void showDialog(final String addr, final String username) {
		if (ConnectingDialog.dialog == null) {
			ConnectingDialog.dialog = new ConnectingDialog(addr, username);
			
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
						client.startInputProcessor();
						ConnectingDialog.dialog.connected = true;
						ConnectingDialog.dialog.dispose();
						ConnectingDialog.dialog = null;
					}
					catch (InterruptedException ignore) {}
					catch (ExecutionException e) {
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
	
	public static ConnectingDialog dialog = null;
	
	boolean connected;
	
	JLabel message;
	ImageIcon loadingImage;
	JLabel loadingLabel;
	
	private ConnectingDialog(final String addr, final String username) {
		
		setTitle("SoundSync Connect");
		
		connected = false;
		
		message = new JLabel();
		loadingImage = new ImageIcon("res/spinner.gif");
		loadingLabel = new JLabel();
		
		message.setText(String.format("Connecting to server at \"%s\" as \"%s\"...", addr, username));
		loadingLabel.setIcon(loadingImage);
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 5, 5);
		getContentPane().add(loadingLabel, c);
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
