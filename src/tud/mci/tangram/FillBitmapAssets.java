/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.io.XSeekable;
import com.sun.star.io.XStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * for converting the tangram specific fill bitmpas to tiger fill patterns
 * @author Spindler
 */
public class FillBitmapAssets 
{
    
    
    public static final String NO_PATTERN = "no_pattern";
    public static String NO_PATTERN_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQAAAADsdIMmAAAADElEQVQYlWP4z4ACAT/QB/kKz8iAAAAAAElFTkSuQmCC";
    public static final String DASHED_LINES = "dashed_lines";
    public static String DASHED_LINES_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAB0AAAAmCAIAAAC3TAc2AAAACXBIWXMAAA7FAAAOxQFHbOz/AAAAaElEQVR4nO3OsQ3AIAxE0RQIai/AFHSWKJmc3jO4ZwcnE0RXgJRI9/t7uhQR14HSCZTum7vWMjNwX0rpvUPug44xQLfW6u6QuyW6dOnSpUuX7ifd1tqcE9znnFFXRFQVv4a6W6L7T/cGARETsAQ0SEYAAAAASUVORK5CYII=";
    public static final String DIAGONAL_LINE_1 = "diagonal_line1";
    public static String DIAGONAL_LINE_1_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAACYAAAAmCAIAAAAnX375AAAACXBIWXMAAA7FAAAOxQFHbOz/AAABC0lEQVR4nL3WQQ5FMBAG4LeYLWdxBmuSOikJa2dwCM5Rv5DnEa+tdma6U+Jb9O/MkLX2E7ymaaqqalmW382yLNu2zfM88CcU7mEVRTEMw00dx7FpmnD1HcmivibT1RgyUY0kU9R4MlpNIuPUVDJCZSDfqjzkV63rep5nt8pG7mrf916Vk3SrXddlWcZPOlRjzK7yk16V0I/whaZKSDaSpqkSbpKyup2lsnrER1M9EwsVu3gnrV4uCZ4VVMJ8hhRpqoTqh2qkqRKqrbK6naWyesRHUz0TCxU1FzVQWr1cEnQWBfXevBTUh34prT63aFH171Qgp7oGESHVM/tIqP5xi10NmvB41dChklFdAScWvgeijgUhAAAAAElFTkSuQmCC";
    public static final String DIAGONAL_LINE_2 = "diagonal_line2";
    public static String DIAGONAL_LINE_2_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAIAAAD9b0jDAAAACXBIWXMAAA7IAAAOyAFOXIFsAAAAoElEQVR4nK3UsQ0EIQxEUQc0RwGUQQ1IlEAZFEFhJNwhod0F3S22mR86eNHIprVGj2KMIQSaSyl574mdgYsTihJvFCgOFCt2FC52FC6WUsxyOhettRMKEWutN4oS6ZoUUBwoVuwoXOwoXHTOrZM6F3POSvRFpOX1QUQNuhXFKEeUoUxRgPJFLioSWahU3KMKcYPqxDdULf5FT8Tf6KH47QOYU+d9Vl2ZHwAAAABJRU5ErkJggg==";
    public static final String DOTTED = "dotted_pattern";
    public static String DOTTED_BASE64 = 
            "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";
    public static final String FULL_PATTERN = "full_pattern";
    public static String FULL_PATTERN_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAHoAAABZCAIAAAB+A6crAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAA2UlEQVR4nO3QMQ0AMAzAsB3lj3ks3KMxgijzAs12wC3tptpNtZtqN9Vuqt1Uu6l2U+2m2k21m2o31W6q3VS7qXZT7abaTbWbajfVbqrdVLupdlPtptpNtZtqN9Vuqt1Uu6l2U+2m2k21m2o31W6q3VS7qXZT7abaTbWbajfVbqrdVLupdlPtptpNtZtqN9Vuqt1Uu6l2U+2m2k21m2o31W6q3VS7qXZT7abaTbWbajfVbqrdVLupdlPtptpNtZtqN9Vuqt1Uu6l2U+2m2k21m2o31W6q3VS7qQ87CQFl4V0x5QAAAABJRU5ErkJggg==";
    public static String FULL_PATTERN_BASE64b = 
            "iVBORw0KGgoAAAANSUhEUgAAAHoAAABZCAIAAAB+A6crAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAA3SURBVHhe7cExAQAAAMKg9U9tDQ8gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHhVA3+XAAFCSi3oAAAAAElFTkSuQmCC";
    public static final String GRID = "grid_pattern";
    public static String GRID_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAI4AAABVCAIAAAAQZkuzAAAACXBIWXMAAA7EAAAOwwHaapjcAAADoklEQVR4nO2dTS9jURzGy72qGq0EK2+J74CG8hGKnfgGFrXRLixGJGLLR6CNhQWxmUQyCwsJG5EIGpNg4V3qpVqC0GJOruTkzMTuf2aSJ/P8ds/myf3np/eexTmH6zjO29ubz6OsrKyhoaG8vNwnI5/PPzw86FhXV1dVVaXKJZ0vLy9XV1c6qkJVK+xUXF5elkolHRsbG+Xj39/fFwoFHWtra4PBoPBRi8Wiqz0pPj4+zs/PJY1fcnt7a73z+fn57OzMeu3fGD/nIe9x5RXk30BVMFAVDG59ff3Nzc1nCAQC2Ww2HA4LSxOJxPT0tI4LCwt9fX0VFRWSzo2NjUgkouPAwMDs7KxaXEg6Fa2trUdHRzqqBVFNTY2wc2xsbHJyUsd0Oj04OCgcP5PJ8FcFA1XBQFUwUBUMVAUDVcFAVTBQFQxUBQNVwUBVMFAVDFQFA1XBQFUwUBUMVAUDVcFAVTBQFQxUBQNVweA+PT3pUCqVJiYmKisrhaVra2tmnJ+f39zcdBxH0nlxcWHGnZ2d8fFx4Y4tn7ebzIxq/EAgIOxcXV014+Li4t7ennD86+vrP1VNTU1JGr9kaWnJeudPD+u15vZFW3z3kPfwBQgDVcFAVTC4oVBIH1tTX+mZmZlgMCgsTaVS5ts5mUx2dHQIv6sHBwejo6M6RqPReDzu9/slnYqhoSH1xdZxbm5OPr5aRqmlhI7Dw8M9PT3C8U9OTly13tOqVF1vb6/8eMH6+roZI5GIleMFZmxqalKd8uMFiUTCVBWLxeTHC7a2tszY1tbW39/P4wX/EVQFA1XBQFUwUBUMVAUDVcFAVTBQFQxUBQNVwUBVMFAVDFQFA1XBQFUwUBUMVAUDVcFAVTBQFQy/Xd79/v6eyWSqq6uFpeYeIMXx8fHu7q7riv4sDg8PzZjP51WnfH95sVg0oxo/FAoJO7PZrBlPT0+tjO/e3d3p/Pr6Go1GJY1fkkwmrXf+8LBe293dbb3zm4e8hy9AGKgKBqqCwfX7/eoT9Rkcx4nFYvLzZdvb2/v7+zp2dnbK/ytMLpdbWVnRsbm5ub29XbgRXLG8vPz4+KijfMeyz1ubmGe/1HO2tLQIxy8UCm44HNZX4qunTKfT1q/EHxkZsX4lfldXl60r8U1VqVTK+pX48XicV+L/X1AVDFQFA1XBQFUwUBUMVAUDVcFAVTBQFQxUBQNVwUBVMFAVDFQFA1XBQFUwUBUMVAUDVcFAVTBQFQy/ALqDO/jJwtC4AAAAAElFTkSuQmCC";
    public static final String HORIZONTAL_LINE = "horizontal_line";
    public static String HORIZONTAL_LINE_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAALEAAABeCAIAAADjWWLFAAAACXBIWXMAAA7EAAAOxAGVKw4bAAABmElEQVR4nO3doW0qYBSG4UtyWIIhiqvBocB1ATQTsUA9IUEhmzSpahihO1Qhubb5FjhHPM8En3iT/7i/ns/nP/ijugcwjiZImiBpgqQJkiZImiBpgqQJkiZImiDV4XDo3sAsdT6fuzcwSz0ej+4NzOKeIGmCVNfrtXsDs9Rut+vewCy1XC67NzCLe4KkCZImSJogaYKkCZImSJogaYKkCZImSLXf77s3MEvdbrfuDczi7SBpglSLxaJ7A7PU5+dn9wZmqc1m072BWdwTJE2QNEHSBEkTJE2QNEHSBEkTJE2QNEGq39/f7g3MUm9vb90bmKU+Pj66NzCLe4KkCVKtVqvuDcxSl8ulewOz1Ovra/cGZnFPkDRB0gRJEyRNkDRB0gRJEyRNkDRB0gSpfn5+ujcwiz9nSfX19dW9gVncEyRNkOrl5aV7A7PU+/t79wZmqfV63b2BWdwTJE2QNEHSBEkTJE2QNEHSBEkTJE2QNEGq7+/v7g3MUsfjsXsDs9T9fu/ewCzuCZImSLXdbrs3MEudTqfuDczyH0+MLf6QCKBqAAAAAElFTkSuQmCC";
    public static final String STAIRS = "stair_pattern";
    public static String STAIRS_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAE4AAABLCAIAAABRBSb5AAAACXBIWXMAAA7DAAAOxAGILj6jAAAB00lEQVR4nO2aMarCQBRFjUw0aIiFiKXYaOUqrO3cgbVLEBsRrFLYCO7A0lUIrsHGQlAUm4gJRPzB4iP5iUZ4M/qf9xQpJsPcdzJ5U424Xq+pKAaDwXA4PJ/PvyPFYrHf73e73cj5n494dwHqgCpHoMoRsd/vI184jhN3OMvA9/0gMXjKixCTySTyxWKxcF1XXnCIw+Ewn8+32628CNHr9eStnpzdbjedTpfLpbyIb+rVdxegDqhyRFQqlYRTC4WCaZpSqwlhGEa5XKZaTYzH44RTdV2v1WpUwUkItmE0GqXTaZLVRKvVIllIBpZlNZtNql/pm3r13QWoA6ocgSpHoMoRqHIEqhyBKkegyhGocgSqHIEqR6DKEahyBKociVVdr9ebzUZZHavV6nQ6hQaFEPl8nioiVnU2m9m27XkeVdJjLpeL4zh/xzVNo4qIVXVd93g83t8c/e+gV2+7qvKKlgIe7arKi3eR+L4fnFVUJ9On/8CEn/sF1Uwm02g06vU6VfZTqtVqLpejWi1WNZvNGoZx366mabbb7U6nQ5X9FF3Xqa63pB6oajdCI0HblEolqmzFfHqvEgJVjkCVI1DlCFQ5AlWOQJUjUOUIVDkCVY5AlSM/o6l1/Y0iCLQAAAAASUVORK5CYII=";
    public static final String VERTICAL_LINE = "vertical_line";
    public static String VERTICAL_LINE_BASE64 = 
            "iVBORw0KGgoAAAANSUhEUgAAAKoAAABlCAIAAABSqu+0AAAACXBIWXMAAA7DAAAOxAGILj6jAAAB3ElEQVR4nO3RIa9BcRyH8TsTFJokaYrkFWiSbjTBJLMRZEEQBDubYooieAFGFImaqapMwPbfbrz/fp/n037b+Z5ztif9fr9/Yq7Xa7fbPRwO4axUKqvVqlwuR4en06nf7x+Px3DWarXZbFYqlaLD/X4/Go3O53M4m83meDwuFovR4Waz+T55uVzC2ev1hsNhoVCIDheLxXQ6vd1u4fy+pNPp5PP56HAymczn8/v9Hs4kSVqtVi6Xiw4Hg8FyuXw8Hr9/Xq/XM5lMdNhut9fr9fP5DOe3S7VaTaVSf69er1ej0dhut+HMZrO73S4d/Zj+MfOjmR/N/GjmRzM/mvnRzI9mfjTzo5kfzfxo5kczP5r50cyPZn4086OZH838aOZHMz+a+dHMj2Z+NPOjmR/N/GjmRzM/mvnRzI9mfjTzo5kfzfxo5kczP5r50cyPZn4086OZH838aOZHMz+a+dHMj2Z+NPOjmR/N/GjmRzM/mvnRzI9mfjTzo5kfzfxo5kczP5r50cyPZn4086OZH838aOZHMz+a+dHMj2Z+NPOjmR/N/GjmRzM/mvnRzI9mfjTzo5kfzfxo5kczP5r50cyPZn4086OZH838aOZHMz+a+dHMj2Z+NPOjmR/N/Ggf6VJU50RuCi8AAAAASUVORK5CYII=";
    public static String VERTICAL_LINE_BASE64b = "iVBORw0KGgoAAAANSUhEUgAAAKoAAABlCAIAAABSqu+0AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAI/SURBVHhe7dEhyiJgFEbhW0w/RqvRaDQa7EabGzC4BMEliFvQNYjdYjMZBJPNZjIIMjPhmzbnZRZwnvpyueHUr/9wu93G43H9NRwOL5dL26Lz+TwajdpZ1WQyuV6vbYsOh8OfL+2sajab3e/3tkW73W4wGLSzqsVi8Xg82hZtt9t+v9/Oqlar1fP5bFu0Xq97vV47q9psNq/Xq23RcrnsdrvtrGq/37/f77ZF8/m80+m0s6rj8fj9ftvGPp/PdDptN1U/Pz+n08n8jfmR+QPzI/MH5kfmD8yPzE/Mn5gfmT8wPzJ/YH5k/sD8yPzE/In5kfkD8yPzB+ZH5g/Mj8xPzJ+YH5k/MD8yf2B+ZP7A/Mj8xPyJ+ZH5A/Mj8wfmR+YPzI/MT8yfmB+ZPzA/Mn9gfmT+wPzI/MT8ifmR+QPzI/MH5kfmD8yPzE/Mn5gfmT8wPzJ/YH5k/sD8yPzE/In5kfkD8yPzB+ZH5g/Mj8xPzJ+YH5k/MD8yf2B+ZP7A/Mj8xPyJ+ZH5A/Mj8wfmR+YPzI/MT8yfmB+ZPzA/Mn9gfmT+wPzI/MT8ifmR+QPzI/MH5kfmD8yPzE/Mn5gfmT8wPzJ/YH5k/sD8yPzE/In5kfkD8yPzB+ZH5g/Mj8xPzJ+YH5k/MD8yf2B+ZP7A/Mj8xPyJ+ZH5A/Mj8wfmR+YPzI/MT8yfmB+ZPzA/Mn9gfmT+wPzI/MT8ifmR+QPzI/MH5kfmD8yPzE/Mn5gfmT8wPzJ/YH5k/sD8yPzkH/lPp98CPVuMBy3ZKAAAAABJRU5ErkJggg==";
    public static final String CIRCLES = "circles";
    public static String CIRCLES_BASE64 = 
            "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";
   
