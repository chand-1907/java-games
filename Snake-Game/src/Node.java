public class Node {
    States state;

    public Node () {
        state = States.EMPTY;
    }

    public String toString () {
        return state.getValue();
    }
}
