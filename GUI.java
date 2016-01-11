import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;

class NextGameInfo {
	public static String gameBoardName = "game_boards/Keren.txt";
	public static int playerOneMode = 2;
	public static int playerTwoMode = 2;
	public static int playerOneDepth = 3;
	public static int playerTwoDepth = 3;
  public static boolean m1Blitz = true;
  public static boolean battle = false;
  public static boolean duel = false;
  public static double attrition = 1.0;
}
class PlayerMenu {
	private JMenu playerMenu;
	private JMenu playerDepthMenu;
	private JMenuItem alphaBeta;
  	private JMenuItem miniMax;
  	private JMenuItem selfPlay;
  	private JMenuItem[] depths;
  	public PlayerMenu(String playerName) {
  		final String pN = playerName;
  		playerMenu = new JMenu("Player " + playerName);
  		playerDepthMenu = new JMenu("Player " + playerName + " Depth");
  		alphaBeta = new JMenuItem(new AbstractAction("AlphaBeta") {
  			@Override
		    public void actionPerformed(ActionEvent e) {
		    	if(pN.equals("One")) {
		    		NextGameInfo.playerOneMode = 1;
		    	} else {
		    		NextGameInfo.playerTwoMode = 1;
		    	}
		   	}
		});
		miniMax = new JMenuItem(new AbstractAction("MiniMax") {
  			@Override
		    public void actionPerformed(ActionEvent e) {
		    	if(pN.equals("One")) {
		    		NextGameInfo.playerOneMode = 2;
		    	} else {
		    		NextGameInfo.playerTwoMode = 2;
		    	}
		   	}
		});
		selfPlay = new JMenuItem(new AbstractAction("SelfPlay") {
  			@Override
		    public void actionPerformed(ActionEvent e) {
		    	if(pN.equals("One")) {
		    		NextGameInfo.playerOneMode = 3;
		    	} else {
		    		NextGameInfo.playerTwoMode = 3;
		    	}
		   	}
		});
		depths = new JMenuItem[10];
		for(int i = 0; i < depths.length; i++) {
			final int dep = i + 1;
			depths[i] = new JMenuItem(new AbstractAction("Depth: " + dep) {
	  			@Override
			    public void actionPerformed(ActionEvent e) {
			    	if(pN.equals("One")) {
			    		NextGameInfo.playerOneDepth = dep;
			    	} else {
			    		NextGameInfo.playerTwoDepth = dep;
			    	}
			   	}
			});
			playerDepthMenu.add(depths[i]);
		} 
		playerMenu.add(alphaBeta);
		playerMenu.add(miniMax);
		playerMenu.add(selfPlay);
  	}
  	public JMenu getPlayerMenu() {
  		return playerMenu;
  	}
  	public JMenu getDepthMenu() {
  		return playerDepthMenu;
  	}
}

public class GUI extends JFrame implements ActionListener, MouseListener{
	private JPanel boardPanel;
	private JPanel statusPanel;
	private Container container;
	private JLabel statusBar;
	private JLabel[][] boardLabels;

	//Menu
	private JMenuBar menuBar;
	private JMenu gameMenu;
	private JMenu boardMenu;
  private JMenu blitzMode;
  private JMenuItem newGame;
  private JMenuItem m1Blitz;
  private JMenuItem battle;
  private JMenuItem duel;
  private JMenuItem attrition;

  private GlobalState gs;

