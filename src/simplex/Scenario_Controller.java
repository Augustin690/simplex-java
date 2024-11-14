package simplex;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import matrix.Matrix;

public class Scenario_Controller extends WindowAdapter 
	implements ActionListener, ItemListener, Runnable {
	Scenario scenario;
	private Simplex simplex;
	int nbDecision;
	private boolean itIsSaid;


	public Scenario_Controller(Scenario scenario) {
		this.scenario = scenario;
	}

	public Scenario_Controller(Scenario scenario, Simplex simplexe) {
		this.scenario = scenario;
		this.simplex = simplexe;
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();
		
		if (source == scenario.buttonRestart) {
			try {
				Simplex.output.setText("");
				simplex.choiceDictionary();
				scenario.allowButtons();
				nbDecision = simplex.getDictionary().getNbNonBasic();
			}
			catch(IOException exc) {
				exc.printStackTrace();
			}
		}
		
		else if (source == scenario.oneStep) {
			simplex.oneStep();
		} 

		else if (source == scenario.buttonPivote) {
			int jE, iS, numE, numS;

			if (simplex.getDictionary().isIncomplete()){
				Simplex.output.println("Please complete your program");
				return; 
			}
			if (simplex.getPhase() == 3) {
				Simplex.output.println("Ir is finished...");
				return;
			}
			
			try {
				if (scenario.vE.getText().equals("")) {
					Simplex.output.println("No entering variable indicated");
					return;
				}
				if (scenario.vS.getText().equals("")) {
					Simplex.output.println("No leaving variable indicated");
					return;
				}
				numE = Integer.parseInt(scenario.vE.getText());
				numS = Integer.parseInt(scenario.vS.getText());
				jE = simplex.getDictionary().nonBasicIndex(numE);
				iS = simplex.getDictionary().basicIndex(numS);			
				if (jE == 0)Simplex.output.println("Invalid number for entering variable number");
				if (iS == 0)Simplex.output.println("Invalid number for leaving variable number");
				if ((jE != 0) && (iS != 0)) {
					if (Dictionary.isNull(simplex.getDictionary().getD()[iS][jE])) {
						Simplex.output.println("Impossible: division by zero");
						return;
					}
					simplex.getDictionary().oneStep(jE, iS);
					scenario.vE.setText("");
					scenario.vS.setText("");	
					treatPivotOrBasis();
				}
			}
			catch(NumberFormatException exc) {
				Simplex.output.println("Error on the numbers");
			}
		}

		else if ((source == scenario.buttonBasis) || (source == scenario.choiceBasis)){
			if (simplex.getDictionary().isIncomplete()){
				Simplex.output.println("Please complete your program");
				return; 
			}
			if (simplex.getPhase() == 3) {
				Simplex.output.println("It is finished...");
				return;
			}
			java.util.Scanner scan = new java.util.Scanner(scenario.choiceBasis.getText());
			ArrayList<Integer> listBasis = new ArrayList<Integer>();
			Matrix B;
			ArrayList<Integer> columns = new ArrayList<Integer>();
			int num;

			try {
				while (scan.hasNext()) {
					num = Integer.parseInt(scan.next());
					if (!simplex.getDictionary().contains(num)) {
						Simplex.output.println("Wrong variable");
						scan.close();
						return;
					}
					if (!listBasis.contains(num))listBasis.add(num);
				}
				if (listBasis.size() != simplex.getDictionary().getNbBasic()) {
					Simplex.output.println("There are not the right number of variables");
					scan.close();
					return;
				}
				scan.close();
			}
			catch(NumberFormatException exc) {
				Simplex.output.println("writing error");
			}
			for (int i : listBasis) columns.add(i - 1);
			B = simplex.getABeginning().extract(columns);
			if (!B.isInvertible()) {
				Simplex.output.println("It is not a basis");
				return;
			}
			Simplex.output.println("\nNew dictionary");
			simplex.setDico(new Dictionary(simplex.getABeginning(), B, listBasis, simplex.getBBeginning(),
					simplex.getZBeginning(), simplex.getZ0Beginning()));
			treatPivotOrBasis();
		}
		
		else if (source == scenario.total) {	
			if (simplex.getDictionary().isIncomplete()){
				Simplex.output.println("Please complete your program");
				return; 
			}
			if (simplex.getPhase() == 3) {
				Simplex.output.println("It is finished...");
				return;	
			}
			new Thread(simplex).start();
		}
		
		else if (source == scenario.sizeList) {
			if (simplex.getDictionary() == null) return;
			Simplex.output.setFont(new Font("Courier New", Font.BOLD, (Integer)scenario.sizeList.getSelectedItem()));
		}
		(new Thread(this)).start();
	}


	public void treatPivotOrBasis() {
		switch (simplex.getPhase()) {
		case -1 :	
			if (simplex.getDictionary().isFeasible()) {
				if (simplex.getDictionary().isIncomplete()) return;
				if (!simplex.getDictionary().isOptimal()) {
					Simplex.output.println("The dictionary is feasible");
					Simplex.output.println("\nPHASE 2");
					simplex.setPhase(2);
				}
				else{
					Simplex.output.println("The dictionary is  optimal");
					Simplex.output.displaySolution(simplex.getDictionary());
					simplex.setPhase(3);
				}
			}
			else Simplex.output.println("The dictionary is still not feasible");
			break;	
		case 0 : 
			if (!simplex.getDictionary().isFeasible()) {
				scenario.oneStep.setEnabled(false);
				scenario.total.setEnabled(false);
				Simplex.output.println("Error: the dictionary is not feasible");
				Simplex.output.println("You have to re-establish the situation by pivoting correctly");
				itIsSaid = true;
				simplex.setPhase(1);
			}
			else traiterRealisable0_1();
			break;
		case 1: 
			if (!simplex.getDictionary().isFeasible()) {
				if (!itIsSaid) {
					scenario.oneStep.setEnabled(false);
					scenario.total.setEnabled(false);
					Simplex.output.println("Error: the dictionary is not feasible");
					Simplex.output.println("You have to re-establish the situation by pivoting correctly");
					itIsSaid = true;
				}
				else {	
					Simplex.output.println("The dictionary is still not feasible");
				}
			}
			else this.traiterRealisable0_1(); 
			break;
		case 2 : 
			if(!simplex.getDictionary().isFeasible()) {
				Simplex.output.println("Error: the dictionary is not feasible");
				Simplex.output.println("We arrive in phase 1");
				simplex.setPhase(-1);
				simplex.memorizeZ();
			}
			else {
				if (simplex.getDictionary().isOptimal()) {
					Simplex.output.println("The dictionary is optimal");
					Simplex.output.displaySolution(simplex.getDictionary());
					simplex.setPhase(3);
					scenario.desactivate();
				}
				else if (!simplex.getDictionary().borned) {
					Simplex.output.println("The problem is unbounded");
					simplex.setPhase(3);
					scenario.desactivate();
				}
			}	
		}
		scenario.choiceBasis.setText("");
		scenario.vS.setText("");
		scenario.vE.setText("");
	}
	
	public void traiterRealisable0_1() {		
		scenario.oneStep.setEnabled(true);
		scenario.total.setEnabled(true);
		itIsSaid = false;
		if (simplex.getDictionary().isOptimal()) {
			Simplex.output.println("The dictionary of the first phase is optimal");
			if (simplex.getDictionary().getD()[0][0] < -Dictionary.epsilon) {
				Simplex.output.println("There is no feasible solution");
				simplex.setPhase(3);
			}
			else {
				Simplex.output.println("End of the first phase");
				Simplex.output.println("\nPHASE 2");
				scenario.buttonBasis.setEnabled(true);
				simplex.setPhase(2);
				simplex.setDico(simplex.getDictionary().initialDictionaryPhase2
						(simplex.getzInitial(), simplex.getZ0Initial(), simplex.getInitialNonBasicVariables()));
			}
		}		
	}
	
	public void windowOpeneds(WindowEvent evt) {
		try {
			Simplex simplex = new Simplex();
			scenario.setSimplex(simplex);
			this.simplex = simplex;
		}
		catch(IOException exc) {
			exc.printStackTrace();
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		Object source = evt.getSource();
		EnteringMethod method = simplex.getDictionary().getMethod();
		if (source == scenario.first) {
			if (method != EnteringMethod.FIRST)
				simplex.getDictionary().setMethod(EnteringMethod.FIRST);
		}
		else if (source == scenario.greatest) {
			if (method != EnteringMethod.GREATEST)
			simplex.getDictionary().setMethod(EnteringMethod.GREATEST);
		}
		else if (source == scenario.advantageous) {
			if (method != EnteringMethod.MORE_ADVANTAGEOUS)
				simplex.getDictionary().setMethod(EnteringMethod.MORE_ADVANTAGEOUS);
		}
			
		else if (source == scenario.bland) 
			simplex.getDictionary().setBland(!simplex.getDictionary().isBland());
	}
	
	public void run() {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException exc) {
			exc.printStackTrace();
		}
		scenario.repaint();
	}
}
