import java.util.*;

class Player {

    Functions f = new Functions();

    int numStates = 2; // number of hidden states
    int possibleEmissions = 9; // number of possible emissions
    int numSpecies = 6;
    ArrayList<ArrayList<HMM>> speciesHMMs;   //store the HMMs for each species. Index = species
    int round = 0;
    //int timeStep = 0;

    int[] lastGuess;
    int correctGuesses = 0;
    int totalGuesses = 0;

    // program is restarted between each environment
    // there will be up to 10 rounds in each environment
    public Player() {
        speciesHMMs = new ArrayList<ArrayList<HMM>>();

        for(int i = 0; i < numSpecies; i++) {
            speciesHMMs.add(new ArrayList<HMM>());
        }
        System.err.println("-------------New environment------------");
    }

    /**
     * Trains hmm with baum welch algorithm
     */
    private void trainHMM(HMM hmm, ArrayList<Integer> emissions) {
        //f.baumWelchMatrixDistance(hmm.A, hmm.B, hmm.pi, emissions);
        f.baumWelchLogProb(hmm.A, hmm.B, hmm.pi, emissions);
    }

    /**
     * Shoot! Called for each time step This is the function where you start your
     * work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of time
     * steps elapsed since the last time this function was called.
     *
     * We should not shoot black storks!!! We have 1-100 time steps for each round
     *
     * @param pState the GameState object with observations etc
     * @param pDue   time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {

        int numBirds = pState.getNumBirds();
        int maxBird = 0;
        double maxProb = -1;
        int bestMove = 0;
        int timeStep = 0;

        timeStep ++; //Lagt till

        if(round < 2 || timeStep < 50) {    //dont shoot on the first round
            return cDontShoot;
        }
        for (int i = 0; i < numBirds; i++) {

            Bird bird = pState.getBird(i);

            if (bird.isAlive()) {

                // calculate prob for it being a black stork
                double[] probSpecies = guessSpecies(bird);

                if ((int) probSpecies[0] == Constants.SPECIES_BLACK_STORK || probSpecies[1] < 0.5) {
                    return cDontShoot;
                }
                ArrayList<Integer> emissions = getPastMoves(bird);

                // make an HMM for each non-dead bird and train it with its past movements
                HMM hmm = new HMM(numStates, possibleEmissions);
                trainHMM(hmm, emissions);

                for (int e = 0; e < possibleEmissions; e++) {
                    // add this emission to the end of the test emission arraylist
                    emissions.add(e);
                    // do the alpha pass to calculate the likelihood of the test emission seq given
                    // our HMM model
                    double prob = f.forwardAlg(hmm.A, hmm.B, hmm.pi, emissions);
                    if (prob > maxProb) {
                        maxProb = prob;
                        bestMove = e;
                        maxBird = i;
                    }
                    //remove it from the arraylist
                    emissions.remove( emissions.size() - 1 );
                }
            }
        }
        if (maxProb > 0.7) {
            System.err.println("Made a shot");
            return new Action(maxBird, bestMove); // predict [maxBird] will make [maxMove] and shoot at it
        }
        // This line chooses not to shoot.
        return cDontShoot;
    }

    /**
     * Retrieves a birds past moves and puts them in an array of doubles
     */
    private ArrayList<Integer> getPastMoves(Bird bird) {

        int numMoves = bird.getSeqLength();
        ArrayList<Integer> moves = new ArrayList<Integer>();

        for (int i = 0; i < numMoves; i++) {
            if (bird.getObservation(i) == -1) {
                break;
            }
            moves.add(bird.getObservation(i));
        }
        return moves;
    }

    private double[] guessSpecies(Bird bird) {

        ArrayList<Integer> emissions = getPastMoves(bird);
        //use alphapass to test which hmm species fits best

        double[] probSpecies = new double[2];
        probSpecies[0] = 0;  //the most likely species (default = pigeon)
        probSpecies[1] = 0;  //the probability of the guessed species.

        for(int i = 0; i < speciesHMMs.size(); i++) {   //loop over species
            for(int j = 0; j < speciesHMMs.get(i).size(); j++) {    //loop over HMMs
                HMM h = speciesHMMs.get(i).get(j);
                double prob = f.forwardAlg(h.A, h.B, h.pi, emissions);
                if(prob > probSpecies[1]) {
                    probSpecies[0] = i;
                    probSpecies[1] = prob;
                }
            }
        }
        return probSpecies;
    }

    /**
     * Guess the species! This function will be called at the end of each round, to
     * give you a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds. Use SPECIES_UNKNOWN to avoid
     * guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue   time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        //guess for all the birds
        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i) {
            lGuess[i] = (int) guessSpecies(pState.getBird(i))[0];
        }
        lastGuess = lGuess;
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified through
     * this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird  the bird you hit
     * @param pDue   time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {

        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those birds
     * through this function.
     *
     * @param pState   the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue     time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {

        //add the HMMs to the right species HMM list.
        for(int i = 0; i < pSpecies.length; i++) {

            if(lastGuess[i] == pSpecies[i]) {
                correctGuesses++;
            }
            totalGuesses++;
            //make an HMM for this species
            if(pState.getNumBirds() < i-1) {
                break;
            }
            Bird bird = pState.getBird(i);
            HMM hmm = new HMM(numStates, possibleEmissions);
            ArrayList<Integer> emissions = getPastMoves(bird);
            trainHMM(hmm, emissions);
            speciesHMMs.get(pSpecies[i]).add(hmm);  //add the hmm to the list of hmms for this species
        }
        round++;
        System.err.println("Guesses: " + (double) 100*correctGuesses/totalGuesses + "%");
    }


    public static final Action cDontShoot = new Action(-1, -1);
}
