/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

/**
 * Transformation describes a matrix with 6 coefficients: 
 * a c e
 * b d f
 * @author Martin.Spindler@tu-dresden.de
 */
public class Transformation {

    static double radians(double degrees)
    {
        return java.lang.Math.toRadians(degrees);
    }
    
    static double degrees(double radians)
    {
        return java.lang.Math.toDegrees(radians);
    }
    
    public double a=1.0, b=0.0, c=0.0, d=1.0, e=0.0, f=0.0;

    public Transformation() 
    {
        a=1.0;
        b=0.0;
        c=0.0;
        d=1.0;
        e=0.0;
        f=0.0;
    }
    
    /**
     * parses from attribute in draw document, e.g. "rotate (0.785398163397448) translate (1.084cm 2.634cm)"
     * white space or comma separated transform definitions in the string:<br>
     * matrix (&lt;a&gt; &lt;b&gt; &lt;c&gt; &lt;d&gt; &lt;e&gt; &lt;f&gt;)<br>
     * rotate (&lt;rotate-angle&gt;)<br>
     * scale (&lt;sx&gt; [&lt;sy&gt;])<br>
     * skewX (&lt;skew-angle&gt;)<br>
     * skewY (&lt;skew-angle&gt;)<br>
     * translate (&lt;tx&gt; [&lt;ty&gt;])
     * @param drawTransformAttributeValue
     * @param isSvgFormat
     */
    public Transformation (String drawTransformAttributeValue, boolean isSvgFormat)
    {
        a=1.0;
        b=0.0;
        c=0.0;
        d=1.0;
        e=0.0;
        f=0.0;
        // parse: look for next definition by finding substring followd by substring in braces ( )
        String rest = drawTransformAttributeValue.trim();
        int brcOpenIdx;
        int brcCloseIdx;
        int commandIdx;
        String commandString;
        String[] paramStrings;
        double[] paramValues = new double[] {a, b, c, d, e, f};
        if (!isSvgFormat) while(rest.length()>0)
        {
            brcOpenIdx = rest.lastIndexOf('(');
            brcCloseIdx = rest.lastIndexOf(')');
            // look for command
            commandIdx = rest.substring(0, brcOpenIdx).lastIndexOf(')');
            if (commandIdx<0) commandIdx = 0;   // if no ')' before current '(' : command starts at 0
            else commandIdx++;                  // else command starts after ')' before current '(' 
            if (brcOpenIdx>0 && brcCloseIdx > brcOpenIdx+1)
            {
                commandString = rest.substring(commandIdx, brcOpenIdx).replace(',', ' ').trim();
                paramStrings = rest.substring(brcOpenIdx+1, brcCloseIdx).replace(',', ' ').split("\\s+");
                
                if (commandString.equals("matrix") && paramStrings.length >= 6)
                {
                    for (int i=0; i<6; i++) 
                    {
                        paramValues[i] = new Measure(paramStrings[i]).getValueInMm100th();
                    }
                    applyTransformation(new Transformation(paramValues[0], paramValues[1], paramValues[2], paramValues[3], paramValues[4], paramValues[5]));
                }
                else if (commandString.equals("rotate") && paramStrings.length >= 1)
                {
                    paramValues[0] = -Transformation.degrees(new Measure(paramStrings[0]).getValueInMm100th());
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Rotation(paramValues[0]));
                }
                else if (commandString.equals("scale") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    if (paramStrings.length >= 2) paramValues[1] = new Measure(paramStrings[1]).getValueInMm100th();
                    else paramValues[1] = 0.0;
                    for (int i=2; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Scaling(paramValues[0], paramValues[1]));
                }
                else if (commandString.equals("skewX") && paramStrings.length >= 1)
                {
                    paramValues[0] = -Transformation.degrees(new Measure(paramStrings[0]).getValueInMm100th());
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(SkewX(paramValues[0]));
                }
                else if (commandString.equals("skewY") && paramStrings.length >= 1)
                {
                    paramValues[0] = -Transformation.degrees(new Measure(paramStrings[0]).getValueInMm100th());
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(SkewY(paramValues[0]));
                }
                else if (commandString.equals("translate") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    if (paramStrings.length >= 2) paramValues[1] = new Measure(paramStrings[1]).getValueInMm100th();
                    else paramValues[1] = 0.0;
                    for (int i=2; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Translation(paramValues[0], paramValues[1]));
                }
                
                // new rest after current command and braces:
                rest = rest.substring(0, commandIdx).trim();
                if (rest.length()<4) break;
            }
            else break;// no more braces: break parsing
        }
        else while(rest.length()>0) /*if (isSvgFormat)*/
        {
            brcOpenIdx = rest.indexOf('(');
            brcCloseIdx = rest.indexOf(')');
            // look for command
            commandIdx = 0;
            if (brcOpenIdx>0 && brcCloseIdx > brcOpenIdx+1)
            {
                commandString = rest.substring(commandIdx, brcOpenIdx).replace(',', ' ').trim();
                paramStrings = rest.substring(brcOpenIdx+1, brcCloseIdx).replace(',', ' ').split("\\s+");
                
                if (commandString.equals("matrix") && paramStrings.length >= 6)
                {
                    for (int i=0; i<6; i++) 
                    {
                        paramValues[i] = new Measure(paramStrings[i]).getValueInMm100th();
                    }
                    applyTransformation(new Transformation(paramValues[0], paramValues[1], paramValues[2], paramValues[3], paramValues[4], paramValues[5]));
                }
                else if (commandString.equals("rotate") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Rotation(paramValues[0]));
                }
                else if (commandString.equals("scale") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    if (paramStrings.length >= 2) paramValues[1] = new Measure(paramStrings[1]).getValueInMm100th();
                    else paramValues[1] = 0.0;
                    for (int i=2; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Scaling(paramValues[0], paramValues[1]));
                }
                else if (commandString.equals("skewX") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(SkewX(paramValues[0]));
                }
                else if (commandString.equals("skewY") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    for (int i=1; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(SkewY(paramValues[0]));
                }
                else if (commandString.equals("translate") && paramStrings.length >= 1)
                {
                    paramValues[0] = new Measure(paramStrings[0]).getValueInMm100th();
                    if (paramStrings.length >= 2) paramValues[1] = new Measure(paramStrings[1]).getValueInMm100th();
                    else paramValues[1] = 0.0;
                    for (int i=2; i<6; i++) paramValues[i]=0.0;
                    applyTransformation(Translation(paramValues[0], paramValues[1]));
                }
                
                // new rest after current command and braces:
                rest = rest.substring(brcCloseIdx+1).trim();
                if (rest.length()<4) break;
            }
            else break;// no more braces: break parsing
        }
    }
    
     public Transformation (String drawTransformAttributeValue)
     {
        this(drawTransformAttributeValue, false);
     }
    
    /**
     *
     * @param flatMatrix must contain and be orderd as: a b c d e f
     */
    public Transformation(double[] flatMatrix)
    {
        if (flatMatrix.length>=6)
        {
            this.a = flatMatrix[0];
            this.b = flatMatrix[1];
            this.c = flatMatrix[2];
            this.d = flatMatrix[3];
            this.e = flatMatrix[4];
            this.f = flatMatrix[5];
        }
    }
    
    public Transformation(double a, double b, double c, double d, double e, double f)
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }
    
    public static Transformation Translation(double x, double y)
    {
        return new Transformation(1.0, 0.0, 0.0, 1.0, x, y);
    }
    
    public static Transformation Scaling (double factor)
    {
        return Scaling(factor, factor);
    }
    
    public static Transformation Scaling (double sx, double sy)
    {
        return new Transformation(sx, 0.0, 0.0, sy, 0, 0);
    }
    
    public static Transformation Rotation (double degree)
    {
        return Rotation(degree, 0.0, 0.0);
    }
    
    public static Transformation Rotation (double degree, double originX, double originY)
    {
        /*
        cos(a),
        sin(a),
        -sin(a),
        cos(a),
        x-x*cos(a)+y*sin(a),
        y-x*sin(a)-y*cos(a)
        */
        return new Transformation(
                Math.cos(radians(degree)),
                Math.sin(radians(degree)),
                0.0-Math.sin(radians(degree)),
                Math.cos(radians(degree)),
                originX-originX*Math.cos(radians(degree))+originY*Math.sin(radians(degree)),
                originY-originX*Math.sin(radians(degree))-originY*Math.cos(radians(degree)));
    }
    
    /**
     * Needed e.g. for arrow end markers conversion from oo draw to svg
     * @param originX
     * @param originY
     * @return
     */
    public static Transformation Rotate90Right (double originX, double originY)
    {
        return new Transformation(0.0, 1.0, -1.0, 0.0, originX + originY, originY - originX);
    }
    
    /**
     * Needed e.g. for arrow start markers conversion from oo draw to svg
     * @param originX
     * @param originY
     * @return
     */
    public static Transformation Rotate90Left (double originX, double originY)
    {
        return new Transformation(0.0, -1.0, 1.0, 0.0, originX - originY, originY + originX);
    }
    
    public static Transformation SkewX (double degree)
    {
        return new Transformation(1.0, 0.0, Math.tan(radians(degree)), 1.0, 0.0, 0.0);
    }
    
    public static Transformation SkewY (double degree)
    {
        return new Transformation(1.0, Math.tan(radians(degree)), 0.0, 1.0, 0.0, 0.0);
    }
    
    public static Transformation Skew (double degreeX, double degreeY)
    {
        return new Transformation(1.0, Math.tan(radians(degreeY)),  Math.tan(radians(degreeX)), 1.0, 0.0, 0.0);
    }
    
    public double[] getFlatMatrix()
    {
        return new double[]{a, b, c, d, e, f};
    }
  
    /**
     * transforms a Point
     * @param x
     * @param y
     * @return the transformed point
     */
    public Point getTransformedPoint(double x, double y)
    {        
        return new Point(
            (float) (a * x + c * y + e),
            (float) (b * x + d * y + f) );
    }
    
    /**
     * transforms a Point
     * @param originalPoint
     * @return the transformed point
     */
    public Point getTransformedPoint(Point originalPoint)
    {        
        return getTransformedPoint(originalPoint.x, originalPoint.y);
    }
      
    /**
     * transforms a rectangle by its 2 points
     * this is only helpful for 90° turns, else the definition of the rectangle by position of size does not sufficiently represent its angle
     * @param originalRectangle
     * @return
     */
    public Rectangle getTransformedRectangle(Rectangle originalRectangle)
    {
        Point pos1 = getTransformedPoint(originalRectangle.x, originalRectangle.y);
        Point pos2 = getTransformedPoint(originalRectangle.x+originalRectangle.width, originalRectangle.y+originalRectangle.height);
        // eventually switch coordinate order for getting the minimum x and y as pos and the maxiumum x and y as second point when constructing the rect
        if (pos2.x<pos1.x)
        {
            double tmp = pos1.x;
            pos1.x = pos2.x;
            pos2.x = (float) tmp;
        }
        if (pos2.y<pos1.y)
        {
            double tmp = pos1.y;
            pos1.y = pos2.y;
            pos2.y = (float) tmp;
        }
        return new Rectangle(pos1, pos2);
    }
    
    private double getDeterminant()
    {
        // (ad1)+(cf0)+(eb0)-(0de)-(0fa)-(1bc) = ad-bc

        return a * d - b * c;
    }
    
    public Transformation getInversion()
    {
        double det = getDeterminant();
        return new Transformation(
                d/det, 
                -b/det, 
                -c/det,                 
                a/det, 
                (c*f-e*d)/det, 
                (-a*f+e*b)/det);
    }
    
    /**
     * multiplies with a following transformation
     * @param following
     */
    private void applyTransformation(Transformation following)
    {
        /*
          a c e   a'c'e'   a*a'+c*b'+e*0 a*c'+c*d'+e*0 a*e'+c*f'+e*1
          b d f * b'd'f' = b*a'+d*b'+f*0 b*c'+d*d'+f*0 b*e'+d*f'+f*1
          0 0 1   0 0 1    0             0             1
        */
        double newA = this.a * following.a + this.c * following.b /* + this.e * 0.0 */; 
        double newB = this.b * following.a + this.d * following.b /* + this.f * 0.0 */; 
        double newC = this.a * following.c + this.c * following.d /* + this.e * 0.0 */;                 
        double newD = this.b * following.c + this.d * following.d /* + this.f * 0.0 */; 
        double newE = this.a * following.e + this.c * following.f + this.e /* * 1.0 */; 
        double newF = this.b * following.e + this.d * following.f + this.f /* * 1.0 */;
        
        this.a = newA;
        this.b = newB;
        this.c = newC;
        this.d = newD;
        this.e = newE;
        this.f = newF;
        
    }
    
    /**
     * multiplies the original transformation with a following transformation
     * @param following
     * @return
     */
    public Transformation transformBy(Transformation following)
    {
        /*
          a c e   a'c'e'   a*a'+c*b'+e*0 a*c'+c*d'+e*0 a*e'+c*f'+e*1
          b d f * b'd'f' = b*a'+d*b'+f*0 b*c'+d*d'+f*0 b*e'+d*f'+f*1
          0 0 1   0 0 1    0             0             1
        */
        return new Transformation(
                a * following.a + c * following.b /* + this.e * 0.0 */,     // a
                b * following.a + d * following.b /* + this.f * 0.0 */,     // b
                a * following.c + c * following.d /* + this.e * 0.0 */,     // c            
                b * following.c + d * following.d /* + this.f * 0.0 */,     // d 
                a * following.e + c * following.f + e /* * 1.0 */,          // e
                b * following.e + d * following.f + f /* * 1.0 */);         // f
    }
    
    /**
     * SVG attribute value "matrix(a,b,c,d,e,f)"
     * @return
     */
    @Override
    public String toString()
    {
        return "matrix("+a+", "+b+", "+c+", "+d+", "+e+", "+f+")";
    }
}