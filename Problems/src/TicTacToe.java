import java.util.Scanner;

// ---------- ENUMS & INTERFACES ----------
enum GameState {
    IN_PROGRESS,
    WIN,
    DRAW
}

interface WinChecker {
    boolean checkWin(Board board, int i, int j, Player player);
}

// ---------- IMPLEMENTATION OF STRATEGY ----------
class DefaultWinChecker implements WinChecker {
    @Override
    public boolean checkWin(Board board, int i, int j, Player player) {
        int idx = player.idx;
        return (
                board.rows[i][idx] == board.n ||
                        board.cols[j][idx] == board.n ||
                        board.diaLeft[idx] == board.n ||
                        board.diaRight[idx] == board.n
        );
    }
}

// ---------- BOARD CLASS ----------
class Board {
    int n;
    Character[][] board;
    int[][] rows;
    int[][] cols;
    int[] diaLeft;
    int[] diaRight;
    int moveCount = 0;

    public Board(int n) {
        this.n = n;
        this.board = new Character[n][n];
        this.rows = new int[n][2];
        this.cols = new int[n][2];
        this.diaLeft = new int[2];
        this.diaRight = new int[2];
    }

    public boolean updateBoard(int i, int j, Player player) {
        if (board[i][j] != null) return false; // already filled
        board[i][j] = player.symbol.charAt(0);
        rows[i][player.idx]++;
        cols[j][player.idx]++;
        if (i == j) diaLeft[player.idx]++;
        if (i + j == n - 1) diaRight[player.idx]++;
        moveCount++;
        return true;
    }

    public boolean isTie() {
        return moveCount == n * n;
    }

    public void visualize() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print((board[i][j] == null ? "-" : board[i][j]) + " ");
            }
            System.out.println();
        }
    }
}

// ---------- PLAYER CLASS ----------
class Player {
    String name;
    String symbol;
    Player opponent;
    int idx;

    public Player(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
        this.idx = symbol.equals("O") ? 0 : 1;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}

// ---------- PLAYER FACTORY (FACTORY PATTERN) ----------
class PlayerFactory {
    public static Player createPlayer(String name, String symbol) {
        return new Player(name, symbol);
    }
}

// ---------- GAME CLASS ----------
class Game {
    Board board;
    Player currentPlayer;
    GameState state;
    WinChecker winChecker;

    public Game(int n, WinChecker winChecker) {
        this.board = new Board(n);
        this.state = GameState.IN_PROGRESS;
        this.winChecker = winChecker;
    }

    public void setPlayers(Player player1, Player player2) {
        player1.setOpponent(player2);
        player2.setOpponent(player1);
        currentPlayer = player1;
    }

    public void makeMove(int i, int j) {
        if (state != GameState.IN_PROGRESS) {
            System.out.println("Game is already over. Please reset.");
            return;
        }

        if (board.updateBoard(i, j, currentPlayer)) {
            board.visualize();

            if (winChecker.checkWin(board, i, j, currentPlayer)) {
                state = GameState.WIN;
                System.out.println(currentPlayer.name + " WON the game!");
                return;
            }

            if (board.isTie()) {
                state = GameState.DRAW;
                System.out.println("It's a DRAW!");
                return;
            }

            currentPlayer = currentPlayer.opponent;
            System.out.println(currentPlayer.name + "'s turn next");
        } else {
            System.out.println("Invalid move, try again.");
        }
    }

    public void reset() {
        this.board = new Board(board.n);
        this.state = GameState.IN_PROGRESS;
    }
}

// ---------- DRIVER CLASS ----------
public class TicTacToe {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Create game with 3x3 board and default win checker (Strategy)
        Game game = new Game(3, new DefaultWinChecker());

        // Use Factory to create players
        Player player1 = PlayerFactory.createPlayer("Devansh", "O");
        Player player2 = PlayerFactory.createPlayer("Lana Rhoades", "X");

        game.setPlayers(player1, player2);

        while (game.state == GameState.IN_PROGRESS) {
            int i = sc.nextInt();
            int j = sc.nextInt();
            game.makeMove(i, j);
        }

        System.out.println("Game Over!");
    }
}
