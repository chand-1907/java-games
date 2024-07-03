public interface Validation {

    final class InputValidator {
        public static boolean foodCountValidate (int count) {
            return count >= Constants.MIN_FOOD_COUNT && count <= Constants.MAX_FOOD_COUNT;
        }
    }
}
