/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import junit.framework.TestCase;

/**
 *
 * @author Spindler
 */
public class TransformationTest extends TestCase {
    
    public TransformationTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of radians method, of class Transformation.
     */
    public void testRadians() {
        double DELTA = 1e-6;
        System.out.println("radians");
        assertEquals(-1 * Math.PI / 2.0, Transformation.radians(-90), DELTA);
        assertEquals(0 * Math.PI / 2.0, Transformation.radians(0), DELTA);
        assertEquals(1 * Math.PI / 2.0, Transformation.radians(90), DELTA);
        assertEquals(2 * Math.PI / 2.0, Transformation.radians(180), DELTA);
        assertEquals(3 * Math.PI / 2.0, Transformation.radians(270), DELTA);
        assertEquals(4 * Math.PI / 2.0, Transformation.radians(360), DELTA);
        assertEquals(5 * Math.PI / 2.0, Transformation.radians(450), DELTA);
    }

    /**
     * Test of degrees method, of class Transformation.
     */
    public void testDegrees() {
        double DELTA = 1e-6;
        System.out.println("degrees");
        assertEquals(-90, Transformation.degrees(-1 * Math.PI / 2.0), DELTA);
        assertEquals(0, Transformation.degrees(0 * Math.PI / 2.0), DELTA);
        assertEquals(90, Transformation.degrees(1 * Math.PI / 2.0), DELTA);
        assertEquals(180, Transformation.degrees(2 * Math.PI / 2.0), DELTA);
        assertEquals(270, Transformation.degrees(3 * Math.PI / 2.0), DELTA);
        assertEquals(360, Transformation.degrees(4 * Math.PI / 2.0), DELTA);
        assertEquals(450, Transformation.degrees(5 * Math.PI / 2.0), DELTA);
    }

    /**
     * Test of Translation method, of class Transformation.
     */
    public void testTranslation() {
        System.out.println("Translation");
        double x = -125.0;
        double y = 21.7;
        Transformation translation = Transformation.Translation(x, y);
        assertNotNull(translation);
        assertEquals(1.0, translation.a);
        assertEquals(0.0, translation.b);
        assertEquals(0.0, translation.c);
        assertEquals(1.0, translation.d);
        assertEquals(x, translation.e);
        assertEquals(y, translation.f);
    }

    /**
     * Test of Scaling method, of class Transformation.
     */
    public void testScaling_double() {
        System.out.println("Scaling");
        double factor = 7.15;
        Transformation scaling = Transformation.Scaling(factor);
        assertNotNull(scaling);
        assertEquals(factor, scaling.a);
        assertEquals(0.0, scaling.b);
        assertEquals(0.0, scaling.c);
        assertEquals(factor, scaling.d);
        assertEquals(0.0, scaling.e);
        assertEquals(0.0, scaling.f);
    }

    /**
     * Test of Scaling method, of class Transformation.
     */
    public void testScaling_double_double() {
        System.out.println("Scaling");
        double sx = 1.5;
        double sy = -0.5;
        Transformation scaling = Transformation.Scaling(sx, sy);
        assertNotNull(scaling);
        assertEquals(sx, scaling.a);
        assertEquals(0.0, scaling.b);
        assertEquals(0.0, scaling.c);
        assertEquals(sy, scaling.d);
        assertEquals(0.0, scaling.e);
        assertEquals(0.0, scaling.f);
    }

    /**
     * Test of Rotation method, of class Transformation.
     */
    public void testRotation() {
        double DELTA = 1e-6;
        System.out.println("Rotation");
        double degree = 60.1;
        Transformation rotation = Transformation.Rotation(degree);
        assertNotNull(rotation);
        assertEquals(Math.cos(Transformation.radians(degree)), rotation.a, DELTA);
        assertEquals(Math.sin(Transformation.radians(degree)), rotation.b, DELTA);
        assertEquals(-Math.sin(Transformation.radians(degree)), rotation.c, DELTA);
        assertEquals(Math.cos(Transformation.radians(degree)), rotation.d, DELTA);
        assertEquals(0.0, rotation.e);
        assertEquals(0.0, rotation.f);
    }

