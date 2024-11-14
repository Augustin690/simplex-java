package simplex;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import matrix.Matrix;


/**
 * Models the simplex method

 */
public class Simplex implements Runnable {
	private Dictionary dictionary;
	private int phase = -1;
	private double[] zInitial;
	private double z0Initial;
	public static Viewing output = new Viewing();
	private Matrix ABeginning;
	private double[] zBeginning;
	private double z0Beginning;
	private double[] bBeginning;
	private int[] initialNonBasicVar;
	static String pathData = "pbs";
	private int counter = 0;
	Scenario_Controller controller;
	Scenario view;

	public Simplex() throws IOException {
	}

	public void memorizeBeginning() {
		computeABeginning();		
		zBeginning = new double[dictionary.getNbBasic() + dictionary.getNbNonBasic()];
		for (int j = 0; j < dictionary.getNbNonBasic(); j++)  zBeginning[j] = dictionary.getD()[0][j + 1];
		z0Beginning = dictionary.getD()[0][0];		
		bBeginning = new double[dictionary.getNbBasic()];
		for (int i = 0; i < dictionary.getNbBasic(); i++)  bBeginning[i] = dictionary.getD()[i + 1][0];
	} 

	public void memorizeZ() {
		initialNonBasicVar = new int[dictionary.getNbNonBasic() + 1];
		zInitial = new double[dictionary.getNbNonBasic() + 1];
		for (int j = 1; j <= dictionary.getNbNonBasic(); j++) {
			zInitial[j] = dictionary.getD()[0][j];
			initialNonBasicVar[j] = dictionary.getNonBasicVar()[j];
		}
		z0Initial = dictionary.getD()[0][0];
	}

	public void computeABeginning() {
		ABeginning = new Matrix(dictionary.getNbBasic(), dictionary.getNbNonBasic() + dictionary.getNbBasic());

		for (int i = 0; i < dictionary.getNbBasic(); i++) 
			for (int j = 0; j < dictionary.getNbNonBasic(); j++)
				ABeginning.setValue(i, j, -dictionary.getD()[i + 1][j + 1]);
		for (int i = 0; i < dictionary.getNbBasic(); i++) {
			for (int j = dictionary.getNbNonBasic(); j < dictionary.getNbNonBasic() + dictionary.getNbBasic(); j++)
				ABeginning.setValue(i, j, 0);
			ABeginning.setValue(i, i + dictionary.getNbNonBasic(), 1);
		}
	}

	public Dictionary choiceDictionary() throws IOException {
		File fichier = null;

		JFileChooser dialogue = new JFileChooser(new File(pathData));
		if (dialogue.showOpenDialog(null)== JFileChooser.APPROVE_OPTION) {
			fichier = dialogue.getSelectedFile();
		}
		else output.println("NoChoice");

		dictionary = new Dictionary(fichier);
		output.println("The chosen file is: " + fichier.getName() + "\n");
		dictionary.setBland(controller.scenario.bland.isSelected());
		if (controller.scenario.first.isSelected())
				dictionary.setMethod(EnteringMethod.FIRST);
		else if (controller.scenario.greatest.isSelected())
			dictionary.setMethod(EnteringMethod.GREATEST);
		else dictionary.setMethod(EnteringMethod.MORE_ADVANTAGEOUS);
		output.displayDictionary(dictionary);

		boolean realisable = dictionary.isFeasible();
		if (dictionary.isIncomplete()) return dictionary;
		if (realisable) {
			controller.scenario.total.setEnabled(true);
			output.println("The dictionary is feasible");
			output.println("\nPHASE 2");
			phase = 2;
		}
		else {
			memorizeZ();
			output.println("The dictionary is not feasible: search for a feasible dictionary");
			output.println("\nPlease click on \"Perform a step\" to have a first dictionary of phase 1");
			output.println("or try to have a dictionary that can be done by pivoting");
			output.println("or trying another basis");
			new Thread(controller).start();
			phase = -1;
		}
		memorizeBeginning();
		view.activate();
		return dictionary;
	}

	public void oneStep() {
		if (dictionary.isIncomplete()){
			Simplex.output.println("Please complete your program");
			return; 
		}
		switch (phase) {
		case -1 :
			Simplex.output.println("\nPHASE 1");
			setDico(dictionary.firstAuxiliaryDictionaryPhase1());
			view.total.setEnabled(true);
			view.buttonBasis.setEnabled(false);
			phase = 0;
			break;
		case 0 : 
			Simplex.output.println("\nWe compute a first feasible dictionary for phase 1");
			dictionary.firstFeasibleDictionaryPhase1();
			phase = 1;
			break;
		case 1 : 
			pivote();
			if (dictionary.isIncomplete()) return; 
			if (dictionary.isOptimal()) {
				if (dictionary.getD()[0][0] < -Dictionary.epsilon) {
					output.println("There is no feasible solution");
					view.desactivate();
					phase = 3;
				}
				else {
					output.println("End of the first phase");
					output.println("\nPHASE 2");
					phase = 2;
					controller.scenario.total.setEnabled(true);
					dictionary = dictionary.initialDictionaryPhase2(zInitial, z0Initial, initialNonBasicVar);
					view.buttonBasis.setEnabled(true);
				}
			}
			break;
		case 2 :
			pivote();
			if (dictionary.isOptimal()) {
				output.displaySolution(dictionary);
				phase = 3;
				view.desactivate();
			}
			else if (!dictionary.isBorned()) {
				output.println("Le probleme est non borne");
				phase = 3;
				view.desactivate();
			}
			break;
		}
	}

	public void pivote(){
		if (!view.vE.getText().equals("")) {
			int numE = Integer.parseInt(view.vE.getText());
			dictionary.oneStep(dictionary.nonBasicIndex(numE));
			view.vE.setText("");
		}
		else {
			dictionary.oneStep();
		}
	}

	public void run() {
		while (phase != 3) {
			if (dictionary.isIncomplete()) {
				Simplex.output.println("Please complete your program");
				return; 
			}
			oneStep();
			counter++;
			if (counter == 100) {
				output.println("Cela semble cycler, on arrete");
				counter=0;
				return;
			}
		}
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDico(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public double[] getzInitial() {
		return zInitial;
	}

	public double getZ0Initial() {
		return z0Initial;
	}

	public Matrix getABeginning() {
		return ABeginning;
	}

	public double[] getZBeginning() {
		return zBeginning;
	}

	public double getZ0Beginning() {
		return z0Beginning;
	}

	public double[] getBBeginning() {
		return bBeginning;
	}

	public int[] getInitialNonBasicVariables() {
		return initialNonBasicVar;
	}
}
