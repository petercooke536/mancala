import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.Timer;

/**
 * Board for Mancala game
 * 
 * @author Jack Gilcrest
 * @date 09.12.2018
 */
public class Board {
	private ArrayList<Cup> cups = new ArrayList<Cup>(); // ArrayList storing Cup objects to simulate a board
	boolean playerTurn; // Tracking whether it is the Player or the AI Opponent's turn
	public boolean animationInProgress = false; // reflects whether an animation is running on screen

	// class variables stating board index ranges
	private final int PLAYER_FIRST_CUP = 0;
	private final int PLAYER_GOAL_CUP = 6;
	private final int AI_FIRST_CUP = 7;
	private final int AI_GOAL_CUP = 13;

	// class variables for action listener
	private int AL_stones_placed = 0;
	private int AL_stones_held = 0;
	private int AL_index = 0;

	// class variable for index of move passed by UI
	private int UI_move;
	
	private HashMap<Integer, Integer> getOpposite = new HashMap<Integer, Integer>();

	/**
	 * construct a new board object
	 */
	public Board() {
		initialize();
	}

	/**
	 * Retrieve an ArrayList<Cup> that reflects the current game state
	 * 
	 * @return the ArrayList<Cup> of cups in the game
	 */
	public ArrayList<Cup> getBoard() {
		return cups;
	}

	/**
	 * Run all initializing methods for starting a new game
	 */
	public void initialize() {
		initCups();
		generateOpposites();
		chooseFirst();
		if(!playerTurn) 
			doMove(AI.getMoveIndex(getBoard()));
		
	}

	private void generateOpposites() {
		getOpposite.put(0, 12);
		getOpposite.put(1, 11);
		getOpposite.put(2, 10);
		getOpposite.put(3, 9);
		getOpposite.put(4, 8);
		getOpposite.put(5, 7);
		getOpposite.put(12, 0);
		getOpposite.put(11, 1);
		getOpposite.put(10, 2);
		getOpposite.put(9, 3);
		getOpposite.put(8, 4);
		getOpposite.put(7, 5);
	}
	/**
	 * Randomly select whether the Player or AI Opponent moves first and set class
	 * variable 'playerTurn' accordingly
	 */
	private void chooseFirst() {
		Random r = new Random();
		playerTurn = r.nextBoolean();
	}

	/**
	 * Checks if the point is a valid spot to place
	 * 
	 * @param p Point of where player clicked
	 * @return The positions index, -1 if not found
	 */
	public int getBoardPosition(Point p) {
		for (int i = 0; i < cups.size(); i++)
			if (cups.get(i).getRectangle().contains(p))
				return i;
		return -1;
	}

	/**
	 * Update All Cups
	 */
	public void updateAllCupsInstantly(Graphics g) {
		for (int i = 0; i < cups.size(); i++)
			cups.get(i).drawStoneCount(g);
	}

	/**
	 * Draws the number of stones in each cup
	 * 
	 * @param g
	 */
	public void paint(Graphics g) {
		for (int i = 0; i < cups.size(); i++)
			cups.get(i).drawStoneCount(g);
	}

	/**
	 * Initialize all Cup objects used and add them to the ArrayList<Cup> cups class
	 * variable States the dimensions for each up individually.
	 */
	private void initCups() {
		// Player game cups
		cups.add(new GameCup(new Rectangle(240, 410, 105, 105)));
		cups.add(new GameCup(new Rectangle(360, 410, 105, 105)));
		cups.add(new GameCup(new Rectangle(485, 410, 105, 105)));
		cups.add(new GameCup(new Rectangle(610, 410, 105, 105)));
		cups.add(new GameCup(new Rectangle(735, 410, 105, 105)));
		cups.add(new GameCup(new Rectangle(855, 410, 105, 105)));
		// Player goal cup
		cups.add(new GoalCup(new Rectangle(980, 235, 105, 280)));
		
		// AI Opponent game cups
		cups.add(new GameCup(new Rectangle(855, 240, 105, 105)));
		cups.add(new GameCup(new Rectangle(735, 240, 105, 105)));
		cups.add(new GameCup(new Rectangle(610, 240, 105, 105)));
		cups.add(new GameCup(new Rectangle(485, 240, 105, 105)));
		cups.add(new GameCup(new Rectangle(360, 240, 105, 105)));
		cups.add(new GameCup(new Rectangle(240, 240, 105, 105)));
		// AI Opponent goal cup
		cups.add(new GoalCup(new Rectangle(115, 235, 102, 280)));
	}

	/**
	 * Passes a move from the UI into the board class
	 * 
	 * @param index the index of the move being passed from the UI
	 */
	public void giveMoveUI(int index) {
		UI_move = index;
	}
	
	/**
	 * Executes a single move and updates graphics accordingly
	 */
	public void doMove(int index) {
		if(index < 0)
			return;
		
		AL_stones_placed = 0;
		AL_index = index; // get the starting index of the move
		animationInProgress = true; // prevent further ui input while true
		AL_stones_held = ((GameCup) cups.get(AL_index % 14)).removeStones(); // remove and remember stones from chosen cup

		// Use timer to do animations
		Timer timer = new Timer(800, getActionListener());
		timer.start();
	}

