package simplex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Observable;
import java.util.Scanner;



import simplex.Dictionary;
import simplex.EnteringMethod;
import simplex.Simplex;

import matrix.Matrix;

;/**
 * Class that models a dictionary
 */
public class Dictionary extends Observable {
	// Decision variables have numbers between 1 et nbNonBasisVar
	// Slack variables have numbers between 1 + nbNonBasisVar + 1 and nbBasisVar + nbNonBasisVar
	// In the first phase looking for a feasible solution,
	// a variable of number 0 is added.
	private int nbNonBasicVar; // number of decision variables 
	private int nbBasicVar;	// number of constraints

	private int[] arrayBasicVar;  // contains from index 1 the numbers of the basic variables, in the order of D (the index 0 is not used)
	int[] arrayNonBasicVar; // contains from index 1 the numbers of the non-basic variables, in the order of D (the box of index 0 is not used)

	private double[][] D;
	// For the modeled dictionary:
	// D[0][0] contains the constant of the function z
	// for j which varies from 1 to nbNonBasicVar;, D[0][j] contains the coefficient in the function z of the non-basic variable of index j
	// for i which varies from 1 to nbBasisVar, D[i][0] contains the constant term of the expression of the basis variable of index  i
	// for i which varies from 1 to nbBasisVar and for j which varies from 1 to nbNonBasicVar, D[i][j] contains, for the basic variable
	// of index i, the coefficient of the non-basic variable of index j

	EnteringMethod method = EnteringMethod.FIRST; // Indicates the method to use to choose the entering variable
	// Can take values EnteringMethod.FIRST, 
	// EnteringMethod.BIGGEST, EnteringMethod.MORE_ADVANTAGEOUS;
	boolean bland; //if this variable is true, we apply the Bland rule
	boolean optimal; // goes to true if the dictionary is optimal
	boolean borned =  true; // goes to false if the problem is not bounded
	private boolean incomplete = false;
	public static double epsilon = 1E-12; // to test that a double is zero...

	/**
	 * Initializes the dictionary from reading a file
	 * @param file the reading file
	 * @throws IOException
	 */
	public Dictionary(File file) throws IOException {
		readFile(file);
	}

	/**
	 * Constructs a dictionary corresponding to the parameters.
	 * Knowing the number of rows and columns, the allocation of the arrays needed for
	 * coding is done by this constructor.
	 * @param nbBasicVar number of constraints
	 * @param nbHorsbase number of decision variables (main variables)
	 * @param method specifes the method to adopt for the choice of the entering variable
	 * @param bland specifes if one uses the rule of Bland
	 */
	public Dictionary(int nbBasicVar, int nbNonBasicVar, EnteringMethod method, boolean bland) {
		this.nbBasicVar = nbBasicVar;
		this.nbNonBasicVar = nbNonBasicVar;
		this.method = method;
		this.bland = bland;
		this.allocate();
	}

	// BEGINNING OF THE METHOD TO COMPLETE


	/**
	 * TO IMPLEMENT FIRST; determines if the dictionary is feasible
	 * It is necessary for that to examine D[i][0] for the index i which varies
	 *  from 1 to nbBasisVar and use the definition of a feasible dictionary.
	 * @return returns true if the dictionary is feasible, returns false otherwise
	 */
	public boolean isFeasible() {
	
		boolean feasible = true;
		
		for (int i=1; i<= nbBasicVar;i++) {
			
			if (D[i][0]<0) {
				
				feasible = false;
			}
		}
		
		return feasible; 
	}

	/**
	 * TO IMPLEMENT; we look for the index in arrayNonBasicVar of the "first" candidate to enter in basis
	 * by considering the variables in the order they occupy in arrayNonBasicVar.
	 * For example, suppose that D[0][1] <= 0 and that D[0][2]> 0. The method then returns the value 2.
	 * If arrayNonBasicVar[2] for example contains the value 3, it means that the variable x3 is chosen as
	 * entering variable but this does not intervene in the programming.
	 * For this search, we go through D[0][j] for j which varies from 1 to nbNonBasicVar and we keep the index
	 * corresponding to the first strictly positive coefficient encountered if there is such an index.
	 * The numbers of the variables do not matter.
	 * @return If there is an entering variable, returns the smallest index of an entering variable, that is,
	 * the smallest positive index of a box of the array D[0][...] containing a strictly positive value.
	 * 		   otherwise, returns 0.
	 */
	public int searchFirstIndexEnteringVariable() {
	
		int index =0;
		for(int j=1; j<= nbNonBasicVar;j++) {
			
			if(D[0][j]>0) {
				
				index = j;
				break;
			}
		}
		
		return index; 
	}

