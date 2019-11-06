
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {

    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String line;
      // read transition matrix
      line = br.readLine();
      String[] list = line.split(" ");

      int numRows = Integer.parseInt(list[0]);
      int numColumns = Integer.parseInt(list[1]);
      double[][] transitionM = new double[numRows][numColumns];

      if (list.length != (numRows * numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      int num = 1;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numColumns; j++) {
          num++;
          transitionM[i][j] = Double.parseDouble(list[num]);
        }
      }
      // Read emission matrix
      line = br.readLine();
      String[] listE = line.split(" ");

      numRows = Integer.parseInt(listE[0]);
      numColumns = Integer.parseInt(listE[1]);
      double[][] emissionM = new double[numRows][numColumns];

      if (listE.length != (numRows * numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      num = 1;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numColumns; j++) {
          num++;
          emissionM[i][j] = Double.parseDouble(listE[num]);
        }
      }

      // read initial state probability
      line = br.readLine();
      String[] listP = line.split(" ");

      numColumns = Integer.parseInt(listP[1]);
      double[] initialstate = new double[numColumns];

      if (listP.length != (numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      num = 1;

      for (int j = 0; j < numColumns; j++) {
        num++;
        initialstate[j] = Double.parseDouble(listP[num]);
      }

      // read emissions
      line = br.readLine();
      String[] listO = line.split(" ");

      numColumns = Integer.parseInt(listO[0]);
      double[] emissions = new double[numColumns];

      if (listO.length != (numColumns + 1)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      num = 0;
      for (int j = 0; j < numColumns; j++) {
        num++;
        emissions[j] = Double.parseDouble(listO[num]);
      }
      //calculate alpha0
      //get the corresponding column in B matrix 
      double[] biO0 = getColumn((int) emissions[0], emissionM); 

      double[] alpha0 = vectorMult(initialstate, biO0);

      double[][] alphaMatrix = new double[alpha0.length][emissions.length];
      updateColumn(0, alpha0, alphaMatrix);

      //fill the alpha matrix
      for(int e = 1; e < emissions.length; e++) {

        double[] res = new double[alphaMatrix.length];
        double[] prevAlpha = getColumn(e-1, alphaMatrix);
        double[] bio = getColumn((int) emissions[e], emissionM);

        for(int i = 0; i < transitionM.length; i++) {

          for(int j = 0; j < prevAlpha.length; j++) {
            res[i] += prevAlpha[j]*transitionM[j][i];
          }
          res[i] *= bio[i];
        }
        updateColumn(e, res, alphaMatrix);
      }
      //last step: sum the last column in alpha
      double[] alphaT = getColumn(alphaMatrix[0].length-1, alphaMatrix);
      double res = 0;
      for(double d : alphaT) {
        res += d;
      }
      res = Math.round(res * 100000000) / 100000000.0;
      System.out.println(res);
    }
  }

  /**
  * Retrieve a column in a matrix and make it into a vector
  */
  private static double[] getColumn(int colIndex, double[][] matrix) {

    double[] res = new double[matrix.length];
    for(int i = 0; i < matrix.length; i++) {
      res[i] = matrix[i][colIndex];
    }
    return res;
  }

  /**
  * Update a column of a matrix given a vector
  */
  private static void updateColumn(int colIndex, double[] v, double[][] matrix) throws IOException {
    if(colIndex >= matrix[0].length) {
      System.err.println("Colindex out of bounds");
      return;
    }
    if(v.length != matrix.length) {

      throw new IOException("vector v must have the same length as the number of rows in matrix!");
    }
    for(int i = 0; i < matrix.length; i++) {
      matrix[i][colIndex] = v[i];
    }
  }

  /**
  * Multiplies a vector with another vector
  */
  private static double[] vectorMult(double[] v1, double[] v2) {

    if(v1.length != v2.length) {
      System.err.println("Vectors must be of equal length");
      return null;
    }
    double[] res = new double[v1.length];

    for(int i = 0; i < v1.length; i++) {
      res[i] = v1[i]*v2[i];
    }
    return res;
  }

  /**
   * Prints a mxn matrix
   */
  public static void printMatrix(double[][] matrix) {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[0].length; j++) {
        System.out.print(matrix[i][j] + " ");
      }
      System.out.println();
    }
  }

  /**
   * Performs matrix multiplication with a 1xm and a mxn matrix.
   *
   * @param 1xm matrix
   * @param mxn matrix
   * @returns the resulting 1xn matrix
   */
  public static double[] matrixMult(double[] m1, double[][] m2) {
    int colsOne = m1.length;
    int rowsTwo = m2.length;
    int colsTwo = m2[0].length;

    if (colsOne != rowsTwo) {
      System.err.println("Columns in Matrix 1 must be equal to the number of rows in matrix 2");
      return null;
    }

    double[] res = new double[colsTwo];

    for (int k = 0; k < colsTwo; k++) {
      for (int i = 0; i < rowsTwo; i++) {
        res[k] += m1[i] * m2[i][k];
      }
    }
    return res;
  }

}
