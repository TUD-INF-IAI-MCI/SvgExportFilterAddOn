/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import com.sun.star.chart2.XFormattedString;
import com.sun.star.xml.sax.XAttributeList;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Martin.Spindler@tu-dresden.de
 */
public class Styles {
    private static ArrayList<MasterPage> m_masterPages = new ArrayList<MasterPage>();
    private static ArrayList<MasterPageStyle> m_masterPageStyles = new ArrayList<MasterPageStyle>();
    private static ArrayList<PageStyle> m_pageStyles = new ArrayList<PageStyle>();
    private static ArrayList<GraphicsStyle> m_graphicsStyles = new ArrayList<GraphicsStyle>();
    private static ArrayList<ParagraphStyle> m_paragraphStyles = new ArrayList<ParagraphStyle>();
    private static ArrayList<TextStyle> m_textStyles = new ArrayList<TextStyle>();
    private static ArrayList<FontDeclStyle> m_fontDeclStyles = new ArrayList<FontDeclStyle>();
    private static ArrayList<StrokeDashStyle> m_strokeDashStyles = new ArrayList<StrokeDashStyle>();
    private static ArrayList<MarkerStyle> m_markerStyles = new ArrayList<MarkerStyle>();    
    private static ArrayList<CircleClipPath> m_circleClipPaths = new ArrayList<CircleClipPath>();
    private static ArrayList<FillImage> m_fillImages = new ArrayList<FillImage>();
    private static ArrayList<Pattern> m_patterns = new ArrayList<Pattern>();
    public static String DocLanguage = "en";
    public static String DocCountry = "EN";

    /**
     * needs to be called for new filter calls (e.g. in startDocument()) to clear static arrays and fields
     */
    public static void init() {
        Styles.getMasterPages().clear();
        Styles.getMasterPageStyles().clear();
        Styles.getPageStyles().clear();
        Styles.getGraphicsStyles().clear();      
        Styles.getParagraphStyles().clear();
        Styles.getTextStyles().clear();
        Styles.getFontDeclStyles().clear();
        Styles.getStrokeDashStyles().clear();
        Styles.getMarkerStyles().clear();
        Styles.getCircleClipPaths().clear();
        Styles.getFillImages().clear();
        Styles.getPatterns().clear();
        DocLanguage = "en";
        DocCountry = "EN";
    }
    
    /**
     * a named style for parsing "style:properties" attributes.
     * Abstract class, to be subclassed for implementations.
     */
    public static abstract class Style
    {
        private final String name;
        protected HashMap<String, String> properties;
        private String parentStyleName;
        
        public Style(String aName)
        {
            this (aName, null, "");
        }
        /**
         *
         * @param aName
         * @param propertyAttributes xml attributes of "style:properties" element, will be copied into hasmap with namespaces deleted
         */
        public Style(String aName, XAttributeList propertyAttributes) 
        {
            this (aName, propertyAttributes, "");
        }
        
        public Style(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            name = XmlUtils.trimToId(aName);
            parentStyleName = (aParentStyleName!=null && !aParentStyleName.isEmpty()) ? XmlUtils.trimToId(aParentStyleName) : "";
            properties = new HashMap<String, String>();
            String attributeName;
            if (propertyAttributes!=null)
            {
                for (short i=0; i < propertyAttributes.getLength(); i++)
                {
                    attributeName = propertyAttributes.getNameByIndex(i);
                    properties.put(
                            attributeName.contains(":")?attributeName.substring(attributeName.indexOf(":")+1):attributeName, 
                            propertyAttributes.getValueByIndex(i)
                    );
                }
            }
        }
        
        /**
         * @return class name of this style, all whitespace characters are removed
         * may be used to define the name of a class in css definitions
         */
        public String getName() 
        { 
            return name; 
        }
        
        public String getPropertyValue(String propertyName) 
        {
            return properties.containsKey(propertyName) ? properties.get(propertyName) : "";
        }
        
        public String getParentStyleName() { return parentStyleName; }
        
        public String getPropertyValueInclParents(String propertyName)
        {
            if (properties.containsKey(propertyName)) return properties.get(propertyName);
            else
            {
                String parentName = getParentStyleName();
                while (parentName!=null && !parentName.isEmpty())
                {
                    Styles.GraphicsStyle parent = Styles.getGraphicsStyle(parentName);
                    if (parent!=null)   // get from parent if possible
                    {
                        if (parent.properties.containsKey(propertyName))
                        {
                            return parent.properties.get(propertyName);
                        }
                        else // else iterate up to next named parent
                        {
                            parentName = parent.getParentStyleName();
                        }
                    }
                }
            }
            return "";
        }
        
        /**
         * @return gets the whole cascade of style names from the highest parent down to this style, seperated by spaces
         * this is useful to make up css class="special standard" attributes of elements in svg that implement draw styles having a parent standard style
         */
        public String getNameInclParents()
        {
            String result = getName();
            // iterate upwards and to front
            String itParentName = parentStyleName;
            while (!itParentName.isEmpty())
            {
                GraphicsStyle parentStyle = getGraphicsStyle(itParentName);
                if (parentStyle != null)
                {
                    result = itParentName + " " + result;
                    itParentName = parentStyle.getParentStyleName();
                }
                else
                {
                    return result;
                }
            }
            return result;
        }
        
        /**
         *
         * @return font size in user units (100th mm in draw => px in svg)
         */
        public float getFontSize()
        {
            // get font size:
            GraphicsStyle standardStyle = getGraphicsStyle("standard");
            String p_val_font_size = getPropertyValue("font-size");
            if (p_val_font_size.isEmpty() && standardStyle!=null) 
            {
                return new Measure(standardStyle.getPropertyValue("font-size")).getValueInMm100th();
            }
            else 
            {
                return new Measure(p_val_font_size).getValueInMm100th();
            }
        }
                       
        /**
         * OpenOffice seems to multiplay the line height with this additional 1.125 factor for % line heights
         * @return 
         */
        public float getAddMysteriuosLineHeightFactor()
        {
            float lineHeightFactor = 1.0f;
            
            // get line height (convert percentage to factor)
            GraphicsStyle standardStyle = getGraphicsStyle("standard");
            
            float fontSize = getFontSize();
            
            String p_val_line_height = getPropertyValue("line-height");
            if (p_val_line_height.isEmpty() && standardStyle!=null) p_val_line_height = standardStyle.getPropertyValue("line-height");
            
            String p_val_line_spacing = getPropertyValue("line-spacing");
            if (p_val_line_spacing.isEmpty() && standardStyle!=null) p_val_line_spacing = standardStyle.getPropertyValue("line-spacing");
                       
            try
            {
                // if line-spacing defined:
                if (!p_val_line_spacing.isEmpty())
                {
                    if (p_val_line_spacing.endsWith("%"))
                    {
                        return 1.125f;
                    }
                    else
                    {
                        return 1.0f;
                    }
                }
                // else if line-height defined:
                else if (!p_val_line_height.isEmpty())
                {
                    if (p_val_line_height.endsWith("%"))
                    {
                        return 1.125f;
                    }
                    else
                    {
                        return 1.0f;
                    }
                }
            }
            catch (NumberFormatException ex)
            {
                return fontSize;
            }
            return fontSize;
        }
        
