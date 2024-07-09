public enum States {

    EMPTY("."),
    SNAKE("#"),
    FOOD("F");

    final String state;

    States(String state) {
        this.state = state;
    }

    public String getValue () {
        return this.state;
    }
}