	/**
	 * TO IMPLEMENT; an entering variable being chosen, look for a leaving variable.
	 * Variable numbers do not matter.
	 * @param jE the index in arrayNonBasicVar of the entering variable, i.e. the index of the column
	 * of D that will be used in this method.
	 * In this method, we must use D[i][0] and D[i][jE] for the index i which varies from 1 to nbBasicVar.
	 * @return If the dictionary shows that the problem is not bounded, returns 0.
	 * 		   Otherwise, returns the index in arrayBasicVar of a leaving variable, i.e. the index of the row of
	 * 		   the matrix D corresponding to the leaving variable.
	 * 
	 */
	public int searchIndexLeavingVariable(int jE)  {
		
		ArrayList<Double> A = new ArrayList<Double>();
		int index =0;
	 
		
		for (int i=1;i<=nbBasicVar;i++) {
			
			Double A_i = -(D[i][0])/(D[i][jE]);

			A.add(A_i);
		}
		int c =0;
		
		for (int k=0; k<= nbBasicVar-2;k++) {
			
			index = k+1;
			if(A.get(k)<0) {
				 c++;

			}
			
			if(A.get(k+1)<A.get(k)) {
				
				index = k+2;
			}
		}
		
		if(A.get(nbBasicVar-1)<0) {
			
			c++;
		}
		
		if (c == nbBasicVar) {
			
			index =0;
		}
		
		return index; 
	}
	
	/**
	 * TO IMPLEMENT; modifies the current dictionary (referenced by this) by pivoting according to
	 * the indicated parameters .
	 * @param iS the index in arrayBasicVar of the leaving variable (i.e. index in D of the line from
	 * from which we pivot)
	 * @paramJE the index in arrayNonBasicVar of the entering variable (i.e. index in D of the column 
	 * from which we pivot)
	 */
	/* 
	 * * Modify the dictionary: 
	 * the entering variable enters in the basis and the leaving variable leaves the basis.
	 * If we use pb1.txt, the first D contains:
	 * 0   4  3
	 * 24 -2 -3
	 * 30 -5 -3
	 * 18 -1 -3
	 * and the first dictionary is:
	 * x3 = 24.0 - 2,00x1 - 3,00x2
	 * x4 = 30.0 - 5,00x1 - 3,00x2
	 * x5 = 18.0 - 1,00x1 - 3,00x2
	 * z = 0.0 + 4,00x1 + 3,00x2
	 * and if x1 (at the index 1 of arrayNonBasicVar) enters in basis and 
	 * x4 (at index 2 of arrayBasicVar) leaves the bases, the method pivote
	 * is invoked with the parameter jE which is equal to 1 and the parameter iS which is equal to 2.
	 * After pivoting, the dictionary must be:
	 * x3 = 12 + 0,4 x4 - 1,8 x2
	 * x1 =  6 - 0,2 x4 - 0,6 x2
	 * x5 =  12 + 0,2 x4 - 2,4 x2
	 * z  = 24 - 0,8 x4 + 0,6 x2
	 * From which the coding of the modified dictionary:
	 * arrayNonBasicVar contains at indices 1 and 2 numbers 4 and 2
	 * arrayBasicVar contains at indices 1, 2 and 3, the numbers 3 then 1 then 5
	 * Table D contains:
	 * 24 -0,8  0,6
	 * 12  0,4 -1,8
	 *  6 -0,2 -0,6
	 * 12  0,2 -2,4 
	 */
	public void pivote(int iS, int jE) {
		
		 double[][] H;
			H= D;
		
		double fact = -D[iS][jE];
		
		for(int j=0;j<=nbNonBasicVar;j++) {
			
			H[iS][j]= D[iS][j]/fact;
		}
		
		H[iS][jE]= -1/fact;
		
		for (int k=0; k< iS;k++) {
			
			for (int l=0; l< jE;l++) {
				
				H[k][l] = D[k][l]+ D[k][jE]*H[iS][l];
			}
			
			H[k][jE]= D[k][jE]*(-1/fact);
			
			for (int l=jE+1; l<=nbNonBasicVar;l++) {
				
				H[k][l] = D[k][l]+ D[k][jE]*H[iS][l];
			}
			
			
		}
		
		for (int k=iS+1; k<=nbBasicVar;k++) {
			
			
			for (int l=0; l< jE;l++) {
				
				H[k][l] = D[k][l]+ D[k][jE]*H[iS][l];
			}
			
			H[k][jE]= D[k][jE]*(-1/fact);
			
			for (int l=jE+1; l<=nbNonBasicVar;l++) {
				
				H[k][l] = D[k][l]+ D[k][jE]*H[iS][l];
			}
		}
		
		D = H;
		
	}


