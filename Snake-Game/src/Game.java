import java.util.*;

public class Game {

    private final Node GRID[][];
    private final LinkedList<Position> snake;
    private final Scanner scanner;

    public Game (int rows, int cols) {
        this.GRID = new Node[rows][cols];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                 this.GRID[i][j] = new Node();

        this.snake = new LinkedList<>();
        this.scanner = new Scanner(System.in);
    }

    public void start () {

        // init the game
        init();

        while (true) {

            System.out.println(this);

            System.out.print("Enter the direction : ");

            char c = scanner.next().charAt(0);

            // some code

            Position position = snake.getLast();
            boolean flag = false;

            switch (c) {
                case 'w' : {
                    flag = gridModifier (position.x - 1, position.y, position);
                    break;
                }

                case 's' : {
                    flag = gridModifier (position.x + 1, position.y, position);
                    break;
                }

                case 'a' : {
                    flag = gridModifier (position.x, position.y - 1, position);
                    break;
                }

                case 'd' : {
                    flag = gridModifier (position.x, position.y + 1, position);
                    break;
                }

                default : System.out.println("wrong input");
            }

            if (!flag) {
                System.out.println("game is over!\neither the snake goes the bound (or) bites it own tail");
                return;
            }
        }
    }

    private boolean gridModifier (int x, int y, Position position) {

        if (x < 0 || y < 0 || x >= this.GRID.length || y >= this.GRID[0].length)
            return false;

        Node node = this.GRID[x][y];

        // if the state is food then we raise the snake
        if (node.state == States.FOOD) {
            node.state = States.SNAKE;
            this.snake.add(new Position (x, y));
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
            return false;

        return true;
    }

    private void init () {
        System.out.print("Enter the food count : ");
        int count = scanner.nextByte();

        if (!Validation.InputValidator.foodCountValidate(count)) {
            throw new InputMismatchException("food count must be in the range of : (" + Constants.MIN_FOOD_COUNT + " - " + Constants.MAX_FOOD_COUNT + ")");
        }

        System.out.println("Enter 1 -> random food allocation : ");
        System.out.println("Enter 2 -> manual food allocation : ");
        System.out.print("Enter : ");

        int allocator = scanner.nextInt();

        Set<Position> positionSet;
        
        switch (allocator) {
            case 1 : {
                positionSet = randomFoodAllocation (count);
                break;
            }

            case 2 : {
                System.out.println("Manual initializer...");
                positionSet = manualFoodAllocation (count);
                break;
            }

            default : throw new InputMismatchException("input is not found!");
        }
        
        snakeRandomPlacer (positionSet);
        initialSleep (5000);
    }

    private Set<Position> randomFoodAllocation (int count) {
        Set<Position> positionSet = new HashSet<>();

        while (count-- > 0) {

            int x = (int) (Math.random() * GRID.length);
            int y = (int) (Math.random() * GRID[0].length);

            while (!positionSet.add(new Position (x, y))) {
                x = (int) (Math.random() * GRID.length);
                y = (int) (Math.random() * GRID[0].length);
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

        for (Node[] arr : GRID) {

            builder.append("    ");

            for (Node node : arr)
                builder.append(node).append(" ");

            builder.append("\n");
        }

        return builder.toString();
    }
}