  public GUI(GlobalState gs) {
    this.gs = gs;

    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    container = getContentPane();
    boardPanel = new JPanel();
    menuBar = new JMenuBar();
    statusBar = new JLabel();
    boardLabels = new JLabel[gs.getBoard().length][gs.getBoard()[0].length];

    gameMenu = new JMenu("Game");
    boardMenu = new JMenu("Boards");
    blitzMode = new JMenu("Blitz_Mode");

    newGame = new JMenuItem("New Game");
    newGame.addActionListener(this);
    m1Blitz = new JMenuItem(new AbstractAction("m1Blitz mode") {
      @Override
      public void actionPerformed(ActionEvent e) {
        NextGameInfo.m1Blitz = true;
        NextGameInfo.battle = false;
        NextGameInfo.duel = false;
      }
    });
    battle = new JMenuItem(new AbstractAction("battle mode") {
      @Override
      public void actionPerformed(ActionEvent e) {
        NextGameInfo.m1Blitz = false;
        NextGameInfo.battle = true;
        NextGameInfo.duel = false;
      }
    });
    duel = new JMenuItem(new AbstractAction("duel mode") {
      @Override
      public void actionPerformed(ActionEvent e) {
        NextGameInfo.m1Blitz = false;
        NextGameInfo.battle = false;
        NextGameInfo.duel = true;
      }
    });

    blitzMode.add(m1Blitz);
    blitzMode.add(battle);
    blitzMode.add(duel);

    this.setJMenuBar(menuBar);
    PlayerMenu pOne = new PlayerMenu("One");
    PlayerMenu pTwo = new PlayerMenu("Two");
    gameMenu.add(newGame);
    menuBar.add(gameMenu);
    menuBar.add(boardMenu);
    menuBar.add(blitzMode);
    menuBar.add(pOne.getPlayerMenu());
    menuBar.add(pTwo.getPlayerMenu());
    menuBar.add(pOne.getDepthMenu());
    menuBar.add(pTwo.getDepthMenu());

    File folder = new File("game_boards");
    File[] listOfFiles = folder.listFiles();

    for (int i = 0; i < listOfFiles.length; i++) {
        if(listOfFiles[i].isFile()) {
          final String pathName = "game_boards/" + listOfFiles[i].getName();
          JMenuItem bd = new JMenuItem(new AbstractAction(pathName) {
          @Override
          public void actionPerformed(ActionEvent e) {
            NextGameInfo.gameBoardName = pathName;
          }
      });
          boardMenu.add(bd);
        } 
    }

    addPixel();

    container.setLayout(new BorderLayout(0,0));
    container.add(boardPanel, BorderLayout.CENTER);
    container.add(statusBar, BorderLayout.SOUTH);
    double width = Toolkit.getDefaultToolkit().getScreenSize().width;
    double height = Toolkit.getDefaultToolkit().getScreenSize().height;
    this.setPreferredSize(new Dimension((int)width,(int)height-20));
    updateStatusBar();
    pack();
  }
	private String getModeStr(Player pl) {
		int i = pl.getMode();
		if(i == 1) {
			return "AlphaBeta";
		} else if(i == 2 ) {
			return "MiniMax";
		} else {
			return "SelfPlay";
		}
	}
  	private void updateStatusBar() {
  		Player one = gs.getPlayerOne();
		Player two = gs.getPlayerTwo();
  		String statusStr = "Board: " + NextGameInfo.gameBoardName + "                    ";
  		statusStr += "Blue Scores: "+ one.getPlayerScore() + "                    " + "Green Scores:" + two.getPlayerScore() + "                    ";
    	statusStr += "Player one Depth: " + one.getDepth() + "                    " + "Player two Depth: " + two.getDepth() + "                    ";
    	statusStr += getModeStr(one) + " vs " + getModeStr(two) + "                    ";
    	statusStr += "Next Move: " + (gs.getPTaking() == gs.getPlayerOne()? "Player One" : "Player Two");
		statusBar.setText(statusStr);
  	}