	/**
	 * TO IMPLEMENT; look for an entering variable of greatest coefficient in the objective function of the current dictionary.
	 * The numbers of the variables do not matter. Only D[0][j] for j which varies from 1 to nbNonBasisVar is concerned.
	 * @return If there is an entering variable, returns the index in arrayNonBasicVar of the entering variable of
	 * greatest coefficient in the objective of the current dictionary, 
	 * i.e. the index >= 1 in D [0][...] of the greatest value box.
	 * <br>otherwise, returns 0c
	 */
	public int searchIndexEnteringVariableGreatestCoeff() {	
		// Two following lines to delete
		Simplex.output.println("Method searchIndexEnteringVariableGreatestCoeff to write");
		incomplete = true;
		
		// To modify
		return 0;
	}

	/**
	 * To implement; looks for an entering variable that maximizes the increase of the objective function in the
	 * next dictionary. The numbers of the variables do not matter.
	 * @return
	 * If there is no entering variable, returns 0;
	 * <br> otherwise if there is an entering variable that gives an infinite increase (problem not bounded),
	 * returns the value -1;
	 * <br> otherwise returns the index in arrayNonBasicVar of the entering variable that maximizes the increase 
	 * of the objective function in the next dictionary.
	 */
	public int searchEnteringAdvantageousVariableIndex() {	
		// Two following lines to delete
		Simplex.output.println("Method searchEnteringAdvantageousVariableIndex to write");
		incomplete = true;
		
		// To modify
		return 0;
	}

	/**
	 * TO IMPLEMENT: look for the index of the entering variable of smallest number (the variable x_i
	 * has number i). Serves when using Bland's rule.
	 * For example, if arrayNonBasicVar contains from index 1 the numbers 6, 7, 2, 3, 5 and if the variables
	 * candidate to enter are the variables of indices 2 and 4 (arrayNonBasicVar[2] is 7, arrayNonBasicVar[4] is 3), 
	 * the returned value  is 4.
	 * @return If there is an entering variable, returns the index in arrayNonBasicVar of the entering variable of smallest number.
	 * <br> Otherwise, returns 0.
	 */
	public int searchIndexEnteringVariableSmallestNumber(){		
		// Two following lines to delete
		Simplex.output.println("Method searchIndexEnteringVariableSmallestNumber to write");
		incomplete = true;
		
		// To modify
		return 0;
	}


	/**
	 * TO IMPLEMENT; an entering variable being chosen, look for the index of the leaving variable of smallest number
	 * (the variable x_i has number i). Serves when using Bland's rule.
	 * For example, if arrayBasicVar contains from index 1 the numbers 4, 5, 1, 2, 7 and if the variables candidate to leave 
	 * are the variables of indices 2 and 4 (arrayBasicVar[2] is 5, arrayBasicVar[4] is 2), the returned value  is 4.
	 * @paramJE the index of the entering variable.
	 * @return If the dictionary shows that the problem is not bounded, returns -1.
	 * <br> else, returns the index of the leaving variable.
	 */
	public int searchIndexLeavingVariableSmallestNumber(int jE) {		
		// Two following lines to delete
		Simplex.output.println("Method searchIndexLeavingVariableSmallestNumber to write");
		incomplete = true;
		
		// To modify
		return 0;
		
		
	}
	// END OF METHODS TO COMPLETE
	