        /**
         *
         * @param fontSize
         * @return line height, defined by font size and line spacing in user units (100th mm in draw => px in svg)
         */
        public float getLineHeight(float fontSize)
        {
            // get line height (convert percentage to factor)
            GraphicsStyle standardStyle = getGraphicsStyle("standard");
                      
            String p_val_line_height = getPropertyValue("line-height");
            if (p_val_line_height.isEmpty() && standardStyle!=null) p_val_line_height = standardStyle.getPropertyValue("line-height");
            
            String p_val_line_spacing = getPropertyValue("line-spacing");
            if (p_val_line_spacing.isEmpty() && standardStyle!=null) p_val_line_spacing = standardStyle.getPropertyValue("line-spacing");
            
            float lineHeightFactor = getAddMysteriuosLineHeightFactor();
            
            try
            {
                // if line-spacing defined:
                if (!p_val_line_spacing.isEmpty())
                {
                    if (p_val_line_spacing.endsWith("%"))
                    {
                        return fontSize * Float.parseFloat(p_val_line_spacing.replaceAll("%", ""))/100.0f;
                    }
                    else
                    {
                        return (new Measure(p_val_line_spacing)).getValueInMm100th();
                    }
                }
                // else if line-height defined:
                else if (!p_val_line_height.isEmpty())
                {
                    if (p_val_line_height.endsWith("%"))
                    {
                        return fontSize * Float.parseFloat(p_val_line_height.replaceAll("%", ""))/100.0f * lineHeightFactor;
                    }
                    else
                    {
                        return (new Measure(p_val_line_height)).getValueInMm100th();
                    }
                }
            }
            catch (NumberFormatException ex)
            {
                return fontSize;
            }
            return fontSize;
        }
        
        private String makeCssPropertyLine(String property, String value)
        {
            return "\t\t" + property + ": " + value + ";" + "\r\n";
        }
        
        public String getCssDefinition()
        {
            String result = "";
            
            String fontsCssDefLines = getFontsCssDefinitionLines();
            
            // --- START CSS CLASS ---------------------------------------------
            
            result += "\t" + "." + getName() + " " + "{" + "\r\n";
            
            // --- stroke ------------------------------------------------------
            boolean dontStroke = false;
            String p_val_stroke = getPropertyValue("stroke");
            if (!p_val_stroke.isEmpty())
            {
                if (p_val_stroke.equals("none"))
                {
                    result += makeCssPropertyLine("stroke","none");
                    dontStroke = true;
                }
                // dashed lines
                else if (p_val_stroke.equals("dash"))
                {
                    String p_val_stroke_dash = getPropertyValue("stroke-dash");
                    if (!p_val_stroke_dash.isEmpty())
                    {
                        // try to get the matching style definition (if loaded)
                        StrokeDashStyle strokeDashStyle = Styles.getStrokeDashStyle(p_val_stroke_dash);
                        if (strokeDashStyle!=null)
                        {
                            String cssDashArray = strokeDashStyle.getSvgStyleDashArrayValue();
                            if (!cssDashArray.isEmpty())
                            {
                                // set svg dasharray
                                result += makeCssPropertyLine("stroke-dasharray", cssDashArray);
                                // eventually set round linecap
                                if (strokeDashStyle.isRoundStyle())
                                {
                                    result += makeCssPropertyLine("stroke-linecap", "round");
                                }
                            }
                        }
                    }
                }
                // solid lines (default in svg)
                else if (p_val_stroke.equals("solid")) 
                { 
                }
            }
            // stroke color
            String p_val_stroke_color = getPropertyValue("stroke-color");
            if (!p_val_stroke_color.isEmpty() && !dontStroke)
            {
                result += makeCssPropertyLine("stroke",p_val_stroke_color);
            }
            // stroke opacity
            String p_val_stroke_opacity = getPropertyValue("stroke-opacity");
            if (!p_val_stroke_opacity.isEmpty() && !dontStroke)
            {
                float opacity;
                if (p_val_stroke_opacity.endsWith("%"))
                {
                    opacity =  1.0f - Float.parseFloat(p_val_stroke_opacity.substring(0, p_val_stroke_opacity.length()-1))/100.0f;
                }
                else
                {
                    opacity =  1.0f - Float.parseFloat(p_val_stroke_opacity);
                }
                result += makeCssPropertyLine("stroke-opacity",Float.toString(opacity));
            }
            // stroke width
            String p_val_stroke_width = getPropertyValue("stroke-width");
            if (!p_val_stroke_width.isEmpty() && !dontStroke)
            {
                Measure strokeWidth = new Measure(p_val_stroke_width);
                // convert hairlines to 0.25pt
                if (strokeWidth.getOriginalValue()==0.0f) strokeWidth = new Measure("0.25pt");
                result += makeCssPropertyLine("stroke-width",Float.toString(strokeWidth.getValueInMm100th()));
                
            }
            // stroke linejoin
            String p_val_stroke_linejoin = getPropertyValue("stroke-linejoin");
            if (!p_val_stroke_linejoin.isEmpty() && !dontStroke)
            {
                result += makeCssPropertyLine("stroke-linejoin",p_val_stroke_linejoin);
            }
            // stroke linecap
            String p_val_stroke_linecap = getPropertyValue("stroke-linecap");
            if (!p_val_stroke_linecap.isEmpty() && !dontStroke)
            {
                result += makeCssPropertyLine("stroke-linecap",p_val_stroke_linecap);
            }
            // markers
            String p_val_marker_start = getPropertyValue("marker-start");
            if (!p_val_marker_start.isEmpty())
            {
                MarkerStyle markerStyle = Styles.getMarkerStyle(p_val_marker_start);
                if (markerStyle!=null)
                {
                    result += makeCssPropertyLine("marker-start","url(#"+markerStyle.getName()+")");
                }
            }
            String p_val_marker_end = getPropertyValue("marker-end");
            if (!p_val_marker_end.isEmpty())
            {
                MarkerStyle markerStyle = Styles.getMarkerStyle(p_val_marker_end);
                if (markerStyle!=null)
                {
                    result += makeCssPropertyLine("marker-end","url(#"+markerStyle.getName()+")");
                }
            }
            String p_val_clip_name = getPropertyValue("mask");
            if (!p_val_clip_name.isEmpty())
            {
                result += makeCssPropertyLine("mask",p_val_clip_name);
            }
            
            // --- fill --------------------------------------------------------
            String p_val_fill = getPropertyValue("fill");
            boolean dontFill = false;
            if (!p_val_fill.isEmpty())
            {
                if (p_val_fill.equals("none"))
                {
                     result += makeCssPropertyLine("fill","none");
                     dontFill = true;
                }
                else if (p_val_fill.equals("solid")) 
                {
                }
            }
            // fill color
            String p_val_fill_color = getPropertyValue("fill-color");
            if (!p_val_fill_color.isEmpty() && !dontFill && !p_val_fill.equals("bitmap"))
            {
                result += makeCssPropertyLine("fill",p_val_fill_color);
            }
            // fill-rule
            String p_val_fill_rule = getPropertyValue("fill-rule");
            if (!p_val_fill_rule.isEmpty())
            {
                result += makeCssPropertyLine("fill-rule",p_val_fill_rule);
            }
            // image fill
            if (p_val_fill.startsWith("url(#"))
            {
                result += makeCssPropertyLine("fill", p_val_fill);
            }
            // fill transparency
            String p_val_fill_opacity = getPropertyValue("transparency");
            if (!p_val_fill_opacity.isEmpty() && !dontFill)
            {
                float opacity;
                if (p_val_fill_opacity.endsWith("%"))
                {
                    opacity = 1.0f - Float.parseFloat(p_val_fill_opacity.substring(0, p_val_fill_opacity.length()-1))/100.0f;
                }
                else
                {
                    opacity = 1.0f - Float.parseFloat(p_val_fill_opacity);
                }
                result += makeCssPropertyLine("fill-opacity",Float.toString(opacity));
            }
            // opacity
            String p_val_opactiy = getPropertyValue("opacity");
            if (!p_val_opactiy.isEmpty())
            {
                result += makeCssPropertyLine("opacity",p_val_opactiy);
            }
            
            // insert font definition lines, see getFontsCssDefinitionLines
            result += fontsCssDefLines;
            
            // text-align, e.g. "center"
            String p_val_text_align = getPropertyValue("text-align");
            if (!p_val_text_align.isEmpty())
            {
                result += makeCssPropertyLine("text-align",p_val_text_align);
            }
                    
            // --- END OF CSS CLASS --------------------------------------------
            result += "\t}\r\n\r\n";
            
            // print version for computer braille font (else ascii version)
            if (fontsCssDefLines.contains("font-family:"))
            {
                int startFamilyIdx = fontsCssDefLines.lastIndexOf("font-family:")+12;
                int afterFamilyIdx = fontsCssDefLines.substring(startFamilyIdx).indexOf(';')+startFamilyIdx;
                String fontFamilyName = fontsCssDefLines.substring(startFamilyIdx,afterFamilyIdx);
                if (fontFamilyName.contains("Braille DE Computer ASCII"))
                {
                    String newFontFamilyName = fontFamilyName.replace("Braille DE Computer ASCII", "Braille DE Computer");
                    result += "\t @media print, embossed, braille { " + "." + getName() + " " + "{" + "\r\n";
                    result += "\t\tfont-family:"+newFontFamilyName+";";
                    result += "\t}}\r\n\r\n";
                }
                else if (fontFamilyName.contains("Braille29 DE ASCII")||fontFamilyName.contains("Braille29 ASCII"))
                {
                    String newFontFamilyName = fontFamilyName.replace("Braille29 DE ASCII", "Braille29 DE").replace("Braille29 ASCII", "Braille29 DE");
                    result += "\t @media print, embossed, braille { " + "." + getName() + " " + "{" + "\r\n";
                    result += "\t\tfont-family:"+newFontFamilyName+";";
                    result += "\t}}\r\n\r\n";
                }
            }
            
            return result;
        }
        