    public static void LoadTangramToolbarBitmaps(com.sun.star.uno.XComponentContext context)
    {
        /*
        Python Constant	Value	filename                list entry string
        NO_PATTERN	 0                              no pattern
        CIRCLES 	 1	circles.png             circles
        DASHED           2	dashed_lines.png	dashed lines
        DIAGONAL_1	 3	diagonal_line1.png	diagonal line 1
        DIAGONAL_2	 4	diagonal_line2.png	diagonal line 2
        DOTTED           5	dotted_pattern.png	dotted
        FULL_PATTERN	 6	full_pattern.png	full
        GRID             7	grid_pattern.png	grid
        H_LINE           8	horizontal_lines.png	horizontal line
        STAIR            9	stair_pattern.png	stairs
        V_LINE          10	vertical_lines.png	vertical line

                                TigerSwell\circles_TS.png	
                                TigerSwell\dashed_lines_TS.png	
                                TigerSwell\diagonal_line1_TS.png	
                                TigerSwell\diagonal_line2_TS.png	
                                TigerSwell\stair_pattern_TS.png	        
        */
        
        try 
        {
            Object graphicProviderObj = context.getServiceManager().createInstanceWithContext("com.sun.star.graphic.GraphicProvider", context);
            if (graphicProviderObj != null)
            {
                // get graphic provider
                com.sun.star.graphic.XGraphicProvider graphicProvider = 
                        (com.sun.star.graphic.XGraphicProvider) com.sun.star.uno.UnoRuntime.queryInterface(com.sun.star.graphic.XGraphicProvider.class, graphicProviderObj);
                
                CIRCLES_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "circles.png");
                DASHED_LINES_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "dashed_lines.png");
                DIAGONAL_LINE_1_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "diagonal_line1.png");
                DIAGONAL_LINE_2_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "diagonal_line2.png");
                DOTTED_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "dotted_pattern.png");
                FULL_PATTERN_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "full_pattern.png");
                GRID_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "grid_pattern.png");
                HORIZONTAL_LINE_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "horizontal_lines.png");
                STAIRS_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "stair_pattern.png");
                VERTICAL_LINE_BASE64 = getBase64EncodedTangramToolbarBitmap(graphicProvider, "vertical_lines.png");
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static String getBase64EncodedTangramToolbarBitmap(com.sun.star.graphic.XGraphicProvider graphicProvider, String bitmapPatternFilename)
    {
        String path = "vnd.sun.star.extension://tud.mci.tangram.PropertiesToolbar/bitmap-pattern/";
        String uri = path+bitmapPatternFilename;
        // load graphic
        com.sun.star.beans.PropertyValue[] propertyValues = new com.sun.star.beans.PropertyValue[2];
        propertyValues[0] = new com.sun.star.beans.PropertyValue();
        propertyValues[0].Name = "URL";
        propertyValues[0].Value = uri;
        propertyValues[1] = new com.sun.star.beans.PropertyValue();
        com.sun.star.graphic.XGraphic graphic = null;
        com.sun.star.beans.XPropertySet graphicProperties = null;
        try {
            graphic = graphicProvider.queryGraphic(propertyValues);
            if (graphic!=null) graphicProperties = graphicProvider.queryGraphicDescriptor(propertyValues);
        } catch (IOException ex) {
            Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (graphic!=null && graphicProperties!=null)
        {
            // get mime type
            String mimeType = null;
            try {
                mimeType = com.sun.star.uno.AnyConverter.toString(graphicProperties.getPropertyValue("MimeType"));
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (mimeType!=null)
            {
                // output to byte array
                ByteArrayXStream xTarget = new ByteArrayXStream();
                // media descriptor for output to stream
                propertyValues[0].Name = "MimeType";
                propertyValues[0].Value = mimeType;
                propertyValues[1].Name = "OutputStream"; 
                propertyValues[1].Value = xTarget;
                try {
                    // store to stream
                    graphicProvider.storeGraphic(graphic, propertyValues);
                    // Close the output and return the result
                    xTarget.closeOutput();
                    xTarget.flush(); 
                } catch (IOException ex) {
                    Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(FillBitmapAssets.class.getName()).log(Level.SEVERE, null, ex);
                }
                // read loaded byte array
                String encoded = java.util.Base64.getEncoder().encodeToString(xTarget.getBuffer());
                return encoded;
            }
        }
        return "";
    }
    
    /**
     * Defines a pattern and its properties. All measures are 100th mm values.
     */
    public static abstract class PatternDefinition
    {
        /**
         * e.g. "horizontal_line"
         */
        public final String id;
        /**
         * pattern width in 100th mm
         */
        public final float width;
        /**
         * pattern height in 100th mm
         */
        public final float height;
        /**
         * pattern units property, defaults to "userSpaceOnUse"
         */
        public final String patternUnits;
        /**
         * the xml element with attributes that represents the pattern background, defaults to a rect 100%x100%, filled white (may also be blac for FullPattern)
         */
        public final XmlElement backgroundRect;
        /**
         * a list of xml elements with attributes that define the pattern foreground (may contain circles, lines, polylines...)
         */
        public final ArrayList<XmlElement> listOfShapes;

        public PatternDefinition(String id, float width, float height, boolean blackBackground) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.patternUnits = "userSpaceOnUse";
            XmlAttributeListImpl bgAtts = new XmlAttributeListImpl();
            bgAtts.AddAttribute("x", "0");
            bgAtts.AddAttribute("y", "0");
            bgAtts.AddAttribute("width", "100%");
            bgAtts.AddAttribute("height", "100%");
            bgAtts.AddAttribute("fill", blackBackground ? "black" : "white");
            bgAtts.AddAttribute("stroke", "none");
            this.backgroundRect = new XmlElement("rect", bgAtts);
            this.listOfShapes = new ArrayList<XmlElement>();
        }
        
        public PatternDefinition(String id, float width, float height)
        {
            this(id, width, height, false);
        }
    }
    
    public static class FullPattern extends PatternDefinition
    {
        public FullPattern() 
        {
            super("full_pattern", 250, 250, true);
        } 
    }
    
    public static class FullPatternSwell extends PatternDefinition
    {
        public FullPatternSwell()
        {
            super("full_pattern", 250, 250);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("cx", "125");
            atts.AddAttribute("cy", "125");
            atts.AddAttribute("r", "60");
            this.listOfShapes.add(new XmlElement("circle", atts));
        }
    }
    
    public static class VerticalLine extends PatternDefinition
    {
        public VerticalLine()
        {
            super("vertical_line", 500, 50);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "125");
            atts.AddAttribute("y1", "-100");
            atts.AddAttribute("x2", "125");
            atts.AddAttribute("y2", "150");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class HorizontalLine extends PatternDefinition
    {
        public HorizontalLine()
        {
            super("horizontal_line", 50, 500);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "-100");
            atts.AddAttribute("y1", "125");
            atts.AddAttribute("x2", "150");
            atts.AddAttribute("y2", "125");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class CirclesPD extends PatternDefinition
    {
        public CirclesPD()
        {
            super("circles_PD", 1250, 1250);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("cx", "500");
            atts.AddAttribute("cy", "500");
            atts.AddAttribute("r", "400");
            atts.AddAttribute("fill", "none");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("circle", atts));
        }
    }
    
    public static class CirclesSpT extends PatternDefinition
    {
        public CirclesSpT()
        {
            super("circles_SP_T", 889, 889);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("cx", "362.9");
            atts.AddAttribute("cy", "362.9");
            atts.AddAttribute("r", "282");
            atts.AddAttribute("fill", "none");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "80");
            this.listOfShapes.add(new XmlElement("circle", atts));
        }
    }
    
    public static class DashedLinesPD extends PatternDefinition
    {
        public DashedLinesPD()
        {
            super("dashed_lines_PD", 750, 1000);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x", "50");
            atts.AddAttribute("y", "50");
            atts.AddAttribute("width", "400");
            atts.AddAttribute("height", "650");
            atts.AddAttribute("fill", "black");
            atts.AddAttribute("stroke", "none");
            this.listOfShapes.add(new XmlElement("rect", atts));
        }
    }
    
    public static class DashedLinesSpT extends PatternDefinition
    {
        public DashedLinesSpT()
        {
            super("dashed_lines_SP_T", 762, 762);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "125");
            atts.AddAttribute("y1", "110");
            atts.AddAttribute("x2", "125");
            atts.AddAttribute("y2", "600");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "100");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "628");
            atts.SetAttribute("x2", "628");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class GridPattern extends PatternDefinition
    {
        public GridPattern()
        {
            super("grid_pattern", 750, 750);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "375");
            atts.AddAttribute("y1", "-100");
            atts.AddAttribute("x2", "375");
            atts.AddAttribute("y2", "850");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "-100");
            atts.SetAttribute("y1", "375");
            atts.SetAttribute("x2", "850");
            atts.SetAttribute("y2", "375");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class DottedPattern extends PatternDefinition
    {
        public DottedPattern()
        {
            super("dotted_pattern", 1000, 1000);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("cx", "125");
            atts.AddAttribute("cy", "125");
            atts.AddAttribute("r", "100");
            atts.AddAttribute("fill", "black");
            atts.AddAttribute("stroke", "none");
            this.listOfShapes.add(new XmlElement("circle", atts));
            atts.SetAttribute("cx", "625");
            atts.SetAttribute("cy", "625");
            this.listOfShapes.add(new XmlElement("circle", atts));
        }
    }
    
    public static class StairPatternPD extends PatternDefinition
    {
        public StairPatternPD()
        {
            super("stair_pattern_PD", 2000, 2000);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("points", "-100,375 625,375 625,-100");
            atts.AddAttribute("fill", "none");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "180");
            this.listOfShapes.add(new XmlElement("polyline", atts));
            atts.SetAttribute("points", "625,2100 625,1375 1625,1375 1625,375 2100,375");
            this.listOfShapes.add(new XmlElement("polyline", atts));
        }
    }
    
    public static class StairPatternSpT extends PatternDefinition
    {
        public StairPatternSpT()
        {
            super("stair_pattern_SP_T", 1270, 1270);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("points", "-127,294 381,294 381,-127");
            atts.AddAttribute("fill", "none");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "100");
            this.listOfShapes.add(new XmlElement("polyline", atts));
            atts.SetAttribute("points", "381,1397 381,919 1009,919 1009,294 1394,294");
            this.listOfShapes.add(new XmlElement("polyline", atts));
        }
    }
    
    public static class DiagonalLine1PD extends PatternDefinition
    {
        public DiagonalLine1PD()
        {
            super("diagonal_line1_PD", 2000, 1000);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "250");
            atts.AddAttribute("y1", "-250");
            atts.AddAttribute("x2", "1750");
            atts.AddAttribute("y2", "1250");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "-250");
            atts.SetAttribute("y1", "250");
            atts.SetAttribute("x2", "750");
            atts.SetAttribute("y2", "1250");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "1250");
            atts.SetAttribute("y1", "-250");
            atts.SetAttribute("x2", "2250");
            atts.SetAttribute("y2", "750");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class DiagonalLine1SpT extends PatternDefinition
    {
        public DiagonalLine1SpT()
        {
            super("diagonal_line1_SP_T", 2032, 1016);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "585");
            atts.AddAttribute("y1", "-127");
            atts.AddAttribute("x2", "1855");
            atts.AddAttribute("y2", "1143");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "80");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "-177");
            atts.SetAttribute("y1", "127");
            atts.SetAttribute("x2", "839");
            atts.SetAttribute("y2", "1143");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "1601");
            atts.SetAttribute("y1", "-127");
            atts.SetAttribute("x2", "2109");
            atts.SetAttribute("y2", "381");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class DiagonalLine2PD extends PatternDefinition
    {
        public DiagonalLine2PD()
        {
            super("diagonal_line2_PD", 1500, 750);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "1250");
            atts.AddAttribute("y1", "-250");
            atts.AddAttribute("x2", "0");
            atts.AddAttribute("y2", "1000");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "130");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "500");
            atts.SetAttribute("y1", "-250");
            atts.SetAttribute("x2", "-250");
            atts.SetAttribute("y2", "500");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "1750");
            atts.SetAttribute("y1", "0");
            atts.SetAttribute("x2", "750");
            atts.SetAttribute("y2", "1000");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static class DiagonalLine2SpT extends PatternDefinition
    {
        public DiagonalLine2SpT()
        {
            super("diagonal_line2_SP_T", 1016, 508);
            XmlAttributeListImpl atts = new XmlAttributeListImpl();
            atts.AddAttribute("x1", "712");
            atts.AddAttribute("y1", "-127");
            atts.AddAttribute("x2", "-50");
            atts.AddAttribute("y2", "635");
            atts.AddAttribute("stroke", "black");
            atts.AddAttribute("stroke-width", "80");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "204");
            atts.SetAttribute("y1", "-127");
            atts.SetAttribute("x2", "-177");
            atts.SetAttribute("y2", "254");
            this.listOfShapes.add(new XmlElement("line", atts));
            atts.SetAttribute("x1", "1093");
            atts.SetAttribute("y1", "0");
            atts.SetAttribute("x2", "458");
            atts.SetAttribute("y2", "635");
            this.listOfShapes.add(new XmlElement("line", atts));
        }
    }
    
    public static enum OutputType { PinMatrixDisplay, SwellPaperPrint, TigerPrinter}    
    
    private static HashMap<String, String> patternIdsByBase64Bitmap = new HashMap<String, String>()
    {
        {
            put(NO_PATTERN_BASE64, NO_PATTERN);
            put(DASHED_LINES_BASE64, DASHED_LINES);
            put(DIAGONAL_LINE_1_BASE64, DIAGONAL_LINE_1);
            put(DIAGONAL_LINE_2_BASE64, DIAGONAL_LINE_2);
            put(DOTTED_BASE64, DOTTED);
            put(FULL_PATTERN_BASE64, FULL_PATTERN);
            put(FULL_PATTERN_BASE64b, FULL_PATTERN);
            put(GRID_BASE64, GRID);
            put(HORIZONTAL_LINE_BASE64, HORIZONTAL_LINE);
            put(STAIRS_BASE64, STAIRS);
            put(VERTICAL_LINE_BASE64, VERTICAL_LINE);
            put(VERTICAL_LINE_BASE64b, VERTICAL_LINE);
            put(CIRCLES_BASE64, CIRCLES);
        }
    };
    
    /**
     * Gets a Pattern Definition for the given bitmap (base64 String). Per default this gets the Tiger version, but pin matrix device or swell paper print can be requested if needed.
     * @param base64CodedBitmap
     * @param type PinMatrixDisplay, SwellPaperPrint, or TigerPrinter
     * @return 
     */
    private static PatternDefinition getPatternDefForBitmap(String base64CodedBitmap, OutputType type)
    {
        String patternId = patternIdsByBase64Bitmap.get(base64CodedBitmap);
        if (patternId==null || patternId.equals("")) return null;
        else if (patternId.equals(DASHED_LINES))
            return (type==OutputType.PinMatrixDisplay) ? new DashedLinesPD() : new DashedLinesSpT();
        else if (patternId.equals(DIAGONAL_LINE_1))
            return (type==OutputType.PinMatrixDisplay) ? new DiagonalLine1PD(): new DiagonalLine1SpT();
        else if (patternId.equals(DIAGONAL_LINE_2))
            return (type==OutputType.PinMatrixDisplay) ? new DiagonalLine2PD(): new DiagonalLine2SpT();
        else if (patternId.equals(DOTTED))
            return new DottedPattern();
        else if (patternId.equals(FULL_PATTERN))
            return (type==OutputType.SwellPaperPrint) ? new FullPatternSwell(): new FullPattern();
        else if (patternId.equals(GRID))
            return new GridPattern();
        else if (patternId.equals(HORIZONTAL_LINE))
            return new HorizontalLine();
        else if (patternId.equals(STAIRS))
            return (type==OutputType.PinMatrixDisplay) ? new StairPatternPD() : new StairPatternSpT();
        else if (patternId.equals(VERTICAL_LINE))
            return new VerticalLine();
        else if (patternId.equals(CIRCLES))
            return (type==OutputType.PinMatrixDisplay) ? new CirclesPD(): new CirclesSpT();
        return null;
    }
    
    /**
     * Gets a Pattern Definition for the given bitmap (base64 String). This gets the Tiger version.
     * TODO: make configurable to use other output version
     * @param base64CodedBitmap
     * @return 
     */
    public static PatternDefinition getPatternDefForBitmap(String base64CodedBitmap)
    {
        return getPatternDefForBitmap(base64CodedBitmap, OutputType.TigerPrinter);
    }
    
    /**
     * GraphicProvider neads a stream with read/write access for the method storeGraphic (Xstream rather than XOutputStream)
     * solution from http://www.oooforum.org/forum/viewtopic.phtml?t=53480#213946
     */
    public static class ByteArrayXStream implements XInputStream, XOutputStream, XSeekable, XStream { 

        // Keep data about our byte array (we read and write to the same byte array) 
        private static final int initialSize = 100240; // 10 kb 
        private int size = 0;             // The current buffer size 
        private int position = 0;         // The current write position, always<=size 
        private int readPosition = 0;     // The current read position, always<=position 
        private boolean closed = false;   // The XStream is closed 
        private byte[] buffer;            // The buffer 

        // Constructor: Initialize the byte array 

        public ByteArrayXStream() { 
            size = initialSize; 
            buffer = new byte[size]; 
        } 

        // Implementation of XOutputStream 
        @Override
        public void closeOutput() 
            throws com.sun.star.io.NotConnectedException, 
                com.sun.star.io.BufferSizeExceededException, 
                com.sun.star.io.IOException { 

            // trim buffer 
            if ( buffer.length > position) { 
                byte[] newBuffer = new byte[position]; 
                System.arraycopy(buffer, 0, newBuffer, 0, position); 
                buffer = newBuffer; 
            } 
            closed = true; 
        } 

        @Override
        public void flush() 
            throws com.sun.star.io.NotConnectedException, 
                com.sun.star.io.BufferSizeExceededException, 
                com.sun.star.io.IOException { 
        } 

        @Override
        public void writeBytes(byte[] values) 
            throws com.sun.star.io.NotConnectedException, 
                com.sun.star.io.BufferSizeExceededException, 
                com.sun.star.io.IOException { 
            if ( values.length > size-position ) { 
                byte[] newBuffer; 
                while ( values.length > size-position ) 
                    size *= 2; 
                newBuffer = new byte[size]; 
                System.arraycopy(buffer, 0, newBuffer, 0, position); 
                buffer = newBuffer; 
            } 
            System.arraycopy(values, 0, buffer, position, values.length); 
            position += values.length;    
        } 

        // Implementation of XInputStream 

        private void _check() throws com.sun.star.io.NotConnectedException, com.sun.star.io.IOException { 
            if(closed) { 
                throw new com.sun.star.io.IOException("input closed");        
            } 
        } 

        @Override
        public int available() throws com.sun.star.io.NotConnectedException, com.sun.star.io.IOException { 
            _check(); 
            return position - readPosition; 
        } 

        @Override
        public void closeInput() throws com.sun.star.io.NotConnectedException, com.sun.star.io.IOException { 
            closed = true; 
        } 

        @Override
        public int readBytes(byte[][] values, int param) throws com.sun.star.io.NotConnectedException, com.sun.star.io.BufferSizeExceededException, com.sun.star.io.IOException { 
            _check(); 
            try {                            
                int remain = (int)(position - readPosition); 
                if (param > remain) param = remain; 
                /* ARGH!!! */ 
                if (values[0] == null){                
                    values[0] = new byte[param]; 
                    // System.err.println("allocated new buffer of "+param+" bytes"); 
                } 
                System.arraycopy(buffer, readPosition, values[0], 0, param); 
                // System.err.println("readbytes() -> "+param); 
                readPosition += param; 
                return param; 
            } catch (ArrayIndexOutOfBoundsException ae) { 
                throw new com.sun.star.io.BufferSizeExceededException("buffer overflow"); 
            } 
        } 

        @Override
        public int readSomeBytes(byte[][] values, int param) throws com.sun.star.io.NotConnectedException, com.sun.star.io.BufferSizeExceededException, com.sun.star.io.IOException { 
            // System.err.println("readSomebytes()"); 
            return readBytes(values, param); 
        } 

        @Override
        public void skipBytes(int param) throws com.sun.star.io.NotConnectedException, com.sun.star.io.BufferSizeExceededException, com.sun.star.io.IOException { 
            // System.err.println("skipBytes("+param+")"); 
            _check(); 
            if (param > (position - readPosition)) 
                throw new com.sun.star.io.BufferSizeExceededException("buffer overflow"); 
            readPosition += param; 
        } 


        // Implementation of XSeekable 

        @Override
        public long getLength() throws com.sun.star.io.IOException { 
            // System.err.println("getLength() -> "+m_length); 
            if (buffer != null) return position; 
            else throw new com.sun.star.io.IOException("no bytes"); 
        } 

        @Override
        public long getPosition() throws com.sun.star.io.IOException { 
            // System.err.println("getPosition() -> "+m_pos); 
            if (buffer != null) return readPosition; 
            else throw new com.sun.star.io.IOException("no bytes");        
        } 
        
        @Override
        public void seek(long param) throws com.sun.star.lang.IllegalArgumentException, com.sun.star.io.IOException { 
            // System.err.println("seek("+param+")"); 
            if (buffer != null) { 
                if (param < 0 || param > position) throw new com.sun.star.lang.IllegalArgumentException("invalid seek position"); 
                else readPosition = (int)param; 
            } else throw new com.sun.star.io.IOException("no bytes");        
         } 

        // Implementation of XStream 
        @Override
        public XInputStream getInputStream() { return this; } 

        @Override
        public XOutputStream getOutputStream() { return this; } 

        // Get the buffer 
        public byte[] getBuffer() { return buffer; } 

    } 

}