        /**
     * Test of Rotation method, of class Transformation.
     */
    public void testRotation_double_double() {
        double DELTA = 1e-6;
        System.out.println("Rotation");
        double degree = 60.1;
        double originX = 1.0;
        double originY = -1.0;
        Transformation rotation = Transformation.Rotation(degree, originX, originY);
        assertNotNull(rotation);
        assertEquals(Math.cos(Transformation.radians(degree)), rotation.a, DELTA);
        assertEquals(Math.sin(Transformation.radians(degree)), rotation.b, DELTA);
        assertEquals(-Math.sin(Transformation.radians(degree)), rotation.c, DELTA);
        assertEquals(Math.cos(Transformation.radians(degree)), rotation.d, DELTA);
        assertEquals(originX-originX*Math.cos(Transformation.radians(degree))+originY*Math.sin(Transformation.radians(degree)), rotation.e);
        assertEquals(originY-originX*Math.sin(Transformation.radians(degree))-originY*Math.cos(Transformation.radians(degree)), rotation.f);
    }
    
    /**
     * Test of Rotate90Right method, of class Transformation.
     */
    public void testRotate90Right() {
        double DELTA = 1e-6;
        System.out.println("Rotate90Right");
        double originX = 14.0;
        double originY = -27.0;
        Transformation rotation90 = Transformation.Rotate90Right(originX, originY);
        assertNotNull(rotation90);
        assertEquals(0.0, rotation90.a);
        assertEquals(1.0, rotation90.b);
        assertEquals(-1.0, rotation90.c);
        assertEquals(0.0, rotation90.d);
        assertEquals(originX + originY, rotation90.e);
        assertEquals(originY - originX, rotation90.f);
        
        Transformation rotationFree = Transformation.Rotation(90, originX, originY);
        assertNotNull(rotationFree);
        assertEquals(0.0, rotationFree.a, DELTA);
        assertEquals(1.0, rotationFree.b, DELTA);
        assertEquals(-1.0, rotationFree.c, DELTA);
        assertEquals(0.0, rotationFree.d, DELTA);
        assertEquals(originX + originY, rotationFree.e, DELTA);
        assertEquals(originY - originX, rotationFree.f, DELTA);
        
    }

    /**
     * Test of Rotate90Left method, of class Transformation.
     */
    public void testRotate90Left() {
        double DELTA = 1e-6;
        System.out.println("Rotate90Left");
        double originX = -1.3;
        double originY = 17.8;
        Transformation rotationM90 = Transformation.Rotate90Left(originX, originY);
        assertNotNull(rotationM90);
        assertEquals(0.0, rotationM90.a);
        assertEquals(-1.0, rotationM90.b);
        assertEquals(1.0, rotationM90.c);
        assertEquals(0.0, rotationM90.d);
        assertEquals(originX - originY, rotationM90.e);
        assertEquals(originY + originX, rotationM90.f);
        
        Transformation rotationFree = Transformation.Rotation(-90, originX, originY);
        assertNotNull(rotationFree);
        assertEquals(0.0, rotationFree.a, DELTA);
        assertEquals(-1.0, rotationFree.b, DELTA);
        assertEquals(1.0, rotationFree.c, DELTA);
        assertEquals(0.0, rotationFree.d, DELTA);
        assertEquals(originX - originY, rotationFree.e, DELTA);
        assertEquals(originY + originX, rotationFree.f, DELTA);
    }

    /**
     * Test of getFlatMatrix method, of class Transformation.
     */
    public void testGetFlatMatrix() {
        System.out.println("getFlatMatrix");
        Transformation instance = new Transformation(1.0,2.0,3.0,4.0,5.0,6.0);
        double[] result = instance.getFlatMatrix();
        assertNotNull(result);
        assertEquals(1.0, result[0]);
        assertEquals(2.0, result[1]);
        assertEquals(3.0, result[2]);
        assertEquals(4.0, result[3]);
        assertEquals(5.0, result[4]);
        assertEquals(6.0, result[5]);
    }

    /**
     * Test of getTransformedPoint method, of class Transformation.
     */
    public void testGetTransformedPoint_double_double() {
        double DELTA = 1e-4;
        System.out.println("getTransformedPoint");
        double x = 8.57095342778808;
        double y = 50.0513649331846;
        Transformation mobilityGeoToMeterTransformation = new Transformation(
                67164.4168979277,
                24445.8485526916,
                38032.6399371235,
                -104493.819429495,
                -2478304.63065115,
                5020843.77604822
        );
        Point result = mobilityGeoToMeterTransformation.getTransformedPoint(x, y);
        assertEquals(943.99945091, result.x, DELTA);
        assertEquals(309.71596815, result.y, DELTA);
    }