        public String getFontsCssDefinitionLines()
        {
            String result = "";
            // font-name
            String p_val_font_name = getPropertyValue("font-name");
            if (!p_val_font_name.isEmpty())
            {
                // get family from declared font
                Styles.FontDeclStyle fontDecl = Styles.getFontDeclStyle(p_val_font_name);

                String family = fontDecl.getPropertyValue("font-family");
                String genericFamily = fontDecl.getPropertyValue("font-family-generic");
                if (fontDecl.getPropertyValue("font-pitch").equals("fixed")) genericFamily="monospace";
                else if (genericFamily.equals("decorative")) genericFamily = "fantasy";
                else if (genericFamily.equals("roman")) genericFamily = "serif";
                else if (genericFamily.equals("script")) genericFamily = "cursive";
                else genericFamily = "sans-serif";

                result += makeCssPropertyLine("font-family",family + ((family.isEmpty()||genericFamily.isEmpty())?"":", ") + genericFamily);
            }            
            // font-size
            String p_val_font_size = getPropertyValue("font-size");
            if (!p_val_font_size.isEmpty())
            {
                result += makeCssPropertyLine("font-size",Float.toString(new Measure(p_val_font_size).getValueInMm100th())+"px");
            }
            // font-weight, e.g. "bold" or "400"
            String p_val_font_weight = getPropertyValue("font-weight");
            if (!p_val_font_weight.isEmpty())
            {
                result += makeCssPropertyLine("font-weight",p_val_font_weight);
            }
            // font-style, e.g. "italic"
            String p_val_font_style = getPropertyValue("font-style");
            if (!p_val_font_style.isEmpty())
            {
                result += makeCssPropertyLine("font-style",p_val_font_style);
            }
            // font color and outline
            boolean textOutline = getPropertyValue("text-outline").equals("true");
            String p_val_text_color = getPropertyValue("color");
            if (textOutline)
            {
                result += makeCssPropertyLine("stroke", p_val_text_color);
                result += makeCssPropertyLine("stroke-width",Float.toString(new Measure("0.25pt").getValueInMm100th()));
                result += makeCssPropertyLine("fill", "#FFFFFF");
            }
            else if (!p_val_text_color.isEmpty())
            {
                result += makeCssPropertyLine("stroke", "none");
                result += makeCssPropertyLine("fill", p_val_text_color);
            }
            // text decoration
            String p_val_text_underline = getPropertyValue("text-underline");
            String p_val_text_overline_style = getPropertyValue("text-overline-style");
            String p_val_text_crossing_out = getPropertyValue("text-crossing-out");
            if (!p_val_text_underline.isEmpty()||!p_val_text_overline_style.isEmpty()||!p_val_text_crossing_out.isEmpty())
            {
                String decoration = "";
                if (!p_val_text_underline.isEmpty()&&!p_val_text_underline.equals("none"))
                {
                    decoration+="underline";
                }
                if (!p_val_text_overline_style.isEmpty()&&!p_val_text_overline_style.equals("none"))
                {
                    if (decoration.length()>0) decoration+=" ";
                    decoration+="overline";
                }
                if (!p_val_text_crossing_out.isEmpty()&&!p_val_text_crossing_out.equals("none"))
                {
                    if (decoration.length()>0) decoration+=" ";
                    decoration+="line-through";
                }
                result += makeCssPropertyLine("text-decoration", (decoration.isEmpty())?"none":decoration);
            }
            // text shadow
            String p_val_text_shadow = getPropertyValue("text-shadow");
            if (!p_val_text_shadow.isEmpty())
            {
                if (p_val_text_shadow.equals("none")||p_val_text_shadow.equals("initial")||p_val_text_shadow.equals("inherit"))
                {
                    result += makeCssPropertyLine("text-shadow",p_val_text_shadow);
                }
                else
                {
                    String shadow = "";
                    String[] shadowProps = p_val_text_shadow.split("\\s+");
                    // h-shadow
                    if (shadowProps.length>0) 
                    {
                        shadow += shadowProps[0];
                        // v-shadow
                        if (shadowProps.length>1)
                        {
                            shadow += " " + shadowProps[1];
                            // blur radius
                            if (shadowProps.length>2)
                            {
                                shadow += " " + shadowProps[2];
                                // color|none|initial|inherit
                                if (shadowProps.length>3)
                                {
                                    shadow += " " + shadowProps[3];
                                }
                                else shadow += " black";
                            }
                            else
                            {
                                shadow += " " + shadowProps[1]+ " black";
                            }
                        }
                        result += makeCssPropertyLine("text-shadow",shadow);
                    }
                }
            }
            // text-position
            String p_val_text_position = getPropertyValue("text-position");
            if (!p_val_text_position.isEmpty())
            {
                // get font size for % calculations:
                float fontSize = getFontSize();
                               
                String[] textPosProps = p_val_text_position.split("\\s+");
                if (textPosProps.length>0)
                {
                    String baseLineShift = textPosProps[0];
                    if (baseLineShift.equals("super")) baseLineShift="33%";
                    else if (baseLineShift.equals("sub")) baseLineShift="-33%";
                    if (!baseLineShift.isEmpty()) 
                    {
                        try
                        {
                            float baselinePercent = Float.parseFloat(baseLineShift.replaceAll("%", ""))/100.0f;
                            result+=makeCssPropertyLine("baseline-shift",baselinePercent*fontSize+"px");
                        }
                        catch (NumberFormatException ex)
                        {
                        }
                    }
                    if (textPosProps.length>1)
                    {
                        String textResize = textPosProps[1];
                        try
                        {
                            float fontPercent = Float.parseFloat(textResize.replaceAll("%", ""))/100.0f;
                            result += makeCssPropertyLine("font-size",fontPercent*fontSize+"px");
                        }
                        catch (NumberFormatException ex)
                        {    
                        }
                    }
                }
            }
            // letter-spacing
            String p_val_letter_spacing = getPropertyValue("letter-spacing");
            if (!p_val_letter_spacing.isEmpty())
            {
                result += makeCssPropertyLine("letter-spacing",Float.toString(new Measure(p_val_letter_spacing).getValueInMm100th())+"px");
            }
            // word-spacing
            String p_val_word_spacing = getPropertyValue("word-spacing");
            if (!p_val_word_spacing.isEmpty())
            {
                result += makeCssPropertyLine("word-spacing",Float.toString(new Measure(p_val_word_spacing).getValueInMm100th())+"px");
            }
            
            return result;
        }
    }
    
