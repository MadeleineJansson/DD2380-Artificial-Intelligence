import java.util.*;

public class Player {

    int playerA = Constants.CELL_X; // X
    int playerB = Constants.CELL_O; // O
    /**
     * Performs a move
     *
     * @param gameState
     *            the current state of the board
     * @param deadline
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState gameState, final Deadline deadline) {

        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(gameState, new Move());
        }
        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */

        int player = gameState.getNextPlayer();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int depth = 3;

        MinMaxState max = new MinMaxState(gameState, 0);

        for(GameState state : nextStates) {
            MinMaxState mss = alphabeta(state, depth, alpha, beta, player);
            if(mss.getV() > max.getV()) {
                max = mss;
            }
        }
        GameState maxState = max.getState();
        return max.getState();
    }
    /**
     * state : the current state we are analyzing
     * α : the current state value achievable by A
     * β : the current state value achievable by B
     * player : the current player (1 or 2?)
     * @returns the minimax value of the state
     */
    private MinMaxState alphabeta(GameState gameState, int depth, int alpha, int beta, int player) {

        MinMaxState res = new MinMaxState(gameState, 0);

        Vector mu = mu(gameState, player);
        Iterator it = mu.iterator();

        if(depth == 0 || mu(gameState, player).size() == 0) {
            //terminal state
            res.setV(gamma(gameState, depth));

        } else if(player == playerA) {  //player A
            res.setV(Integer.MIN_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.max(res.getV(), alphabeta(child, depth-1, alpha, beta, playerB).getV()));
                alpha = Math.max(alpha, res.getV());
                if(beta <= alpha) {
                    break;  //beta prune
                }
            }
        } else {    //player B
            res.setV(Integer.MAX_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.min(res.getV(), alphabeta(child, depth-1, alpha, beta, playerA).getV()));
                beta = Math.min(beta, res.getV());
                if(beta <= alpha) {
                    break;  //alpha prune
                }
            }
        }
        return res;
    }
    /**
     * P × S → P (S) (where P (S) is the set of all subsets of S) is a function that given a
     * player and a game state returns the possible game states that the player may achieve
     * with one legal move by the current player.
     */
    private Vector<GameState> mu(GameState gameState, int player) {

        Vector<GameState> vec = new Vector<GameState>();
        gameState.findPossibleMoves(vec);
        return vec;
    }

    /**
     * γ : P × S → R is a utility function (aka evaluation function) that given a player and a state says how “useful” the
     * state is for the player.
     */
    private int gamma(GameState gameState, int depth) {
        
        int reward = 0;

        //variables for storing X's in the diagonals (left-right (1) and right-left (2))
        int diagX1 = 0;
        int diagX2 = 0; 

        boolean diagX1Blocked = false; 
        boolean diagX2Blocked = false; 

        //variables for storing O's in the diagonals (left-right (1) and right-left (2))
        int diagO1 = 0;
        int diagO2 = 0; 

        boolean diagO1Blocked = false; 
        boolean diagO2Blocked = false; 

        int player = playerA; 
        int opponent = playerB; 

        for(int i = 0; i < gameState.BOARD_SIZE; i++) {

            int rowX = 0; //row counter for X's
            int colX = 0; //col counter for X's

            boolean rowXBlocked = false; 
            boolean colXBlocked = false; 

            int rowO = 0; 
            int colO = 0; 

            boolean rowOBlocked = false; 
            boolean colOBlocked = false; 


            for(int j = 0; j < gameState.BOARD_SIZE; j++) {
                if(gameState.at(i, j) == player && !rowXBlocked) {   //reward when many X's in a row 
                    rowX++; 
                    
                    rowO = 0; 
                    rowOBlocked = true; 

                } else if(gameState.at(i,j) == opponent) {          //penalty when X's row is blocked
                    rowX = 0;    
                    rowXBlocked = true; 

                    rowO++;
                }
                //give more reward for blocking O, the bigger the number of Os in that row, the bigger the reward.
                if(gameState.at(i, j) == player && !rowOBlocked) {   
                    reward += rowO; 
                }
                if(gameState.at(j,i) == player && !colXBlocked) {    //reward when many X's in a col
                    colX++;

                    colO = 0; 
                    colOBlocked = true; 

                } else if(gameState.at(j,i) == opponent) {          //penalty when X's col is blocked 
                    colX = 0;
                    colXBlocked = true; 

                    colO++; 
                }
                //give more reward for blocking O, the bigger the number of Os in that col, the bigger the reward.
                if(gameState.at(j, i) == player && !colOBlocked) {   
                    reward += colO; 
                }
                if(i == j) {    //left-right diagonal
                    if(gameState.at(i, j) == player && !diagX1Blocked) {    //reward X diagonal
                        diagX1++;

                        diagO1 = 0; 
                        diagO1Blocked = true; 

                    } else if(gameState.at(i,j) == opponent) {              //penalty X diagonal (blocked)
                        diagX1 = 0;
                        diagX1Blocked = true; 

                        diagO1++;
                    }
                    //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
                    if(gameState.at(i, j) == player && !diagO1Blocked) {   
                        reward += diagO1; 
                    }
                } 
                if(j == gameState.BOARD_SIZE-1-i) { //right-left diagonal
                    if(gameState.at(i,j) == player && !diagX2Blocked) {     //reward X diagonal
                        diagX2++;

                        diagO2 = 0; 
                        diagO2Blocked = true; 
                        
                    } else if(gameState.at(i, j) == opponent) {             //penalty X diagonal (blocked)
                        diagX2 = 0;
                        diagX2Blocked = false;

                        diagO2++;
                    }
                    //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
                    if(gameState.at(i, j) == player && !diagO2Blocked) {   
                        reward += diagO2; 
                    }
                }
            }

            reward += reward(rowX, depth) + reward(colX, depth); 
        }
        //reward for putting Xs in corners 
        if(gameState.at(0, gameState.BOARD_SIZE-1) == player || gameState.at(0, 0) == player) {
            reward += 10;
        }
        if(gameState.at(gameState.BOARD_SIZE-1, gameState.BOARD_SIZE-1) == player 
        || gameState.at(gameState.BOARD_SIZE-1, 0) == player) {
            reward += 10;
        }
        reward += reward(diagX1, depth) + reward(diagX2, depth);

        return reward;
    }

    /**
     * f(1) = 1 
     * f(2) = 10 
     * f(3) = 100 
     * f(4) = 1000 
     * 
     * sumPieces is the number of markers in the row, column or diagonal we are evaluting 
     */

    private int reward(int sumPieces, int depth) {
        if(sumPieces == 4) {
            return 100000;
        }
        return (int) Math.pow(10, sumPieces-1) - depth;
    }
  }
