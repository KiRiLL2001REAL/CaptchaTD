package cw.utils.imgenerator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdditionalMathTest {
    @Test
    void mapValueTest() {
        double value = 1;
        double srcFrom = 0;
        double srcTo = 4;
        double dstFrom = 0;
        double dstTo = 10;
        double actual = AdditionalMath.mapValue(value, srcFrom, srcTo, dstFrom, dstTo);
        assertEquals(2.5, actual);
    }

    @Test
    void euclideanDistance2DTest() {
        double[] point1 = {0, 0};
        double[] point2 = {1, 2};
        double actual = AdditionalMath.euclideanDistance(point1, point2);
        assertEquals(Math.sqrt(5), actual);
    }

    @Test
    void euclideanDistance3DTest() {
        double[] point1 = {1, 2, 3};
        double[] point2 = {0, 0, 0};
        double actual = AdditionalMath.euclideanDistance(point1, point2);
        assertEquals(Math.sqrt(14), actual);
    }
}