    public static class PageStyle extends Style {
              
        public PageStyle(String aName)
        {
            super(aName);
        }
        
        public PageStyle(String aName, XAttributeList propertyAttributes) 
        {
            super (aName, propertyAttributes);
        }
        
        public PageStyle(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            super(aName, propertyAttributes, aParentStyleName);
        }
    }
    
    /**
     * has a name, holds style information of master pages size, margins and print orientation
     */
    public static class MasterPageStyle extends Style{
               
        public MasterPageStyle(String aName) 
        {
            super(aName);
        }
        
        public MasterPageStyle(String aName, XAttributeList propertyAttributes) 
        {
            super(aName, propertyAttributes);
        }
        
        public MasterPageStyle(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            super(aName, propertyAttributes, aParentStyleName);
        }
        
        public Measure getWidth() { return new Measure(this.getPropertyValue("page-width")); }
        public Measure getHeight() { return new Measure(this.getPropertyValue("page-height")); }
        public Measure getTop() { return new Measure(this.getPropertyValue("margin-top")); }
        public Measure getBottom() { return new Measure(this.getPropertyValue("margin-bottom")); }
        public Measure getLeft() { return new Measure(this.getPropertyValue("margin-left")); }
        public Measure getRight() { return new Measure(this.getPropertyValue("margin-right")); }
        public boolean isLandscape() { return (this.getPropertyValue("print-orientation").equals("landscape")); }
    }

    /**
     * has a name, holds MasterPageStyle and PageStyle information
     */
    public static class MasterPage
    {
        private final String name;
        private MasterPageStyle masterPageStyle;
        private PageStyle pageStyle;
        
        /**
         * 
         * @param aName
         * @param masterPageStyleName if a MasterPageStyle with this name is already kept by styles, this will be registered
         * @param pageStyleName if a PageStyle with this name is already kept by styles, this will be registered
         */
        public MasterPage(String aName, String masterPageStyleName, String pageStyleName) 
        {
            name = aName;
            masterPageStyle = null;
            pageStyle = null;
            
            // look for masterPageStyleName to find masterPageStyle
            MasterPageStyle tmpMasterPageStyle = Styles.getMasterPageStyle(masterPageStyleName);
            if (tmpMasterPageStyle!=null) masterPageStyle = tmpMasterPageStyle;
            
            // look for pageStyleName to find pageStyle
            PageStyle tmpPageStyle = Styles.getPageStyle(pageStyleName);
            if (tmpPageStyle!=null) pageStyle = tmpPageStyle;
        }
        
        public String getName() { return name; }
        public MasterPageStyle getMasterPageStyle() { return masterPageStyle; }
        public PageStyle getPageStyle() { return pageStyle; }
    }
    
    public static class GraphicsStyle extends Style {
      
        private Pattern fillPattern;
        
        public GraphicsStyle(String aName)
        {
            super(aName);
            fillPattern = null;
        }
              
