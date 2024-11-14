package simplex;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Change extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	JTextArea enterData = new JTextArea(30, 40);
	JButton save = new JButton("Save");
	JButton saveUnder = new JButton("Save as");
	String filePath;

	public Change() {
		JFileChooser dialog = new JFileChooser(Simplex.pathData);
		BufferedReader readData;
		File file;
		String line;
		save.addActionListener(this);
		saveUnder.addActionListener(this);
		Box box = Box.createVerticalBox();
		box.add(save);
		box.add(saveUnder);
		JPanel panneau = new JPanel();
		panneau.add(box);
		add(panneau, BorderLayout.NORTH);


		try {
			if (dialog.showOpenDialog(null) ==  JFileChooser.APPROVE_OPTION) {
				file = dialog.getSelectedFile();
				filePath = file.getPath();
				readData = new BufferedReader(new FileReader(file.getPath()));
				while ((line = readData.readLine()) != null)
					enterData.append(line + "\n");
				add(enterData);
				setLocation(200, 200);
				pack();
				setVisible(true);	
				readData.close();
			}
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}	
	}

	public void actionPerformed(ActionEvent evt) {
		PrintWriter output;
		File file;
		Object source = evt.getSource();
		if (source == save) {
			try {
				file = new File(filePath);
				output = new PrintWriter(new FileWriter(file.getPath(), false));
				output.println(enterData.getText());
				output.close();
				dispose();
			}
			catch(IOException exc) {
				exc.printStackTrace();
			}
		}
		else {

			try {

				JFileChooser dialog = new JFileChooser(Simplex.pathData);
				if (dialog.showSaveDialog(null) ==  JFileChooser.APPROVE_OPTION) {
					file = dialog.getSelectedFile();
					output = new PrintWriter(new FileWriter(file.getPath(), false));
					output.println(enterData.getText());
					output.close();
					dispose();
				}
			}
			catch(IOException exc) {
				exc.printStackTrace();
			}
		}
	}
}
