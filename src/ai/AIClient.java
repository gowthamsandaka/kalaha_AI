package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard) {
        int mvbyAI = deepening_Search(currentBoard);
        return mvbyAI;
    }
    /**
	 * The below funtion shows the implementation of the deepening_Search takes place here 
	 * which returns the Object which consists of score, best move. 
	 *
	 **/
    public int deepening_Search(GameState current_game) {
        long totalTime = System.currentTimeMillis() + 5000;// I've set the current time to the number of milliseconds that have passed.
        int opt_move = 0;
        int playerOne = current_game.getNextPlayer();
        int playerTwo = 0;
        int depthHigh = 1;
        int Moving_state = 0;
        int i = 1;
        switch (playerOne) {
            case 1:
                playerTwo = 2;
                break;
            default:
                playerTwo = 1;
                break;

        }
        while (i <= 6) {  // This loop returns the best move after iterating until the maximum execution time is reached.
            long now = System.currentTimeMillis();

            if (now >= totalTime) {
                break;
            } else if (current_game.gameEnded()) //If your pebble lands in a home but you don't have any more movements, return 0.
            {
                return 0;
            }
            while (System.currentTimeMillis() < totalTime && current_game.moveIsPossible(i)) //the depth of tree is continiously incremented. 
            {
                GameState gamestate = (GameState) current_game.clone();
                gamestate.makeMove(i);
                Moving_state = optimized_move(current_game.clone(), 1, true, false, depthHigh, totalTime, playerOne, playerTwo);
                if (Moving_state != -1) {
                    opt_move = Moving_state;
                }
                depthHigh++;
            }
            i++;
        }
        return opt_move;// after executing for the longest possible time, return the best optmv value.
    }
    /**
     *Used for calculating the difference between the highest and lowest score from the current Game of the Player 1 or 2.
     * 
     * 
     */
    public int calculating_board_score(GameState scoring, int highestnum, int lowestnum, int calHighest, int calMin) {
        calHighest = scoring.getScore(highestnum);
        calMin = scoring.getScore(lowestnum);
        calHighest = calHighest - calMin;
        return calHighest;
    }
    
    /**
	 * The below miniMaxAlgoritmImplementation takes place here 
	 * which returns the optimized_movee. 
	 *
	 **/
    public int optimized_move(GameState boarding_the_currentMove, int depthPresent, boolean Maximum_turn, boolean Minimum_turn, int depthHigh, long Time_limit, int First_player, int Second_player) {
        int alpha = -9999999;
        int beta = 9999999;
        int extracting_bestScore = -9999999;
        int extracting_badScore = 9999999;
        int optmv = 1;
        int rounds = 1;
        int score_of_minimum = 0;
        int score_of_maximum = 0;
        int MAXIMUM_Move = 0;
        int MINIMUM_Move = 0;
        int saving_bestmv = 0;


        while (rounds <= 6) {
            if (System.currentTimeMillis() >= Time_limit) //return -1 if 5 seconds have passed. 
            {
                return -1;
            }
            int maxDecision = (Maximum_turn && boarding_the_currentMove.moveIsPossible(rounds)) ? 1 : 0;
            switch (maxDecision) {
                case 1:
                    GameState currentGame_Board1 = boarding_the_currentMove.clone();
                    currentGame_Board1.makeMove(rounds);
                    if (currentGame_Board1.getNextPlayer() == First_player) {
                        saving_bestmv = optimized_move(currentGame_Board1.clone(), depthPresent, true, false, depthHigh, Time_limit, First_player, Second_player);
                        switch (saving_bestmv) {
                            case -1:
                                return -1;
                            default:
                                currentGame_Board1.makeMove(saving_bestmv);
                        }
                    }

                    if (depthPresent < depthHigh) {
                        score_of_minimum = optimized_move(currentGame_Board1.clone(), depthPresent + 1, false, true, depthHigh, Time_limit, First_player, Second_player);
                        switch (score_of_minimum) {
                            case -1:
                                return -1;
                            default:
                                currentGame_Board1.makeMove(score_of_minimum);
                        }
                    }
                    MAXIMUM_Move = calculating_board_score(currentGame_Board1, First_player, Second_player, MAXIMUM_Move, MINIMUM_Move);
                    int best_score = Math.max(extracting_bestScore, MAXIMUM_Move);
                    if (extracting_bestScore < best_score) {
                        extracting_bestScore = best_score;
                        optmv = rounds;
                    }
                    alpha = Math.max(alpha, extracting_bestScore);
                    if (alpha >= beta) {
                        break;
                    }
                case 0:
                    int mindecision = (Minimum_turn && boarding_the_currentMove.moveIsPossible(rounds)) ? 1 : 0;
                    switch (mindecision) {
                        case 1:
                            GameState currentGame_Board2 = boarding_the_currentMove.clone();
                            currentGame_Board2.makeMove(rounds);
                            if (currentGame_Board2.getNextPlayer() == Second_player) {
                                saving_bestmv = optimized_move(currentGame_Board2.clone(), depthPresent, false, true, depthHigh, Time_limit, First_player, Second_player);
                                switch (saving_bestmv) {
                                    case -1:
                                        return -1;
                                    default:
                                        currentGame_Board2.makeMove(saving_bestmv);
                                }
                                MINIMUM_Move = saving_bestmv;
                                if (MINIMUM_Move <= extracting_badScore) {
                                    extracting_badScore = MINIMUM_Move;
                                }
                            }
                            MAXIMUM_Move = calculating_board_score(currentGame_Board2, First_player, Second_player, MAXIMUM_Move, MINIMUM_Move);
                            if (depthPresent < depthHigh) {
                                score_of_maximum = optimized_move(currentGame_Board2.clone(), depthPresent + 1, true, false, depthHigh, Time_limit, First_player, Second_player);
                                switch (score_of_maximum) {
                                    case -1:
                                        return -1;
                                    default:
                                        currentGame_Board2.makeMove(score_of_maximum);
                                }
                                MAXIMUM_Move = calculating_board_score(currentGame_Board2, First_player, Second_player, MAXIMUM_Move, MINIMUM_Move);
                            }
                            int worst_score = Math.min(extracting_badScore, MAXIMUM_Move);
                            if (extracting_badScore > worst_score) {
                                extracting_badScore = worst_score;
                                optmv = rounds;
                            }
                            beta = Math.min(beta, extracting_badScore);
                            if (beta <= alpha) {
                                break;
                            }
                    }

            }
            rounds++;

        }
        return optmv;
    }
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
}