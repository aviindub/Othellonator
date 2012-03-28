/**
 *alpha-beta algorithm: 
 *based on pseudocode from wikipedia http://en.wikipedia.org/wiki/Alpha-beta_pruning
 *and berkeley.edu http://www.ocf.berkeley.edu/~yosenl/extras/alphabeta/alphabeta.html
 * 
 */
package othellonator;
import java.util.* ;
import java.util.concurrent.*;


public class Search {
    
    //debugging stuff
    public static int betaCuts = 0;
    public static int alphaCuts = 0;
    public static int masterAlphaUpdates = 0;
    public static int masterBetaUpdates = 0;
    public static int searchAlphaUpdates = 0;
    public static int searchBetaUpdates = 0;
    public static int alphaNoUpdate = 0;
    public static int betaNoUpdate = 0;
    
    
    public static Move randSearch (Board board) {       //ramdom search for testing purposes
        board.generateLegalMoves(board.getMyColor());
        LinkedList movesList = board.getLegalMovesList() ;
        int randomMoveNumber = (int) Math.floor(Math.random() * movesList.size()) ;
        return (Move) movesList.get(randomMoveNumber) ;
    }
    

    public static Move alphaBetaSearch(Board board, int depth, int color) {

        //sets up SearchThreads for each child node of board and returns the one with the highest value


        LinkedList<Move> movesList = board.getLegalMovesList();
        int size = movesList.size();
        System.out.println("(C moves list size = " + Integer.toString(size) + ")");
        Move[] moves = new Move[size];
        for (int i = 0; i < size; i++) { //turn moves list in to array (this should not be necessary)
            moves[i] = movesList.get(i).cloneMove();
        }
        ABConcurrencyMaster master = new ABConcurrencyMaster(); //keeps track of and syncs best alpha and beta vals across threads
        Collection<Callable<Board>> searches = new ArrayList<Callable<Board>>();
        for (int i = 0; i < size; i++) {
            //moves[i] = movesList.get(i) ; //throws nullpointerexception for some stupid reason
            board.applyMove(color, moves[i].cloneMove());
            Board threadBoard = new Board(board);
            searches.add(new SearchThread(threadBoard, master, depth - 1, i)); 
            board.revertMove();
            //System.out.println("(C added a board )") ;
        }

        int numThreads = size; //getCores() > numChildren ? numChildren : getCores(); //max threads = fewer of number of cores or number of moves 
        try {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<Board>> results = executor.invokeAll(searches); //results FROM THE FUTURE!!! ooowowowoooowooowowooo
            System.out.println("(C results list size = " + Integer.toString(results.size()) + ")");
//            int bestMoveID = 0;
//            float highestValue = Float.NEGATIVE_INFINITY;
//            for (int i = 0; i < size; i++) {
//                //figure out which returned board has highest val
//                Board boardResult = results.get(i).get();
//                float boardVal = boardResult.getHeuristicValue();
//                if (boardVal > highestValue) {
//                    bestMoveID = boardResult.getMoveID();
//                    highestValue = boardVal;
//                }
//                System.out.println("(C thread " + Integer.toString(i) + " returned value of " + Float.toString(boardVal) + " )");
//            }
            int bestMoveID = master.getBestThreadID();
            executor.shutdown(); //always reclaim resources
            return moves[bestMoveID] ;
        } 
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        } 
//        catch (ExecutionException e) {
//            System.out.println(e.getMessage());
//            return null;
//        }


    }
    
 //   (* Initial call *)
