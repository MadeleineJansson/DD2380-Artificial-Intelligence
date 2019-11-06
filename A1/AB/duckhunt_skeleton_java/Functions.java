
import java.util.*;


public class Functions {

    /**
     * Baum welch algorithm when convergence is defined with logprob
     */

    public void baumWelchLogProb(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions) {

        double logProb = Double.NEGATIVE_INFINITY;
        double oldLogProb = Double.NEGATIVE_INFINITY;
        int iters = 0;
        int maxIters = 10000;
        boolean first = true;

        while(iters < maxIters && (logProb > oldLogProb || first)) {
            first = false;
            double[] cFactor = new double[emissions.size()];

            baumWelchStep(A, B, pi, emissions, cFactor);

            oldLogProb = logProb;
            logProb = logProb(cFactor);
            iters++;
        }

    }

    /**
     * Baum Welch algorithm when convergence is calculated with matrix distance
     * Updates A, B and pi given emissions
     */

    public void baumWelchMatrixDistance(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions) {

        int iters = 0;
        int maxIters = 1000;

        double epsilon = Math.pow(10, -7);
        double maxDiff = Math.pow(10, 5);
        double diffA = Math.pow(10, 5);
        double diffB = Math.pow(10, 5);
        double diffPi = Math.pow(10, 5);

        while(iters < maxIters && maxDiff > epsilon) {

            double[] cFactor = new double[emissions.size()];
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
    }
    public double[][] alphaPass(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions, double[] cFactor) {

        int N = A.length;
        int M = B[0].length;
        int T = emissions.size();

        //alpha pass
        double[][] alpha = new double[N][T];
        double c0 = 0;
        //compute alpha0(i)

        for(int i = 0; i < N; i++) {
            alpha[i][0] = pi[i];
            alpha[i][0] *= B[i][(int) emissions.get(0)];
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
                alpha[i][t] *= B[i][(int) emissions.get(t)];
                ct += alpha[i][t];
            }
            //scale alphat(i)
            ct = (double) 1/ct;
            cFactor[t] = ct;

            for(int i = 0; i < N; i++) {
                alpha[i][t] *= ct;
            }
        }
        return alpha;
    }
    /**
     * Alpha pass algorithm but not intended for baum-welch, doesnt use cFactor
     * @returns the probability of P(O|lambda) by summing the last column in alpha
     */
    public double forwardAlg(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions) {
        int N = A.length;
        int M = B[0].length;
        int T = emissions.size();

        //alpha pass
        double[][] alpha = new double[N][T];
        //compute alpha0(i)
        for(int i = 0; i < N; i++) {
            alpha[i][0] = pi[i]*B[i][(int) emissions.get(0)];
        }

        //compute alphat(i)
        for(int t = 1; t < T; t++) {
            double ct = 0;
            for(int i = 0; i < N; i++) {
                alpha[i][t] = 0;
                for(int j = 0; j < N; j++) {
                    alpha[i][t] += alpha[j][t-1]*A[j][i];
                }
                alpha[i][t] *= B[i][(int) emissions.get(t)];
            }

        }
        //sum the last column
        double sum = 0;
        for(int i = 0; i < alpha.length; i++) {
            sum += alpha[i][alpha[0].length-1];
        }
        return sum;
    }

    public double[][] betaPass(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions, double[] cFactor) {

        int N = A.length;
        int M = B[0].length;
        int T = emissions.size();

        double[][] beta = new double[N][T];
        //Let β T −1 (i) = 1, scaled by c T −1
        for(int i = 0; i < N; i++) {
            beta[i][T-1] = cFactor[T-1];
        }
        for(int t = T-2; t >= 0; t--) {
            for(int i = 0; i < N; i++) {
                beta[i][t] = 0;
                for(int j = 0; j < N; j++) {
                    beta[i][t] += A[i][j]*B[j][(int) emissions.get(t+1)]*beta[j][t+1];
                }
                //scale β t (i) with same scale factor as α t (i)
                beta[i][t] *= cFactor[t];
            }
        }

        return beta;
    }

    public void baumWelchStep(double[][] A, double[][] B, double[] pi, ArrayList<Integer> emissions, double[] cFactor) {

        int N = A.length;
        int M = B[0].length;
        int T = emissions.size();

        double[][] alpha = alphaPass(A, B, pi, emissions, cFactor);
        double[][] beta = betaPass(A, B, pi, emissions, cFactor);

        double[][] gamma = new double[N][T];
        double[][][] digamma = new double[N][N][T];

        for(int t = 0; t < T-1; t++) {
            for(int i = 0; i < N; i++) {
                gamma[i][t] = 0;

                for(int j = 0; j < N; j++) {
                    digamma[i][j][t] = alpha[i][t]*A[i][j]*B[j][(int) emissions.get(t+1)]*beta[j][t+1];
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
                    if(emissions.get(t) == j) {
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

        for(int i = 0; i < m1.length; i++) {
            for(int j = 0; j < m1[0].length; j++) {
                double diff = Math.sqrt(Math.pow(m1[i][j] - m2[i][j], 2));
                if(diff > res) {
                    res = diff;
                }
            }
        }
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
        res = Math.sqrt(res);
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
            res[i] = Math.abs((double) 1/N + (r.nextDouble()-0.5)/100);
            sum += res[i];
        }
        //divide by the sum to make the sum of the row equal to 1
        for(int i = 0; i < N; i++) {
            res[i] = res[i]/sum;
        }
        return res;
    }

/**
* Randomize A matrix try 2
*/
/**     public double[][] randomizeTransitionMatrix(int N) {

      Random r = new Random();

      double[][] A = new double[N][N];
      double rangeMin = 0.9;
      double rangeMax = 1;
      double sum = 0;

      double randomDiagonal = rangeMin + (rangeMax - rangeMin) * r.nextDouble(); //Extracting a value between 0.9-1
      double[] randomRestRow = randomizeVector(N-1); //Extracting random values and later divide the values with a denominator

      for(int i = 0; i < N; i++){
        for(int j=0; j < N; j++){
        if(i == j){
          A[i][i] = randomDiagonal;
          sum += randomDiagonal;
        } else{
          double scale = randomRestRow[j]/10;
          A[i][j] = scale;
          sum += scale;
        }
      }
      for(int k = 0; k < N; k++) {
        A[i][k] = A[i][k]/sum;
      }
    }
   for (int i = 0; i < A.length; i++) {
      for (int j = 0; j < A[0].length; j++) {
        System.err.print(A[i][j] + " ");
      }
      System.out.println();
    }
    return A;
  }
*/

/**
* Randomize A matrix try 1
*/

/*      for(int i= 0; i < N; i++){
          double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
          sum += randomValue;

          for(int t = 0; t < N-1; t++){
            double randValueRest = 0 + ((1-randomValue) - 0) * r.nextDouble();
            sum += randValueRest;

            for(int j=0; j < N; j++){
              if(i == j){
                A[i][i] = randomValue;
              } else{
                A[i][j] = randValueRest;
              }
            }
        }
        for(int k = 0; k < N; k++) {
            A[i][k] = A[i][k]/sum;
        }
      }
        for (int i = 0; i < A.length; i++) {
          for (int j = 0; j < A[0].length; j++) {
            System.err.print(A[i][j] + " ");
          }
          System.out.println();
        }
      return A;
    }*/
}