        public GraphicsStyle(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            super(aName, propertyAttributes, aParentStyleName);
            fillPattern = null;
            // some standard style defs
            if (aName.equals("standard"))
            {
                properties.put("stroke-linejoin", "round");
                properties.put("fill-rule", "evenodd");
            }
            
            // if markers are used, create instances with style and size defined by this style
            String p_val_marker_start = getPropertyValue("marker-start");
            if (!p_val_marker_start.isEmpty())
            {
                MarkerStyle originMarkerStyle = Styles.getMarkerStyle(p_val_marker_start);
                if (originMarkerStyle!=null)
                {
                    // make new start marker instance
                    MarkerStyle startMarkerInstance = new MarkerStyle(originMarkerStyle.getName()+"_"+this.getName()+"_start");
                    // copy properties to new start marker
                    startMarkerInstance.copyPropertiesFrom(originMarkerStyle);
                    
                    // add some more properties
                    // set refX and refY for marker center point
                    startMarkerInstance.setReferencePoint(getPropertyValue("marker-start-center"),false);
                    // styling
                    String strokeColorString = getPropertyValueInclParents("stroke-color");
                    if (!strokeColorString.isEmpty()) startMarkerInstance.properties.put("stroke", strokeColorString);
                    // width
                    String markerWidthString = getPropertyValueInclParents("marker-start-width");
                    if (!markerWidthString.isEmpty()) startMarkerInstance.properties.put("markerWidth",markerWidthString);
                    
                    // add to marker style collection
                    m_markerStyles.add(startMarkerInstance);
                    // set usage of this start marker to current graphics style
                    properties.replace("marker-start",startMarkerInstance.getName());
                }
            }
            String p_val_marker_end = getPropertyValue("marker-end");
            if (!p_val_marker_end.isEmpty())
            {
                MarkerStyle originMarkerStyle = Styles.getMarkerStyle(p_val_marker_end);
                if (originMarkerStyle!=null)
                {
                    // make new end marker instance
                    MarkerStyle endMarkerInstance = new MarkerStyle(originMarkerStyle.getName()+"_"+this.getName()+"_end");
                    // copy properties from original marker
                    endMarkerInstance.copyPropertiesFrom(originMarkerStyle);
                    
                    // add some more properties
                    // set refX and refY for marker center point
                    endMarkerInstance.setReferencePoint(getPropertyValue("marker-end-center"),true);
                    String strokeColorString = getPropertyValueInclParents("stroke-color");
                    if (!strokeColorString.isEmpty()) endMarkerInstance.properties.put("stroke", strokeColorString);
                    // width
                    String markerWidthString = getPropertyValueInclParents("marker-end-width");
                    if (!markerWidthString.isEmpty()) endMarkerInstance.properties.put("markerWidth",markerWidthString);
                    
                    // add to marker style collection
                    m_markerStyles.add(endMarkerInstance);
                    // set usage of this end marker to current graphics style
                    properties.replace("marker-end",endMarkerInstance.getName());
                }
            }
            
            // if fill patterns are used, create custom instances
            String p_val_fill = getPropertyValue("fill");
            String p_val_fill_image_name = getPropertyValue("fill-image-name");
            String p_val_fill_image_width = getPropertyValue("fill-image-width");
            String p_val_fill_image_height = getPropertyValue("fill-image-height");
            String p_val_fill_image_x = getPropertyValue("fill-image-ref-point-x");
            String p_val_fill_image_y = getPropertyValue("fill-image-ref-point-y");
            if (p_val_fill.equals("bitmap")&&!p_val_fill_image_name.isEmpty())
            {
                String newPatternName = getName()+"_"+XmlUtils.trimToId(p_val_fill_image_name)+"_pattern";
                Pattern newPattern = new Pattern(newPatternName);
                newPattern.setFillImage(p_val_fill_image_name);
                if (!p_val_fill_image_width.isEmpty())
                {
                    // keep %, but change measures to 100th mm
                    if (!p_val_fill_image_width.endsWith("%"))
                    {
                        float value = (new Measure(p_val_fill_image_width).getValueInMm100th());
                        if (value!=0.0f) newPattern.setWidthString(Float.toString(value));
                    }
                    else newPattern.setWidthString(p_val_fill_image_width);
                }
                if (!p_val_fill_image_height.isEmpty())
                {
                    // keep %, but change measures to 100th mm
                    if (!p_val_fill_image_height.endsWith("%"))
                    {
                        float value = (new Measure(p_val_fill_image_height).getValueInMm100th());
                        if (value!=0.0f) newPattern.setHeightString(Float.toString(value));
                    }
                    else newPattern.setWidthString(p_val_fill_image_height);
                }
                if (!p_val_fill_image_x.isEmpty())
                {
                    // keep %, but change measures to 100th mm
                    if (!p_val_fill_image_x.endsWith("%"))
                    {
                        float value = (new Measure(p_val_fill_image_x).getValueInMm100th());
                        if (value!=0.0f) newPattern.setXString(Float.toString(value));
                    }
                    else 
                    {
                        float perc = new Measure(p_val_fill_image_x.replace('%', ' ').trim()).getValueInMm100th()/100.0f;
                        if (getFillImage(p_val_fill_image_name)!=null) newPattern.setXString(Float.toString(getFillImage(p_val_fill_image_name).get100thMMWidth()*perc));
                        else newPattern.setXString(p_val_fill_image_x);
                    }
                }
                if (!p_val_fill_image_y.isEmpty())
                {
                    // keep %, but change measures to 100th mm
                    if (!p_val_fill_image_y.endsWith("%"))
                    {
                        float value = (new Measure(p_val_fill_image_y).getValueInMm100th());
                        if (value!=0.0f) newPattern.setYString(Float.toString(value));
                    }
                    else 
                    {
                        float perc = new Measure(p_val_fill_image_y.replace('%', ' ').trim()).getValueInMm100th()/100.0f;
                        if (getFillImage(p_val_fill_image_name)!=null) newPattern.setYString(Float.toString(getFillImage(p_val_fill_image_name).get100thMMHeight()*perc));
                        else newPattern.setYString(p_val_fill_image_y);
                    }
                }
                m_patterns.add(newPattern);
                properties.remove("fill");
                properties.put("fill","url(#"+newPatternName+")");
                this.fillPattern = newPattern;
            }
        }
        
        public Pattern getPattern() { return fillPattern; }
    }
    
    public static class ParagraphStyle extends Style {
   
        public ParagraphStyle(String aName)
        {
            super(aName);
        }
        
        public ParagraphStyle(String aName, XAttributeList propertyAttributes) 
        {
            super(aName, propertyAttributes);
        }
        
        public ParagraphStyle(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            super(aName, propertyAttributes, aParentStyleName);
        }
    }
    
    public static class TextStyle extends Style {
        public TextStyle(String aName)
        {
            super(aName);
        }
        
        public TextStyle(String aName, XAttributeList propertyAttributes) 
        {
            super(aName, propertyAttributes);
        }
        
        public TextStyle(String aName, XAttributeList propertyAttributes, String aParentStyleName) 
        {
            super(aName, propertyAttributes, aParentStyleName);
        }
    }
    
    public static class FontDeclStyle extends Style {
        public FontDeclStyle(String aName)
        {
            super(aName);
        }
        
        public FontDeclStyle(String aName, XAttributeList propertyAttributes) 
        {
            super(aName, propertyAttributes);
        }
    }
    
    public static class StrokeDashStyle extends Style {

        public StrokeDashStyle(String aName)
        {
            super(aName);
        }
        
        public StrokeDashStyle(String aName, XAttributeList propertyAttributes)
        {
            super(aName, propertyAttributes);
        }
        
        public String getSvgStyleDashArrayValue()
        {
            String result = "";
            String dots1 = getPropertyValue("dots1");
            String dots1Length = getPropertyValue("dots1-length");
            String dots2 = getPropertyValue("dots2");
            String dots2Length = getPropertyValue("dots2-length");
            String distance = getPropertyValue("distance");
            
            float valueDistance = (new Measure(distance)).getValueInMm100th();
            
            if (!dots1.isEmpty() && !dots1Length.isEmpty())
            {
                int numDots1 = Integer.parseInt(dots1);
                float valueDots1Length = (new Measure(dots1Length)).getValueInMm100th();
                for (int i=0; i < numDots1; i++)
                {
                    result += valueDots1Length + " " + valueDistance + " ";
                }
            }
            
            if (!dots2.isEmpty() && !dots2Length.isEmpty())
            {
                int numDots2 = Integer.parseInt(dots2);
                float valueDots2Length = (new Measure(dots2Length)).getValueInMm100th();
                for (int i=0; i < numDots2; i++)
                {
                    result += valueDots2Length + " " + valueDistance + " ";
                }
            }
            
            return result.trim();
        }
        
        /**
         * This should be used to generate svg style stroke-linecap: round, else stroke-linecap: butt.
         * @return true, if draw:style="round", else the default ("rect") should be assumed
         */
        public boolean isRoundStyle()
        {
            return getPropertyValue("style").equals("round");
        }
    }
    
    /* TODO: collect markers while parsing OoDraw XML as "marker template" defs
        later, when generating the svg create "marker instance" defs, for everey 
        intanced usage by shape elements, thes are to be scaled and styled right!
        output marker size is defined in oo draw shape element that uses it, color fill is also applied to marker) 
    */
    public static class MarkerStyle extends Style 
    {
        boolean end=false;
        private Rectangle viewBox;
        
        public MarkerStyle(String aName)
        {
            super(aName);
            viewBox = new Rectangle();
            initReferencePoint();
        }
        
