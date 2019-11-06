
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
      double[][] A = new double[numRows][numColumns];

      if (list.length != (numRows * numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      int num = 1;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numColumns; j++) {
          num++;
          A[i][j] = Double.parseDouble(list[num]);
        }
      }
      
      // Read emission matrix
      line = br.readLine();
      String[] listE = line.split(" ");

      numRows = Integer.parseInt(listE[0]);
      numColumns = Integer.parseInt(listE[1]);
      double[][] B = new double[numRows][numColumns];

      if (listE.length != (numRows * numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      num = 1;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numColumns; j++) {
          num++;
          B[i][j] = Double.parseDouble(listE[num]);
        }
      }

      // read initial state probability
      line = br.readLine();
      String[] listP = line.split(" ");

      numColumns = Integer.parseInt(listP[1]);
      double[] pi = new double[numColumns];

      if (listP.length != (numColumns + 2)) {
        throw new IOException("Matrix columns and rows do not match given dimensions");
      }

      num = 1;

      for (int j = 0; j < numColumns; j++) {
        num++;
        pi[j] = Double.parseDouble(listP[num]);
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
      
      int numStates = A.length;
      int numEmissions = emissions.length; 

      double[] cFactor = new double[numEmissions];

      int iters = 0;
      int maxIters = 100;
      double logProb = 0; 
      double oldLogProb = Double.NEGATIVE_INFINITY;


      
      while(iters < maxIters ) {

        oldLogProb = logProb;

        double[][] alpha = alpha(A, B, pi, emissions, cFactor); 
        double[][] beta = beta(A, B, pi, emissions, cFactor);
        //calculate digamma and gamma 
        double[][] gamma = new double[numStates][numEmissions];
        double[][][] digamma = new double[numStates][numStates][numEmissions];
        
        setGammor(A, B, alpha, beta, emissions, gamma, digamma);

        reEstimate(numEmissions, A, B, pi, emissions, beta, alpha, gamma, digamma);
        logProb = logProb(cFactor);
        System.out.println("New log prob " + logProb);
        iters++;
      }
      System.out.println("Made " + iters + " re-estimates");

      outputKattisMatrix(A);
      outputKattisMatrix(B);
    }
  }

  /**
   * Compute log[P(O|gamma)], the probability of of getting our emissions given our new estimate 
   */

   private static double logProb(double[] cFactor) {

    double logProb = 0;
    for(int i = 0; i < cFactor.length; i++) {
      logProb += Math.log(cFactor[i]);
    }
    logProb = -logProb;
    return logProb;
   }

  /**
   * 2.35: Estimate A matrix 
   */
  private static void reEstimate(int numEmissions, double[][] A, double[][] B, double[] pi, double[] emissions, double[][] beta, double[][] alpha, double[][] gamma, double[][][] digamma) {
    
    int N = A.length; 
    int M = B[0].length;

    //re-estimate pi 
    for(int i = 0; i < A.length; i++) {
      pi[i] = gamma[i][0];
    }

    //re-estimate A 
    for(int i = 0; i < A.length; i++) {
      for(int j = 0; j < A.length; j++) {

        double sumDiGamma = 0; 
        double sumGamma = 0; 

        for(int t = 0; t < numEmissions-1; t++) {
          sumDiGamma += digamma[i][j][t];
          sumGamma += gamma[i][t];
        }
        A[i][j] = sumDiGamma/sumGamma; 
      }
    }

    //re-estimate B 
    for(int i = 0; i < N; i++) {
      for(int j = 0; j < M; j++) {
        double numer = 0; 
        double denom = 0; 
        for(int t = 0; t < numEmissions-1; t++) {
          double gamma_ti = gamma[i][t];
          if(emissions[t] == j) {
            numer += gamma_ti;
          }
          denom += gamma_ti;
        }
        //newB[i][j] = numer/denom;
        B[i][j] = numer/denom;
      }
    }
  }

  /**
   * Array copy 
   */
  private static void arrayCopy(double[] from, double[] to) {
    for(int i = 0; i < from.length; i++) {
      to[i] = from[i];
    }
  }

  /**
   * Matrix copy, copy contents from matrix "from" to matrix "to"
   */
  private static void matrixCopy(double[][] from, double[][] to) {

    for(int i = 0; i < from.length; i++) {
      for(int j = 0; j < from[0].length; j++) {
        to[i][j] = from[i][j];
      }
    }
  }

  /**
   * Calculate gamma matrix 
   */
  private static void setGammor(double[][] A, double[][] B, double[][] alpha, double[][] beta, double[] emissions, double[][] gamma, double[][][] digamma) {

    int T = emissions.length;
    int N = A.length; 

    for(int t = 0; t < T-2; t++) {
      int denom = 0; 
      for(int i = 0; i < N; i++) {
        for(int j = 0; j < N; j++) {
          denom += alpha[i][t]*A[i][j]*B[j][(int) emissions[t+1]]*beta[j][t+1];
        }
      }
      for(int i = 0; i < N; i++) {
        gamma[i][t] = 0;
        for(int j = 0; j < N; j++) {
          digamma[i][j][t] = (alpha[i][t]*A[i][j]*B[j][(int) emissions[t+1]]*beta[j][t+1])/denom;
          gamma[i][t] += digamma[i][j][t];
        }
      }
    }

    //special case 
    int denom = 0; 
    for(int i = 0; i < N; i++) {
      denom += alpha[i][T-1];
    } 
    for(int i = 0; i < N; i++) {
      gamma[i][T-1] = alpha[i][T-1]/denom;
    }
  }

  /**
   * Gamma function 
   */
  private static double gamma(int t, int i, int N, double[][] A, double[][] B, double[] pi, double[] emissions, double[][] beta, double[][] alpha) {

    double gamma = 0; 

    for(int j = 0; j < N; j++) {
      gamma += diGamma(i, j, t, A, B, pi, emissions, beta, alpha);
    }
    return gamma;
  }

  /**
   * Di Gamma function 
   */
  private static double diGamma(int i, int j, int t, double[][] A, double[][] B, double[] pi, double[] emissions, double[][] beta, double[][] alpha) {

    double sumAlphaT = sumColumn(alpha.length-1, alpha);
    double digamma = alpha[i][t]*A[i][j]*B[j][(int) emissions[t+1]]*beta[j][t+1]/sumAlphaT;
    return digamma;
  }

  /**
   * Build the beta matrix 
   */
  private static double[][] beta(double[][] A, double[][] B, double[] pi, double[] emissions, double[] cFactor) {
    
    double[][] betaMatrix = new double[A.length][emissions.length];

    //2.26: fill the last column with 1s scaled by c_T-1
    for(int i = 0; i < A.length; i++) {
      betaMatrix[i][emissions.length-1] = cFactor[emissions.length-1]; 
    }
    //2.30
    for(int t = emissions.length-2; t >= 0; t--) {
      //get previous beta column 
      double[] prevBeta = getColumn(t+1, betaMatrix);

      for(int i = 0; i < A.length; i++) {

        for(int j = 0; j < A.length; j++) {
          //get beta_t+1(j)
          betaMatrix[i][t] += A[i][j]*B[j][(int) emissions[t+1]]*prevBeta[j];
        }
        betaMatrix[i][t] = cFactor[t]*betaMatrix[i][t];
      }
    }
    return betaMatrix;
  }

  /**
   * Build the alpha matrix 
   */
  private static double[][] alpha(double[][] A, double[][] B, double[] pi, double[] emissions, double[] cFactor) {

      //calculate alpha0
      //get the corresponding column in B matrix 
      double[] biO0 = getColumn((int) emissions[0], B); 

      double[] alpha0 = vectorMult(pi, biO0);
      //initialize skalfaktor
      double c0 = 0;
      for(int i = 0; i < alpha0.length; i++) {
        c0 += alpha0[i];
      }
      c0 = 1/c0;
      //scale alpha0
      for(int i = 0; i < alpha0.length; i++) {
        alpha0[i] = c0*alpha0[i];
      }
      cFactor[0] = c0;  

      double[][] alphaMatrix = new double[alpha0.length][emissions.length];
      try {
        updateColumn(0, alpha0, alphaMatrix);
      } catch(IOException e) {
        System.err.println(e);
      }
      //fill the alpha matrix
      for(int e = 1; e < emissions.length; e++) {

        double ct = 0;  //skalfaktor 

        double[] res = new double[alphaMatrix.length];
        double[] prevAlpha = getColumn(e-1, alphaMatrix);
        double[] bio = getColumn((int) emissions[e], B);

        for(int i = 0; i < A.length; i++) {

          for(int j = 0; j < A.length; j++) {
            res[i] += prevAlpha[j]*A[j][i];
          }
          res[i] *= bio[i];
          ct += res[i];
        }
        //System.out.println("c"+e+": "+ct);
        ct = 1/ct;
        cFactor[e] = ct; 

        for(int j = 0; j < A.length; j++) {
          res[j] = ct*res[j];
        }
        try {
          updateColumn(e, res, alphaMatrix);
        } catch(IOException E) {
          System.err.println(E);
        }
      }

      return alphaMatrix;

  }
  /**
   * Sum the last column in alpha 
   */
  private static double sumColumn(int colIndex, double[][] matrix) {
          
      //last step: sum the last column in alpha
      double[] col = getColumn(matrix[0].length-1, matrix);
      double res = 0;
      for(double d : col) {
        res += d;
      }
      //res = Math.round(res * 100000000) / 100000000.0;
      //System.out.println(res);
      return res; 
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

  /**
   * Each matrix is given on a separate line with the number of rows and columns followed by the matrix elements (ordered row by row).
   */
  private static void outputKattisMatrix(double[][] matrix) {

    int numRows = matrix.length; 
    int numCols = matrix[0].length;
    System.out.print(numRows + " " + numCols);
    for(int i = 0; i < numRows; i++) {
      for(int j = 0; j < numCols; j++) {
        System.out.print(" " + matrix[i][j]);
      }
    }
    System.out.println();
  }
}