	public int searchIndexEnteringVariable(EnteringMethod methode){
		switch(methode) {
		case FIRST :
			return searchFirstIndexEnteringVariable();
		case GREATEST :
			return searchIndexEnteringVariableGreatestCoeff();
		case MORE_ADVANTAGEOUS  :
			return searchEnteringAdvantageousVariableIndex();
		}
		return 0;
	}


	/**
	 * Performs a step in the simplex method.
	 * If the dictionary is optimal, set the  attribute "optimal" to true
	 * <br> If step has shown that the problem is not bounded, set the terminal attribute to false
	 * In the other cases, performs a step of the simplex method by choosing an
	 * entering variable, an leaving variable and pivoting. 
	 */
	public void oneStep() {		
		int jE;

		if (incomplete) return;
		jE = searchIndexEnteringVariable(method);
		if (incomplete) {
			return;
		}
		if (jE == 0) {
			optimal = true;
		}
		else if (jE == -1) {
			borned = false;
		}
		else {
			oneStep(jE);
		}
	}

	public void oneStep(int jE) {
		int iS;
		if (D[0][jE] <= 0) {			
			Simplex.output.println("The variable indicated as entering is not correct");
			return;
		}
		iS = searchIndexLeavingVariable(jE);
		if (incomplete) return;
		if (iS == 0) {
			borned = false;
		}
		else {
			if (bland  && D[iS][0] == 0) {
				jE = searchIndexEnteringVariableSmallestNumber();
				if (incomplete) return;
				if (jE == 0) {
					optimal = true;
					return;
				}
				iS =  searchIndexLeavingVariableSmallestNumber(jE);
				if (incomplete) return;
				if (iS == 0) {
					borned = false;
					return;
				}
			}
			Simplex.output.println("\nEntering variable: x" + this.arrayNonBasicVar[jE]);
			Simplex.output.println("Leaving variable : x" + this.arrayBasicVar[iS]);
			oneStep(jE, iS);
		}
	}


	public void oneStep(int jE, int iS) {
		pivote(iS, jE);
		if (incomplete) return;
		Simplex.output.displayDictionary(this);
	}


	/**
	 * Caomputes a first dictionary, not feasible, for the first phase.
	 * <br> The introduced variable is named x0 (in other words it has the number 0) and
	 * it is assumed that no other variable has the number 0.
	 * @return returns a first dictionary, not feasible, for the first phase.
	 */
	public Dictionary firstAuxiliaryDictionaryPhase1() {
		Dictionary dic = new Dictionary(this.nbBasicVar, this.nbNonBasicVar + 1, method, bland);

		// Init of basic and non-basic variables
		dic.arrayNonBasicVar[this.nbNonBasicVar + 1] = 0;
		for (int j = 1; j <= this.nbNonBasicVar; j++) dic.arrayNonBasicVar[j] = this.arrayNonBasicVar[j];
		for (int i = 1; i <= this.nbBasicVar; i++) dic.arrayBasicVar[i] = this.arrayBasicVar[i];

		// Init of constants
		for (int i = 1; i <= this.nbBasicVar; i++) dic.D[i][0] = this.D[i][0];

		// Init  of other coefficients of D
		for (int i = 1; i <= this.nbBasicVar; i++) {
			for (int j = 1; j <= this.nbNonBasicVar; j++) dic.D[i][j] = this.D[i][j];
		}

		for (int i = 1; i <= nbBasicVar; i++) {
			if (dic.D[i][0] < 0) dic.D[i][dic.nbNonBasicVar] = 1;
		}		
		dic.D[0][dic.nbNonBasicVar] = -1;
		Simplex.output.displayDictionary(dic);

		return dic;
	}

