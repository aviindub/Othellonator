
package othellonator;
import java.util.* ;

/**
 *
 * @author Avi
 */
public class Move {
    
    private int movePosition ; //the position of the move
    private Stack<Integer> flipStack = new Stack<Integer>() ; //legal flips for this move
    
    public Move (int position, Stack<Integer> flips) {
        movePosition = position ; 
        flipStack = flips ;
    }
    
    public Move (Move move) {
        movePosition = move.movePosition ;
        flipStack = move.getFlips() ;
    }
    
    public Move () {
        movePosition = -1;
    }
   
    public int getPosition () {
        return movePosition ;
    }
    
    public int popFlip () {
        int i = flipStack.pop() ;
        return i ;
    }
    
    public void pushFlip (int flip) {
        flipStack.push(flip);
    }
    
    public void setFlips (Stack<Integer> newList) {
        flipStack = newList ;
    }
    
    public Stack<Integer> getFlips () {
        Stack<Integer> stack = new Stack<Integer>();
        stack = (Stack<Integer>) flipStack.clone() ;
        return stack ;
    }
    
    public int getFlipStackSize () {
        return flipStack.size();
    }
    
    public Move cloneMove () {
        return new Move(this) ;
    }
           
}