	/**
	 * Create an ActionListener object to reflect a move in the UI
	 * 
	 * @return the ActionListener object
	 */
	private ActionListener getActionListener() {
		ActionListener a = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AL_stones_placed == AL_stones_held) {
					animationInProgress = false;
					System.out.println("Index Before turnEnd Call: "+ AL_index);
					
					((Timer) e.getSource()).stop();
					turnEnd(AL_index % 14);
				} else {
					AL_index++;
					
					if(opposingGoalCup(AL_index))
						AL_index++;
					
					cups.get(AL_index % 14).addStone();
					AL_stones_placed++;
					
					if((cups.get(AL_index % 14).getNumStones() == 1) 
							&& (AL_stones_placed == AL_stones_held) 
							&& cups.get(AL_index % 14) instanceof GameCup
							&& cups.get(getOpposite.get(AL_index % 14)).getNumStones() > 0) {
						if(playerTurn) {
								if(PLAYER_GOAL_CUP > AL_index && PLAYER_FIRST_CUP <= AL_index) {
									int sideStones = ((GameCup) cups.get(AL_index % 14)).removeStones();
									int oppSideStones = ((GameCup) cups.get(getOpposite.get(AL_index % 14))).removeStones();
									cups.get(6).addStone(sideStones + oppSideStones);
								}
						} else {
								if(AI_GOAL_CUP > AL_index && AI_FIRST_CUP <= AL_index) {
									int sideStones = ((GameCup) cups.get(AL_index % 14)).removeStones();
									int oppSideStones = ((GameCup) cups.get(getOpposite.get(AL_index % 14))).removeStones();
									cups.get(13).addStone(sideStones + oppSideStones);
								}
						}
					}
					
				}
				Main.window.repaintGamePanel();
	
			}
		};
		return a;
	}

	/**
	 * determines whether the last stone placed in a turn was in a cup that ends the
	 * turn (i.e. either goal cup (index 6 and 13) or in a cup with 0 stones
	 * 
	 * @param index the index to start at
	 * @throws InterruptedException
	 */
	private void turnEnd(int index) {
		checkEndConditions();
		if(playerTurn) {
			if(index == PLAYER_GOAL_CUP) {
				//Is waiting for input...
				return;
			} else {
				//End Turn
				playerTurn = false;
				int m_index = AI.getMoveIndex(getBoard());
				doMove(m_index);
				return;
			}	
		} else {
			if(index == AI_GOAL_CUP) {
				int m_index = AI.getMoveIndex(getBoard());
				doMove(m_index);
				return;
			} else {
				playerTurn = true;
				return;
			}
		}
		
	}
	
	/**
	 * Checks for end conditions.  WIN, TIE, OR LOSS
	 */
	private void checkEndConditions() {
		if(endCondition()) {
			if(tie()) {
				Main.window.showEndPanel(EndCondition.TIE);
			} else if(playerWins()) {
				Scores.increaseScore(true);
				Main.window.showEndPanel(EndCondition.WIN);
			} else {
				Scores.increaseScore(false);
				Main.window.showEndPanel(EndCondition.LOSE);
			}
		}
	}
	

	/**
	 * Refers to playerTurn boolean to determine if a cup is the opposing player's
	 * cup
	 * 
	 * @param index the index of the cup being evaluated
	 * @return true if the cup is the opposing player's goal cup, and false
	 *         otherwise
	 */
	private boolean opposingGoalCup(int index) {
		if (playerTurn)
			return index == AI_GOAL_CUP;
		else
			return index == PLAYER_GOAL_CUP;
	}

	/**
	 * Checks player cups to see if empty then computer cups to see if entry
	 * 
	 * @return true if either the player cups or computer cups are completely empty
	 */
	private boolean endCondition() {
		boolean playerCupsEmpty = true;
		for (int i = 0; i < 6; i++) {
			if (cups.get(i).getNumStones() != 0)
				playerCupsEmpty = false;
		}
		if (playerCupsEmpty)
			return true;
		else {
			for (int i = 7; i < 13; i++) {
				if (cups.get(i).getNumStones() != 0)
					return false;
			}
			return true;
		}
	}

	/**
	 * Determine if the game ends in a tie. Should not be used before endCondition
	 * returns true
	 * 
	 * @return true if the player and computer have equal scores at the end of the
	 *         game, and false otherwise
	 */
	private boolean tie() {
		return cups.get(6) == cups.get(13);
	}

	/**
	 * Determine if the game ends by the player winning.
	 * 
	 * @return true if the player wins, false if the computer wins.
	 */
	private boolean playerWins() {
		return cups.get(6).getNumStones() > cups.get(13).getNumStones();
	}

	/**
	 * Evaluate a given move to see if it conforms to the rules
	 * 
	 * @param move an integer giving the position to start moving from
	 * @return true if the move is valid, and false otherwise
	 */
	public boolean validateMove(int move) {
		// validate that the computer or player chose a cup that is on their side
		if (playerTurn) {
			// validate that the move index falls within the Player's game cup range
			if (move > PLAYER_GOAL_CUP || move < PLAYER_FIRST_CUP) {
				System.out.println("Move chosen was on the AI Opponent's side of the board. Invalid move for Player.");
				return false;
			}
		} else {
			// validate that the move index falls within the AI Opponent's game cup range
			if (move >= AI_GOAL_CUP || move < AI_FIRST_CUP) {
				System.out.println("Move chosen was on the Player's side of the board. Invalid move for AI Opponent.");
				return false;
			}
		}

		// validate that a bank was not chosen
		if (move == PLAYER_GOAL_CUP || move == AI_GOAL_CUP) {
			System.out.println("Cannot do move inside bank.");
			return false;
		}

		// validate that an empty cup was not chosen
		else if (cups.get(move).getNumStones() == 0) {
			System.out.println("No stones in cup.");
			return false;
		}
		
		// validate that the animation is not in progress
		else if (animationInProgress)
			return false;

		// all validations have been successfully passed
		else
			return true;

	}

}
