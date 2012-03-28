/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package othellonator;
import java.io.* ;


/**
 *
 * @author Avi
 */
public class Othellonator {
    
    private static Game currentGame ;
    private static boolean gameOver = false ;
    private static long timeAllowed = 1800000 ; //30 mins * 60 secs * 1000 milisecs
    private static long timeElapsed = 0 ;
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        //dummy constructor to make the first iteration of the main loop kosher
        currentGame = new Game() ;  
        
        
        if (args.length > 0) {
            try {
                timeAllowed = Long.parseLong(args[0]) * 1000 ;
            } catch (NumberFormatException e) {
                System.out.println("(C Invalid input for time allowed )");
            }
        }
        
        do {

            try {
                //if opponents turn, wait for input and parse
                if (currentGame.getColor() != currentGame.getTurn()) { //opponent's turn
                    System.out.println("(C Othellonator : Please input a command )");
                    BufferedReader commandBuffer = new BufferedReader(new InputStreamReader(System.in));
                    String commandString = commandBuffer.readLine();
                    commandString = commandString.replace("(", "");
                    commandString = commandString.replace(")", "");
                    commandString = commandString.trim();
                    String[] commandTokens = commandString.split(" ");

                    /* TO DO:
                     * better handling of (num) input ie opponent claims end game
                     * catch invalid pass (?maybe already done)
                     */


                    //Debugging stuff
//                for (String s: commandTokens)
//                    System.out.println(s) ;
//                System.out.println(Integer.toString(commandTokens.length)) ;
//                

                    switch (commandTokens.length) {
                        case 1: //commands with one token
                            //System.out.println("case 1") ;
                            switch (commandTokens[0].charAt(0)) {
                                case 'W': //white passes
                                    switch (currentGame.getColor()) {
                                        case 1:
                                            System.out.println("(C Invalid command, you are not white )");
                                            break;
                                        default:
                                            currentGame.changeTurn(false); //'false' calls overload of changeturn() so that pass flag is set
                                            System.out.println("(C Othellonator's opponent -white- passes )");
                                    }
                                    break;
                                case 'B': //black passes
                                    switch (currentGame.getColor()) {
                                        case -1:
                                            System.out.println("(C Invalid command, you are not black )");
                                            break;
                                        default:
                                            currentGame.changeTurn(false); //'false' calls overload of changeturn() so that pass flag is set
                                            System.out.println("(C Othellonator's opponent -black- passes )");
                                    }
                                    break;
                                    case 'E':
                                        System.exit(0);
                                        break;
                                default: //opponent claiming end game
                                    //numeric = opponent claiming end game--evaluate.  otherwise error.
                                    if (currentGame.isGameOver()) {
                                        gameOver = true;
                                        String blackPieces = Integer.toString(currentGame.countBlackPieces());
                                        System.out.println("(C Othellonator agrees. The game is over with "
                                                + blackPieces + " black pieces. )");
                                    }
                                    else System.out.println("(C Othellonator disagrees. Game is not over. Or possibly invalid input. )") ;
                                    break;

                            }
                            break;
                        case 2: //commands with 2 tokens
                            //System.out.println("case 2") ;
                            switch (commandTokens[0].charAt(0)) {
                                case 'I': //initialization commands
                                    switch (commandTokens[1].charAt(0)) {
                                        //init as black
                                        case 'B':
                                            currentGame = new Game(-1);
                                            gameOver = false;
                                            System.out.println("(C initialized as black )");
                                            System.out.println("(R B)");
                                            break;
                                        //init as white
                                        case 'W':
                                            currentGame = new Game(1);
                                            gameOver = false;
                                            System.out.println("(C initialized as white )");
                                            System.out.println("(R W)");
                                            break;
                                        default: //error
                                            System.out.println("(C Invalid Command )");
                                            break;
                                    }
                                    break;
                                default: //error
                                    System.out.println("(C Invalid Command )");
                                    break;
                            }
                            break;
                        case 3: //commands with 3 tokens (should only be moves)
                            try {
                                int position,
                                        color;
                                switch (commandTokens[0].charAt(0)) {
                                    //parse color that is making move
                                    case 'B':
                                        color = -1;
                                        break;
                                    case 'W':
                                        color = 1;
                                        break;
                                    default:
                                        color = -2;
                                        break;
                                }
                                if (color == currentGame.getColor() || color == -2) { 
                                    //error, opponent making move for wrong color or other bad input
                                    System.out.println("(C Error: that is not your color )");
                                    break;
                                }
                                position = convertFromCoordinates(commandTokens[1].charAt(0),
                                        Integer.parseInt(commandTokens[2]));
                                boolean legal = currentGame.makeMove(color, position);
                                if (!legal) {
                                    System.out.println("(C Not a legal move. )");
                                }
                                break;
                            }
                            catch (NullPointerException e) {
                                System.out.println("(C Not a legal move. )");
                            }
                            catch (StringIndexOutOfBoundsException e) {
                                System.out.println("(C Not a legal move. )");
                            }
                            
                        default: //error
                            System.out.println("(C Invalid Command )");
                            break;
                    }
                } else { //othellonator's turn
                    long startTime = System.currentTimeMillis(); //timestamp start of turn
                    //search for and make a move
                    int myMoveInt = currentGame.makeMove(timeAllowed - timeElapsed);
                    String myMove = convertToCoordinates(myMoveInt);
                    String myColor = currentGame.getColorString();
                    String myColorShort = myColor.substring(0, 1).toUpperCase();
                    switch (myMoveInt) {
                        case -1: //no legal moves for othellonator
                            System.out.println("(C Othellonator -" + myColor + "- passes. )");
                            System.out.println("(" + myColorShort + ")");
                            break;
                        case -2: //game is over
                            currentGame.evaluateGameOver() ;
                            gameOver = true;
                            break;
                        default: //print move
                            System.out.println("(C Othellonator -" + myColor + "- moves to " + myMove + ")");
                            System.out.println("(" + myColorShort + " " + myMove + ")");
                    }
                    //increment timeElapsed and check if time remaining
                    long lapTime = System.currentTimeMillis() - startTime;
                    System.out.println("(C lap time: " + Long.toString(lapTime/1000) + " seconds )");
                    timeElapsed += lapTime ;
                    if (timeElapsed >= timeAllowed) {
                        //gameOver = true;
                        //System.out.println("(C Othellonator is out of time and forfiets )");
                        String overTime = Long.toString((timeElapsed - timeAllowed)/1000) ;
                        System.out.println("(C Othellonator is overtime by " + overTime + " seconds )");
                    } else {
                        int timeRemainingSecs = (int) (timeAllowed - timeElapsed) / 1000;
                        System.out.println("(C Othellonator has " + Long.toString(timeRemainingSecs) + " seconds remaining )");
                    }
                }
                
                currentGame.printCurrentBoard();
                
                //debugging stuff
//                System.out.println("(C " + Integer.toString(Board.pushCount) + " pushes )") ;
//                System.out.println("(C " + Integer.toString(Board.popCount) + " pops )") ;
//                System.out.println("(C " + Integer.toString(Search.alphaCuts) + " alpha cuts )") ;
//                System.out.println("(C " + Integer.toString(Search.betaCuts) + " beta cuts )") ;
//                System.out.println("(C " + Integer.toString(Search.masterAlphaUpdates) + " master alpha updates )") ;
//                System.out.println("(C " + Integer.toString(Search.masterBetaUpdates) + " master beta updates )") ;
//                System.out.println("(C " + Integer.toString(Search.searchAlphaUpdates) + " search alpha updates )") ;
//                System.out.println("(C " + Integer.toString(Search.searchBetaUpdates) + " search beta updates )") ;
//                System.out.println("(C " + Integer.toString(Search.alphaNoUpdate) + " comparisons resulted in no alpha update )") ;
//                System.out.println("(C " + Integer.toString(Search.betaNoUpdate) + " comparisons resulted in no beta update )") ;
                
                if (currentGame.isGameOver()) {
                    gameOver = true ;
                    currentGame.evaluateGameOver() ;
                }
            } 
            
            catch (NullPointerException e) {
                System.out.println("(C Game not initialized. Please try again. )");
            }
            catch (StringIndexOutOfBoundsException e) {
                System.out.println("(C Blank command is not a command. Please try again. )") ;
            }
            catch (NumberFormatException e) {
                System.out.println("(C Invalid input. Please try again. )") ;
            }

            

        } while (!gameOver);
        
    }
    
    public static int convertFromCoordinates (char alpha, int num) {
        //converts alphanumeric coordinate pair to single int board position 0-63       
        switch (alpha) {
            case 'a' : return num + 10 ; 
            case 'b' : return num + 20 ; 
            case 'c' : return num + 30 ; 
            case 'd' : return num + 40 ; 
            case 'e' : return num + 50 ;
            case 'f' : return num + 60 ;
            case 'g' : return num + 70 ;
            case 'h' : return num + 80 ;
            default : return -1 ;
        }
    }
    
    public static String convertToCoordinates (int position) {
        //converts int 0-63 board position back to alphanumeric coordinate pair
        int num, convert ;
        char alpha = 'x';
        String coordinates ;
        
        num = (int) position % 10 ;
        convert = position - num ;
        
        switch (convert) {
            case 10 : alpha = 'a' ; break;
            case 20 : alpha = 'b' ; break;         
            case 30 : alpha = 'c' ; break;
            case 40 : alpha = 'd' ; break;                 
            case 50 : alpha = 'e' ; break;                     
            case 60 : alpha = 'f' ; break;                         
            case 70 : alpha = 'g' ; break;                             
            case 80 : alpha = 'h' ; break;
        }
        coordinates = Character.toString(alpha) + " " + Integer.toString(num) ;
        return  coordinates;
    }
    

}
