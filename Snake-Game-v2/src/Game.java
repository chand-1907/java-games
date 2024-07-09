import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.*;

public class Game {

    private final Node[][] GRID;
    private final LinkedList<Position> snake;
    private final Scanner scanner;
    private int foodCount;
    Thread inputThread;
    long gridPrintingInterval = 1000;
    char currentDirection = 'w';
    private final int gameSpeed = 7;

    public Game (int rows, int cols) {

        // row and column validation
        if (!Validation.InputValidator.rowColumnValidator(rows, cols))
            throw new InputMismatchException("Row range is 2 - 10 || Column range is 2 - 20");

        this.GRID = new Node[rows][cols];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                 this.GRID[i][j] = new Node();

        this.snake = new LinkedList<>();
        this.scanner = new Scanner(System.in);
    }

    private void changeTheDirection (int input) {
        Position position = snake.getLast();
        int flag = 0;

        switch ((char)(input)) {
            case 'w' : {
                flag = gridModifier (position.x - 1, position.y, position);
                currentDirection = 'w';
                break;
            }

            case 's' : {
                flag = gridModifier (position.x + 1, position.y, position);
                currentDirection = 's';
                break;
            }

            case 'a' : {
                flag = gridModifier (position.x, position.y - 1, position);
                currentDirection = 'a';
                break;
            }

            case 'd' : {
                flag = gridModifier (position.x, position.y + 1, position);
                currentDirection = 'd';
                break;
            }

            default : System.out.println("wrong input");
        }

        if (flag == -1) {
            System.out.println("game is over!\neither the snake goes the bound (or) bites it own tail");
            System.exit(0);
        }

        if (flag == 1) {
            System.out.println("Hurray, You win\n" + this);
            inputThread.interrupt();
            System.exit(0);
        }
    }

    public void start () {

        // init the game
        init();
        // init the input thread to take the input process
        inputThreadStartup();

        while (true) {

            // move the snake position
            changeTheDirection(currentDirection);

            try {
                Thread.sleep(gridPrintingInterval - (gameSpeed * 100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // wait for some time
            // and print the grid
            System.out.println(this);

            try {
                Thread.sleep(gridPrintingInterval  - (gameSpeed * 100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // wait for some time
            // and clear the screen
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    private void inputThreadStartup() {

        // input thread is a responsible for the single character input
        inputThread = new Thread (() -> {
            Terminal terminal;
            try {
                terminal = TerminalBuilder.terminal();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Attributes originalAtt = terminal.enterRawMode();

            while (true) {

                int input;

                try {
                    input = terminal.reader().read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                changeTheDirection (input);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        });

        inputThread.start();
    }

    // -1 -> occurs only if the snake bite itself
    // 0 -> normal condition
    // 1 the player is won the game
    private int gridModifier (int x, int y, Position position) {

        if (x < 0 || y < 0 || x >= this.GRID.length || y >= this.GRID[0].length)
            return -1;

        Node node = this.GRID[x][y];

        // if the state is food then we raise the snake
        if (node.state == States.FOOD) {
            node.state = States.SNAKE;
            this.snake.add(new Position (x, y));

            if (--this.foodCount <= 0) {
                if (randomFoodAllocation(1) == null)
                    return 1;
            }
        }

        else if (node.state == States.EMPTY) {

            node.state = States.SNAKE;
            // remove the tailnode from snake list
            Position tailNode = this.snake.removeFirst();

            // remove from the grid
            this.GRID[tailNode.x][tailNode.y].state = States.EMPTY;

            // add the new head
            this.snake.add(new Position (x, y));
        }

        else
            return -1;

        return 0;
    }

    private void init () {
        System.out.print("Enter the food count : ");
        foodCount = scanner.nextByte();

        if (!Validation.InputValidator.foodCountValidate(foodCount) && foodCount < this.GRID.length * this.GRID[0].length - 4) {
            throw new InputMismatchException("food count must be in the range of : (" + Constants.MIN_FOOD_COUNT + " - " + Constants.MAX_FOOD_COUNT + ")");
        }

        System.out.println("Enter 1 -> random food allocation : ");
        System.out.println("Enter 2 -> manual food allocation : ");
        System.out.print("Enter : ");

        int allocator = scanner.nextInt();

        Set<Position> positionSet;
        
        switch (allocator) {
            case 1 : {
                positionSet = randomFoodAllocation (foodCount);
                break;
            }

            case 2 : {
                System.out.println("Manual initializer...");
                positionSet = manualFoodAllocation (foodCount);
                break;
            }

            default : throw new InputMismatchException("input is not found!");
        }

        assert positionSet != null;
        snakeRandomPlacer (positionSet);
        initialSleep (2000);
    }

    private Set<Position> randomFoodAllocation (int count) {
        Set<Position> positionSet = new HashSet<>();

        int MAX_ITERATION = this.GRID.length * this.GRID[0].length;

        while (count-- > 0) {

            int temp = 0;

            int x = (int) (Math.random() * GRID.length);
            int y = (int) (Math.random() * GRID[0].length);

            while (this.GRID[x][y].state == States.SNAKE || !positionSet.add(new Position (x, y))) {
                x = (int) (Math.random() * GRID.length);
                y = (int) (Math.random() * GRID[0].length);

                // *** review the code ***
                if (MAX_ITERATION == this.snake.size() && MAX_ITERATION - temp++ < 0)
                    return null;
            }

            this.GRID[x][y].state = States.FOOD;
        }

        return positionSet;
    }

    private Set<Position> manualFoodAllocation (int count) {
        Set<Position> positionSet = new HashSet<>();

        while (count-- > 0) {

            System.out.print("Enter the coordinates : ");
            int x = scanner.nextInt();
            int y = scanner.nextInt();

            while (!positionSet.add(new Position (x, y))) {
                System.out.println("The coordinates is already exist, try again : ");
                x = scanner.nextInt();
                y = scanner.nextInt();
            }

            this.GRID[x][y].state = States.FOOD;
        }

        return positionSet;
    }

    private void snakeRandomPlacer (Set<Position> positionSet) {

        int x = (int) (Math.random() * GRID.length);
        int y = (int) (Math.random() * GRID[0].length);

        while (!positionSet.add(new Position (x, y))) {
            x = (int) (Math.random() * GRID.length);
            y = (int) (Math.random() * GRID[0].length);
        }

        this.GRID[x][y].state = States.SNAKE;
        this.snake.add(new Position (x, y));
    }

    private void initialSleep (long val) {

        System.out.print("Game is initializing");

        while (val != 0) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.print("interrupt : " + e);
            }

            if (val == 1000)
                System.out.println(".");
            else
                System.out.print(".");
            val -= 1000;
        }
    }

    public String toString () {
        StringBuilder builder = new StringBuilder();

//        for (Node[] arr : GRID) {
//
//            builder.append("    ");
//
//            for (Node node : arr)
//                builder.append(node).append(" ");
//
//            builder.append("\n");
//        }


        for (int i = -1; i <= this.GRID.length; i++) {

            for (int j = -1; j <= this.GRID[0].length; j++) {

                if (i == -1 || i == this.GRID.length)
                    builder.append("- ");

                else if (j == -1 || j == this.GRID[0].length)
                    builder.append("| ");

                else {
                    if (this.snake.getLast().x == i && this.snake.getLast().y == j)
                        builder.append("@").append(" ");

                    else if (!this.GRID[i][j].state.getValue().equals(States.EMPTY.getValue()))
                        builder.append(this.GRID[i][j]).append(" ");

                    else {
                        builder.append("  ");
                    }
                }
            }

            builder.append("\n");
        }

        return builder.toString();
    }
}
