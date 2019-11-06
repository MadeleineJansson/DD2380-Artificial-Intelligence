
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main {

  private static Functions f;

  public static void main(String[] args) throws IOException {

    f = new Functions(); 

    //test with more or less hidden states 
    int numStates = 3;
    int possibleObservations = 4; 

    //Q10A: initialize with uniform distribution.-------------------- 
    double[][] A = new double[numStates][numStates];
    double[][] B = new double[numStates][possibleObservations];
    
    //fill A
    for(int i = 0; i < A.length; i++) {
      for(int j = 0; j < A[0].length; j++) {
        A[i][j] = (double) 1/A.length;
      }
    }
    //fill B
    for(int i = 0; i < B.length; i++) {
      for(int j = 0; j < B[0].length; j++) {
        B[i][j] = (double) 1/B[0].length;
      }
    }
    //fill pi 
    double[] pi = new double[numStates];
    for(int i = 0; i < pi.length; i++) {
      pi[i] = (double) 1/pi.length;
    }

    //Q10B: initialize diagonal A -------------------------------------
    double[][] Adiag = new double[numStates][numStates];
    double[] piB = new double[numStates];
    
    //fill diagonal A 
    for(int i = 0; i < numStates; i++) {
      Adiag[i][i] = 1;
    }

    //fill pi 
    piB[0] = 0; 
    piB[1] = 0;
    piB[2] = 1; 
    //fill B2 
    double[][] B2 = new double[numStates][possibleObservations];
    B2[0][0] = 0.5;
    B2[0][1] = 0.22;
    B2[0][2] = 0.19;
    B2[0][3] = 0.2;

    B2[1][0] = 0.28;
    B2[1][1] = 0.21;
    B2[1][2] = 0.11;
    B2[1][3] = 0.23;

    B2[2][0] = 0.15;
    B2[2][1] = 0.19;
    B2[2][2] = 0.27;
    B2[2][3] = 0.45;
    
    //Q10C: initializa with matrices that are close to the solution -----------------
    double[][] Aclose = new double[numStates][numStates];
    double[][] Bclose = new double[numStates][possibleObservations];
    double[] piclose = new double[numStates];

    Aclose[0][0] = 0.6;
    Aclose[0][1] = 0.1;
    Aclose[0][2] = 0.3;

    Aclose[1][0] = 0.0;
    Aclose[1][1] = 0.9;
    Aclose[1][2] = 0.25; 

    Aclose[2][0] = 0.3;
    Aclose[2][1] = 0.2;
    Aclose[2][2] = 0.5; 

    Bclose[0][0] = 0.6;
    Bclose[0][1] = 0.2;
    Bclose[0][2] = 0.2;
    Bclose[0][3] = 0.1;
    Bclose[1][0] = 0.0;
    Bclose[1][1] = 0.5;
    Bclose[1][2] = 0.2; 
    Bclose[1][3] = 0.2; 
    Bclose[2][0] = 0.2;
    Bclose[2][1] = 0.0;
    Bclose[2][2] = 0.1; 
    Bclose[2][3] = 0.7; 

    piclose[0] = 0.9;
    piclose[1] = 0.05; 
    piclose[2] = 0.05;

    //read emissions 
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

      String line;
      // read emissions O
      line = br.readLine();
      double[] emissions = f.readVector(line, 0);
      
      //Do estimations with uniform initialization
      System.out.println("When initializing with uniform distribution");
      f.baumWelchMatrixDistance(A, B, pi, emissions);
      System.out.println();
      //Do estimations with diagonal matrix A and pi = [0,0,1]
      System.out.println("When initializing with diagonal A and pi=[0,0,1]");
      f.baumWelchMatrixDistance(Adiag, B2, piB, emissions);
      System.out.println();
      //Do estimations close to the solution
      System.out.println("When initializing with values close to solution");
      f.baumWelchMatrixDistance(Aclose, Bclose, piclose, emissions);
      System.out.println();
    }

  }




}
