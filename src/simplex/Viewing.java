package simplex;

import java.awt.Font;
import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Viewing extends JTextArea implements Runnable {
	private static final long serialVersionUID = 1L;
	JScrollPane scroll;
	boolean toDo;

	public Viewing() {
		super(35, 100);
		setFont(new Font("Courier New", Font.BOLD, 16));
		scroll = new JScrollPane(this);
		setEditable(false);
		new Thread(this).start();
	}

	public void print(String s) {
		append(s);
		int height = getHeight();
		scroll.getViewport().setViewPosition(new Point(0, height));
	}

	synchronized public void println(String s) {
		append(s + "\n");
		int height = getHeight();
		scroll.getViewport().setViewPosition(new Point(0, height));
	}

	public void println(Object obj) {
		println(obj.toString());
	}

	synchronized public void displayDictionary(Dictionary dict) {		
		char obj ='z';
		if ((dict.basicIndex(0) != 0 ) || (dict.nonBasicIndex(0) != 0)) obj = 'w';
		for (int i = 1; i <= dict.getNbBasic(); i++) {
			append("x" + dict.getBasicVar()[i] + " = ");
			if (Dictionary.isNull(dict.getD()[i][0]))
				append(String.format("%6s", " "));
			else append(String.format("%6.2f", dict.getD()[i][0]));
			for (int j = 1; j <= dict.getNbNonBasic(); j++ ) {
				if (Dictionary.isNull(dict.getD()[i][j]))
					append(String.format("%11s", " "));
				else {
					if(dict.getD()[i][j] >=  0) {
						append(" + ");
						append(String.format("%5.2f", dict.getD()[i][j]));
					}
					else {
						append(" - ");
						append(String.format("%5.2f", -dict.getD()[i][j]));
					}
					append(" ");
					append("x" + dict.getNonBasicVar()[j]);
				}
			}
			append("\n");
		}
		append(obj + "  = ");
		if (Dictionary.isNull(dict.getD()[0][0]))
			append(String.format("%6s", " "));
		else
			append(String.format("%6.2f", dict.getD()[0][0]));
		for (int j = 1; j <= dict.getNbNonBasic(); j++ ){
			if (Dictionary.isNull(dict.getD()[0][j]))
				append(String.format("%11s", " "));
			else {
				if(dict.getD()[0][j] >=  0) {
					append(" + ");
					append(String.format("%5.2f", dict.getD()[0][j]));
				}
				else {
					append(" - ");
					append(String.format("%5.2f", -dict.getD()[0][j]));
				}
				append(" ");
				append("x" + dict.getNonBasicVar()[j]);
			}
		}
		append("\n");
		toDo = true;
		notify();
	}

	/**
	 * Displays the solution of the problem; is used if the dictionary is optimal.
	 */
	synchronized public void displaySolution(Dictionary dico) {

		double[] solution = new double[dico.getNbNonBasic() + dico.getNbBasic() + 1];
		for(int j = 1; j <= dico.getNbNonBasic(); j++) solution[dico.getNonBasicVar()[j]] = 0;
		for(int i = 1; i <= dico.getNbBasic(); i++) solution[dico.getBasicVar()[i]] = dico.getD()[i][0];
		append("\nThe optimal solution is obtained for: \n");
		for (int i = 1; i <= dico.getNbNonBasic(); i++) {
			append("x" + i + " = ");
			append(String.format("%.2f\n", solution[i]));
		}

		println("The slack variables are equal to: ");
		for (int i = dico.getNbNonBasic() + 1; i <= dico.getNbNonBasic() + dico.getNbBasic(); i++) {
			append("x" + i + " = ");
			append(String.format("%.2f\n", solution[i]));
		}
		append("The optimal value of the objective function is: ");
		append(String.format("%.2f\n", dico.getD()[0][0]));
		toDo = true;
		notifyAll();
	}

	synchronized public void run() {
		while (true) {
			try {
				while (!toDo) wait();	
				Thread.sleep(10);
				int hauteur = getHeight();
				if (scroll.getViewport().getHeight() > getLineCount() * getRowHeight()) {
					scroll.getViewport().setViewPosition(new Point(0, 0));
				}
				else {
					scroll.getViewport().setViewPosition(new Point(0, hauteur));
					append("\n");
				}
				toDo = false;
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
