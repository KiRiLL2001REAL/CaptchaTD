package cw.utils.imgenerator;

public class AdditionalMath {
    /**
     * Mapping value from one range to another one.
     * @param value number to be mapped.
     * @param oldMinValue the lower limit of the range in which the {@code value} is.
     * @param oldMaxValue the upper limit of the range in which the {@code value} is.
     * @param newMinValue the lower limit of the range to which the {@code value} should be mapped.
     * @param newMaxValue the upper limit of the range to which the {@code value} should be mapped.
     * @return Mapped value.
     */
    public static double mapValue(double value, double oldMinValue, double oldMaxValue, double newMinValue,
                                  double newMaxValue)
    {
        return ((value - oldMinValue) / (oldMaxValue - oldMinValue)) * (newMaxValue - newMinValue) + newMinValue;
    }

    /**
     * Euclidean distance from point1 to point2.
     * @param point1 representation of the first point as a {@code double[]}.
     * @param point2 representation of the second point as a {@code double[]}.
     * @return the distance between the specified points as a {@code double}.
     */
    public static double euclideanDistance(double[] point1, double[] point2) {
        int dimensions = Math.min(point1.length, point2.length);
        double quadraticSum = 0d;
        for (int i = 0; i < dimensions; i++)
            quadraticSum += sqr(point1[i] - point2[i]);
        return Math.sqrt(quadraticSum);
    }

    protected static double sqr(double arg) {
        return arg * arg;
    }

    /**
     * Linearly interpolate between two values.
     * @param x specify the start of the range in which to interpolate.
     * @param y specify the end of the range in which to interpolate.
     * @param a specify the value to use to interpolate between {@code x} and {@code y}.
     */
    public static double mix(double x, double y, double a) {
        assert (a >= 0d && a <= 1d);
        double ra = 1 - a;
        return x * ra + y * a;
    }

    /**
     * Performs smooth Hermite interpolation between {@code 0} and {@code 1} when {@code edge0 < x < edge1}. This is
     * useful in cases where a threshold function with a smooth transition is desired.
     * <p><b>WARNING</b> Results are undefined if {@code edge0 ≥ edge1}.
     * @param edge0 specifies the value of the lower edge of the Hermite function.
     * @param edge1 specifies the value of the upper edge of the Hermite function.
     * @param x specifies the source value for interpolation.
     */
    public static double smoothStep(double edge0, double edge1, double x) {
        double value = (x - edge0) / (edge1 - edge0);
        double t = Math.min(Math.max(value, 0d), 1d);
        return t * t * (3d - 2d * t);
    }
}
