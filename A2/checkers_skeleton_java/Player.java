import java.util.*;

public class Player {

    int player = Constants.CELL_WHITE;
    int opponent = Constants.CELL_RED;

    Hashtable<String, Integer> hashtable = new Hashtable<String, Integer>();


        private int[] weights =
        {4, 4, 4, 4,
        4, 3, 3, 3,
        3, 2, 2, 4,
        4, 2, 1, 3,
        3, 1, 2, 4,
        4, 2, 2, 3,
        3, 3, 3, 4,
        4, 4, 4, 4};


    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = mu(pState);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
        */

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        //iterative deepening
        int maxDepth = 1;
        GameState maxState = new GameState(pState, new Move());
        int maxV = -1000000;
        boolean done = false;

        while(pDue.timeUntil() > 100000000) {
            for(GameState state : lNextStates) {

                MinMaxState mms;

                //check if we have already checked this state

                if(hashtable.get(state.toMessage()) != null) {
                    mms = new MinMaxState(state, hashtable.get(state.toMessage()));
                } else {
                    mms = alphabeta(state, maxDepth, alpha, beta, player, pDue);
                    hashtable.put(state.toMessage(), mms.getV());
                }

                if(mms.getV() > maxV) {
                    maxState = state;
                    maxV = mms.getV();
                }
            }
            maxDepth += 2;
        }
        System.err.println(maxDepth);

        return maxState;
    }

    private int gamma(GameState gameState, int depth) {

        int white = 0;
        int red = 0;

        for(int i = 0; i < gameState.NUMBER_OF_SQUARES; i++) {
            if((gameState.get(i) & (int) Constants.CELL_WHITE) == 1) {
                white += weights[i];
                if((gameState.get(i) & (int) Constants.CELL_KING) == 1) {
                    white += weights[i]*10;
                }
            } else if((gameState.get(i) & (int) Constants.CELL_RED) == 1) {
                red += weights[i];
                if((gameState.get(i) & (int) Constants.CELL_KING) == 1) {
                    red += weights[i]*10;
                }
            }
        }
        return white-red;
    }

    private Vector<GameState> mu(GameState gameState) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        gameState.findPossibleMoves(lNextStates);
        return lNextStates;

    }
    /**
     * alpha beta pruning with move ordering
     *
     * state : the current state we are analyzing
     * α : the current state value achievable by A
     * β : the current state value achievable by B
     * player : the current player
     * @returns the minimax value of the state
     */
    private MinMaxState alphabeta(GameState gameState, int depth, int alpha, int beta, int thisplayer, final Deadline pDue) {

        MinMaxState res = new MinMaxState(gameState, 0);

        Vector mu = mu(gameState);  //get all the next possible states
        Iterator it = mu.iterator();


        /*Make a comperator
        **/
        /*
        for(i = 0; i < mu.size(); i++) {
          if(mu.get(i).isJump()){
            muSorted.add(mu.get(i));
          }else if(mu.get(i).isNormal())} {

          }
        }
**/


        if(depth == 0 || mu.size() == 0) {
            //terminal state
            res.setV(gamma(gameState, depth));

        } else if(thisplayer == player) {  //player A
            res.setV(Integer.MIN_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.max(res.getV(), alphabeta(child, depth-1, alpha, beta, opponent, pDue).getV()));
                alpha = Math.max(alpha, res.getV());

                if(beta <= alpha || pDue.timeUntil() < 1000000) {
                    break;  //beta prune
                }
            }
        } else {    //player B
            res.setV(Integer.MAX_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.min(res.getV(), alphabeta(child, depth-1, alpha, beta, player, pDue).getV()));
                beta = Math.min(beta, res.getV());
                if(beta <= alpha || pDue.timeUntil() < 1000000) {
                    break;  //alpha prune
                }
            }
        }
        return res;
    }

}
