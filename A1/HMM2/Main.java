
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

      int numStates = transitionM.length;
      int numEmissions = emissions.length;

      //create our delta matrix
      double[][] deltaMatrix = new double[numStates][numEmissions];
      double[][] deltaidx = new double[numStates][numEmissions]; 

      //calculate delta0 and store it in our delta matrix
      double[] bio = getColumn((int) emissions[0], emissionM);
      double[] delta0 = vectorMult(bio, initialstate);
      updateColumn(0, delta0, deltaMatrix);

      //Viterbi algorithm
      for(int e = 1; e < numEmissions; e++) {
        //get delta_t-1
        double[] prevDelta = getColumn(e-1, deltaMatrix);

        for(int i = 0; i < delta0.length; i++) {
          //calculate max
          double[][] tempVec = new double[numStates][2];

          for(int j = 0; j < numStates; j++) {

            tempVec[j][0] = prevDelta[j]*transitionM[j][i]*emissionM[i][(int) emissions[e]];
            tempVec[j][1] = j; //store the corresponding state
          }
          //update the deltamatrix and deltaidx
          findMax(e, i, deltaMatrix, deltaidx,  tempVec);
        }
      }
      //backtracking 
      double[] stateSequence = backtrack(deltaidx, deltaMatrix); 
      
      //Output the most probablle sequence of states as zero based indices, without outputting the length of the sequence
      for(double d : stateSequence) {
        System.out.print((int) d + " ");
      }
      System.out.println();
    }
  }

  /**
   * Backtracking using deltaidx, return the resulting state sequence  
   */
   private static double[] backtrack(double[][] deltaidx, double[][] deltaMatrix) {
    //calculate base case (X_T* = argmax_[j...T] deltamatrix[j][T]
    double X_T = -1;
    double maxProb = -1;
    int T = deltaMatrix[0].length-1; //the last index in the deltamatrix (last time step)

    double[] stateSequence = new double[deltaMatrix[0].length];

    for(int j = 0; j < deltaMatrix.length; j++) {
      if(deltaMatrix[j][T] > maxProb) {
        maxProb = deltaMatrix[j][T]; 
        //X_T = deltaidx[j][T];
        X_T = j;
        stateSequence[T] = X_T; 
      }
    }
    //backtracking 
    for(int t = T-1; t >= 0; t--) {
      stateSequence[t] = deltaidx[(int) stateSequence[t+1]][t+1]; 
    }
    return stateSequence; 
   }

  /**
   * Find argmax and store it in the argmax vector, then the max probability and store it into our deltaMatrix
   */
  private static void findMax(
    int emissionIndex,
    int rowIndex,
    double[][] deltaMatrix,
    double[][] deltaidx,
    double[][] tempVec) {

    //go through tempvec to find argmax
    int maxState = -1;
    double maxProbability = -1;
    for(int j = 0; j < tempVec.length; j++) {
      if(tempVec[j][0] > maxProbability) {
        maxState = (int) tempVec[j][1];
        maxProbability = tempVec[j][0];
      }
    }
    //update  deltamatrix
    deltaMatrix[rowIndex][emissionIndex] = maxProbability;
    //update deltaidx 
    deltaidx[rowIndex][emissionIndex] = maxState; 
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
        System.out.print((Math.round(matrix[i][j]*100000) / 100000.00) + "\t");
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
