package simplex;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Modelise l'interface graphique.
 */
public class Scenario extends JFrame {
	private static final long serialVersionUID = 1L;
	JButton total = new JButton("Perform until the end");
	JButton oneStep = new JButton("Perform a step");
	JButton buttonPivote = new JButton("Pivote");
	JButton buttonRestart = new JButton("Choose a dictionary");
	JButton buttonBasis = new JButton("Apply");
	JCheckBox bland = new JCheckBox("Apply the Bland's rule", false);
	JRadioButton first = new JRadioButton("The first candidate met", true);
	JRadioButton greatest = new JRadioButton("The greatest coefficient", false);
	JRadioButton advantageous = new JRadioButton("The most advantageous", false);
	JTextField vE = new JTextField(3);
	JTextField vS = new JTextField(3);
	JComboBox sizeList;
	JTextField choiceBasis = new JTextField(8);
	JLabel labelChoice;
	Scenario_Controller controller;
	Simplex simplex;
	
	public Scenario(Simplex simplex) {
		this.simplex = simplex;
		controller = new Scenario_Controller(this, simplex);
		addWindowListener(controller);
		JPanel highPanel = new JPanel();
		Box leftPanel = Box.createVerticalBox();
		Box rightPanel = Box.createVerticalBox();
		JPanel panel;
		ButtonGroup group = new ButtonGroup();

		JScrollPane scroll = new JScrollPane(Simplex.output);

		Simplex.output.scroll = scroll;

		highPanel.setLayout(new BoxLayout(highPanel, BoxLayout.X_AXIS));

		panel = new JPanel();
		panel.add(new JLabel("Method for choosing the entering variable"));
		leftPanel.add(panel);

		group.add(first);
		group.add(greatest);
		group.add(advantageous);

		Box choiceMethod = Box.createVerticalBox();
		choiceMethod.add(first);
		choiceMethod.add(greatest);
		choiceMethod.add(advantageous);
		leftPanel.add(choiceMethod);

		first.addItemListener(controller);

		greatest.addItemListener(controller);

		advantageous.addItemListener(controller);

		leftPanel.add(Box.createVerticalStrut(10));

		bland.addItemListener(controller);
		leftPanel.add(bland);

		leftPanel.add(Box.createVerticalStrut(10));

		panel = new JPanel();
		panel.add(total);
		leftPanel.add(panel);
		total.addActionListener(controller);

		leftPanel.add(Box.createVerticalStrut(20));

		panel = new JPanel();
		panel.add(oneStep);
		leftPanel.add(panel);
		oneStep.addActionListener(controller);
		highPanel.add(leftPanel);

		rightPanel.add(Box.createVerticalStrut(10));

		buttonRestart.addActionListener(controller);
		rightPanel.add(buttonRestart);

		rightPanel.add(Box.createVerticalStrut(10));

		Integer[] tailles = {10, 12, 14, 16, 18, 20, 22, 24, 30, 36};
		sizeList = new JComboBox(tailles);
		sizeList.addActionListener(controller);
		sizeList.setSelectedItem(16);
		JScrollPane listeTaillesAscenseur = new JScrollPane(sizeList);
		panel = new JPanel();
		panel.add(new JLabel("Size of the font "));
		panel.add(listeTaillesAscenseur);
		rightPanel.add(panel);

		Box box = Box.createVerticalBox();
		panel = new JPanel();
		panel.add(new JLabel("Number entering variable"));
		vE.setText("");
		panel.add(vE);
		box.add(panel);
		panel = new JPanel();
		panel.add(new JLabel("Number leaving variable"));
		vS.setText("");
		panel.add(vS);
		box.add(panel);
		
		buttonPivote.addActionListener(controller);
		box.add(buttonPivote);
		
		box. setBorder(BorderFactory.createEtchedBorder());
		panel = new JPanel();
		panel.add(box);
		rightPanel.add(panel);
		
		box = Box.createVerticalBox();
		buttonBasis.addActionListener(controller);
		choiceBasis.addActionListener(controller);
		labelChoice = new JLabel("Choice of a basis (give the numbers)");
		panel = new JPanel();
		panel.add(labelChoice);
		panel.add(choiceBasis);
		box.add(panel);
		box.add(buttonBasis);

		box. setBorder(BorderFactory.createEtchedBorder());
		panel = new JPanel();
		panel.add(box);
		rightPanel.add(panel);
		
		
		highPanel.add(rightPanel);

		add(highPanel, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		

		buttonPivote.setEnabled(false);
		buttonBasis.setEnabled(false);
		
		buttonsGrayed();
		setJMenuBar(new Menu());
		pack();
		setLocation(200, 20);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void allowButtons() {
		total.setEnabled(true);
		oneStep.setEnabled(true);
		buttonPivote.setEnabled(true);
		buttonBasis.setEnabled(true);	
		first.setEnabled(true);
		greatest.setEnabled(true);
		advantageous.setEnabled(true);
		bland.setEnabled(true);
	}
	
	public void buttonsGrayed() {
		total.setEnabled(false);
		oneStep.setEnabled(false);
		buttonPivote.setEnabled(false);
		buttonBasis.setEnabled(false);	
		first.setEnabled(false);
		greatest.setEnabled(false);
		advantageous.setEnabled(false);
		bland.setEnabled(false);
	}
	
	public void setSimplex(Simplex simplex) {
		this.simplex = simplex;
		controller = new Scenario_Controller(this, simplex);
		controller.nbDecision = simplex.getDictionary().getNbNonBasic();
		
	}

	public void desactivate() {
		buttonBasis.setEnabled(false);
		buttonPivote.setEnabled(false);
		oneStep.setEnabled(false);
		total.setEnabled(false);
	}
	
	public void activate() {
		buttonBasis.setEnabled(true);
		buttonPivote.setEnabled(true);
		oneStep.setEnabled(true);
		total.setEnabled(true);
	}
}

