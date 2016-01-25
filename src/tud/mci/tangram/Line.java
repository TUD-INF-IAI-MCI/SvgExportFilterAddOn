/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

/**
 *
 * @author Martin.Spindler@tu-dresden.de
 */
public class Line {
    float x1;
    float y1;
    float x2;
    float y2;
    
    Line(float x1, float y1, float x2, float y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    Line(Point p1, Point p2)
    {
        this.x1 = p1.x;
        this.y1 = p1.y;
        this.x2 = p2.x;
        this.y2 = p2.y;
    }
    
    Point getP1()
    {
        return new Point(x1, y1);
    }
    
    Point getP2()
    {
        return new Point(x2, y2);
    }
    
    /**
     * helpful for testing
     * @return
     */
    public XmlAttributeListImpl getAsSvgRectAttributes()
    {
        XmlAttributeListImpl result = new XmlAttributeListImpl("id", XmlUtils.trimToId(""));
        result.AddAttribute("class", "debug");
        result.AddAttribute("x1", Float.toString(x1));
        result.AddAttribute("y1", Float.toString(y1));
        result.AddAttribute("x2", Float.toString(x2));
        result.AddAttribute("y2", Float.toString(y2));
        return result;
    }
    
    public String getSvgPathData()
    {
        return "M" + x1 + " " + y1 + " " + "L" + x2 + " " + y2;
    }
    
    /**
     * relative to x-axis through x1/y1, left->right direction
     * @return
     */
    public double getAngleInRad()
    {
        return Math.atan((y2-y1)/(x2-x1));
    }
    
    /**
     * relative to x-axis through x1/y1, left->right direction
     * @return
     */
    public double getAngleInDeg()
    {
        return Transformation.degrees(getAngleInRad());
    }
    
    public float getBBoxHeight()
    {
        return y2-y1;
    }
    
    public float getCenterX()
    {
        return (x2-x1)/2.0f + x1;
    }
    
    public float getCenterY()
    {
        return (y2-y1)/2.0f + y1;
    }
    
    /**
     * creates a line with the same length and the same center but with 0 rotation. this is where the text is aligned at before turned in the center of the line with the lines angle
     * @return
     */
    public Line getRotationNormalizedLine()
    {
        float length = (float) Math.sqrt(((x2-x1)*(x2-x1))+((y2-y1)*(y2-y1)));
        return new Line(getCenterX()-0.5f*length, getCenterY(), getCenterX()+0.5f*length, getCenterY());
    }
}
