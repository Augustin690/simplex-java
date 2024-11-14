package matrix;

import java.util.ArrayList;

/** 
 * @author ASLAN Hikmet
 * @version 1.1
 */
public class Matrix
{
	private double[][] coeff = null;
	//----------------------------------------------//
	// CONSTRUCTOR //
	//----------------------------------------------//
	/** Constructor Matrix
	 * @param
	 * i - row
	 * j - column
	 */
	public Matrix(int i, int j)
	{
		this.setDimension(i,j);
	}
	public Matrix()
	{
		this(0,0);
	}
	public Matrix(double[][] mat)
	{
		this.coeff = mat;
	}

	// define a matrix of type double[][]
	public void setMatrix(double[][] mat)
	{
		this.coeff = mat;
	}
	// define a value at position i and j
	// i - row
	// j - column
	public void setValue(int i, int j, double value)
	{
		this.coeff[i][j] = value;
	}
	// we define the size of the matrix
	public void setDimension(int i, int j)
	{
		this.coeff = new double[i][j];
	}
	//----------------------------------------------//
	// GETTER //
	//----------------------------------------------//
	// returns the matrix as a double[][]
	public double[][] getMatrix
	()
	{
		return this.coeff;
	}
	// returns the number of rows
	public int getNbRows()
	{
		return this.coeff.length;
	}
	// returns the number of columns
	public int getNbColumns()
	{
		return this.coeff[0].length;
	}
	
	public double[] getColonne(int j) {
		double[] col = new double[getNbRows()];
		
		for (int i = 0; i < getNbRows(); i++) col[i] = getValue(i, j);
		return col;
	}

	// returns the value at the position i and j
	public double getValue(int i, int j) {
		return this.coeff[i][j];
	}

	// returns the determinant of a matrix
	public double getDeterminant()
	{
		Matrix a = null;
		double value = 0;
		if (this.getNbRows() == 2)
			return (this.getValue(0,0) * this.getValue(1,1) - this.getValue(1,0)* this.getValue(0,1));
		else if (this.getNbRows() == 1) return this.getValue(0,0);
		for (int j=0; j<this.getNbColumns(); j++)
		{
			a = this.getExtractMatrix(0,j);
			value += (int)Math.pow(-1,j)*(this.getValue(0,j)*a.getDeterminant());
		}
		return value;
	}

	// return the inverse matrix  of the matrix this
	public Matrix getInverseMatrix()
	{
		Matrix a = new Matrix(this.getNbRows(), this.getNbColumns());
		Matrix tmp = null;
		double det = this.getDeterminant();
		for (int i=0; i<this.getNbRows(); i++)
			for (int j=0; j< this.getNbColumns(); j++)
			{
				tmp = this.getExtractMatrix(i,j);
				a.setValue(i,j,(int)Math.pow(-1,i+j)*(tmp.getDeterminant()/det));
			}
		return a.getTransposeMatrix();
	}

	/* Retourne une nouvelle matrice mais en supprimant
	 * la ligne row et la colonne columns
	 */
	private Matrix getExtractMatrix(int row, int column)
	{
		Matrix a = new Matrix(this.getNbRows()-1, this.getNbColumns()-1);
		int k = -1, m = 0;
		for (int i=0; i<this.getNbRows(); i++)
		{
			k++;
			if (i == row)
			{
				k--;
				continue;
			}
			m = -1;
			for (int j=0; j<this.getNbColumns(); j++)
			{
				m++;
				if (j==column)
				{
					m--;
					continue;
				}
				a.setValue(k,m,this.getValue(i,j));
			}
		}
		return a;
	}

	// transpose la matrice
	public Matrix getTransposeMatrix()
	{
		Matrix a = new Matrix(this.getNbColumns(), this.getNbRows());
		double tmp = 0;
		for (int i=0; i<a.getNbRows(); i++)
			for (int j=0; j<a.getNbColumns(); j++)
			{
				tmp = this.getValue(j,i);
				a.setValue(i,j,tmp);
			}
		return a;
	}

	// multiplication
	public Matrix multiply(final Matrix matrix)
	{
		Matrix a = new Matrix(this.getNbRows(), this.getNbColumns());
		int k,i,j;
		double value = 0;
		for (k=0; k<this.getNbColumns(); k++)
		{
			for (i=0; i<this.getNbRows(); i++)
			{
				for (j=0; j<this.getNbColumns(); j++)
					value += this.getValue(i,j)*matrix.getValue(j,k);
				a.setValue(i,k,value);
				value = 0;
			}
		}
		return a;
	}

	// addition
	public Matrix matrixSum(Matrix matrix)
	{
		Matrix a = new Matrix(this.getNbRows(), this.getNbColumns());
		for (int i=0; i<this.getNbRows(); i++)
			for (int j=0; j<this.getNbColumns(); j++)
				a.setValue(i,j,this.getValue(i,j)+matrix.getValue(i,j));
		return a;
	}

	// substraction
	public Matrix substraction(final Matrix matrix)
	{
		Matrix a = new Matrix(this.getNbRows(), this.getNbColumns());
		for (int i=0; i<this.getNbRows(); i++)
			for (int j=0; j<this.getNbColumns(); j++)
				a.setValue(i,j,this.getValue(i,j)-matrix.getValue(i,j));
		return a;
	}

	// détermine if the matrix is invertible
	public boolean isInvertible()
	{
		return (this.getDeterminant() != 0);
	}



	@Override
	public String toString()
	{
		String out = "";
		for (int i=0; i<this.getNbRows(); i++)
		{
			for (int j=0; j< this.getNbColumns(); j++)
				out += this.coeff[i][j] + "\t ";
			out += "\n";
		}
		return out;
	}
	

	
	public Matrix extract(ArrayList<Integer> colonnes) {
		int num;
		int nb = getNbRows();
		Matrix B = new Matrix(nb, nb);

		for (int j = 0; j < nb; j++) {
			num = colonnes.get(j);
			for (int i = 0; i < nb; i++) 
				B.setValue(i, j , getValue(i, num));
		}
		return B;
	}
	
	public double[] leftProduct(double[] row) {
		double P[] = new double[getNbRows()];
		
		for (int j = 0; j < this.getNbColumns(); j++) {
			P[j] = 0;
			for (int i = 0; i < getNbColumns(); i++) P[j] += row[i] * getValue(i, j);
		}
		return P;
	}
	

	public double[] rightProduct(double[] col) {
		double P[] = new double[getNbRows()];
		
		for (int i = 0; i < this.getNbRows(); i++) {
			P[i] = 0;
			for (int j = 0; j < getNbColumns(); j++) P[i] += getValue(i, j) * col[j];
		}
		return P;
	}
	
	public static double product(double[] ligne, double[] col) {
		double P = 0;
		for (int i = 0; i < ligne.length; i++) P += ligne[i] * col[i];
		return P;
	}
}
