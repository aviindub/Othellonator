
package othellonator;

public class Game {
    
    private int turn, computerColor ;
    private Board currentBoard ;
    private boolean opponentPassed, computerPassed ;
    
    public Game (int color) {
    
        currentBoard = new Board(color) ;
        computerColor = color ;
        turn = -1 ;
        opponentPassed = false ;
        computerPassed = false ;
    }
    
    public Game () {
        //dummy constructor
        //prevents stupid null pointer error 
        //and makes the turn evaluator work properly
        //on the first iteration of the main loop
        turn = -3 ;
        computerColor = -4 ;
    }
    
    
    public void setBoard (Board newBoard) {        
        currentBoard = newBoard ;
    }
    
    public Board getBoard () {
        return currentBoard ;
    }
    
    public int getTurn () {
        return turn ;
    }
    
    public void changeTurn () {
        turn = turn * -1 ;
    }
    
    public void changeTurn (boolean passed) {
        turn = turn * -1 ;
        opponentPassed = true ;
    }
    
    public int getColor () {
        return computerColor ;
    }
    
    public String getColorString () {
        switch (computerColor){
            case 1: return "white" ;
            case -1: return "black" ;
            default: return null ;
        }
    }
   
    public boolean makeMove (int color, int position) {
        boolean legal = currentBoard.evaluateOpponentMove(color, position);
        if (legal) {
            changeTurn();
            opponentPassed = false ;
            return true ;
        }
        else return false ;
    }

    public int makeMove (long timeRemaining) {
        int myMove = currentBoard.makeComputerMove(timeRemaining);
        changeTurn();
        
        if (myMove == -1) { //no move this turn
            computerPassed = true ;
            //no move this turn AND EITHER opponent passed last turn OR opponent has no move next turn
            if (opponentPassed || !this.opponentHasMove()) myMove = -2 ; 
        }
        return myMove ;
    }

    public void printCurrentBoard () {
        currentBoard.printBoard() ;
    }
    
    public boolean didOpponentPass () {
        if (opponentPassed) return true ;
        else return false ;
    }
    
    public boolean isGameOver () { //for evaluating when opponent claims game over
//        if (computerPassed && !currentBoard.generateLegalMoves(computerColor * -1) ) 
//            return true ; //true if othellonator passed AND no moves for opponent
        if (!currentBoard.generateLegalMoves(computerColor * -1) &&
                !currentBoard.generateLegalMoves(computerColor))
            return true ; //true if no moves for othellonator AND no moves for opponent
//        else if (computerPassed && opponentPassed)
//            return true ; //true if both players have passed
        else return false ;
    }
    
    public void evaluateGameOver () {
        System.out.println("(C Othellonator claming game over )");
        int black = this.countBlackPieces();
        int white = this.countWhitePieces();

        if (black > white) {
            System.out.println("(C Black wins )");
        } else {
            System.out.println("(C White wins )");
        }

        System.out.println("(C Black:" + Integer.toString(black)
                + "  White:" + Integer.toString(white) + ")");
        System.out.println("(" + Integer.toString(black) + ")");
    }
    
    public int countBlackPieces () {
        return currentBoard.countBlackPieces() ;
    }
    
    public int countWhitePieces () {
        return currentBoard.countWhitePieces() ;
    }
    
    public boolean opponentHasMove () {
        return currentBoard.generateLegalMoves(computerColor * -1) ;
    }
}


