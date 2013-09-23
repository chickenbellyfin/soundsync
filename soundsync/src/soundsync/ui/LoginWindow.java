package soundsync.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginWindow extends JFrame {
	
	JLabel title;
	JLabel login_lbl;
	JTextField login_name;
	JButton login_btn, exit_btn;
	
	boolean start_client;
	
	public LoginWindow() {
		setTitle("SoundSync Client");
		
		start_client = false;
		
		title = new JLabel();
		login_lbl = new JLabel();
		login_name = new JTextField();
		login_btn = new JButton();
		exit_btn = new JButton();
		
		title.setText("SoundSync Login");
		title.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		login_lbl.setText("Username:");
		
		login_name.setColumns(20);
		login_name.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					start_client = true;
					dispose();
				}
			}
		});
		login_name.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkValue();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkValue();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkValue();
			}
			
			private void checkValue() {
				login_btn.setEnabled(!login_name.getText().equals(""));
			}
		});
		
		login_btn.setText("Log in");
		login_btn.setEnabled(false);
		login_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				start_client = true;
				dispose();
			}
		});
		
		exit_btn.setText("Exit");
		exit_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.insets = new Insets(10, 0, 15, 0);
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(title, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 0);
		getContentPane().add(login_lbl, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 5, 5);
		getContentPane().add(login_name, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		getContentPane().add(login_btn, c);
		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 1;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(exit_btn, c);
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				if (start_client) new ClientWindow(login_name.getText());
				else System.exit(0);
			}
		});
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
