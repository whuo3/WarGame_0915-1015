import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class Player {
	HashSet<Integer> playerState; //Record the index of the location the player occupy.
	int[][] board;
	GlobalState gs;
	int depth;
	int mode; //Either alpha_beta --> 1 or miniMax --> 2 or selfPlay --> 3
	int expandedNodes;
	int numOfMoves;
	//Time data recorder.
	double totalTime = 0;
	public Player(GlobalState gs) {
		playerState = new HashSet<Integer>();
		this.gs = gs;
		this.depth = 3; //default value of depth is 3
		this.mode = 2;  //default mode is self play
		this.expandedNodes = 0;
		this.numOfMoves = 0;
		this.board = gs.getBoard(); 
	}
	public HashSet<Integer> getState() {
		return playerState;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public int getMode() {
		return mode;
	}
	public int getPlayerScore() {
		int sum = 0;
		for(int i : playerState) {
			int x = gs.getGbObj().idxToX(i);
			int y = gs.getGbObj().idxToY(i);
			sum += board[x][y];
		}
		return sum;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public boolean isAlphaBeta() {
		return mode == 1;
	}
	public boolean isMiniMax() {
		return mode == 2;
	}
	/******************** Action **************************/
	//For each action, it will return history recorder
	/*HashMap: location indexes that will be changed after execution ---> Player who has this location idx before execution
																		  Null -> not owned by anyOne
																		  not Null -> either by player one or player two
	*/	  
	public HashMap<Integer, Player> move(int idx) {				
		//Assume all the input indexes are correct
		if(isBlitz(idx) && gs.m1Blitz) {
			return blitz(idx);
		} else if(isBlitz(idx) && gs.battle) {
			return battle(idx);
		} else if(isBlitz(idx) && gs.duel) {
			return duel(idx);
		} else {
			return paraDrop(idx);
		}
	}
	public boolean isBlitz(int idx) {
		int x = gs.getGbObj().idxToX(idx);
		int y = gs.getGbObj().idxToY(idx);
		return (x - 1 >= 0 && playerState.contains(gs.getGbObj().xyToIdx(x - 1, y))) ||
			   (x + 1 < board.length && playerState.contains(gs.getGbObj().xyToIdx(x + 1, y))) ||
			   (y - 1 >= 0 && playerState.contains(gs.getGbObj().xyToIdx(x, y - 1))) ||
			   (y + 1 < board[0].length && playerState.contains(gs.getGbObj().xyToIdx(x, y + 1)));
	}
	private HashMap<Integer, Player> paraDrop(int idx) {         
		//Assume all the input indexes are correct
		HashMap<Integer, Player> oriStates = new HashMap<Integer, Player>();
		oriStates.put(idx, null);
		gs.getGlobal().put(idx, this);
		playerState.add(idx);
		return oriStates;
	}
	private HashMap<Integer, Player> blitz(int idx) {
		HashMap<Integer, Player> oriStates = new HashMap<Integer, Player>();
		int xTaking = gs.getGbObj().idxToX(idx);
		int yTaking = gs.getGbObj().idxToY(idx);
		Player pWaiting = gs.getPWaiting();
		HashSet<Integer> pWaitingState = pWaiting.getState();
		HashMap<Integer, Player> global = gs.getGlobal();
		//up
		if(xTaking - 1 >= 0 && pWaitingState.contains(idx - board.length)) {
			oriStates.put(idx - board.length, pWaiting);
		}
		//down
		if(xTaking + 1 < board.length && pWaitingState.contains(idx + board.length)) {
			oriStates.put(idx + board.length, pWaiting);
		}
		//left
		if(yTaking - 1 >=0 && pWaitingState.contains(idx - 1)) {
			oriStates.put(idx - 1, pWaiting);
		}
		//right
		if(yTaking + 1 < board[0].length && pWaitingState.contains(idx + 1)) {
			oriStates.put(idx + 1, pWaiting);
		}
		//update the states
		for(int pWaitingIdx : oriStates.keySet()) {
			this.playerState.add(pWaitingIdx);
			gs.getGlobal().put(pWaitingIdx, this);
			pWaitingState.remove(pWaitingIdx);
		}
		this.playerState.add(idx);
		gs.getGlobal().put(idx, this);
		oriStates.put(idx, null);
		return oriStates;
	}
	//Extended rules
	private double getUnitResources() {
		return ((double)getPlayerScore() / playerState.size()) * gs.attrition;
	}
	private boolean isNeighbor(int x, int y, int _x, int _y) {
		return (x == _x && (Math.abs(y - _y) == 1)) || (y == _y && (Math.abs(x - _x) == 1));
	}
	private int adjacentNum(Player pl, int x, int y) {
		int result = 0;
		if(x - 1 >= 0 && pl.getState().contains(gs.getGbObj().xyToIdx(x - 1, y))) {
			result++;
		}
		if(x + 1 < board.length && pl.getState().contains(gs.getGbObj().xyToIdx(x + 1, y))) {
			result++;
		}
		if(y - 1 >= 0 && pl.getState().contains(gs.getGbObj().xyToIdx(x, y - 1))) {
			result++;
		}
		if(y + 1 < board[0].length && pl.getState().contains(gs.getGbObj().xyToIdx(x, y + 1))) {
			result++;
		}
		return result;
	}
	private boolean canTake(int x, int y) {
		double takingScore = adjacentNum(this, x, y) * getUnitResources();
		double waitingScore = adjacentNum(gs.getPWaiting(), x, y) * gs.getPWaiting().getUnitResources();
		return takingScore > waitingScore;
	}
	private HashMap<Integer, Player> battle(int idx) {
		HashMap<Integer, Player> oriStates = new HashMap<Integer, Player>();
		this.playerState.add(idx);
		oriStates.put(idx, null);
		gs.getGlobal().put(idx, this);
		int xTaking = gs.getGbObj().idxToX(idx);
		int yTaking = gs.getGbObj().idxToY(idx);
		Player pWaiting = gs.getPWaiting();
		HashSet<Integer> pWaitingState = pWaiting.getState();
		//up
		if(xTaking - 1 >= 0 && pWaitingState.contains(idx - board.length) && canTake(xTaking - 1, yTaking)) {
			oriStates.put(idx - board.length, pWaiting);
		}
		//down
		if(xTaking + 1 < board.length && pWaitingState.contains(idx + board.length) && canTake(xTaking + 1, yTaking)) {
			oriStates.put(idx + board.length, pWaiting);
		}
		//left
		if(yTaking - 1 >= 0 && pWaitingState.contains(idx - 1) && canTake(xTaking, yTaking - 1)) {
			oriStates.put(idx - 1, pWaiting);
		}
		//right
		if(yTaking + 1 < board[0].length && pWaitingState.contains(idx + 1) && canTake(xTaking, yTaking + 1)) {
			oriStates.put(idx + 1, pWaiting);
		}
		for(int occupiedIdx : oriStates.keySet()) {
			this.playerState.add(occupiedIdx);
			gs.getGlobal().put(occupiedIdx, this);
			pWaitingState.remove(occupiedIdx);
		}
		return oriStates;
	}
	private HashMap<Integer, Player> duel(int idx) {
		HashMap<Integer, Player> oriStates = new HashMap<Integer, Player>();
		this.playerState.add(idx);
		oriStates.put(idx, null);
		gs.getGlobal().put(idx, this);
		int xTaking = gs.getGbObj().idxToX(idx);
		int yTaking = gs.getGbObj().idxToY(idx);
		Player pWaiting = gs.getPWaiting();
		HashSet<Integer> pWaitingState = pWaiting.getState();
		//up
		if(xTaking - 1 >= 0 && pWaitingState.contains(idx - board.length) && gs.getPTaking().getUnitResources() > gs.getPWaiting().getUnitResources()) {
			oriStates.put(idx - board.length, pWaiting);
		}
		//down
		if(xTaking + 1 < board.length && pWaitingState.contains(idx + board.length) && gs.getPTaking().getUnitResources() > gs.getPWaiting().getUnitResources()) {
			oriStates.put(idx + board.length, pWaiting);
		}
		//left
		if(yTaking - 1 >= 0 && pWaitingState.contains(idx - 1) && gs.getPTaking().getUnitResources() > gs.getPWaiting().getUnitResources()) {
			oriStates.put(idx - 1, pWaiting);
		}
		//right
		if(yTaking + 1 < board[0].length && pWaitingState.contains(idx + 1) && gs.getPTaking().getUnitResources() > gs.getPWaiting().getUnitResources()) {
			oriStates.put(idx + 1, pWaiting);
		}
		for(int occupiedIdx : oriStates.keySet()) {
			this.playerState.add(occupiedIdx);
			gs.getGlobal().put(occupiedIdx, this);
			pWaitingState.remove(occupiedIdx);
		}
		return oriStates;
	}
}


//*******************************************Global State***************************************
public class GlobalState {
	Player one;
	Player two;
	Player pTaking;  //Player who is going to take move
	Player pWaiting;  //Player who is waiting
	int[][] board;
	GameBoard gb;
	HashMap<Integer, Player> global; //Map location Idx to Player, if neigher player one nor two, then it's null

	//Fields for extended rules
	boolean m1Blitz = true;
	boolean battle = false;
	boolean duel = false;
	double attrition = 1.0;

	public GlobalState(GameBoard gb) {
		this.gb = gb;
		board = gb.getBoard();
		one = new Player(this);
		two = new Player(this);
		global = new HashMap<Integer, Player>();
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[0].length; j++) {
				global.put(gb.xyToIdx(i, j), null);
			}
		}
		pTaking = one;
		pWaiting = two;
	}
	public HashMap<Integer, Player> getGlobal() {
		return global;
	}
	public boolean isAvailable(int idx) {
		return !one.getState().contains(idx) && !two.getState().contains(idx);
	}
	public HashSet<Integer> getPTakingState() {
		return pTaking.getState();
	}
	public HashSet<Integer> getPWaitingState() {
		return pWaiting.getState();
	}
	public HashSet<Integer> getPlOneState() {
		return one.getState();
	}
	public HashSet<Integer> getPlTwoState() {
		return two.getState();
	}
	public Player getPTaking() {
		return pTaking;
	}
	public Player getPWaiting() {
		return pWaiting;
	}
	public Player getPlayerOne() {
		return one;
	}
	public Player getPlayerTwo() {
		return two;
	}
	public void swapPlayer() {
		Player t = pTaking;
		pTaking = pWaiting;
		pWaiting = t;
	}
	public int[][] getBoard() {
		return board;
	}
	public GameBoard getGbObj() {
		return gb;
	}
	public void recoverState(HashMap<Integer, Player> oriStates) {
		for(int idx : oriStates.keySet()) {
			Player pl = oriStates.get(idx);
			if(pl == null) {
				getPlOneState().remove(idx);
				getPlTwoState().remove(idx);
			} else if(pl == one) {
				getPlTwoState().remove(idx);
				getPlOneState().add(idx);
			} else {
				getPlOneState().remove(idx);
				getPlTwoState().add(idx);
			}
			global.put(idx, pl);
		}
	}
	public List<Integer> getAvailableMoves() {
		List<Integer> availableMoves = new ArrayList<Integer>();
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[0].length; j++) {
				if(isAvailable(gb.xyToIdx(i, j))) {
					availableMoves.add(gb.xyToIdx(i, j));
				}
			}
		}
		return availableMoves;
	}
}