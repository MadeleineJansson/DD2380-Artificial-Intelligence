import java.util.*;

public class Player {

    int player = Constants.CELL_X; // X 
    int opponent = Constants.CELL_O; // O

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

        long starttime = System.currentTimeMillis();

        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(gameState, new Move());
        }

        int player = gameState.getNextPlayer();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int depth = 0;    //our depth should be 1 smaller than the real depth 

        MinMaxState max = new MinMaxState(gameState, 0);

        for(GameState state : nextStates) { //64 iterationer (om startar på s0)
            MinMaxState mss = alphabeta(state, depth, alpha, beta, player, deadline);   //2 ggr om depth = 1 
            if(mss.getV() > max.getV()) {
                max = mss;
            }
        }
        GameState maxState = max.getState();
        System.err.println("func play time: " + (System.currentTimeMillis()-starttime));
        return maxState;
    }

    /**
     * state : the current state we are analyzing
     * α : the current state value achievable by A
     * β : the current state value achievable by B
     * player : the current player 
     * @returns the minimax value of the state
     */
    private MinMaxState alphabeta(GameState gameState, int depth, int alpha, int beta, int thisplayer, final Deadline deadline) {

        MinMaxState res = new MinMaxState(gameState, 0);

        Vector mu = mu(gameState);  //get all the next possible states 
        Iterator it = mu.iterator();

        if(depth == 0 || mu.size() == 0) {
            //terminal state
            res.setV(gamma(gameState, depth));

        } else if(thisplayer == player) {  //player A
            res.setV(Integer.MIN_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.max(res.getV(), alphabeta(child, depth-1, alpha, beta, opponent, deadline).getV()));
                alpha = Math.max(alpha, res.getV());
                if(beta <= alpha) {
                    break;  //beta prune
                }
            }
        } else {    //player B
            res.setV(Integer.MAX_VALUE);
            while(it.hasNext()) {
                GameState child = (GameState) it.next();
                res.setV(Math.min(res.getV(), alphabeta(child, depth-1, alpha, beta, player, deadline).getV()));
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
    private Vector<GameState> mu(GameState gameState) {

        Vector<GameState> vec = new Vector<GameState>();
        gameState.findPossibleMoves(vec);
        return vec;
    }

    /**
     * γ : P × S → R is a utility function (aka evaluation function) that given a player and a state says how “useful” the
     * state is for the player.
     * This is seen from player Xs perspektiv 
     */
    private int gamma(GameState gameState, int depth) {
        
        int reward = 10; 
                
        //check if winning move, if yes then give max reward
        if(gameState.isXWin()) {
            return reward(4, depth);
        }

        for(int i = 0; i < gameState.BOARD_SIZE; i++) { 
            
            reward += checkLayer(i, gameState, depth);
            reward += checkView2(i, gameState, depth); 
            reward += checkView3(i, gameState, depth);
        }
        reward += checkCorners(gameState);
        reward += checkMainDiagonals(gameState, depth);
        return reward;
    }
    /**
     * Calculate the reward given the number of pieces in a row and the depth we are at 
     */
    private int reward(int numInRow, int depth) {

        return (int) Math.pow( 10, numInRow-1) - depth;
    }
    /**
     * Calculate reward for the four main diagonals which intersect the midpoint of the cube 
     */
    private int checkMainDiagonals(GameState gameState, int depth) {

        int reward = 0; 
        
        int diagX1 = 0;      //corner (0,0,0) to (3,3,3)
        boolean diagX1Blocked = false; 
        
        int diagX2 = 0;      //corner (3,3,0) to (0,0,3)
        boolean diagX2Blocked = false; 

        int diagX3 = 0;      //corner (3,0,0) to (0,3,3) 
        boolean diagX3Blocked = false; 

        int diagX4 = 0;      //corner (0,3,0) to (3,0,3)
        boolean diagX4Blocked = false; 

        int diagO1 = 0; 
        boolean diagO1Blocked = false; 

        int diagO2 = 0;      //corner (3,3,0) to (0,0,3)
        boolean diagO2Blocked = false; 

        int diagO3 = 0;      //corner (3,0,0) to (0,3,3) 
        boolean diagO3Blocked = false; 

        int diagO4 = 0;      //corner (0,3,0) to (3,0,3)
        boolean diagO4Blocked = false; 


        for(int layer = 0; layer < gameState.BOARD_SIZE; layer++) {
            for(int row = 0; row < gameState.BOARD_SIZE; row++) {
                for(int col = 0; col < gameState.BOARD_SIZE; col++) {
                    //diag 1 
                    if(row == col && col == layer) {
                        if(gameState.at(row, col, layer) == player && !diagX1Blocked) {

                            diagX1++;

                            if(!diagO1Blocked) {
                                reward += diagO1*10; 
                            }

                            diagO1Blocked = true; 
                            diagO1 = 0; 

                        } else if(gameState.at(row, col, layer) == opponent) {

                            diagX1Blocked = true; 
                            diagX1 = 0; 

                            if(!diagO1Blocked) {
                                diagO1++;
                            }
                        }
                    }
                    //diag 2 
                    if(row == col && layer == gameState.BOARD_SIZE-1-row) {
                        if(gameState.at(row, col, layer) == player && !diagX2Blocked) {

                            diagX2++;

                            if(!diagO2Blocked) {
                                reward+= diagO2*10; 
                            }

                            diagO2Blocked = true; 
                            diagO2 = 0;

                        } else if(gameState.at(row, col, layer) == opponent) {

                            diagX2Blocked = true; 
                            diagX2 = 0; 

                            if(!diagO2Blocked) {
                                diagO2++; 
                            }
                        }
                    }

                    //diag 3 
                    if(col == layer && row == gameState.BOARD_SIZE-1-col) {
                        if(gameState.at(row, col, layer) == player && !diagX3Blocked) {
                            diagX3++;
                            if(!diagO3Blocked) {
                                reward += diagO3*10; 
                            }
                            diagO3Blocked = true; 
                            diagO3 = 0; 

                        } else if(gameState.at(row, col, layer) == opponent) {
                            diagX3Blocked = true; 
                            diagX3 = 0; 
                            if(!diagO3Blocked) {
                                diagO3++; 
                            }
                        }
                    }

                    //diag 4 
                    if(row == layer && col == gameState.BOARD_SIZE-1-layer) {
                        if(gameState.at(row, col, layer) == player && !diagX4Blocked) {

                            diagX4++;
                            if(!diagO4Blocked) {    //give reward for the first time we block opponent
                                reward += diagO4*10;
                            }
                            diagO4Blocked = true; 
                            diagO4 = 0; 

                        } else if(gameState.at(row, col, layer) == opponent) {
                            diagX4Blocked = true; 
                            diagX4 = 0; 
                            if(!diagO4Blocked) {
                                diagO4++; 
                            }
                        }
                    }
                }
            }
        }

        reward += reward(diagX1, depth) + reward(diagX2, depth) + reward(diagX3, depth) + reward(diagX4, depth);

        return reward; 
    }
    /**
     * Calculate reward for diagonals (view 3, birds eye view)
     */
    private int checkView3(int row, GameState gameState, int depth) {

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

        //rows remain constant 
        for(int col = 0; col < gameState.BOARD_SIZE; col++) {

            for(int layer = 0; layer < gameState.BOARD_SIZE; layer++) {

                if(layer == col) {    //left-right diagonal
                    if(gameState.at(row, col, layer) == player && !diagX1Blocked) {    //reward X diagonal
                        diagX1++;

                        diagO1Blocked = true; 

                    } else if(gameState.at(row, col, layer) == opponent) {              //penalty X diagonal (blocked)
                        diagX1 = 0;
                        diagX1Blocked = true; 

                        diagO1++;
                    }
                    
                } 
                if(col == gameState.BOARD_SIZE-1-layer) { //right-left diagonal
                    if(gameState.at(row, col, layer) == player && !diagX2Blocked) {     //reward X diagonal
                        diagX2++;

                        diagO2Blocked = true; 
                        
                    } else if(gameState.at(row, col, layer) == opponent) {             //penalty X diagonal (blocked)
                        diagX2 = 0;
                        diagX2Blocked = false;

                        diagO2++;
                    }

                }
            }
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO1Blocked) {   
            reward += diagO1; 
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO2Blocked) {   
            reward += diagO2; 
        }

        reward += reward(diagX1, depth) + reward(diagX2, depth);

        return reward; 
    }

    /**
     * Calculate reward for rows and diagonals (view 2, from right hand plane)
     */
    private int checkView2(int col, GameState gameState, int depth) {

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

        for(int row = 0; row < gameState.BOARD_SIZE; row++) {

            int rowX = 0; //row counter for X's

            boolean rowXBlocked = false; 

            int rowO = 0; 

            boolean rowOBlocked = false; 

            //columns are constant here 

            for(int layerIndex = 0; layerIndex < gameState.BOARD_SIZE; layerIndex++) {

                if(gameState.at(row, col, layerIndex) == player && !rowXBlocked) {   //reward when many X's in a row 
                    rowX++; 
                    
                    rowOBlocked = true; 

                } else if(gameState.at(row, col, layerIndex) == opponent) {          //penalty when X's row is blocked
                    rowX = 0;    
                    rowXBlocked = true; 

                    rowO++;
                }
                
                if(row == layerIndex) {    //left-right diagonal
                    if(gameState.at(row, col, layerIndex) == player && !diagX1Blocked) {    //reward X diagonal
                        diagX1++;

                        diagO1Blocked = true; 

                    } else if(gameState.at(row,col, layerIndex) == opponent) {              //penalty X diagonal (blocked)
                        diagX1Blocked = true; 

                        diagO1++;
                    }
                    
                } 
                if(layerIndex == gameState.BOARD_SIZE-1-row) { //right-left diagonal
                    if(gameState.at(row, col, layerIndex) == player && !diagX2Blocked) {     //reward X diagonal
                        diagX2++;
                        diagO2Blocked = true; 
                        
                    } else if(gameState.at(row, col, layerIndex) == opponent) {             //penalty X diagonal (blocked)
                        diagX2 = 0;
                        diagX2Blocked = false;

                        diagO2++;
                    }

                }
            }
            //give more reward for blocking O, the bigger the number of Os in that row, the bigger the reward.
            if(rowOBlocked) {   
                reward += rowO; 
            }
            reward += reward(rowX, depth); 
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO1Blocked) {   
            reward += diagO1; 
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO2Blocked) {   
            reward += diagO2; 
        }

        reward += reward(diagX1, depth) + reward(diagX2, depth);

        return reward; 
    }

    /**
     * Calculate reward (for X) for a specific layer 
     */
    private int checkLayer(int layerIndex, GameState gameState, int depth) {

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
                if(gameState.at(i, j, layerIndex) == player && !rowXBlocked) {   //reward when many X's in a row 
                    rowX++; 
                    
                    rowOBlocked = true; 

                } else if(gameState.at(i,j, layerIndex) == opponent) {          //penalty when X's row is blocked
                    rowX = 0;    
                    rowXBlocked = true; 

                    rowO++;
                }
                
                if(gameState.at(j, i, layerIndex) == player && !colXBlocked) {    //reward when many X's in a col
                    colX++;

                    colOBlocked = true; 

                } else if(gameState.at(j, i, layerIndex) == opponent) {          //penalty when X's col is blocked 
                    colX = 0;
                    colXBlocked = true; 

                    colO++; 
                }
                
                if(i == j) {    //left-right diagonal
                    if(gameState.at(i, j, layerIndex) == player && !diagX1Blocked) {    //reward X diagonal
                        diagX1++;

                        diagO1 = 0; 
                        diagO1Blocked = true; 

                    } else if(gameState.at(i,j, layerIndex) == opponent) {              //penalty X diagonal (blocked)
                        diagX1 = 0;
                        diagX1Blocked = true; 

                        diagO1++;
                    }

                } 
                if(j == gameState.BOARD_SIZE-1-i) { //right-left diagonal
                    if(gameState.at(i, j, layerIndex) == player && !diagX2Blocked) {     //reward X diagonal
                        diagX2++;

                        diagO2Blocked = true; 
                        
                    } else if(gameState.at(i, j, layerIndex) == opponent) {             //penalty X diagonal (blocked)
                        diagX2 = 0;
                        diagX2Blocked = false;

                        diagO2++;
                    }

                }

            }
            //give more reward for blocking O, the bigger the number of Os in that row, the bigger the reward.
            if(rowOBlocked) {   
                reward += rowO*10; 
            }
            //give more reward for blocking O, the bigger the number of Os in that col, the bigger the reward.
            if(colOBlocked) {   
                reward += colO*10; 
            }
            reward += reward(rowX, depth) + reward(colX, depth); 
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO1Blocked) {   
            reward += diagO1; 
        }
        //give more reward for blocking O, the bigger the number of Os in that diagonal, the bigger the reward.
        if(diagO2Blocked) {   
            reward += diagO2; 
        }

        reward += reward(diagX1, depth) + reward(diagX2, depth);

        return reward; 
    }

    /**
     * Give reward for putting markers in the corners of the cube 
     */
    private int checkCorners(GameState gameState) {

        int reward = 0; 

        //reward for putting Xs in corners 
        if(gameState.at(0, gameState.BOARD_SIZE-1, 0) == player || gameState.at(0, gameState.BOARD_SIZE-1, gameState.BOARD_SIZE-1) == player) {
            reward += 2;
        }
        if(gameState.at(gameState.BOARD_SIZE-1, gameState.BOARD_SIZE-1, 0) == player 
        || gameState.at(gameState.BOARD_SIZE-1, gameState.BOARD_SIZE-1, gameState.BOARD_SIZE-1) == player) {
            reward += 2;
        }

        if(gameState.at(0, 0, 0) == player || gameState.at(gameState.BOARD_SIZE-1, 0, 0) == player) {
            reward += 2;
        }
        if(gameState.at(0, 0, gameState.BOARD_SIZE-1) == player 
        || gameState.at(gameState.BOARD_SIZE-1, 0, gameState.BOARD_SIZE-1) == player) {
            reward += 2;
        }
        return reward;
    }
}