//alphabeta(origin board, search depth, 0, -infinity, +infinity, MaxPlayer color)
   
    private static float alphaBeta(Board board, int depth, int ply, float alpha, float beta, int color, 
            ABConcurrencyMaster master) {
        if (depth == 0) 
            return board.evaluate(color) ;
        else if (board.isTerminalNode(color)) { //isTerminalNode() also does generateLegalMoves()
            return board.evaluate(color) ;
        }
        if (color == board.getMyColor()) {  //maxi
            LinkedList movesList = board.getLegalMovesList() ;
            if (movesList.size()==0) {
                board.applyMove(color, new Move()) ;
                float score = alphaBeta(board, depth-1, ply + 1, alpha, beta, color * -1, master) ;
                board.revertMove();
                if (score > alpha) {
                    alpha = score ;
                }
                else if (alpha >= beta) {
                    //alphaCuts++ ;
                    return alpha ;
                }
                else if (ply == 1) { //synchronize with the master concurrency thingy
                    AlphaBetaObject abo;
                    abo = master.testAndSet(new AlphaBetaObject(alpha, Float.POSITIVE_INFINITY, board.getMoveID()));
                    alpha = abo.getAlpha();
                    //beta = abo.getBeta();
                }
            }
            for (int i = 0 ; i < movesList.size() ; i++) {
                board.applyMove(color, (Move) movesList.get(i)) ;
                float score = alphaBeta(board, depth-1, ply + 1, alpha, beta, color * -1, master) ;
                board.revertMove();
                if (score > alpha) {
                    alpha = score ;
                }
                else if (alpha >= beta) {
                    //alphaCuts++ ;
                    return alpha ;
                }
                else if (ply == 1) { //synchronize with the master concurrency thingy
                    AlphaBetaObject abo;
                    abo = master.testAndSet(new AlphaBetaObject(alpha, Float.POSITIVE_INFINITY, board.getMoveID()));
                    alpha = abo.getAlpha();
                    //beta = abo.getBeta();
                }
            }
            return alpha ;
        }
        else { //mini
            LinkedList movesList = board.getLegalMovesList() ;
            for (int i = 0 ; i < movesList.size() ; i++) {
                board.applyMove(color, (Move) movesList.get(i));
                float score = alphaBeta(board, depth - 1, ply + 1, alpha, beta, color * -1, master);
                board.revertMove();
                if (score < beta) {
                    beta = score;
                }
                else if (alpha >= beta) {
                    //betaCuts++;
                    return beta;
                }
            }
            return beta ;
        }
    }
    
    private static final class SearchThread implements Callable<Board> {
        //callable thread object, takes a board and runs alphabeta on it in its own thread
        //sets a heuristic value for the board and returns it so the ID can be used to pick the best move
        
        private Board board ;
        private ABConcurrencyMaster master;
        private final int depth ;
        
        
        public SearchThread (Board b, ABConcurrencyMaster m, int dep, int id) {
            board = b ;
            master = m ;
            depth = dep ;
            board.setMoveID(id);
        }
        
        @Override
        public Board call() throws Exception {
            float value = alphaBeta(this.board, depth, 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 
                    this.board.getMyColor() * -1, this.master) ;
            board.setHeuristicValue(value) ;
            return board ;
        }
    }
    
    private static final class ABConcurrencyMaster {
        //keeps track of best alpha and beta vals for all threads
        private float bestAlpha, bestBeta ;
        private int bestThreadID;
        
        public ABConcurrencyMaster () {
                bestAlpha = Float.NEGATIVE_INFINITY ;
                bestBeta = Float.POSITIVE_INFINITY ;
        }
        
        public synchronized AlphaBetaObject testAndSet (AlphaBetaObject ab) {
            //synchronized test and set. called by alphabeta()
            //to compare the master's best values to the searches and return whichever is better
            float alpha = this.bestAlpha ; 
            float beta = this.bestBeta ;
            float a = ab.getAlpha() ;
            float b = ab.getBeta() ;
            
            
            if (a > this.bestAlpha) {
                this.bestAlpha = a ;
                alpha = a ;
                masterAlphaUpdates++ ;
                this.bestThreadID = ab.getThreadID();
            } 
            else if (a == this.bestAlpha) alphaNoUpdate++;
            else searchAlphaUpdates++ ;
            if (b < this.bestBeta) {
                this.bestBeta = b ;
                beta = b ;
                masterBetaUpdates++;
            } 
            else if (b == this.bestBeta) betaNoUpdate++;
            else searchBetaUpdates++ ;
            
            AlphaBetaObject results = new AlphaBetaObject(alpha, beta, 0) ;
            return results ; 
        }
        public int getBestThreadID () {
            return this.bestThreadID;
        }
    }
    
    private static class AlphaBetaObject {
        //simple object to allow alpha and beta to be returned together
        private float alpha, beta;
        private int threadID;
        
        public AlphaBetaObject (float a, float b, int t) {
            alpha = a ;
            beta = b ;
            threadID = t;
        }
        public AlphaBetaObject () {
            alpha = Float.NEGATIVE_INFINITY ;
            beta = Float.POSITIVE_INFINITY ;
        }        

        public float getAlpha () {
            return this.alpha ;
        }
        
        public float getBeta () {
            return this.beta ;
        }
        public int getThreadID () {
            return this.threadID ;
        }
    }
}


