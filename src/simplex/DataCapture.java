package simplex;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DataCapture extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	JTextArea textArea = new JTextArea(30, 40);
	JButton save = new JButton("Save");
	
	public DataCapture() {
		save.addActionListener(this);
		JPanel panel = new JPanel();
		panel.add(save);
		add(panel, BorderLayout.NORTH);
		
		textArea.setText("Warning: write at the beginning of the file\n");
		textArea.append("number of variables then number of constraints\n");
		textArea.append("then the constraints, if a1 x1 + a2 x2 <= b: write a1 a2 b\n");
		textArea.append("then the objective function, if z = c1 x1 + c2 x2: write c1 c2\n");
		add(textArea);
		setLocation(200, 200);
		pack();
		setVisible(true);		
	}

	public void actionPerformed(ActionEvent evt) {
		JFileChooser fileChoice = new JFileChooser(Simplex.pathData);
		PrintWriter output;
		File file;

		try {
			if (fileChoice.showSaveDialog(null) ==  JFileChooser.APPROVE_OPTION) {
				file = fileChoice.getSelectedFile();
				output = new PrintWriter(new FileWriter(file.getPath(), false));
				output.println(textArea.getText());
				output.close();
				dispose();
			}
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}
	}
}