    /**
     * Test of getTransformedPoint method, of class Transformation.
     */
    public void testGetTransformedPoint_Point() {
        double DELTA = 1e-2;
        System.out.println("getTransformedPoint");
        Point originalPoint = new Point(
                8.57095342778808f, 
                50.0513649331846f);
        Transformation mobilityGeoToMeterTransformation = new Transformation(
                67164.4168979277,
                24445.8485526916,
                38032.6399371235,
                -104493.819429495,
                -2478304.63065115,
                5020843.77604822
        );
        Point result = mobilityGeoToMeterTransformation.getTransformedPoint(originalPoint);
        assertEquals(943.99945091, result.x, DELTA);
        assertEquals(309.71596815, result.y, DELTA);
    }

    /**
     * Test of getTransformedRectangle method, of class Transformation.
     */
    public void testGetTransformedRectangle() {
        double DELTA = 1e-6;
        System.out.println("getTransformedRectangle");
        Rectangle originalRectangle = new Rectangle(0, 0, 5, 4);
        Transformation transformation = Transformation.Rotation(90);
        Rectangle result = transformation.getTransformedRectangle(originalRectangle);
        assertEquals(-4.0, result.x , DELTA);
        assertEquals(0.0, result.y, DELTA);
        assertEquals(4.0, result.width, DELTA);
        assertEquals(5.0, result.height, DELTA);
    }

    /**
     * Test of getInversion method, of class Transformation.
     */
    public void testGetInversion() {
        double DELTA = 1e-8;
        System.out.println("getInversion");
        Transformation mobilityGeoToMeterTransformation = new Transformation(
                67164.4168979277,
                24445.8485526916,
                38032.6399371235,
                -104493.819429495,
                -2478304.63065115,
                5020843.77604822
        );
        Transformation mobilityMeterToGeoTransformation = new Transformation(
                0.0000131471731959239,
                0.000003075720713419,
                0.0000047851797080544,
                -8.45047320866756E-06,
                8.55706045694406,
                50.0510787010112
        );
        
        Transformation result = mobilityGeoToMeterTransformation.getInversion();
        assertEquals(mobilityMeterToGeoTransformation.a, result.a, DELTA);
        assertEquals(mobilityMeterToGeoTransformation.b, result.b, DELTA);
        assertEquals(mobilityMeterToGeoTransformation.c, result.c, DELTA);
        assertEquals(mobilityMeterToGeoTransformation.d, result.d, DELTA);
        assertEquals(mobilityMeterToGeoTransformation.e, result.e, DELTA);
        assertEquals(mobilityMeterToGeoTransformation.f, result.f, DELTA);
        
        Transformation thereTrans = new Transformation(-1.0, -2.0, -3.0, -4.0, -5.0, -6.0);
        Transformation backTrans = thereTrans.getInversion();
        result = backTrans.getInversion();
        assertEquals(-1.0, result.a);
        assertEquals(-2.0, result.b);
        assertEquals(-3.0, result.c);
        assertEquals(-4.0, result.d);
        assertEquals(-5.0, result.e);
        assertEquals(-6.0, result.f);
    }

    /**
     * Test of transformBy method, of class Transformation.
     */
    public void testTransformBy() {
        System.out.println("transformBy");
        Transformation translationToOrigin = Transformation.Translation(17.0, -5.0);
        Transformation rotation = Transformation.Rotation(60);
        Transformation translationBack = Transformation.Translation(-17.0, 5.0);
        // chaining: translation to rotatino center, rotation, translate back
        Transformation result = translationToOrigin.transformBy(rotation).transformBy(translationBack);
        // should be the same as calling the rotation around center function that implements the calclutation of the factors directly
        Transformation expResult = Transformation.Rotation(60, 17.0, -5.0);
        
        assertEquals(expResult.a, result.a);
        assertEquals(expResult.b, result.b);
        assertEquals(expResult.c, result.c);
        assertEquals(expResult.d, result.d);
        assertEquals(expResult.e, result.e);
        assertEquals(expResult.f, result.f);
    }
    
}
