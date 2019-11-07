/**
 * Class to help with alpha beta 
 * GameState tells us which state we are in
 */
public class MinMaxState {

    private GameState gameState; 
    private int v; 

    public MinMaxState(GameState gameState, int v) {
        this.gameState = gameState; 
        this.v = v; 
    }
    
    public GameState getState() {
        return gameState;
    }

    public int getV() {
        return v; 
    }

    public void setState(GameState gameState) {
        this.gameState = gameState; 
    }

    public void setV(int v) {
        this.v = v; 
    }
}