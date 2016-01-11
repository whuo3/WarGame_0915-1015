public class WarGame{
	public static void main(String[] args) {
		GUI gui = new GUI(new GlobalState(new GameBoard("game_boards/Keren.txt")));
	}
}