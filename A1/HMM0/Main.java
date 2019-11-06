
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
      /*
      System.out.println("State transition matrix");
      printMatrix(transitionM);
      System.out.println("Emission matrix");
      printMatrix(emissionM);
      System.out.println("State transition matrix");
      for (double d : initialstate) {
        System.out.print(d + " ");
      }
      System.out.println();
      System.out.println();
      */
      // given the current state probability distribution what is the probabity for
      // the different emissions after the next transition (i.e. after the system has
      // made a single transition)

      // first calculate initialstiate * transitionM

      double[] piA = matrixMult(initialstate, transitionM);

      // then calculate ( intialstate * transititionM ) * emissionM
      double[] piAB = matrixMult(piA, emissionM);

      System.out.print("1 " + piAB.length + " ");
      for (int i = 0; i < piAB.length; i++) {
        System.out.print(Math.round(piAB[i] * 100) / 100.0);
        if (i < piAB.length - 1) {
          System.out.print(" ");
        }
      }
      System.out.println();
    }
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
