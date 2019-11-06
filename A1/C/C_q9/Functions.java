import java.util.*;

public class Functions {

    /**
     * Baum welch algorithm when convergence is defined with logprob 
     */

    public void baumWelchLogProb(double[][] A, double[][] B, double[] pi, double[] emissions) {
        
        double logProb = Double.NEGATIVE_INFINITY;
        double oldLogProb = Double.NEGATIVE_INFINITY;
        int iters = 0; 
        int maxIters = 10000; 
        boolean first = true; 

        while(iters < maxIters && (logProb > oldLogProb || first)) {
            first = false;
            double[] cFactor = new double[emissions.length];

            baumWelchStep(A, B, pi, emissions, cFactor);

            oldLogProb = logProb;
            logProb = logProb(cFactor);
            iters++;
        }
        System.out.println("iters: " + iters);

        printMatrix(A);
        System.out.println();
        printMatrix(B);
        System.out.println();
        for(double d : pi) {
            System.out.print(d + " ");
        }
        System.out.println();
    }
    
    /**
     * Baum Welch algorithm when convergence is calculated with matrix distance
     */
    
    public void baumWelchMatrixDistance(double[][] A, double[][] B, double[] pi, double[] emissions) {

        int iters = 0; 
        int maxIters = 10000; 

        double epsilon = Math.pow(10, -10);
        double maxDiff = Double.POSITIVE_INFINITY;
        double diffA = Double.POSITIVE_INFINITY;
        double diffB = Double.POSITIVE_INFINITY;
        double diffPi = Double.POSITIVE_INFINITY;


        while(iters < maxIters && maxDiff > epsilon) {
            
            double[] cFactor = new double[emissions.length];
            //store the old HMM model 
            double[][] oldA = new double[A.length][A[0].length];
            double[][] oldB = new double[B.length][B[0].length];
            double[] oldPi = new double[pi.length];
            matrixCopy(A, oldA);
            matrixCopy(B, oldB);
            arrayCopy(pi, oldPi);
            //re-estimate the model 
            baumWelchStep(A, B, pi, emissions, cFactor);
            //calculate differences 
            diffA = matrixDistance(A, oldA);
            diffB = matrixDistance(B, oldB);
            diffPi = vectorDistance(pi, oldPi);
            //set maxdiff to the maximum difference 
            maxDiff = Math.max(diffA, diffB);
            maxDiff = Math.max(maxDiff, diffPi);

            iters++;
        }
        System.out.println("iters: " + iters);

        printMatrix(A);
        System.out.println();
        printMatrix(B);
        System.out.println();
        //print pi 
        for(double d : pi) {
            System.out.print(d + " ");
        }
        System.out.println();
    }
    

    public void baumWelchStep(double[][] A, double[][] B, double[] pi, double[] emissions, double[] cFactor) {

        int N = A.length; 
        int M = B[0].length;
        int T = emissions.length; 

        //alpha pass 
        double[][] alpha = new double[N][T];
        double c0 = 0; 
        //compute alpha0(i)
        for(int i = 0; i < N; i++) {
            alpha[i][0] = pi[i]*B[i][(int) emissions[0]];
            c0 += alpha[i][0];
        }
        //scale alpha0(i)
        c0 = (double) 1/c0; 
        cFactor[0] = c0;

        for(int i = 0; i < N; i++) {
            alpha[i][0] *= c0;
        }
        //compute alphat(i)
        for(int t = 1; t < T; t++) {
            double ct = 0; 
            for(int i = 0; i < N; i++) {
                alpha[i][t] = 0; 
                for(int j = 0; j < N; j++) {
                    alpha[i][t] += alpha[j][t-1]*A[j][i];
                }
                alpha[i][t] *= B[i][(int) emissions[t]];
                ct += alpha[i][t];
            }
            //scale alphat(i)
            ct = (double) 1/ct; 
            cFactor[t] = ct;

            for(int i = 0; i < N; i++) {
                alpha[i][t] *= ct;
            }
        }
        //beta pass
        double[][] beta = new double[N][T];
        //Let β T −1 (i) = 1, scaled by c T −1
        for(int i = 0; i < N; i++) {
            beta[i][T-1] = cFactor[T-1];
        }
        for(int t = T-2; t >= 0; t--) {
            for(int i = 0; i < N; i++) {
                beta[i][t] = 0; 
                for(int j = 0; j < N; j++) {
                    beta[i][t] += A[i][j]*B[j][(int) emissions[t+1]]*beta[j][t+1];
                }
                //scale β t (i) with same scale factor as α t (i)
                beta[i][t] *= cFactor[t];
            }
        }

        double[][] gamma = new double[N][T];
        double[][][] digamma = new double[N][N][T];
        
        for(int t = 0; t < T-1; t++) {
            for(int i = 0; i < N; i++) {
                gamma[i][t] = 0;

                for(int j = 0; j < N; j++) {
                    digamma[i][j][t] = alpha[i][t]*A[i][j]*B[j][(int) emissions[t+1]]*beta[j][t+1];
                    gamma[i][t] += digamma[i][j][t];
                }
            }
        }
        for(int i = 0; i < N; i++) {
            gamma[i][T-1] = alpha[i][T-1];
        }

        //re-estimate pi 
        for(int i = 0; i < N; i++) {
            pi[i] = gamma[i][0];
        }

        //re-estimate A
        for(int i = 0; i < N; i++) {
            double denom = 0;
            for(int t = 0; t < T-1; t++) {
                denom += gamma[i][t];
            }
            for(int j = 0; j < N; j++) {
                double numer = 0;
                for(int t = 0; t < T-1; t++) {
                    numer += digamma[i][j][t];
                }
                A[i][j] = numer/denom;
            }
        }
        //re-estimate B 
        for(int i = 0; i < N; i++) {
            double denom = 0; 
            for(int t = 0; t < T; t++) {
                denom += gamma[i][t];
            }
            for(int j = 0; j < M; j++) {
                double numer = 0;
                for(int t = 0; t < T; t++) {
                    if(emissions[t] == j) {
                        numer += gamma[i][t];
                    }
                }
                B[i][j] = numer/denom;
            }
        }
    }

