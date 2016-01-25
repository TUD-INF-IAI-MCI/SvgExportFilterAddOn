/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

/**
 * Defines an upper left position (x, y) and a width (to the right) plus a height (to the bottom)
 * Might represent a viewBox
 * @author Martin.Spindler@tu-dresden.de
 */
public class Rectangle {
    float x, y, width, height;

    /**
     * creates empty (0 * 0) rectangle at 0, 0
     */
    public Rectangle()
    {
        x = 0.0f;
        y = 0.0f;
        width = 0.0f;
        height = 0.0f;
    }
    
    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public Rectangle(Point pos, Size size)
    {
        this.x = pos.x;
        this.y = pos.y;
        this.width = size.width;
        this.height = size.height;
    }
    
    /**
     *
     * @param upperLeft with upperLeft.x <= lowerRight.x
     * @param lowerRight with upperLeft.y <= lowerRight.y
     */
    public Rectangle(Point upperLeft, Point lowerRight)
    {
        this.x = Math.min(upperLeft.x, lowerRight.x);
        this.y = Math.min(upperLeft.y, lowerRight.y);
        this.width = Math.abs(lowerRight.x - upperLeft.x);
        this.height = Math.abs(lowerRight.y - upperLeft.y);
    }
    
    /**
     * Measures will be evaluated and converted to 100th mm values
     * @param viewBoxAttributeString
     */
    public Rectangle(String viewBoxAttributeString)
    {
        String[] words = viewBoxAttributeString.split("\\W+");  // split into words
        if (words.length>0) x = new Measure(words[0]).getValueInMm100th();
        if (words.length>1) y = new Measure(words[1]).getValueInMm100th();
        if (words.length>2) width = new Measure(words[2]).getValueInMm100th();
        if (words.length>3) height = new Measure(words[3]).getValueInMm100th();    
    }
    
    public Size getSize()
    {
        return new Size(width, height);
    }
    
    public Point getPos()
    {
        return new Point(x, y);
    }
    
    public Point getCenter()
    {
        return new Point (getCenterX(), getCenterY());
    }  
    
    public float getCenterX()
    {
        return x+width*0.5f;
    }  
    
    public float getCenterY()
    {
        return y+height*0.5f;
    }  
    
    /**
     * helpful for testing
     * @return
     */
    public XmlAttributeListImpl getAsSvgRectAttributes()
    {
        XmlAttributeListImpl result = new XmlAttributeListImpl("id", XmlUtils.trimToId(""));
        result.AddAttribute("class", "debug");
        result.AddAttribute("x", Float.toString(x));
        result.AddAttribute("y", Float.toString(y));
        result.AddAttribute("width", Float.toString(width));
        result.AddAttribute("height", Float.toString(height));
        return result;
    }
}