        public MarkerStyle(String aName, XAttributeList propertyAttributes)
        {
            super(aName, propertyAttributes);
            // remove the "name" property, so that simly all properties can be output later for svg marker def generation, see getPropertiesAsAttributes()
            String name_prop_val = getPropertyValue("name");
            if (!name_prop_val.isEmpty())
            {
                this.properties.remove("name");
            }
            viewBox = new Rectangle(getPropertyValue("viewBox"));
            initReferencePoint();
        }
        
        /**
         * calc default refX and refY properties if not already defined but viewBox porperty is already set
         * does nothing if viewBox is missing
         */
        private void initReferencePoint()
        {
            if (!properties.keySet().contains("refX"))
            {
                 properties.put("refX", Float.toString(0.5f*viewBox.width));
            }
            if (!properties.keySet().contains("refY"))
            {
                properties.put("refY", Float.toString(0.3f*viewBox.height));
            }
        }
        
        /**
         * sets refX / refY by Oo draw marker-start-center or marker-end-center position
         * @param markerStartOrEndCenter: default or "false" is 0.3, "true" is 0.5, else value from 0.0 to 1.0
         * @param end: if true, reference point for an end marker, else for a start marker
         */
        public void setReferencePoint(String markerStartOrEndCenter, boolean end)
        {
            this.end = end;
            float centerX = 0.5f * viewBox.width;
            float fractionY = 0.3f;
            if (markerStartOrEndCenter == null || markerStartOrEndCenter.isEmpty() || markerStartOrEndCenter.equals("false")) 
            {
                fractionY = 0.0f;
            }
            else if (markerStartOrEndCenter.equals("true")) 
            {
                fractionY = 0.5f;
            }
            else
            {
                try 
                { 
                    fractionY = Float.parseFloat(markerStartOrEndCenter); 
                }
                catch(NumberFormatException ex) { }
            }
            float centerY = fractionY * viewBox.height;
            properties.put("refX",Float.toString(centerX));
            properties.put("refY",Float.toString(centerY));                    
        }
        
        /**
         * copies (or overwrites existing) properties from other style
         * @param other
         */
        public void copyPropertiesFrom(MarkerStyle other)
        {
            for (String key : other.properties.keySet())
            {
                if (this.properties.containsKey(key)) this.properties.replace(key, other.properties.get(key));
                else this.properties.put(key, other.properties.get(key));
            }
            viewBox = new Rectangle(getPropertyValue("viewBox"));
            initReferencePoint();
        }
        
        public Rectangle getViewBox()
        {
            return viewBox;
        }
        
        /**
         * calculates the width that curresponds to draw height
         * height and witdh are switched in svg, compared to draw, because of marker rotation
         * the ration of the viewBox is used
         * @return
         */
        public float getSvgMarkerWidth()
        {
            return getSvgMarkerHeight() * viewBox.height / viewBox.width;
        }
        
        /**
         * gets the original draw width
         * height and witdh are switched in svg, compared to draw, because of marker rotation
         * @return
         */
        public float getSvgMarkerHeight()
        {
            return (new Measure(getPropertyValue("markerWidth"))).getValueInMm100th();
        }
        
        /**
         * gets all properties as attibutes, except the path, which must be converted to a child element in svg
         * also cares for 90° rotation, converting oo draw to svg
         * @return
         */
        public XAttributeList getPropertiesAsAttributes()
        {         
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getName());
            for (String propertyName : properties.keySet())
            {
                if (!propertyName.equals("viewBox") &&
                    !propertyName.equals("d") && !propertyName.equals("markerWidth") && !propertyName.equals("stroke"))
                {
                    attributes.AddAttribute(propertyName, properties.get(propertyName));
                }
            }
            // seems to be the default in openoffice, but not in Svg
            attributes.AddAttribute("orient", "auto");
                        
            if (properties.containsKey("markerWidth"))
            {
                float markerWidth = new Measure(getPropertyValue("markerWidth")).getValueInMm100th();
                // calculate respective height (keeping aspect ratio)
                float markerHeight = markerWidth * viewBox.height / viewBox.width;
                attributes.AddAttribute("markerUnits", "userSpaceOnUse");
                
                attributes.AddAttribute("markerWidth", Float.toString(markerHeight));
                attributes.AddAttribute("markerHeight", Float.toString(markerWidth));
                
                // create rotated viewbox:
                float refX = new Measure(properties.get("refX")).getValueInMm100th();
                float refY = new Measure(properties.get("refY")).getValueInMm100th();

                // rotate viewBox
                Transformation viewBoxRotation;
                if (end)
                {
                    viewBoxRotation = Transformation.Rotate90Right(refX, refY);
                }
                else
                {
                    viewBoxRotation = Transformation.Rotate90Left(refX, refY);
                }
                Rectangle transBox = viewBoxRotation.getTransformedRectangle(viewBox);
                
                attributes.AddAttribute("viewBox",transBox.x+" "+transBox.y+" "+transBox.width+" "+transBox.height);
                
            }
            // style
            String style = "";
            String strokeString = getPropertyValue("stroke");
            if (!strokeString.isEmpty()) style += "fill:"+strokeString+"; ";
            style = style.trim();
            attributes.AddAttribute("fill-rule","evenodd");
            if (!style.isEmpty()) attributes.AddAttribute("style", style);
            
            return attributes;
        }
        