    /**
     * Calculate distance between two matrices 
     */
    public double matrixDistance(double[][] m1 ,double[][] m2) {
        double res = 0; 
        //using min square error
        for(int i = 0; i < m1.length; i++) {
            for(int j = 0; j < m1[0].length; j++) {
                double diff = Math.pow(m1[i][j] - m2[i][j], 2);
                res += diff;
            }
        }
        res = res/(m1.length*m1[0].length);
        return res; 
    }

    /**
     * Caluclate distance between 1xn matrices
     */
    public double vectorDistance(double[] v1, double[] v2) {
        double res = 0;

        for(int i = 0; i < v1.length; i++) {
            res += Math.pow(v1[i]-v2[i], 2);
        }
        //res = Math.sqrt(res);
        res = res/v1.length;
        return res; 
    }

    /**
     * Compute log[P(O|gamma)], the probability of of getting our emissions given our new estimate 
     */

    public double logProb(double[] cFactor) {

        double logProb = 0;
        for(int i = 0; i < cFactor.length; i++) {
            logProb += Math.log(cFactor[i]);
        }
        logProb = -logProb;
        return logProb;
    }

    public double[][] readMatrix(String line) {

        String[] list = line.split(" ");

        int numRows = Integer.parseInt(list[0]);
        int numColumns = Integer.parseInt(list[1]);

        double[][] res = new double[numRows][numColumns];
  
        int num = 1;
        for (int i = 0; i < numRows; i++) {
          for (int j = 0; j < numColumns; j++) {
            num++;
            res[i][j] = Double.parseDouble(list[num]);
          }
        }
        return res;
    }
    /**
     * Reads in a vector from the line. Index shows which index the vector length is given.
     */
    public double[] readVector(String line, int index) {

        String[] list = line.split(" ");
        int numColumns = Integer.parseInt(list[index]);
        double[] res = new double[numColumns];

        int num = index + 1;
        for (int j = 0; j < numColumns; j++) {
            res[j] = Double.parseDouble(list[num]);
            num++;
        }
        return res;
    }

    /**
     * Multiplies a vector with another vector
    */
    public double[] vectorMult(double[] v1, double[] v2) {

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
    * Each matrix is given on a separate line with the number of rows and columns followed by the matrix elements (ordered row by row).
    */
    public void outputKattisMatrix(double[][] matrix) {

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

      /**
     * Array copy 
     */
    public void arrayCopy(double[] from, double[] to) {
        for(int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

    /**
     * Matrix copy, copy contents from matrix "from" to matrix "to"
     */
    public void matrixCopy(double[][] from, double[][] to) {

        for(int i = 0; i < from.length; i++) {
                for(int j = 0; j < from[0].length; j++) {
                    to[i][j] = from[i][j];
                }
        }
    }

    /**
     * Randomize the numbers (between 0 and 1) in a vector of length N, the sum of all values must be 1.
     */

    public double[] randomizeVector(int N) {

        Random r = new Random(); 

        double[] res = new double[N];
        double sum = 0; 

        for(int i = 0; i < N; i++) {
            res[i] = (double) 1/N + (r.nextDouble()-0.5)/100;
            sum += res[i];
        }
        for(int i = 0; i < N; i++) {
            res[i] = res[i]/sum;
        }

        return res; 
    }

}