package simplex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menu extends JMenuBar implements ActionListener {
	private static final long serialVersionUID = 1L;
	JMenuItem newFile = new JMenuItem("New dictionary");
	JMenuItem modify = new JMenuItem("Modify a dictionary");
	JMenuItem quit = new JMenuItem("Quit");
	 
	 public Menu() {
		 JMenu menu = new JMenu("File");
		 newFile.addActionListener(this);
		 menu.add(newFile);
		 modify.addActionListener(this);
		 menu.add(modify);
		 quit.addActionListener(this);
		 menu.add(quit);
		 add(menu);
	 }
		
	 public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		if (source == newFile) {
			new DataCapture();
		}
		else if (source == modify) {
			new Change();
		}
		else if (source == quit) {
			System.exit(0);
		}
	 }
}
