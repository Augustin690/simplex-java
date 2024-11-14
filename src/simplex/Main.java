package simplex;

import java.io.IOException;

import java.io.File;


/**
 * Contains the main method
 */
public class Main { 
	public static void main(String[] arg) throws IOException {
		Simplex simplex = new Simplex();
		Scenario scenario = new Scenario(simplex);
		simplex.view = scenario;
		simplex.controller = scenario.controller;
	}
	
	/*public static void main(String[] arg) throws IOException {
		File f = new File("pbs/pb30");
		Dictionary dico = new Dictionary(f);
		dico.isFeasible();
		System.out.print(dico.searchFirstIndexEnteringVariable());
	}*/
	
}
