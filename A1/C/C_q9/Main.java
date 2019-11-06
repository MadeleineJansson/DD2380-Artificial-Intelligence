
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

    //randomize A, B, and pi 
    double[][] A = new double[numStates][numStates];
    double[][] B = new double[numStates][possibleObservations];
    
    //randomize A
    for(int i = 0; i < A.length; i++) {
      A[i] = f.randomizeVector(A[0].length);
    }
    //randomize B
    for(int i = 0; i < B.length; i++) {
      B[i] = f.randomizeVector(B[0].length);
    }
    //randomize pi 
    double[] pi = f.randomizeVector(numStates);
    
    f.printMatrix(A);
    System.out.println();
    f.printMatrix(B);
    System.out.println();
    for(double d : pi) {
      System.out.print(d + " ");
    }
    System.out.println();
    
    //read emissions 
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

      String line;
      // read emissions O
      line = br.readLine();
      double[] emissions = f.readVector(line, 0);
      //Do estimations 
      System.out.println("With matrix distance");
      f.baumWelchMatrixDistance(A, B, pi, emissions);
    }

  }




}
