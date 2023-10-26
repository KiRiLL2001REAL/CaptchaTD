package cw.exceptions;

/**
 * You don't need to create instances of this class manually, it is better to use method
 * {@link cw.captcha.PuzzleCaptcha#checkCorrectnessOfIndices(int, int)}.
 * <p>So, you only have to handle the exception (just print {@link PuzzleCaptchaIndexOutOfBoundsException#getMessage()}).
 */
public class PuzzleCaptchaIndexOutOfBoundsException extends Exception {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private final String message;

    /**
     * @param mnemonicIndex "row" or "column" or whatever, but it should be understandable for you.
     * @param idx requested index
     * @param arrSize size of array
     */
    public PuzzleCaptchaIndexOutOfBoundsException(String mnemonicIndex, int idx, int arrSize) {
        super();

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String methodName = elements[3].getClassName() + "." + elements[3].getMethodName();

        StringBuilder builder = new StringBuilder();
        builder.append("An error occurred in method:\n\t").append(methodName)
                .append("\nCause:\n\t")
                .append("Attempt to access the grid at ").append(mnemonicIndex).append(" index ").append(idx);
        if (idx < 0)
            builder.append(", which is less than zero.");
        else
            builder.append(", although there are only ").append(arrSize).append(" elements.");
        builder.append("\n");

        message = ANSI_RED + builder + ANSI_RESET;
    }

    public String getMessage() {
        return message;
    }
}
