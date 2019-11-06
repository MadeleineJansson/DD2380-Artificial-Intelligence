
public class Functions {

    /**
     * Baum Welch algorithm 
     */
    public void baumWelch(double[][] A, double[][] B, double[] pi, double[] emissions) {
        
        double logProb = Double.NEGATIVE_INFINITY;
        double oldLogProb = Double.NEGATIVE_INFINITY;
        int iters = 0; 
        int maxIters = 100; 
        boolean first = true; 

        while(iters < 100 && (logProb > oldLogProb || first)) {
            first = false;
            double[] cFactor = new double[emissions.length];

            baumWelchStep(A, B, pi, emissions, cFactor);

            oldLogProb = logProb;
            logProb = logProb(cFactor);
            iters++;
        }
        outputKattisMatrix(A);
        outputKattisMatrix(B);
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
}