	/**
	 * The current dictionary is the first dictionary, not feasible,
	 * of the first phase (the dictionary obtained by the method firstDicoPbAuxiliary);
	 * modifies this dictionary to have a first feasible dictionary for the first phase.
	 */
	public void firstFeasibleDictionaryPhase1() {
		double min = 0;
		int iS = -1;

		for (int i = 1; i <= nbBasicVar; i++) {
			double constante = this.D[i][0];
			if (constante < min) {
				min = constante;
				iS = i;
			}
		}	
		Simplex.output.println("Entering variable: x0");
		Simplex.output.println("Leaving variable : x" + this.arrayBasicVar[iS]);
		pivote(iS, this.nbNonBasicVar);
		if (incomplete) return;

		Simplex.output.displayDictionary(this);
	}
	/**
	 * From a dictionary of the first phase (dictionary of the auxiliary problem) such as the value of the
	 * objective function in the associated basic solution is 0 (which shows that the initial problem is feasible),
	 * computes the first dictionary of the second phase. Do not change the dictionary. For it :
	 * <br> - we remove the variable x0 (the number columns decreases by 1);
	 * <br> - we compute the objective function according to the non-basis variables
	 * @param zInitial the linear part of the objective function expressed according to the decision variables
	 * @param z0Initial the constant of the objective function expressed according to the decision variables
	 * @ param initialNonBasicVar the non-basic variables before starting the first phase
	 * @return returns the resulting dictionary giving the initial dictionary of the second phase
	 */
	public Dictionary initialDictionaryPhase2(double[] zInitial, double z0Initial, int[] initialNonBasicVar) {
		Dictionary dict = new Dictionary(nbBasicVar, nbNonBasicVar - 1, method, bland);
		int columnX0 = 0;
		int variable, row, column;
		double multiplicator;

		for (int j = 1; j <= nbNonBasicVar; j++)
			if (arrayNonBasicVar[j] == 0) {
				columnX0 = j;
				break;
			}

		if (columnX0 == 0) return null;
		// Init of basic and non-basic variables
		for (int j = 1; j < columnX0; j++) dict.arrayNonBasicVar[j] = arrayNonBasicVar[j];
		for (int j = columnX0; j < nbNonBasicVar; j++) dict.arrayNonBasicVar[j] = arrayNonBasicVar[j + 1];

		for (int i = 1; i <= nbBasicVar; i++) dict.arrayBasicVar[i] = arrayBasicVar[i];

		// Init of constants
		for (int i = 1; i <= nbBasicVar; i++) dict.D[i][0] = D[i][0];

		// Init of other coefficients of D
		for (int i = 1; i <= nbBasicVar; i++) {
			for (int j = 1; j < columnX0; j++) dict.D[i][j] = D[i][j];
			for (int j = columnX0; j < nbNonBasicVar; j++)dict.D[i][j] = D[i][j + 1];
		}

		dict.D[0][0] = z0Initial;
		/* 
		 * zInitial [j] is the coefficient in z of the variable of number j + 1 in the initial dictionary
		 * We compute the contribution of zInitial [j] X_ (j + 1) in the new z
		 */
		for (int j = 1; j <= dict.nbNonBasicVar; j++) {
			variable = initialNonBasicVar[j];
			row = dict.basicIndex(variable);

			if (row == 0) {
				column = dict.nonBasicIndex(variable);
				dict.D[0][column] += zInitial[j]; 
			}
			else {
				multiplicator = zInitial[j];
				for (int k = 0; k <= dict.nbNonBasicVar; k++)
					dict.D[0][k] += multiplicator * dict.D[row][k];
			}
		}	
		Simplex.output.println("Feasible dictionary for the initial problem:");
		Simplex.output.displayDictionary(dict);
		return dict;
	}

	/**
	 * Check if the variable indicated in parameter is in base.
	 * @param var the number of the searched variable.
	 * @return if the variable indicated in parameter is in base, returns the index of
	 * this variable in arrayBasicVar (between 1 and nbLines); 
	 * otherwise, returns 0.
	 */
	public int basicIndex(int var) {
		for (int i = 1; i <= nbBasicVar; i++) if (arrayBasicVar[i] == var) return i;
		return 0;
	}

	/**
	 * Check if the variable specified in parameter is non-basic.
	 * @param var the number of the searched variable
	 * @return if the variable specified in parameter is non-basic, returns the index
	 * of this variable (between 1 and nbColums); 
	 * otherwise, returns 0
	 */
	public int nonBasicIndex(int var) {
		for (int j = 1; j <= nbNonBasicVar; j++) if (arrayNonBasicVar[j] == var) return j;
		return 0;
	}

