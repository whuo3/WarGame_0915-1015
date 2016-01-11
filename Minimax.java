import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Minimax {
	//Return the action that will be Made
	public static int miniMaxDecision(GlobalState gs, int depth) {
		gs.getPTaking().expandedNodes++;
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		List<Integer> availableMoves = gs.getAvailableMoves();
		double max = Double.NEGATIVE_INFINITY;
		int argMax = -1;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			double utility = minValue(gs, depth - 1);
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
	private static double maxValue(GlobalState gs, int depth) {
		gs.getPTaking().expandedNodes++;
		if(depth == 0) {
			return (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore());
		}
		List<Integer> availableMoves = gs.getAvailableMoves();
		if(availableMoves.size() == 0) {
			return (double)(gs.getPTaking().getPlayerScore() - gs.getPWaiting().getPlayerScore());
		}
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		double max = Double.NEGATIVE_INFINITY;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			max = Math.max(max, minValue(gs, depth - 1));
			gs.swapPlayer();
			gs.recoverState(oriStates);
		}
		return max;
	}
	//Return the utility
	private static double minValue(GlobalState gs, int depth) {
		gs.getPWaiting().expandedNodes++;
		if(depth == 0) {
			return (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore());
		}
		List<Integer> availableMoves = gs.getAvailableMoves();
		if(availableMoves.size() == 0) {
			return (double)(gs.getPWaiting().getPlayerScore() - gs.getPTaking().getPlayerScore());
		}
		Player pTaking = gs.getPTaking();
		Player pWaiting = gs.getPWaiting();
		double min = Double.POSITIVE_INFINITY;
		for(int idx : availableMoves) {
			HashMap<Integer, Player> oriStates = pTaking.move(idx);
			gs.swapPlayer();
			min = Math.min(min, maxValue(gs, depth - 1));
			gs.swapPlayer();
			gs.recoverState(oriStates);
		}
		return min;
	}
}