public interface Validation {

    final class InputValidator {
        public static boolean foodCountValidate (int count) {
            return count >= Constants.MIN_FOOD_COUNT && count <= Constants.MAX_FOOD_COUNT;
        }

        public static boolean rowColumnValidator (int row, int col) {
            return (row <= Constants.MAX_ROW_COUNT && row >= Constants.MIN_ROW_COUNT && col <= Constants.MAX_COL_COUNT && col >= Constants.MIN_COL_COUNT);
        }
    }
}