        /**
         * gets an attributeslist, just containing the d attribute of for the path and the 90° rotation that is needed to convert from OoDraw to SVG marker paths
         * also cares for 90° rotation, converting oo draw to svg
         * @return
         */
        public XAttributeList getPathAttributes()
        {
            float refX = new Measure(properties.get("refX")).getValueInMm100th();
            float refY = new Measure(properties.get("refY")).getValueInMm100th();
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("d", properties.get("d"));
            // center for inkscape rotation, but switched beacaus of 90° rotation from oo draw to svg!
            attributes.AddAttribute("inkscape:transform-center-x",Float.toString((viewBox.height / 2.0f) - refY));
            attributes.AddAttribute("inkscape:transform-center-y",Float.toString((viewBox.width / 2.0f) - refX));
            attributes.AddAttribute("transform", "rotate(" + ( end ? 90 : -90 ) + " "+refX+" "+refY+")");
            return attributes;
        }
    }
    
    public static ArrayList<CircleClipPath> getCircleClipPaths()
    {
        return m_circleClipPaths;
    }
    
    public static CircleClipPath getCircleClipPath(String circleClipPathName)
    {
        String trimmedName = XmlUtils.trimToId(circleClipPathName);
        for (CircleClipPath _circleClipPath : m_circleClipPaths)
        {
            if (_circleClipPath.getName().equals(trimmedName))
            {
                return _circleClipPath;
            }
        }
        return null;
    }
    
    public static class CircleClipPath extends Style
    {
        
        float x_s;
        float y_s;
        float r_s;
        
        float x_e;
        float y_e;
        float r_e;
        
        public CircleClipPath(String aName) {
            super(aName);
            x_s = 0.0f;
            y_s = 0.0f;
            r_s = 0.0f;
            x_e = 0.0f;
            y_e = 0.0f;
            r_e = 0.0f;
        }
        
        public CircleClipPath(String aName, XAttributeList propertyAttributes)
        {
            super(aName, propertyAttributes);
            x_s = 0.0f;
            y_s = 0.0f;
            r_s = 0.0f;
            x_e = 0.0f;
            y_e = 0.0f;
            r_e = 0.0f;
        }

        XAttributeList getPropertiesAsAttributes() {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getName());
            return attributes;
        }
        
        XAttributeList getStartCircleAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("cx", Float.toString(x_s));
            attributes.AddAttribute("cy", Float.toString(y_s));
            attributes.AddAttribute("r", Float.toString(r_s));
            return attributes;
        }
        
        XAttributeList getEndCircleAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("cx", Float.toString(x_e));
            attributes.AddAttribute("cy", Float.toString(y_e));
            attributes.AddAttribute("r", Float.toString(r_e));
            return attributes;
        }
    }
    
    public static class FillImage extends Style
    {
        private final String m_base64ImageData;
        private final int m_pixelWidth;
        private final int m_pixelHeight;
        private final int m_100thMmWidth;
        private final int m_100thMmHeight;
        private static final int DPI = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        private static final int HUNDREDTH_MM_PER_INCH = 2540;
        
        public FillImage(String id, String base64ImageData) {
            super(id);
            m_base64ImageData = base64ImageData;
            java.awt.image.BufferedImage img = XmlUtils.decodeToImage(base64ImageData);
            if (img!=null)
            {
                m_pixelWidth = img.getWidth();
                m_pixelHeight = img.getHeight();
                
                m_100thMmWidth = m_pixelWidth * HUNDREDTH_MM_PER_INCH / DPI;
                m_100thMmHeight = m_pixelHeight * HUNDREDTH_MM_PER_INCH / DPI;
            }
            else
            {
                m_pixelWidth = 0;
                m_pixelHeight = 0;
                m_100thMmWidth = 0;
                m_100thMmHeight = 0;
            }
        }
        
        public String getBase64ImageData() { return m_base64ImageData; }
        public int getPxWidth() { return m_pixelWidth; }
        public int getPxHeight() { return m_pixelHeight; }
        public int get100thMMWidth() { return m_100thMmWidth; }
        public int get100thMMHeight() { return m_100thMmHeight; }
        
        public XAttributeList getPropertiesAsAttributes()
        {         
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getName());
            attributes.AddAttribute("width", Integer.toString(get100thMMWidth()));
            attributes.AddAttribute("height", Integer.toString(get100thMMHeight()));
            attributes.AddAttribute("xlink:href", "data:image/png;base64,"+getBase64ImageData());
            return attributes;
        }
    }
    
    public static class Pattern extends Style
    {
        private float m_100thMmWidth;
        private float m_100thMmHeight;
        private FillImage m_fillImage;
        private FillBitmapAssets.PatternDefinition m_patternDefinition;
        private String m_widthString;
        private String m_heightString;
        private String m_xString;
        private String m_yString;
        private Transformation m_transformation;
        
        public Pattern(String id)
        {
            super(id);
            m_100thMmWidth=0.0f;
            m_100thMmHeight=0.0f;
            m_fillImage=null;
            m_patternDefinition=null;
            m_widthString="";
            m_heightString="";
            m_xString="";
            m_yString="";
            m_transformation = null;
        }
        
        public void setFillImage(String fillImageId)
        {
            m_fillImage = Styles.getFillImage(fillImageId);
            if (m_fillImage!=null)
            {
                if (m_100thMmWidth==0 && m_100thMmHeight==0)
                {
                    m_100thMmWidth = m_fillImage.get100thMMWidth();
                    m_100thMmHeight = m_fillImage.get100thMMHeight();
                }
                
                // try to replace tangram fillings with pattern defs instead of bitmaps
                // TODO: make config dialog to choose tiger or swell or pin matrix export type
                m_patternDefinition = FillBitmapAssets.getPatternDefForBitmap(m_fillImage.m_base64ImageData);
                if (m_patternDefinition!=null)
                {
                    m_100thMmWidth = m_patternDefinition.width;
                    m_100thMmHeight = m_patternDefinition.height;
                    m_fillImage = null;
                }
            }
        }
        public void setWidthString(String widthString) { m_widthString = widthString; }
        public void setHeightString(String heightString) { m_heightString = heightString; }
        public void setXString(String xString) { m_xString = xString; }
        public void setYString(String yString) { m_yString = yString; }
        public void setTransformation(Transformation trans)
        {
            this.m_transformation = trans;
        }
        
        public FillImage getFillImage() { return m_fillImage; }
        
        public boolean isImageFill() { return m_fillImage!=null; }
        
        public FillBitmapAssets.PatternDefinition getPatternDefinition()
        {
            return m_patternDefinition; 
        }
        
        public boolean isPatternDefFill() { return m_patternDefinition!=null; }
        
        public XAttributeList getPropertiesAsAttributes()
        {         
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getName());
            attributes.AddAttribute("width", (m_widthString.isEmpty() && !m_widthString.equals("0.0") ? Float.toString(m_100thMmWidth) : m_widthString));
            attributes.AddAttribute("height", (m_heightString.isEmpty() && !m_heightString.equals("0.0") ? Float.toString(m_100thMmHeight) : m_heightString));
            if (!m_xString.isEmpty()) attributes.AddAttribute("x", m_xString);
            if (!m_yString.isEmpty()) attributes.AddAttribute("y", m_yString);
            attributes.AddAttribute("patternUnits", "userSpaceOnUse");
            if (m_transformation!=null) attributes.AddAttribute("patternTransform", m_transformation.toString());
            return attributes;
        }


    }
    
    public static ArrayList<MasterPageStyle> getMasterPageStyles()
    {
        return m_masterPageStyles;
    }  
   
    public static MasterPageStyle getMasterPageStyle(String masterPageStyleName)
    {
        String trimmedName = XmlUtils.trimToId(masterPageStyleName);
        for (MasterPageStyle _masterPageStyle : m_masterPageStyles)
        {
            if (_masterPageStyle.getName().equals(trimmedName))
            {
                return _masterPageStyle;
            }
        }
        return null;
    }
        
    public static ArrayList<MasterPage> getMasterPages()
    {
        return m_masterPages;
    }    
    
    public static MasterPage getMasterPage(String masterPageName)
    {
        for (MasterPage _masterPage : m_masterPages)
        {
            if (_masterPage.name.equals(masterPageName))
            {
                return _masterPage;
            }
        }
        return null;
    }
    
    public static ArrayList<PageStyle> getPageStyles()
    {
        return m_pageStyles;
    }    
    
    public static PageStyle getPageStyle(String pageStyleName)
    {
        String trimmedName = XmlUtils.trimToId(pageStyleName);
        for (PageStyle _pageStyle : m_pageStyles)
        {
            if (_pageStyle.getName().equals(trimmedName))
            {
                return _pageStyle;
            }
        }
        return null;
    }

    public static ArrayList<GraphicsStyle> getGraphicsStyles()
    {
        return m_graphicsStyles;
    }    
    
    public static GraphicsStyle getGraphicsStyle(String graphicsStyleName)
    {
        String trimmedName = XmlUtils.trimToId(graphicsStyleName);
        for (GraphicsStyle _graphicsStyle : m_graphicsStyles)
        {
            if (_graphicsStyle.getName().equals(trimmedName))
            {
                return _graphicsStyle;
            }
        }
        return null;
    }

    public static ArrayList<ParagraphStyle> getParagraphStyles()
    {
        return m_paragraphStyles;
    }    
    
    public static ParagraphStyle getPragraphStyle(String paragraphStyleName)
    {
        String trimmedName = XmlUtils.trimToId(paragraphStyleName);
        for (ParagraphStyle _paragraphStyle : m_paragraphStyles)
        {
            if (_paragraphStyle.getName().equals(trimmedName))
            {
                return _paragraphStyle;
            }
        }
        return null;
    }

    public static ArrayList<TextStyle> getTextStyles()
    {
        return m_textStyles;
    }    
    
    public static TextStyle getTextStyle(String textStyleName)
    {
        String trimmedName = XmlUtils.trimToId(textStyleName);
        for (TextStyle _textStyle : m_textStyles)
        {
            if (_textStyle.getName().equals(trimmedName))
            {
                return _textStyle;
            }
        }
        return null;
    }
    
        public static ArrayList<FontDeclStyle> getFontDeclStyles()
    {
        return m_fontDeclStyles;
    }    
    
    public static FontDeclStyle getFontDeclStyle(String fontDeclStyleName)
    {
        String trimmedName = XmlUtils.trimToId(fontDeclStyleName);
        for (FontDeclStyle _fontDeclStyle : m_fontDeclStyles)
        {
            if (_fontDeclStyle.getName().equals(trimmedName))
            {
                return _fontDeclStyle;
            }
        }
        return null;
    }
    
    public static ArrayList<StrokeDashStyle> getStrokeDashStyles()
    {
        return m_strokeDashStyles;
    }    
    
    public static StrokeDashStyle getStrokeDashStyle(String strokeDashStyleName)
    {
        String trimmedName = XmlUtils.trimToId(strokeDashStyleName);
        for (StrokeDashStyle _strokeDashStyle : m_strokeDashStyles)
        {
            if (_strokeDashStyle.getName().equals(trimmedName))
            {
                return _strokeDashStyle;
            }
        }
        return null;
    }
    
    public static ArrayList<MarkerStyle> getMarkerStyles()
    {
        return m_markerStyles;
    }    
    
    public static MarkerStyle getMarkerStyle(String markerStyleName)
    {
        String trimmedName = XmlUtils.trimToId(markerStyleName);
        for (MarkerStyle _markerStyle : m_markerStyles)
        {
            if (_markerStyle.getName().equals(trimmedName))
            {
                return _markerStyle;
            }
        }
        return null;
    }
    
    /**
     * get the whole collection of loaded fill images, identified by names, objects are base 64 encoded image files (e.g. png) as strings
     * @return
     */
    public static ArrayList<FillImage> getFillImages()
    {
        return m_fillImages;
    }
    
    /**
     * returns a base64 encoded image file string identified by name, 
     * @param name identifies the fill image
     * @return 64 encoded image files (e.g. png) as string
     */
    public static FillImage getFillImage(String name)
    {
        String trimmedName = XmlUtils.trimToId(name);
        for (FillImage _fillImage : m_fillImages)
        {
            if (_fillImage.getName().equals(trimmedName))
            {
                return _fillImage;
            }
        }
        return null;
    }
    
    public static ArrayList<Pattern> getPatterns()
    {
        return m_patterns;
    }
    
    public static Pattern getPattern(String name)
    {
        String trimmedName = XmlUtils.trimToId(name);
        for (Pattern _pattern : m_patterns)
        {
            if (_pattern.getName().equals(trimmedName))
            {
                return _pattern;
            }
        }
        return null;
    }
    
    /**
     * generates a CSS definition block from the loaded styles
     * @return
     */
    public static String getCssDefinitions()
    {
        String result = "\r\n";
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Standard text styles                                                       */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "\ttext, tspan {\r\n";
        GraphicsStyle standardGraphicsStyle = getGraphicsStyle("standard");
        String fontsCssDefLines = standardGraphicsStyle.getFontsCssDefinitionLines();
        if (standardGraphicsStyle!=null)
        {
            result += fontsCssDefLines;
        }
        result += "\t\tstroke: none;\r\n";
        result += "\t}\r\n\r\n";
        
        // print version for computer braille font (else ascii version)
        if (standardGraphicsStyle!=null && fontsCssDefLines.contains("font-family:"))
        {
            int startFamilyIdx = fontsCssDefLines.lastIndexOf("font-family:")+12;
            int afterFamilyIdx = fontsCssDefLines.substring(startFamilyIdx).indexOf(';')+startFamilyIdx;
            String fontFamilyName = fontsCssDefLines.substring(startFamilyIdx,afterFamilyIdx);
            if (fontFamilyName.contains("Braille DE Computer ASCII"))
            {
                fontFamilyName = fontFamilyName.replace("Braille DE Computer ASCII", "Braille DE Computer");
                result += "\t @media print, embossed, braille { text, tspan {\r\n";
                result += "\t\tfont-family:"+fontFamilyName+";";
                result += "\t}}\r\n\r\n";
            }
            else if (fontFamilyName.contains("Braille29 DE ASCII")||fontFamilyName.contains("Braille29 ASCII"))
            {
                String newFontFamilyName = fontFamilyName.replace("Braille29 DE ASCII", "Braille29 DE").replace("Braille29 ASCII", "Braille29 DE");
                result += "\t @media print, embossed, braille { text, tspan {\r\n";
                result += "\t\tfont-family:"+newFontFamilyName+";";
                result += "\t}}\r\n\r\n";
            }
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Graphics Styles                                                            */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        for (GraphicsStyle graphicsStyle : m_graphicsStyles)
        {
            result += graphicsStyle.getCssDefinition();
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Paragraph Styles                                                           */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        for (ParagraphStyle paragraphStyle : m_paragraphStyles)
        {
            result += paragraphStyle.getCssDefinition();
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Text Styles                                                                */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        for (TextStyle textStyle : m_textStyles)
        {
            result += textStyle.getCssDefinition();
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Master Page Styles                                                         */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        for (MasterPageStyle masterPageStyle : m_masterPageStyles)
        {
            result += masterPageStyle.getCssDefinition();
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Page Styles                                                                */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        for (PageStyle pageStyle : m_pageStyles)
        {
            result += pageStyle.getCssDefinition();
        }
        
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "/* Defaults for rendering SVG shape elements like OpenOffice Draw (overiding) */\r\n";
        result += "/* -------------------------------------------------------------------------- */\r\n";
        result += "\t.drawG {\r\n";
        result += "\t}\r\n\r\n";

        result += "\t.drawRect {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawLine {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawPath {\r\n";
        result += "\t}\r\n\r\n";
                
        result += "\t.drawConnector {\r\n";
        result += "\t\tfill: none;\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawPolyline {\r\n";
        result += "\t\tfill: none;\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawCircle {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawEllipse {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawImage {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawTextBox {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawTextBoxGroup {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.drawShapeWithText {\r\n";
        result += "\t}\r\n\r\n";
        
        result += "\t.debug {\r\n";
        result += "\t\tfill: none;\r\n";
        result += "\t\tstroke: crimson;\r\n";
        result += "\t\tstroke-width: 8.819445;\r\n";
        result += "\t}\r\n\r\n";
        
        return result;
    } 
}
