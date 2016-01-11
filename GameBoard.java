import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class GameBoard{
	private int[][] board;
	public GameBoard(String fileName) {
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int i = 0;
			List<List<Integer>> helper = new ArrayList<List<Integer>>();
			while((line = bufferedReader.readLine()) != null) {
				String[] t = line.split("\t");
				helper.add(new ArrayList<Integer>());
				for(int j = 0; j < t.length; j++) {
					helper.get(i).add(Integer.parseInt(t[j]));
				}
				i++;
            }
            board = new int[helper.size()][helper.get(0).size()];
			for(i = 0; i < helper.size(); i++) {
				for(int j = 0; j < helper.get(i).size(); j++) {
					board[i][j] = helper.get(i).get(j);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public int xyToIdx(int x, int y) {
		return x * board.length + y;
	}
	public int idxToX(int idx) {
		return idx / board.length;
	}
	public int idxToY(int idx) {
		return idx % board.length;
	}
	public int[][] getBoard() {
		return board;
	}
}