  	private void cleanPixel() {
  		boardPanel.removeAll();
  	}
  	private void addPixel() {
  		boardPanel.setLayout(new GridLayout(gs.getBoard().length, gs.getBoard()[0].length));
  		boardLabels = new JLabel[gs.getBoard().length][gs.getBoard()[0].length];
  		for(int i = 0; i < gs.getBoard().length; i++) {
			for(int j = 0; j < gs.getBoard()[0].length; j++) {
				boardLabels[i][j] = new JLabel("" + gs.getBoard()[i][j], SwingConstants.CENTER);
				boardLabels[i][j].setSize(24, 24);
				boardLabels[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
				boardLabels[i][j].setOpaque(true);
				boardLabels[i][j].addMouseListener(this);
				boardLabels[i][j].setBackground(Color.WHITE);
				boardPanel.add(boardLabels[i][j]);
			}
		}
  	}
	
	public void mouseEntered(MouseEvent e) {}   
  	public void mouseExited(MouseEvent e) {}
  	public void mousePressed(MouseEvent e) {}
  	public void mouseReleased(MouseEvent e) {}
  	public void mouseClicked(MouseEvent e) {
  		for(int i = 0; i < gs.getBoard().length; i++) {
  			for(int j = 0; j < gs.getBoard()[0].length; j++) {
  				if(e.getSource().equals(boardLabels[i][j])) {
  					step(i, j);
  				}
  			}
  		}
  	}
  	public void actionPerformed(ActionEvent e) {
  		if(e.getSource().equals(newGame)) {
  			initNewGame();
  		}
  	}
  	private void initNewGame() {
  		this.gs = new GlobalState(new GameBoard(NextGameInfo.gameBoardName));
  		
		gs.getPlayerOne().setDepth(NextGameInfo.playerOneDepth);
		gs.getPlayerTwo().setDepth(NextGameInfo.playerTwoDepth);
		gs.getPlayerOne().setMode(NextGameInfo.playerOneMode);
		gs.getPlayerTwo().setMode(NextGameInfo.playerTwoMode);
    gs.m1Blitz = NextGameInfo.m1Blitz;
    gs.battle = NextGameInfo.battle;
    gs.duel = NextGameInfo.duel;
		cleanPixel();
		addPixel();
		pack();
		updateStatusBar();
  	}
  	private void setColor(int idx, Color cl) {
  		boardLabels[gs.getGbObj().idxToX(idx)][gs.getGbObj().idxToY(idx)].setBackground(cl);
  	}
  	private void step(int x, int y) {
  		List<Integer> avaMoves = gs.getAvailableMoves();
  		Player pTaking = gs.getPTaking();
  		if(avaMoves.size() == 0) {
  			int pOneScore = gs.getPlayerOne().getPlayerScore();
  			int pTwoScore = gs.getPlayerTwo().getPlayerScore();
  			String finalInfo = "" + pOneScore + " vs " + pTwoScore + "!!\n";
  			if(pOneScore == pTwoScore) {
  				finalInfo += "It's a Tie Game!!";
  			} else if(pOneScore > pTwoScore) {
  				finalInfo += "Winner is Player One!!";
  			} else {
  				finalInfo += "Winner is Player Two!!";
  			}
        System.out.println("Player One expanded nodes total: " + gs.getPlayerOne().expandedNodes + " Average: " + (int)(gs.getPlayerOne().expandedNodes / gs.getPlayerOne().numOfMoves));
        System.out.println("Player Two expanded nodes total: " + gs.getPlayerTwo().expandedNodes + " Average: " + (int)(gs.getPlayerTwo().expandedNodes / gs.getPlayerTwo().numOfMoves));
        System.out.println("Player One average time consumed is: " + (gs.getPlayerOne().totalTime / gs.getPlayerOne().numOfMoves));
        System.out.println("Player Two average time consumed is: " + (gs.getPlayerTwo().totalTime / gs.getPlayerTwo().numOfMoves));
  			JOptionPane.showMessageDialog(null, finalInfo, "Conclution: ", JOptionPane.INFORMATION_MESSAGE);
  			return;
  		} else if(pTaking.isAlphaBeta() || pTaking.isMiniMax()) { //AI Move, for the simplicity of the code, may need to refactor sometime.
        long start_time = System.nanoTime();
  			int aiMoveIdx = pTaking.isMiniMax()? Minimax.miniMaxDecision(gs, gs.getPTaking().getDepth()) : AlphaBeta.alphaBetaDecision(gs, pTaking.getDepth());
  			System.out.println();
  			System.out.print((gs.getPTaking() == gs.getPlayerOne()? "Player One" : "Player Two ") + (pTaking.isBlitz(aiMoveIdx)? "Blitz " : "ParaDrop "));
  			System.out.print(gs.getGbObj().idxToX(aiMoveIdx) + " " + gs.getGbObj().idxToY(aiMoveIdx));
  			HashMap<Integer, Player> changes = pTaking.move(aiMoveIdx);
        long end_time = System.nanoTime();
        double difference = (end_time - start_time)/1e9;
        gs.getPTaking().totalTime += difference;
        pTaking.numOfMoves++;
        if((gs.getPlayerOne().numOfMoves + gs.getPlayerTwo().numOfMoves) % 2 == 0) {
          gs.attrition *= .9;
        }
  			Color cl = (pTaking == gs.getPlayerOne()? Color.BLUE : Color.GREEN);
  			System.out.print(" Taking ");
  			for(int idx : changes.keySet()) {
  				setColor(idx, cl);
  				System.out.print(" " + gs.getGbObj().idxToX(idx) + " " + gs.getGbObj().idxToY(idx) + ", ");
  			}
  			System.out.println();
        System.out.println(gs.getPlayerOne().expandedNodes);
        System.out.println(gs.getPlayerTwo().expandedNodes);
  			gs.swapPlayer();
  		} else { //Player move
  			System.out.println("Self Play move: " + x + " " + y);
  			//check whether the player is moving to the available position
  			int i = 0;
  			for(i = 0; i < avaMoves.size(); i++) {
  				int avaX = gs.getGbObj().idxToX(avaMoves.get(i));
  				int avaY = gs.getGbObj().idxToY(avaMoves.get(i));
  				if(avaX == x && avaY == y) {
  					break;
  				}
  			}
  			if(i == avaMoves.size()) {
  				return;
  			}
  			//Move after checking availability
  			HashMap<Integer, Player> changes = pTaking.move(gs.getGbObj().xyToIdx(x, y));
  			Color cl = (pTaking == gs.getPlayerOne()? Color.BLUE : Color.GREEN);
  			for(int idx : changes.keySet()) {
  				setColor(idx, cl);
  			}
  			gs.swapPlayer();
  		}
  		updateStatusBar();
  	}
}