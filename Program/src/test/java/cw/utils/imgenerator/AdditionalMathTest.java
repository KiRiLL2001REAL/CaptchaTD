package cw.utils.imgenerator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AdditionalMathTest {

    @Test
    public void testMapValue() {
        double value = 1;
        double srcFrom = 0;
        double srcTo = 4;
        double dstFrom = 0;
        double dstTo = 10;
        double actual = AdditionalMath.mapValue(value, srcFrom, srcTo, dstFrom, dstTo);
        assertEquals(2.5, actual, 0.0001);
    }

    @Test
    public void testEuclideanDistance2D() {
        double[] point1 = {0, 0};
        double[] point2 = {1, 2};
        double actual = AdditionalMath.euclideanDistance(point1, point2);
        assertEquals(Math.sqrt(5), actual, 0.0001);
    }

    @Test
    public void testEuclideanDistance3D() {
        double[] point1 = {1, 2, 3};
        double[] point2 = {0, 0, 0};
        double actual = AdditionalMath.euclideanDistance(point1, point2);
        assertEquals(Math.sqrt(14), actual, 0.0001);
    }
}