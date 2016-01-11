import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

class MyComparator implements Comparator<Integer> {
	GlobalState gs;
	public MyComparator(GlobalState gs) {
		this.gs = gs;
	}
	@Override
	public int compare(Integer idxOne, Integer idxTwo) {
		int xOne = gs.getGbObj().idxToX(idxOne);
		int yOne = gs.getGbObj().idxToY(idxOne);
		int xTwo = gs.getGbObj().idxToX(idxTwo);
		int yTwo = gs.getGbObj().idxToY(idxTwo);
		int oneVal = gs.getBoard()[xOne][yOne];
		int twoVal = gs.getBoard()[xTwo][yTwo];
		if(oneVal == twoVal) {
			return 0;
		}
		return oneVal < twoVal? 1 : -1;
	}
}
public class AlphaBeta{
	//Return the action that will be Made
	public static int alphaBetaDecision(GlobalState gs, int depth) {
		gs.getPTaking().expandedNodes++;
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		List<Integer> availableMoves = gs.getAvailableMoves();
		double max = Double.NEGATIVE_INFINITY;
		int argMax = -1;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			double utility = minValue(gs, depth - 1, max, Double.POSITIVE_INFINITY);
			if(max <= utility) {
				max = utility;
				argMax = idx;
			}
			gs.swapPlayer();
			gs.recoverState(oriStates);
		}
		return argMax;
	}
	//Return the utility
	private static double maxValue(GlobalState gs, int depth, double alpha, double beta) {
		gs.getPTaking().expandedNodes++;
		if(depth == 0) {
			return gs.m1Blitz? (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore()) : (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore()) * 0.8 + gs.getPTaking().getState().size() * 0.2;
		}
		List<Integer> availableMoves = gs.getAvailableMoves();
		Collections.sort(availableMoves, new MyComparator(gs));
		if(availableMoves.size() == 0) {
			return gs.m1Blitz? (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore()) : (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore()) * 0.8 + gs.getPTaking().getState().size() * 0.2;
		}
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		double max = Double.NEGATIVE_INFINITY;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			max = Math.max(max, minValue(gs, depth - 1, max, beta));
			gs.swapPlayer();
			gs.recoverState(oriStates);
			if(max >= beta) {
				return Double.POSITIVE_INFINITY;
			}
		}
		return max;
	}
	//Return the utility
	private static double minValue(GlobalState gs, int depth, double alpha, double beta) {
		gs.getPWaiting().expandedNodes++;
		if(depth == 0) {
			return gs.m1Blitz? (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore()) : (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore()) * 0.8 + gs.getPWaiting().getState().size() * 0.2;
		}
		List<Integer> availableMoves = gs.getAvailableMoves();
		Collections.sort(availableMoves, new MyComparator(gs));
		if(availableMoves.size() == 0) {
			return gs.m1Blitz? (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore()) : (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore()) * 0.8 + gs.getPWaiting().getState().size() * 0.2;
		}
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		double min = Double.POSITIVE_INFINITY;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			min = Math.min(min, maxValue(gs, depth - 1, alpha, min));
			gs.swapPlayer();
			gs.recoverState(oriStates);
			if(min <= alpha) {
				return Double.NEGATIVE_INFINITY;
			}
		}
		return min;
	}
}