	/** 
	 * @param listBasic list of variable numbers you want in base
	 * @return the two lists in an array of two ArrayList of indices of entering variables and
	 * indices of leaving variables
	 */
	public ArrayList<Integer>[] enteringLeaving(ArrayList<Integer> listBasic) {
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] lists = (ArrayList<Integer>[]) new ArrayList[2];
		ArrayList<Integer> listLeaving = lists[0] = new ArrayList<Integer>();
		ArrayList<Integer> listEntering = lists[1] = new ArrayList<Integer>();;

		int iS = 0, jE;

		for (Integer numE : listBasic) {
			jE = nonBasicIndex(numE);
			if (jE != -1) {// la variable de numero numE est hors base, à l'indice jE
				listEntering.add(numE); // la variable de numero numE doit entrer
				while (listBasic.contains(arrayBasicVar[iS])) iS++;
				listLeaving.add(arrayBasicVar[iS]);
				iS++;
			}
		}
		return lists;
	}

	/**
	 * pivotes by considering the numbers of the entering and leaving variables
	 * @param numE the number of the entering variable
	 * @param numS the number of the leaving variable
	 */
	public void pivoteAccordingToNumbers(int numS, int numE) {
		pivote(basicIndex(numS), nonBasicIndex(numE));
	}

	/**
	 * Allocates the memory space needed to code the dictionary.
	 */
	public void allocate(){
		arrayBasicVar = new int[nbBasicVar + 1];
		arrayNonBasicVar = new int[nbNonBasicVar + 1];
		D = new double[nbBasicVar + 1][nbNonBasicVar + 1];
	}

	public Matrix computeB(ArrayList<Integer> listBasic ) {
		Matrix B = new Matrix(nbBasicVar, nbBasicVar);
		int j, col = 0;

		for (int num : listBasic) {
			j = nonBasicIndex(num);
			if (j != -1) 
				for (int k = 0; k < nbBasicVar; k++)
					B.setValue(k, col, -D[k][j]);
			else  {
				for(int k = 0; k < nbBasicVar; k++) B.setValue(k, col, 0);
				B.setValue(basicIndex(num),  col,  1);
			}
			col++;
		}
		Simplex.output.println(B.toString());
		return B;
	}

	/** 
	 * Compute a dictionary from a file.
	 */
	/* Initializes nbBasicVar, nbNonBasicvar, D, arrayBasicVar, arrayNonBasicVar
		The file always corresponds to a problem in standard form
	 If the problem is 
	 maximize z = 4x1 + 3 x2 
	 with  
	      2x1 + 3x2 <= 24
	      5x1 + 3x2 <= 30
	      x1  + 3x2 <= 18
	      x1 >= 0, x2 >= 0
	 then the file contains :
	 2 3
	 2 3 24
	 5 3 30
	 1 3 18
	 4 3
	The first feasible dictionary is:
	 x3 = 24 - 2x1 - 3x2
	 x4 = 30 - 5x1 - 3x2
	 x5 = 18 -  x1 - 3x2
	 z  = 0  + 4x1 + 3x2
	 and, after initialization : 
	 nbNonBasicVar = 2
	 nbBasicVar = 3
	 arrayNonBasicVar is of dimension 3 and contains at indices 1 an 2, the integers 1, 2 (for decision variables x1 and x2)
	 arrayBasicVaris of dimension 4 and contains at indices 1, 2 and 3, the integers 3, 4, 5 (for the slack variables x3, x4, x5)
	 The matrix D contains:
	 0   4  3
	 24 -2 -3
	 30 -5 -3
	 18 -1 -3 
	 REMARK: we will say that the variable x1 is the variable of number 1, the variable x2 is the variable of number 2, ...
		When we talk about index, it will always be indexes in arrays and not variable indices.
	 */
	public void readFile(File file) throws IOException {
		Scanner reader ;

		reader = new Scanner(file);

		reader.useLocale(Locale.FRANCE);
		nbNonBasicVar = reader.nextInt();
		nbBasicVar = reader.nextInt();
		allocate();
		for (int i = 1; i <= nbBasicVar; i++) {
			arrayBasicVar[i] = nbNonBasicVar + i;
			for (int j = 1; j <= nbNonBasicVar; j++)  
				D[i][j] = -reader.nextDouble();
			D[i][0] = reader.nextDouble();
		}
		D[0][0] = 0;
		for (int j = 1; j <= nbNonBasicVar; j++) {
			arrayNonBasicVar[j] = j;
			D[0][j] = reader.nextDouble();
		}
		reader.close();
	}

	public Dictionary(Matrix A, Matrix B, ArrayList<Integer> base, double [] b, double []zDeb, double z0Deb) {
		ArrayList<Integer> columns = new ArrayList<Integer>();
		Matrix BInv;
		int nb = base.size();
		double[] cB, y;
		
		for (int x : base) columns.add(x - 1); // column contains the numbers of the basic variables decreased by 1
		BInv = B.getInverseMatrix();
		this.nbBasicVar = nb;
		this.nbNonBasicVar = A.getNbColumns() - nb;
		this.allocate();
		
		double [] constantes = BInv.rightProduct(b);
		for (int i = 1; i <= nb; i++) D[i][0] = constantes[i - 1];
			
		cB = new double[nb];
		for (int i = 0; i < nb; i++) cB[i] = zDeb[columns.get(i)];
		
		D[0][0] = z0Deb + Matrix.product(cB, constantes);
		y = BInv.leftProduct(cB);
		
		int indiceBase = 1;
		int indiceHorsBase = 1;
		for (int j = 0; j < A.getNbColumns(); j++) {
			if (columns.contains(j)) {
				arrayBasicVar[indiceBase] = j + 1;
				indiceBase++;
				continue;
			}
			double[] d;
			double[] a;

			arrayNonBasicVar[indiceHorsBase] = j + 1;
			a = A.getColonne(j);
			D[0][indiceHorsBase] = zDeb[j] - Matrix.product(y, a);
			d = BInv.rightProduct(a);
			for (int i = 1; i <= nb; i++) D[i][indiceHorsBase] = -d[i - 1];
			indiceHorsBase++;
		}	
		Simplex.output.displayDictionary(this);
	}


	/**
	 * Test the "almost null of a double"
	 * @param v the parameter tests.
	 * @return returns true if v is considered null, false otherwise.
	 */
	public static boolean isNull(double v) {
		return v < Dictionary.epsilon && v > -Dictionary.epsilon;
	}

	public EnteringMethod getMethod() {
		return method;
	}


	private boolean isWritten = true;
	/**
	* Enables to choose the algorithm of choice of the entering variable
	* @param method the method chosen for the entering variable
	 */
	public void setMethod(EnteringMethod method) {
		this.method = method;
		if (!isWritten) Simplex.output.println("We go to the method: " + method + "\n");
		isWritten = !isWritten;
	}


	public boolean isBland() {
		return bland;
	}

	/**
	 * Enables to choose if you use the Bland rule
	 * @param bland if the parameter is true, the Bland rule will be used, otherwise it will not be

	 */
		boolean beginning =true;
		public void setBland(boolean bland) {
		this.bland = bland;
		if (beginning) {
			beginning = false;
			return;
		}
		if (bland) Simplex.output.println("We use the criterion of Bland\n");
		else Simplex.output.println("We no longer use the Bland criterion\n");
	}

	public boolean isOptimal() {
		for (int j = 1; j <= this.nbNonBasicVar; j++)
			if (D[0][j] > 0) return false;
		return true;
	}

	public boolean isBorned() {
		return borned;
	}

	public int getNbNonBasic() {
		return nbNonBasicVar;
	}

	public int getNbBasic() {
		return nbBasicVar;
	}

	public int[] getBasicVar() {
		return arrayBasicVar;
	}

	public int[] getNonBasicVar() {
		return arrayNonBasicVar;
	}

	public double[][] getD() {
		return D;
	}
	
	public boolean isIncomplete() {
		return incomplete;
	}

	public boolean contains(int num) {
		return isBasic(num) || isNonBasic(num);
	}
	
	public boolean isBasic(int num) {
		for (int i = 1; i <= nbBasicVar; i++) if (num == arrayBasicVar[i]) return true;
		return false;
	}
	
	public boolean isNonBasic(int num) {
		for (int j = 1; j <= nbNonBasicVar; j++) if (num == arrayNonBasicVar [j]) return true;
		return false;		
	}

}

