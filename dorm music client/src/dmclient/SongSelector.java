package dmclient;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import dmsongfs.FSElement;

public class SongSelector extends JDialog implements TreeSelectionListener {
	
	public static URL[] selectSong(Frame parent, FSElement root) {
		SongSelector ss = new SongSelector(parent, root);
		ss.setVisible(true);
		if (ss.selected.isEmpty() || !ss.hit_ok) return new URL[] {};
		else {
			URL[] urls = new URL[ss.selected.size()];
			for (int i = 0; i < ss.selected.size(); i++)
				urls[i] = ss.selected.get(i).getURL();
			return urls;
		}
	}
	
	JTree tree;
	JScrollPane scrollpane;
	JButton ok_btn, cancel_btn;
	
	boolean hit_ok;
	ArrayList<FSElement> selected;
	
	public SongSelector(Frame parent, FSElement root) {
		super(parent, "Select a song", true);
		
		DefaultMutableTreeNode top = makeTree(root);
		
		tree = new JTree(top);
		scrollpane = new JScrollPane();
		ok_btn = new JButton("OK");
		cancel_btn = new JButton("Cancel");
		
		ok_btn.setEnabled(false);
		ok_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				select();
				hit_ok = true;
				dispose();
			}
		});
		
		cancel_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		scrollpane.setViewportView(tree);
		
		hit_ok = false;
		selected = new ArrayList<FSElement>();
		
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1 && e.getClickCount() == 2) {
					select();
					if (!selected.isEmpty()) {
						hit_ok = true;
						dispose();
					}
				}
			}
		});
		
		getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(scrollpane, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 5, 0);
		getContentPane().add(ok_btn, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		getContentPane().add(cancel_btn, c);
		
		pack();
		setLocationByPlatform(true);
	}
	
	private DefaultMutableTreeNode makeNode(FSElement fse) {
		return new DefaultMutableTreeNode(fse, true);
	}
	
	private DefaultMutableTreeNode makeTree(FSElement fse) {
		DefaultMutableTreeNode n = makeNode(fse);
		if (fse.getChildren() != null) for (FSElement c : fse.getChildren())
			n.add(makeTree(c));
		return n;
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		select();
	}
	
	private void select() {
		TreePath[] paths = tree.getSelectionPaths();
		selected = new ArrayList<FSElement>();
		ok_btn.setEnabled(false);
		for (TreePath p : paths) {
			FSElement e = (FSElement)(((DefaultMutableTreeNode)p.getLastPathComponent()).getUserObject());
			if (e.getChildren() == null) {
				selected.add(e);
				ok_btn.setEnabled(true);
			}
		}
	}
	
}
