/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import com.sun.star.xml.sax.XAttributeList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author Martin.Spindler@tu-dresden.de
 */
public class Shapes {
      
    private static ArrayList<Layer> m_layers  = new ArrayList<Layer>();
    private static Shape m_lastAddedShape = null;
    private static Layer m_lastAddedLayer = null;
    private static G m_currentGroup = null;
    private static TextParagraph m_lastAddedTextParagraph = null;
    
    /**
     * needs to be called for new filter calls (e.g. in startDocument()) to clear static arrays and fields
     */
    public static void init()
    {
        m_layers.clear();
        m_lastAddedShape = null;
        m_lastAddedLayer = null;
        m_currentGroup = null;
        m_lastAddedTextParagraph = null;
    }
    
    /**
     * a named draw shape for parsing "draw:*" and thier attributes.
     * Abstract class, to be subclassed for implementations.
     */
    public static abstract class Shape
    {
        private final String type;
        private String id;
        protected HashMap<String, String> properties;
        public String title;
        public String desc;
        private G parentGroup;
        private final ArrayList<TextParagraph> textParagraphChildren;
        protected String originClassName;
        
        public Shape(String aType)
        {
            this(aType, null);
        }
        
        public Shape(String aType, XAttributeList propertyAttributes)
        {
            type = aType;
            properties = new HashMap<String, String>();
            title = "";
            desc = "";
            parentGroup = null;
            textParagraphChildren = new ArrayList<TextParagraph>();
            originClassName = "";
                    
            if (propertyAttributes!=null)
            {
                // if id is not defined, create one from name or generate 
                id = propertyAttributes.getValueByName("id");
                if (id == null || id.isEmpty())
                {
                    // trim existing name to id or create new id
                    id = XmlUtils.trimToId(propertyAttributes.getValueByName("draw:name"));
                    // dont' use "name" in properties any more, instead insert id.
                    properties.remove("name");
                    properties.put("id", id);
                }
                // copy other attributes to properties
                for (short i=0; i < propertyAttributes.getLength(); i++)
                {
                    String attributeName = propertyAttributes.getNameByIndex(i);
                    properties.put(
                            attributeName.contains(":")?attributeName.substring(attributeName.indexOf(":")+1):attributeName, 
                            propertyAttributes.getValueByIndex(i)
                    );
                }
            }
            // in case of empty attributes (e.g. for a group): just generate id
            else
            {
                id = XmlUtils.trimToId("");
                properties.put("id", id);
            }
        }
        
        public String getOriginClassName()
        {
            return originClassName;
        }
        
        /**
         * adds text paragraph, also sets this shape as its original parent shape
         * @param textParagraph
         */
        public void addTextParagraph(TextParagraph textParagraph)
        {
            textParagraphChildren.add(textParagraph);
            textParagraph.setOriginalParentShape(this);
            m_lastAddedTextParagraph = textParagraph;
        }
        
        public ArrayList<TextParagraph> getTextParagraphChildren()
        {
            return textParagraphChildren;
        }
        
        /**
         * calculates the vertical baseline offset from top
         * @return
         */
        public float getMaxLeadingPlusAscentOfFirstLine()
        {
            if (getTextParagraphChildren().size()>0)
            {
                return getTextParagraphChildren().get(0).getMaxLeadingPlusAscentOfSpans();
            }
            else return 0.0f;
        }
        
        public float getMaxDescentOfFirstLine()
        {
            if (getTextParagraphChildren().size()>0)
            {
                return getTextParagraphChildren().get(0).getMaxDescentOfSpans();
            }
            else return 0.0f;
        }
        
        public float getMaxAscentOfFirstLine()
        {
            if (getTextParagraphChildren().size()>0)
            {
                return getTextParagraphChildren().get(0).getMaxAscentOfSpans();
            }
            else return 0.0f;
        }
        
        public float getTextLineHeight()
        {
            Styles.ParagraphStyle shapeParagraphStyle = getParagraphStyle();
            Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
            
            float lineHeight = 0.0f;
            float fontSize;

            if (shapeParagraphStyle!=null)
            {
                fontSize = shapeParagraphStyle.getFontSize();
                // but do font size correction if this line contains spans
                lineHeight = shapeParagraphStyle.getLineHeight(fontSize); // line height
            }
            else if (standardStyle!=null)
            {
                fontSize = new Measure(standardStyle.getPropertyValue("font-size")).getValueInMm100th();
                lineHeight = fontSize;
            }
            
            return lineHeight;
        }
        
        public boolean hasNonEmptyTextParagraphChildren()
        {
            for (TextParagraph textPara: textParagraphChildren)
            {
                if (textPara.hasNonEmptyTSpans())
                    return true;
            }
            return false;
        }
        
        public G getParentGroup()
        {
            return parentGroup;
        }
        
        /**
         * finds the layer that contains this shape or null
         * @return
         */
        private Layer getHighestLayerParent()
        {
            return getHighestLayerParent(this);
        }
        
        /**
         * recursive search
         * @param shape
         * @return 
         */
        private static Layer getHighestLayerParent(Shape shape)
        {
            // check parent of shape: if no parent cancel
            G parent = shape.getParentGroup();
            if (parent == null) return null;
            // if  parent is already a layer return the layer
            else if (parent instanceof Layer) return (Layer) parent;
            // otherwise do recursion on parent group
            else return getHighestLayerParent(parent);
        }
        
        private Shape getHighestNonLayerShapeOrParent()
        {
            return getHighestNonLayerShapeOrParent(this);
        }
        
        /**
         * recursive search
         * @param shape
         * @return 
         */
        private static Shape getHighestNonLayerShapeOrParent(Shape shape)
        {
            // check parent of shape: if no parent or parent is already a layer just return the shape itself
            G parent = shape.getParentGroup();
            if ((parent == null) || (parent instanceof Layer)) return shape;
            // otherwise do recursion
            else return getHighestNonLayerShapeOrParent(parent);
        }
        
        public void setParentGroup(G group)
        {
            parentGroup = group;
            
            // calculate resulting transformation (parents and this)
            ArrayList<String> transformStrings = new ArrayList<String>();
            String transformString = getPropertyValue("transform");
            if (!transformString.isEmpty()) transformStrings.add(0, transformString);
            
            G currentParent = parentGroup;
            do
            {
                transformString = currentParent.getPropertyValue("transform");
                if (!transformString.isEmpty()) transformStrings.add(0, transformString);
                currentParent = currentParent.getParentGroup();
            }
            while (currentParent!=null);
            
            Transformation result = new Transformation();
            for (String transString : transformStrings)
            {
                result = result.transformBy(new Transformation(transString, true)); // svg format
            }
            
            Styles.GraphicsStyle style = getGraphicsStyle();
            if (style != null)
            {
                Styles.Pattern fillPattern = style.getPattern();
                if (fillPattern!=null)
                {
                    fillPattern.setTransformation(result.getInversion());
                }
            }
        }
        
        /**
         * @return type of the draw element, as the tag in the OO draw xml document (without namespace)
         */
        public String getType() { return type; }
        
        /**
         * @return id this element, all whitespace characters were removed in constructor by calling XmlUtils.trimToId()
         */
        public String getId() { return id; }
        
        /**
         * returns property value if key is defined, otherwise returns empty string
         * @param propertyName
         * @return property value if key is defined, otherwise empty string
         */
        public String getPropertyValue(String propertyName) 
        {
            return properties.containsKey(propertyName) ? 
                    properties.get(propertyName) : 
                    "";
        }
        
        /**
         * tries to parse the property as measure
         * @param propertyName
         * @return measure. 0.0 in 100th mm if no success
         */
        public Measure getMeasureProperty(String propertyName)
        {
            try
            {
                String valueString = properties.get(propertyName);
                if (valueString.isEmpty()) return new Measure();
                else return new Measure(properties.get(propertyName));
            }
            catch (Exception ex)
            {
                return new Measure();
            }
        }
        
        public String getOriginalLayerName() { return getPropertyValue("layer"); }
        
        public String getGraphicsStyleName() { return getPropertyValue("style-name"); }
        
        public Styles.GraphicsStyle getGraphicsStyle()
        {
            String style_name = getGraphicsStyleName();
            if (!style_name.isEmpty())
            {
                return Styles.getGraphicsStyle(style_name);
            }
            return null;
        }
        
        public String getCssClasses() 
        { 
            String result = originClassName;
            Styles.GraphicsStyle style = getGraphicsStyle();
            if (style!=null) 
            {
                if (!result.isEmpty()) result += " ";
                result += style.getNameInclParents();
            }
//            Styles.ParagraphStyle pStyle = getParagraphStyle();
//            if (pStyle!=null)
//            {
//                if (!result.isEmpty()) result += " ";
//                result += pStyle.getNameInclParents();
//            }
            return result;
        }
        
        public String getParagraphStyleName() 
        { 
            return getPropertyValue("text-style-name"); 
        }
        
        public Styles.ParagraphStyle getParagraphStyle()
        {
            String style_name = getParagraphStyleName();
            if (!style_name.isEmpty())
            {
                return Styles.getPragraphStyle(style_name);
            }
            return null;
        }
        
        /**
         * gets all properties as attibutes
         * might be overridden to produce the right svg attributes
         * @return
         */
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            for (String propertyName : properties.keySet())
            {
                attributes.AddAttribute(propertyName, properties.get(propertyName));
            }
            return attributes;
        }
        
        public abstract Rectangle getBoundingBox();
        
        /**
         * might be smaller than the boudning box (see getBoundingBox()) because of padding properties
         * @return
         */
        public Rectangle getTextBoundingBox()
        {
            Rectangle textBoxBounds;
            G g = getParentGroup();
            textBoxBounds = getBoundingBox();                // use the groups boundings box
            if (this instanceof TextBox && g!=null && !(g instanceof Layer))
            {
                Rectangle boundsWoTextBox = g.getBoundingBoxWithoutTextBoxes(); // for textboxes that are in a group get the groups bounding box (this excludes the textbox itself)
                textBoxBounds.height = Math.min(textBoxBounds.height, boundsWoTextBox.height);
            }
            Styles.GraphicsStyle shapeGraphicsStyle = getGraphicsStyle();
            
            // text positioning box withing shape
            if (shapeGraphicsStyle!=null)
            {
                float paddingTop = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("padding-top")).getValueInMm100th();
                float paddingBottom = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("padding-bottom")).getValueInMm100th();
                float paddingLeft = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("padding-left")).getValueInMm100th();
                float paddingRight = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("padding-right")).getValueInMm100th();
                float minWidth = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("min-width")).getValueInMm100th();
                float minHeight = new Measure(shapeGraphicsStyle.getPropertyValueInclParents("min-height")).getValueInMm100th();
                
                // if all pox is padded out, use empty centered box:
                if (paddingLeft+paddingRight>textBoxBounds.width)
                {
                    textBoxBounds.x = textBoxBounds.getCenterX();
                    textBoxBounds.width = 0.0f;
                }
                else
                {
                    textBoxBounds.x=textBoxBounds.x+paddingLeft;
                    textBoxBounds.width = textBoxBounds.width-paddingLeft-paddingRight;
                }
                if (paddingTop+paddingBottom>textBoxBounds.height)
                {
                    textBoxBounds.y = textBoxBounds.getCenterY();
                    textBoxBounds.height = 0.0f;
                }
                else 
                {
                    textBoxBounds.y=textBoxBounds.y+paddingTop;
                    textBoxBounds.height = textBoxBounds.height-paddingTop-paddingBottom;
                }
//                textBoxBounds.width = minWidth;
//                textBoxBounds.height = minHeight;
//                textBoxBounds.x = textBoxBounds.x+paddingLeft;
//                textBoxBounds.y = textBoxBounds.y+paddingTop;
//                textBoxBounds.width = Math.max(textBoxBounds.width-paddingLeft-paddingRight, minWidth);
//                textBoxBounds.height = Math.max(textBoxBounds.height-paddingTop-paddingBottom, minHeight);    
                                
                // if all box is padded out, use empty centered box:
//                if (paddingLeft+paddingRight>textBoxBounds.width)
//                {
//                    textBoxBounds.x = textBoxBounds.getCenterX();
//                    textBoxBounds.width = 0.0f;
//                }
//                else
//                {
//                    
//                }
//                // if all box is padded out, use empty centered box:
//                if (paddingTop+paddingBottom>textBoxBounds.height)
//                {
//                    textBoxBounds.y = textBoxBounds.getCenterY();
//                    textBoxBounds.height = 0.0f;
//                }
//                else 
//                {
//                    
//                }
            }
            return textBoxBounds;
        }
        
        public void parseTransformProperty()
        {
            final String TRANSFORM = "transform";           
            // transform = "rotate(...) translate(... ...)" (skew would not happen for textboxes in OO fraw)
            if (properties.containsKey(TRANSFORM))
            {
                String origTransformString = properties.get(TRANSFORM);
                int skewXIdx = origTransformString.indexOf("skewX");
                int rotateIdx = origTransformString.indexOf("rotate");
                int translateIdx = origTransformString.indexOf("translate");
                int brcOpen, brcClose;
                String[] paramStrings;
                float skewDeg, rotateDeg, translateX, translateY;
                
                String result = "";
                
                if (skewXIdx>=0)
                {
                    brcOpen = origTransformString.substring(skewXIdx).indexOf('(');
                    brcClose = origTransformString.substring(skewXIdx).indexOf(')');
                    paramStrings = origTransformString.substring(skewXIdx+brcOpen+1, skewXIdx+brcClose).replace(',', ' ').split("\\s+");
                    skewDeg = (float) Transformation.degrees((paramStrings.length>0) ? 0-(new Measure(paramStrings[0]).getValueInMm100th()) : 0.0f);
                    result = "skewX(" + Float.toString(skewDeg)+ ")" + (result.isEmpty()?"":" ") + result;
                }
                if (rotateIdx>=0)
                {
                    brcOpen = origTransformString.substring(rotateIdx).indexOf('(');
                    brcClose = origTransformString.substring(rotateIdx).indexOf(')');
                    paramStrings = origTransformString.substring(rotateIdx+brcOpen+1, rotateIdx+brcClose).replace(',', ' ').split("\\s+");
                    rotateDeg = (float) Transformation.degrees((paramStrings.length>0) ? 0-(new Measure(paramStrings[0]).getValueInMm100th()) : 0.0f);
                    result = "rotate(" + Float.toString(rotateDeg)+ ")" + (result.isEmpty()?"":" ") + result;
                }
                if (translateIdx>=0) 
                {
                    brcOpen = origTransformString.substring(translateIdx).indexOf('(');
                    brcClose = origTransformString.substring(translateIdx).indexOf(')');
                    paramStrings = origTransformString.substring(translateIdx+brcOpen+1, translateIdx+brcClose).replace(',', ' ').split("\\s+");
                    translateX = (paramStrings.length>0) ? new Measure(paramStrings[0]).getValueInMm100th() : 0.0f;
                    translateY = (paramStrings.length>1) ? new Measure(paramStrings[1]).getValueInMm100th() : 0.0f;
                    if (this instanceof Shapes.Rect && !(properties.containsKey("x") && properties.containsKey("y")) && (rotateIdx<0 && skewXIdx<0))
                    {
                        properties.put("x", Float.toString(translateX));
                        properties.put("y", Float.toString(translateY));
                    }
                    else
                    {
                        result = "translate(" + Float.toString(translateX)+ " " + Float.toString(translateY) + ")" + (result.isEmpty()?"":" ") + result;
                    }
                }
                properties.replace(TRANSFORM, result);
            }
            // if no transform is given, but x and y are defined, set up the translation
            else if ((this instanceof Shapes.Polyline ||
                    this instanceof Shapes.Polygon ||
                    this instanceof Shapes.Path
                    ) && properties.containsKey("x") && properties.containsKey("y"))
            {
                float translateX = new Measure(properties.get("x")).getValueInMm100th();
                float translateY = new Measure(properties.get("y")).getValueInMm100th();
                properties.put("transform", "translate(" + Float.toString(translateX)+ " " + Float.toString(translateY) + ")");
            }
        }
    }
    
    public static class G extends Shape
    {
        public final static String TAG = "g";
        
        private final ArrayList<Shape> children;
        
        public G()
        {
            super(TAG);
            children = new ArrayList<Shape>();
            originClassName="";
        }
        
        public G(String originClassName, XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            children = new ArrayList<Shape>();
            this.originClassName = originClassName;
            parseTransformProperty();
        }
        
        /**
         * add a child shape and sets this group as its parent
         * @param shape
         */
        public void addChild(Shape shape)
        {
            children.add(shape);
            shape.setParentGroup(this);
        }
        
        /**
         * accessing the children of a group or layer
         * @return
         */
        public ArrayList<Shape> getChildren()
        {
            return children;
        }
        
        public Shape getChild(String id)
        {
            for (Shape shape : children)
            {
                if (shape.getId().equals(id)) return shape;
            }
            return null;
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }

        @Override
        public Rectangle getBoundingBox() {
            // TODO: iterate all children and sum up boundings
            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            
            for (Shape child : getChildren())
            {
                // exclude text boxes, their size is aligned to the bounding box but may not correspond
                {
                    Rectangle childBox = child.getBoundingBox();
                    if (childBox!=null)
                    {
                        minX = Math.min(minX, childBox.x );
                        minY = Math.min(minY, childBox.y );
                        maxX = Math.max(maxX, childBox.x+childBox.width );
                        maxY = Math.max(maxY, childBox.y+childBox.height );
                    }
                }
            }
            
            return new Rectangle(minX, minY, maxX-minX, maxY-minY);
        }
        
        public Rectangle getBoundingBoxWithoutTextBoxes() {
            // TODO: iterate all children and sum up boundings
            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            
            for (Shape child : getChildren())
            {
                // exclude text boxes, their size is aligned to the bounding box but may not correspond
                if (!(child instanceof TextBox))
                {
                    Rectangle childBox = child.getBoundingBox();
                    if (childBox!=null)
                    {
                        minX = Math.min(minX, childBox.x );
                        minY = Math.min(minY, childBox.y );
                        maxX = Math.max(maxX, childBox.x+childBox.width );
                        maxY = Math.max(maxY, childBox.y+childBox.height );
                    }
                }
            }
            
            return new Rectangle(minX, minY, maxX-minX, maxY-minY);
        }
    }
    
    public static class Layer extends G
    {
        private final String m_originalName;
        
        public Layer(String aName) {
            super("drawLayer", new XmlAttributeListImpl("name", aName));
            m_originalName = aName;
        }
        
        public Layer(String aName, XAttributeList propertyAttributes) {
            super("drawLayer", propertyAttributes);
            m_originalName = aName;
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            String id = getPropertyValue("id");
            if (id!=null && !id.isEmpty()) attributes.AddAttribute("id", getPropertyValue("id"));
            String classes = getCssClasses();
            if (classes!=null && !classes.isEmpty()) attributes.AddAttribute("class", getCssClasses());
            attributes.AddAttribute("inkscape:groupmode", "layer");
            
            if (!m_originalName.isEmpty())
            {
                String friendlyName = Character.toUpperCase(m_originalName.charAt(0))+m_originalName.substring(1);
                if (m_originalName.equals("layout"))
                {
                    if (Styles.DocLanguage.equals(("de"))) friendlyName = "Layout";
                }
                if (m_originalName.equals("background"))
                {
                    if (Styles.DocLanguage.equals(("de"))) friendlyName = "Hintergrund";
                }
                if (m_originalName.equals("backgroundobjects"))
                {
                    if (Styles.DocLanguage.equals(("de"))) friendlyName = "Hintergrundobjekte";
                    else friendlyName = "Background Objects";
                }
                if (m_originalName.equals("controls"))
                {
                    if (Styles.DocLanguage.equals(("de"))) friendlyName = "Steuerung";
                }
                if (m_originalName.equals("measurelines"))
                {
                    if (Styles.DocLanguage.equals(("de"))) friendlyName = "Ma\u00DFlinien";
                    else friendlyName = "Measure Lines";
                }
                attributes.AddAttribute("inkscape:label", friendlyName);
            }
            return attributes;
        }
    }
    
    /**
     * registers a new layer. does not check if already existing
     * @param layerName
     */
    public static void addNewLayer(String layerName)
    {
        Layer layer = new Layer(layerName);
        m_layers.add(layer);
        m_lastAddedLayer = layer;
    }
    
    /**
     * this is helpful for adding title and desc elements during parsing
     * @return
     */
    public static Layer getLastAddedLayer()
    {
        return m_lastAddedLayer;
    }
    
    public static Layer getLayerByOriginalName(String originalLayerName)
    {
        for (Layer l : m_layers)
        {
            if (l.m_originalName.equals(originalLayerName)) return l;
        }
        return null;
    }
    
    /**
     * Acces to Layers
     * @return
     */
    public static ArrayList<Layer> getLayers()
    {
        return m_layers;
    }
         
    /**
     * adds a shape and automatically add it to the right group and layer
     * @param shape
     */
    public static void addShape(Shape shape)
    {       
        // check current group: if not in a group use a temp layer, when shapes in the group define the layer, the content will be moved to the right layer later
        if (m_currentGroup == null)
        {
            Layer l = getLayerByOriginalName("tud_tangram_mci_Temp");
            if (l==null)
            {
                l = new Layer("tud_tangram_mci_Temp");
                m_layers.add(l);
            }
            m_currentGroup = l;
        }
        
        // add to current group and register group as parent
        m_currentGroup.addChild(shape);
        shape.setParentGroup(m_currentGroup);
            
        // if it's a group, set it as current group
        if (shape instanceof G)
        {
            m_currentGroup = (G) shape;
        }
        // other shapes: eventually move their containing groups to right layer
        else
        {
            // if defined, what would be the right layer?
            String originalLayerName = shape.getOriginalLayerName();
            if (!originalLayerName.isEmpty())
            {   
                Layer definedLayer = getLayerByOriginalName(originalLayerName);
                Layer currentLayer = shape.getHighestLayerParent();
                // if layer found as defined and shape was is temp layer
                if (definedLayer!=null && currentLayer.m_originalName.equals("tud_tangram_mci_Temp"))
                {
                    // move to right layer, set layer as right parent
                    Shape s = shape.getHighestNonLayerShapeOrParent();
                    definedLayer.addChild(s);
                    currentLayer.getChildren().remove(s);
                    s.setParentGroup(definedLayer);
                }
            }
            
            // correct too high text boxes if grouped with non-text-box-shape(s)
            if (shape instanceof Shapes.TextBox && !((TextBox)shape).getPropertyValue("height").isEmpty())
            {
                Shapes.G parentGroup = shape.getParentGroup();
                if (!(parentGroup instanceof Shapes.Layer))
                {
                    int countTextBoxes=0;
                    int countGroups=0;
                    int countNonTextBoxOrGroups=0;
                    
                    float minY = Float.MAX_VALUE;
                    float maxY = Float.MIN_VALUE;
                    
                    for (Shapes.Shape child : parentGroup.getChildren())
                    {
                        if (child instanceof Shapes.TextBox) 
                        {
                            countTextBoxes++;
                            if (countTextBoxes>1) break;
                        }
                        else if (child instanceof Shapes.G) 
                        {
                            countGroups++;
                            break;
                        }
                        else 
                        {
                            countNonTextBoxOrGroups++;
                            Rectangle childBox = child.getBoundingBox();
                            minY = Math.min(minY, childBox.y );
                            maxY = Math.max(maxY, childBox.y+childBox.height );
                        }
                    }
                    if (countTextBoxes==1 && countGroups==0 && countNonTextBoxOrGroups>0)
                    {
                        float height = Math.min(maxY-minY, new Measure(((TextBox)shape).getPropertyValue("height")).getValueInMm100th());
                        if (height>0.0f)
                        {
                            ((TextBox)shape).properties.replace("height", Float.toString(height));
                        }
                    }          
                }
            }
        }
        
        // TODO: set start / end marker pos to used clipPath for paths and polylines too!
        if (shape instanceof Line)
        {
            Line line = ((Line)shape);
            float x_s = (new Measure(line.getPropertyValue("x1"))).getValueInMm100th();
            float y_s = (new Measure(line.getPropertyValue("y1"))).getValueInMm100th();
            float x_e = (new Measure(line.getPropertyValue("x2"))).getValueInMm100th();
            float y_e = (new Measure(line.getPropertyValue("y2"))).getValueInMm100th();
            Styles.GraphicsStyle style = shape.getGraphicsStyle();
            if (style!=null)
            {
                String clipName = "clip_"+style.getName();
                Styles.CircleClipPath clipPath = Styles.getCircleClipPath(clipName);
                if (clipPath!=null)
                {
                    Styles.MarkerStyle startMarkerStyle = Styles.getMarkerStyle(style.getPropertyValue("marker-start"));
                    if (startMarkerStyle!=null)
                    {
                        clipPath.x_s = x_s;
                        clipPath.y_s = y_s;
                    }
                    Styles.MarkerStyle endMarkerStyle = Styles.getMarkerStyle(style.getPropertyValue("marker-end"));
                    if (endMarkerStyle!=null)
                    {
                        clipPath.x_e = x_e;
                        clipPath.y_e = y_e;
                    }
                }
            }
        }
        
        m_lastAddedShape = shape;
    }
    
    public static G getCurrentGroup()
    {
        return m_currentGroup;
    }
    
    /**
     * has to be called on endElement() with "draw:g" to step back to parent of current group as current group
     */
    public static void endOfCurrentGroup()
    {
        m_currentGroup = m_currentGroup.getParentGroup();
    }
    
    /**
     * this is helpful for adding child elements during parsing
     * @return
     */
    public static Shape getLastAddedShape()
    {
        return m_lastAddedShape;
    }
    
    public static class Rect extends Shape
    {
        public final static String TAG = "rect";
        
        public Rect()
        {
            super(TAG); 
        }
                 
        public Rect(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            attributes.AddAttribute("class", getCssClasses());
            
            String xValString = getPropertyValue("x");
            String yValString = getPropertyValue("y");
            String widthValString = getPropertyValue("width");
            String heightValString = getPropertyValue("height");
            
            if (!xValString.isEmpty()) attributes.AddAttribute("x", Float.toString((new Measure(xValString)).getValueInMm100th()));
            if (!yValString.isEmpty()) attributes.AddAttribute("y", Float.toString((new Measure(yValString)).getValueInMm100th()));
            if (!widthValString.isEmpty()) attributes.AddAttribute("width", Float.toString((new Measure(widthValString)).getValueInMm100th()));
            if (!heightValString.isEmpty()) attributes.AddAttribute("height", Float.toString((new Measure(heightValString)).getValueInMm100th()));
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            float x = getMeasureProperty("x").getValueInMm100th();
            float y = getMeasureProperty("y").getValueInMm100th();
            float width = getMeasureProperty("width").getValueInMm100th();
            float height = getMeasureProperty("height").getValueInMm100th();

            return new Rectangle(x,y,width,height);
        }
    }
   
     public static class TextBox extends Shape
    {      
        
        public final static String TAG = "g";
                
        public TextBox()
        {
            super(TAG);
        }
        
        public TextBox(String originClassName, XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            this.originClassName = originClassName;
            
            final String TRANSFORM = "transform";           
            // transform = "rotate(...) translate(... ...)" (skew would not happen for textboxes in OO fraw)
            if (properties.containsKey(TRANSFORM) && !(properties.containsKey("x") && properties.containsKey("y")))
            {
                String origTransformString = properties.get(TRANSFORM);
                int rotateIdx = origTransformString.indexOf("rotate");
                int translateIdx = origTransformString.indexOf("translate");
                if (rotateIdx>=0 && translateIdx>rotateIdx)
                {
                    String translateString=origTransformString.substring(translateIdx).trim();
                    int brcOpen = translateString.indexOf('(');
                    int brcClose = translateString.indexOf(')');
                    String[] paramStrings = translateString.substring(brcOpen+1, brcClose).replace(',', ' ').split("\\s+");
                    float translateX = (paramStrings.length>0) ? new Measure(paramStrings[0]).getValueInMm100th() : 0.0f;
                    float translateY = (paramStrings.length>1) ? new Measure(paramStrings[1]).getValueInMm100th() : 0.0f;
                    
                    String rotateString=origTransformString.substring(0, translateIdx).trim();
                    brcOpen = rotateString.indexOf('(');
                    brcClose = rotateString.indexOf(')');
                    paramStrings = rotateString.substring(brcOpen+1, brcClose).replace(',', ' ').split("\\s+");
                    double rotateDeg = Transformation.degrees((paramStrings.length>0) ? 0-(new Measure(paramStrings[0]).getValueInMm100th()) : 0.0f);
                    
                    properties.replace(TRANSFORM, "rotate("+rotateDeg+" "+translateX+" "+translateY+")");
                    properties.put("x", Float.toString(translateX));
                    properties.put("y", Float.toString(translateY));
                }
            }
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id")+"_grouped_with_text");
            attributes.AddAttribute("class", "drawTextBoxGroup");
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            return attributes;
        }

        XAttributeList getRectSvgAttributes() {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            attributes.AddAttribute("class", getCssClasses());
            
            String xValString = getPropertyValue("x");
            String yValString = getPropertyValue("y");
            String widthValString = getPropertyValue("width");
            String heightValString = getPropertyValue("height");
            
            if (!xValString.isEmpty()) attributes.AddAttribute("x", Float.toString((new Measure(xValString)).getValueInMm100th()));
            if (!yValString.isEmpty()) attributes.AddAttribute("y", Float.toString((new Measure(yValString)).getValueInMm100th()));
            if (!widthValString.isEmpty()) attributes.AddAttribute("width", Float.toString((new Measure(widthValString)).getValueInMm100th()));
            if (!heightValString.isEmpty()) attributes.AddAttribute("height", Float.toString((new Measure(heightValString)).getValueInMm100th()));
            
            return attributes;
        }

        @Override
        public Rectangle getBoundingBox() {
            float x = getMeasureProperty("x").getValueInMm100th();
            float y = getMeasureProperty("y").getValueInMm100th();
            float width = getMeasureProperty("width").getValueInMm100th();
            float height = getMeasureProperty("height").getValueInMm100th();

            return new Rectangle(x,y,width,height);
        }
    }
    
    
    public static class Line extends Shape
    {
        public final static String TAG = "line";
        
        public Line()
        {
            super(TAG); 
        }
                 
        public Line(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            attributes.AddAttribute("class", getCssClasses());
                                             
            float x1 = (new Measure(getPropertyValue("x1"))).getValueInMm100th();
            float y1 = (new Measure(getPropertyValue("y1"))).getValueInMm100th();
            float x2 = (new Measure(getPropertyValue("x2"))).getValueInMm100th();
            float y2 = (new Measure(getPropertyValue("y2"))).getValueInMm100th();
            
            attributes.AddAttribute("x1", Float.toString(x1));
            attributes.AddAttribute("y1", Float.toString(y1));
            attributes.AddAttribute("x2", Float.toString(x2));
            attributes.AddAttribute("y2", Float.toString(y2));
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            float x1 = getMeasureProperty("x1").getValueInMm100th();
            float y1 = getMeasureProperty("y1").getValueInMm100th();
            float x2 = getMeasureProperty("x2").getValueInMm100th();
            float y2 = getMeasureProperty("y2").getValueInMm100th();
            
            float x=Math.min(x1, x2);
            float y=Math.min(y1, y2);
            float width=Math.abs(x2-x1);
            float height=Math.abs(y2-y1);
            
            return new Rectangle(x,y,width,height);
        }
        
        public tud.mci.tangram.Line getAsLine()
        {
            float x1 = getMeasureProperty("x1").getValueInMm100th();
            float y1 = getMeasureProperty("y1").getValueInMm100th();
            float x2 = getMeasureProperty("x2").getValueInMm100th();
            float y2 = getMeasureProperty("y2").getValueInMm100th();
            
            return new tud.mci.tangram.Line(x1, y1, x2, y2);
        }
    }
    
    public static class Path extends Shape
    {
        public final static String TAG = "path";
        
        public Path()
        {
            super(TAG); 
        }
                 
        public Path(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
                                 
            //String viewBoxString = getPropertyValue("viewBox");
            String dString = getPropertyValue("d");

            String styleString = getPropertyValue("style");
            
            //if (!viewBoxString.isEmpty()) attributes.AddAttribute("viewBox", viewBoxString);
            if (!dString.isEmpty()) attributes.AddAttribute("d", dString);
            // translate start of path if x and y are given

            if (!styleString.isEmpty()) attributes.AddAttribute("style", styleString);
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            
            if (originClassName.equals("drawConnector"))
            {
                float x1 = getMeasureProperty("x1").getValueInMm100th();
                float y1 = getMeasureProperty("y1").getValueInMm100th();
                float x2 = getMeasureProperty("x2").getValueInMm100th();
                float y2 = getMeasureProperty("y2").getValueInMm100th();

                float x=Math.min(x1, x2);
                float y=Math.min(y1, y2);
                float width=Math.abs(x2-x1);
                float height=Math.abs(y2-y1);

                return new Rectangle(x,y,width,height);
            }
            else
            {
                Rectangle result = new Rectangle(getPropertyValue("viewBox"));
                if (!getPropertyValue("x").isEmpty())
                {
                    float x = getMeasureProperty("x").getValueInMm100th();
                    result.x=result.x+x;
                }
                if (!getPropertyValue("y").isEmpty())
                {
                    float y = getMeasureProperty("y").getValueInMm100th();
                    result.y=result.y+y;
                }
                return result;
            }
        }
    }
    
    public static class Polygon extends Shape
    {
        public final static String TAG = "polygon";
        
        public Polygon()
        {
            super(TAG); 
        }
                 
        public Polygon(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
                                 
            String pointsString = getPropertyValue("points");

            if (!pointsString.isEmpty()) attributes.AddAttribute("points", pointsString);

            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
         @Override
        public Rectangle getBoundingBox() {
            
            Rectangle result = new Rectangle(getPropertyValue("viewBox"));
            if (!getPropertyValue("x").isEmpty())
            {
                float x = getMeasureProperty("x").getValueInMm100th();
                result.x=result.x+x;
            }
            if (!getPropertyValue("y").isEmpty())
            {
                float y = getMeasureProperty("y").getValueInMm100th();
                result.y=result.y+y;
            }
            return result;
        }
    }
    
    public static class Polyline extends Shape
    {
        public final static String TAG = "polyline";
        
        public Polyline()
        {
            super(TAG); 
        }
                 
        public Polyline(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
                                 
            String pointsString = getPropertyValue("points");

            if (!pointsString.isEmpty()) attributes.AddAttribute("points", pointsString);
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            
            Rectangle result = new Rectangle(getPropertyValue("viewBox"));
            if (!getPropertyValue("x").isEmpty())
            {
                float x = getMeasureProperty("x").getValueInMm100th();
                result.x=result.x+x;
            }
            if (!getPropertyValue("y").isEmpty())
            {
                float y = getMeasureProperty("y").getValueInMm100th();
                result.y=result.y+y;
            }
            return result;
        }
    }
    
    public static class Image extends Shape
    {
        public final static String TAG = "image";
        private String base64EncodedImage;
        
        public Image()
        {
            super(TAG); 
            base64EncodedImage = "";
        }
                 
        public Image(String originalClassName, XAttributeList propertyAttributes) 
        {
            super(TAG, propertyAttributes);
            base64EncodedImage = "";
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        public void appendBase64EncodedImage(String characters)
        {
            base64EncodedImage+=characters;
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
                                 
            String xString = getPropertyValue("x");
            String yString = getPropertyValue("y");
            String widthString = getPropertyValue("width");
            String heightString = getPropertyValue("height");
            String hrefString = getPropertyValue("href");
            
            
            if (!xString.isEmpty()) attributes.AddAttribute("x",Float.toString(new Measure(xString).getValueInMm100th()));
            if (!yString.isEmpty()) attributes.AddAttribute("y",Float.toString(new Measure(yString).getValueInMm100th()));
            if (!widthString.isEmpty()) attributes.AddAttribute("width",Float.toString(new Measure(widthString).getValueInMm100th()));
            if (!heightString.isEmpty()) attributes.AddAttribute("height",Float.toString(new Measure(heightString).getValueInMm100th()));
            if (!hrefString.isEmpty()) 
            {
                if (hrefString.length()>1 && hrefString.startsWith("#")) 
                {
                    hrefString = hrefString.substring(1);
                }
                attributes.AddAttribute("xlink:href",hrefString);
            }
            else if (base64EncodedImage!=null && !base64EncodedImage.isEmpty())
            {
                attributes.AddAttribute("xlink:href","data:image/png;base64,"+base64EncodedImage);
            }
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            float x = getMeasureProperty("x").getValueInMm100th();
            float y = getMeasureProperty("y").getValueInMm100th();
            float width = getMeasureProperty("width").getValueInMm100th();
            float height = getMeasureProperty("height").getValueInMm100th();

            return new Rectangle(x,y,width,height);
        }
    }
    
    public static class Circle extends Shape
    {
        public final static String TAG = "circle";
        
        public Circle()
        {
            super(TAG);
        }
        
        public Circle(String originalClassName, XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
            
            String xValString = getPropertyValue("x");
            String yValString = getPropertyValue("y");
            String widthValString = getPropertyValue("width");
            
            float r = new Measure(widthValString).getValueInMm100th()/2.0f;
            float x = new Measure(xValString).getValueInMm100th()+r;
            float y = new Measure(yValString).getValueInMm100th()+r;
            
            attributes.AddAttribute("cx", Float.toString(x));
            attributes.AddAttribute("cy", Float.toString(y));
            attributes.AddAttribute("r", Float.toString(r));
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            float x = getMeasureProperty("x").getValueInMm100th();
            float y = getMeasureProperty("y").getValueInMm100th();
            float width = getMeasureProperty("width").getValueInMm100th();
            float height = getMeasureProperty("height").getValueInMm100th();

            return new Rectangle(x,y,width,height);
        }
    }
    
    public static class Ellipse extends Shape
    {
        public final static String TAG = "ellipse";
        
        public Ellipse()
        {
            super(TAG);
        }
        
        public Ellipse(String originalClassName, XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            this.originClassName = originalClassName;
            parseTransformProperty();
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            String cssClasses = getCssClasses();
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", getCssClasses());
            
            String xValString = getPropertyValue("x");
            String yValString = getPropertyValue("y");
            String widthValString = getPropertyValue("width");
            String heightValString = getPropertyValue("height");
            
            float rx = new Measure(widthValString).getValueInMm100th()/2.0f;
            float ry = new Measure(heightValString).getValueInMm100th()/2.0f;
            float x = new Measure(xValString).getValueInMm100th()+rx;
            float y = new Measure(yValString).getValueInMm100th()+ry;
            
            attributes.AddAttribute("cx", Float.toString(x));
            attributes.AddAttribute("cy", Float.toString(y));
            attributes.AddAttribute("rx", Float.toString(rx));
            attributes.AddAttribute("ry", Float.toString(ry));
            
            String tranformString = getPropertyValue("transform");
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            
            return attributes;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            float x = getMeasureProperty("x").getValueInMm100th();
            float y = getMeasureProperty("y").getValueInMm100th();
            float width = getMeasureProperty("width").getValueInMm100th();
            float height = getMeasureProperty("height").getValueInMm100th();

            return new Rectangle(x,y,width,height);
        }
    }
    
    public static class TSpan extends Shape
    {
        public final static String TAG = "tspan";
        
        private String text;
        
        private TextParagraph parentParagraph;
        
        public TSpan()
        {
            super(TAG);
            text = "";
            parentParagraph = null;
        }
        
        public TSpan(XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            text = "";
            parentParagraph = null;
        }
              
        protected void setParent (TextParagraph paragraph)
        {
            parentParagraph = paragraph;
        }
        
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            
            String cssClasses = "";
            // parent text paragraph classes:
            if (parentParagraph!=null && parentParagraph.getParagraphStyleName()!=null)
            {
                cssClasses += parentParagraph.getPropertyValue("style-name");
            }
            // text span classes:
            cssClasses += getCssClasses();
            String styleNameString = getPropertyValue("style-name");
            if (!styleNameString.isEmpty()) 
            {
                /*
                new String(textAtts.getValueByName("class")+" "+spanAtts.getValueByName("class")).trim()
                */
                if (!cssClasses.isEmpty()) cssClasses += " ";
                cssClasses += styleNameString;
            }
            attributes.AddAttribute("class", cssClasses);
            
            String xString = getPropertyValue("x");
            String yString = getPropertyValue("y");
            String dxString = getPropertyValue("dx");
            String dyString = getPropertyValue("dy");
            String lengthAdjustString = getPropertyValue("lengthAdjust");
            String rotateString = getPropertyValue("rotate");
            String textLengthString = getPropertyValue("textLength");
            
            if (!xString.isEmpty()) attributes.AddAttribute("x", xString);
            if (!yString.isEmpty()) attributes.AddAttribute("y", yString);
            if (!dxString.isEmpty()) attributes.AddAttribute("dx", dxString);
            if (!dyString.isEmpty()) attributes.AddAttribute("dy", dyString);
            if (!lengthAdjustString.isEmpty()) attributes.AddAttribute("lengthAdjust", lengthAdjustString);
            if (!rotateString.isEmpty()) attributes.AddAttribute("rotate", rotateString);
            if (!textLengthString.isEmpty()) attributes.AddAttribute("textLength", textLengthString);
            
            return attributes;
        }
        
        public String getPropertyInclParagraphOrStandard(String propertyName)
        {
            String value = "";
            
            Styles.TextStyle spanStyle = Styles.getTextStyle(getPropertyValue("style-name"));
            // span style
            if (spanStyle!=null && !spanStyle.getPropertyValue(propertyName).isEmpty())
            {
                value = spanStyle.getPropertyValue(propertyName);
            }
            else if (parentParagraph!=null)// paragraph style
            {
                Styles.ParagraphStyle paraStyle = Styles.getPragraphStyle(parentParagraph.getPropertyValue("style-name"));
                if (paraStyle!=null && !paraStyle.getPropertyValue(propertyName).isEmpty())
                {
                    value = paraStyle.getPropertyValue(propertyName);
                }
                else
                {
                    Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
                    if (standardStyle!=null && !standardStyle.getPropertyValue(propertyName).isEmpty())
                    {
                        value = standardStyle.getPropertyValue(propertyName);
                    }
                }
            }
            else // standard
            {
                Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
                if (standardStyle!=null && !standardStyle.getPropertyValue(propertyName).isEmpty())
                {
                    value = standardStyle.getPropertyValue(propertyName);
                }
            }
            
            return value;
        }
        
        public java.awt.Font getFont()
        {
            String fontName = getPropertyInclParagraphOrStandard("font-name");
            if (fontName.isEmpty()) return null;
            
            Styles.FontDeclStyle fontDecl = Styles.getFontDeclStyle(fontName);
            if (fontDecl!=null)
            {
                String family = fontDecl.getPropertyValue("font-family");
                if (family.startsWith("'") && family.endsWith("'")) 
                {
                    family = family.substring(1, family.length()-1);
                }
                int pointSize=0;
                String p_val_font_size = getPropertyInclParagraphOrStandard("font-size");
                if (!p_val_font_size.isEmpty()) 
                {
                    pointSize = Math.round(new Measure(p_val_font_size).getValueInMm100th());
                }
                if (pointSize==0) pointSize=1;
                int italic = (getPropertyInclParagraphOrStandard("font-style").equals("italic")) ? java.awt.Font.ITALIC : 0;
                int bold = (getPropertyInclParagraphOrStandard("font-weight").equals("bold")) ? java.awt.Font.BOLD : 0;
                return new java.awt.Font(family, italic | bold, pointSize);
            }
            return null;
        }
        
        public float getFontHeight()
        {
            float height = 0.0f;

            java.awt.Font font = getFont();
            if (font!=null)
            {
                java.awt.Component canvas = new java.awt.Canvas();
                java.awt.FontMetrics fontMetrics = canvas.getFontMetrics(font);
                height = (float) fontMetrics.getHeight();
            }
            else
            {
                String p_val_font_size = getPropertyInclParagraphOrStandard("font-size");
                if (!p_val_font_size.isEmpty()) 
                {
                    height = Math.round(new Measure(p_val_font_size).getValueInMm100th());
                }
            }
            return height;
        }
        
        /**
         * uses java.awt.fontmetrics to estimate the string with of the spans text, also includes letter-spacing style property
         * @return
         */
        public float getWidthEstimation()
        {
            return getWidthEstimation(this.text);
        }
        
        /**
         * for estimation of strings before insertion, use this method
         * @param someText
         * @return
         */
        public float getWidthEstimation(String someText)
        {
            if (someText.length()<1) return 0.0f;
            
            java.awt.Component canvas = new java.awt.Canvas();
            java.awt.Font font = getFont();
            int width;
            float letterSpacing = (new Measure(getPropertyInclParagraphOrStandard("letter-spacing")).getValueInMm100th());
            if (font!=null)
            {
                java.awt.FontMetrics fontMetrics = canvas.getFontMetrics(font);
                width = fontMetrics.stringWidth(someText);
                // text width + spacing between letters (might also be negative)
                return (float)width + letterSpacing*(someText.length()-1);
            }
            else return 0.0f;
        }
        
        /**
         * for estimation of a single character before insertion, use this method
         * @param someChar
         * @return
         */
        public float getWidthEstimation(char someChar)
        {           
            java.awt.Component canvas = new java.awt.Canvas();
            java.awt.Font font = getFont();
            if (font!=null)
            {
                java.awt.FontMetrics fontMetrics = canvas.getFontMetrics(font);
                return fontMetrics.stringWidth(Character.toString(someChar));
            }
            else return 0.0f;
        }
  
        public void appendText(String characters)
        {
            appendText(this, characters);
        }
        
        public static void appendText(TSpan spanToAddTextTo, String characters)
        {
            TSpan span = spanToAddTextTo;
            if (span==null || characters==null || characters.isEmpty()) return;
            
            // easy way: if not wrapping, or if not possible because missing parent data: just append text
            if (span.parentParagraph==null || !span.parentParagraph.isWrapping() || span.parentParagraph.getOriginalParentShape()==null)
            {
                span.text+=characters;
                return;
            }
            // else auto line breaking 
            
            Shape originalParentShape = span.parentParagraph.getOriginalParentShape();

            float textBoxWidth = originalParentShape.getTextBoundingBox().width;
            float currentParagraphWidth;

            float letterSpacing = (new Measure(span.getPropertyInclParagraphOrStandard("letter-spacing")).getValueInMm100th());

            // add word-wise (for auto-linebreaking), added words will be removed from front of the list
            ArrayList<String> wordsToAppend = WordList.splitWords(characters);
            if (wordsToAppend.isEmpty()&& !characters.isEmpty()) wordsToAppend.add(characters);
            while (!wordsToAppend.isEmpty())
            {
                // update current width
                currentParagraphWidth = span.parentParagraph.getWidthEstimation();

                String word = wordsToAppend.get(0);
                float wordWidth = span.getWidthEstimation(word);

                // if word fits: simply add to current span and remove from list
                if (currentParagraphWidth + letterSpacing + wordWidth < textBoxWidth)
                {
                    // skip leading whitespaces in lines
                    if (span.text.isEmpty() && word.length()==1 && Character.isWhitespace(word.charAt(0)))
                    {
                        wordsToAppend.remove(0);
                    }
                    else
                    {
                        span.text += word;
                        wordsToAppend.remove(0);
                    }
                }
                // if word does not fit
                else
                {
                    // if there is no text in this parent paragraph yet, add at at least as many characters of first word as possible, absolute minimum is 1 character!
                    if (span.parentParagraph.getTextOfAllSpans().isEmpty())
                    {
                        String substring;
                        String rest;
                        boolean foundSomeFittingSubstring = false;
                        for (int l=word.length();l>0;l--)
                        {
                            substring = word.substring(0,l);    // from first char (inclusive) up until l (excluding l)
                            rest = (l < word.length()) ? word.substring(l) : "";  // from l (inclusive) up until end (length)
                            wordWidth = span.getWidthEstimation(substring);
                            // if substring fits, add substring, keep rest
                            if (currentParagraphWidth + letterSpacing + wordWidth < textBoxWidth)
                            {
                                span.text += substring;
                                if (rest.length()>0) wordsToAppend.set(0, rest);
                                else wordsToAppend.remove(0); // if rest.length()==0 (but this should not even happen)
                                foundSomeFittingSubstring = true;
                                break;
                            }
                            // else will try one character less in next for loop cycle
                        }
                        // if not even one character fits, add at least the first character, memorize the rest
                        if (!foundSomeFittingSubstring)
                        {
                            substring = word.substring(0, 1);   // only first char
                            rest = (1 < word.length()) ? word.substring(1): ""; // rest from 1
                            span.text += substring;
                            if (rest.length()>0) wordsToAppend.set(0, rest);
                            else wordsToAppend.remove(0); // if rest.length()==0 (but this should not even happen)
                        }
                    }

                    // create line break (insert new text paragraph with new span, update parentParagraph and every thing to continue loop correctly)
                    
                    // if current line ends with whitespace: remove last whitespace from end
                    if (span.text.length() > 0 && Character.isWhitespace(span.text.charAt(span.text.length()-1)))
                    {
                        span.text = span.text.substring(0,span.text.length()-1);
                    }
                    
                    // if paragraph is set to justify: set word-spacing for spans in current line
                    Styles.ParagraphStyle textParStyle = span.parentParagraph.getParagraphStyle();
                    if (textParStyle!=null && textParStyle.getPropertyValue("text-align").equals("justify"))
                    {
                        int numWhitespaceChars = span.parentParagraph.getNumWhitespaceChars();
                        currentParagraphWidth = span.parentParagraph.getWidthEstimation();
                        float widthDiff = textBoxWidth - currentParagraphWidth;
                        if (widthDiff>0.0f && numWhitespaceChars>0)
                        {
                            float wordSpacing = widthDiff / (float) numWhitespaceChars;
                            String style = span.parentParagraph.getPropertyValue("style");
                            if (style.isEmpty())
                            {
                                span.parentParagraph.properties.put("style", "word-spacing: "+wordSpacing+"px;");
                            }
                            else
                            {
                                int wSpos = style.indexOf("word-spacing:");
                                if (wSpos>=0)
                                {
                                    // crop out existing word-spacing-defs:
                                    int semicolonPos = style.substring(wSpos).indexOf(';');
                                    if (semicolonPos>=0)
                                    {
                                        style = (style.substring(0,wSpos)+style.substring(wSpos+semicolonPos+1)).trim();
                                    }
                                }
                                span.parentParagraph.properties.replace("style", style+" word-spacing: "+wordSpacing+"px;");
                            }
                        }
                    }
                    
                    // copy properties to new text paragraph after line break
                    TextParagraph newParagraph = new TextParagraph();                    
                    Set<String> propertyKeys = span.parentParagraph.properties.keySet();
                    for (String key : propertyKeys)
                    {
                        if (!key.isEmpty() && !key.equals("id") && !key.equals("name")) // id was automatically generated for new one, don't copy it from old paragraph
                            newParagraph.properties.put(key, span.parentParagraph.properties.get(key));
                    }
                    newParagraph.properties.put("isNewline", "true");
                    newParagraph.setOriginalParentShape(span.parentParagraph.getOriginalParentShape());
                    originalParentShape.addTextParagraph(newParagraph);
                    span.parentParagraph = newParagraph; // update

                    // copy properties to new span in new paragraph to continue there
                    TSpan newSpan = new TSpan();
                    propertyKeys = span.properties.keySet();
                    for (String key : propertyKeys)
                    {
                        if (!key.isEmpty() && !key.equals("id") && !key.equals("name")) // id was automatically generated for new one, don't copy it from old paragraph
                            newSpan.properties.put(key, span.properties.get(key));
                    }
                    newParagraph.addTSpan(newSpan);
                    // proceed with new span (in new text line)
                    span = newSpan;
                    // --> next loop cycle will continue adding text there 
                }
            }
        }
        
        public String getText()
        {
            return text;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            return null;
        }       
    }
    
    //TextParagraph
    public static class TextParagraph extends Shape
    {
        public final static String TAG = "text";
        
        private final ArrayList<TSpan> tspans;
        
        private Shape originalParentShape;
        
        public TextParagraph()
        {
            super(TAG);
            tspans = new ArrayList<TSpan>();
            originalParentShape=null;
        }
        
        public TextParagraph(XAttributeList propertyAttributes)
        {
            super(TAG, propertyAttributes);
            tspans = new ArrayList<TSpan>();
            originalParentShape=null;
        }
        
        @Override
        public String getParagraphStyleName() { return getPropertyValue("style-name"); }
                
        @Override
        public XAttributeList getSvgAttributes()
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("id", getPropertyValue("id"));
            
            String cssClasses = getCssClasses();
            String styleNameString = getPropertyValue("style-name");
            if (styleNameString.isEmpty())
            {
                Shape parentShape = getOriginalParentShape();
                if (parentShape!=null) styleNameString = parentShape.getParagraphStyleName();
            }
            if (!styleNameString.isEmpty()) 
            {
                if (!cssClasses.isEmpty()) cssClasses += " ";
                cssClasses += styleNameString;
            }
            if (!cssClasses.isEmpty()) attributes.AddAttribute("class", cssClasses);
           
            String xString = getPropertyValue("x");
            String yString = getPropertyValue("y");
            String dxString = getPropertyValue("dx");
            String dyString = getPropertyValue("dy");
            String lengthAdjustString = getPropertyValue("lengthAdjust");
            String rotateString = getPropertyValue("rotate");
            String textLengthString = getPropertyValue("textLength");
            String styleString = getPropertyValue("style");
            String tranformString = getPropertyValue("transform");
            
            if (!xString.isEmpty()) attributes.AddAttribute("x", xString);
            if (!yString.isEmpty()) attributes.AddAttribute("y", yString);
            if (!dxString.isEmpty()) attributes.AddAttribute("dx", dxString);
            if (!dyString.isEmpty()) attributes.AddAttribute("dy", dyString);
            if (!lengthAdjustString.isEmpty()) attributes.AddAttribute("lengthAdjust", lengthAdjustString);
            if (!rotateString.isEmpty()) attributes.AddAttribute("rotate", rotateString);
            if (!textLengthString.isEmpty()) attributes.AddAttribute("textLength", textLengthString);
            if (!tranformString.isEmpty()) attributes.AddAttribute("transform", tranformString);
            if (!styleString.isEmpty()) attributes.AddAttribute("style", styleString);
                        
            return attributes;
        }
        
        /**
         * Also sets the spans parent by this paragraph
         * @param tspan
         */
        public void addTSpan(TSpan tspan)
        {
            tspans.add(tspan);
            tspan.setParent(this);
        }
        
        /**
         * calculates the baseline y offset needed from top
         * @return
         */
        public float getMaxLeadingPlusAscentOfSpans()
        {
            float result = 0.0f;
            if (tspans.size()<1) return result;
            java.awt.Component canvas = new java.awt.Canvas();
            java.awt.Font font;
            java.awt.FontMetrics fontMetrics;
            for (TSpan span : tspans)
            {
                font = span.getFont();
                if (font!=null)
                {
                    fontMetrics = canvas.getFontMetrics(font);
                    result = Math.max(result, fontMetrics.getLeading()+fontMetrics.getAscent());
                }
            }
            return result;
        }
        
        public float getMaxDescentOfSpans()
        {
            float result = 0.0f;
            if (tspans.size()<1) return result;
            java.awt.Component canvas = new java.awt.Canvas();
            java.awt.Font font;
            java.awt.FontMetrics fontMetrics;
            for (TSpan span : tspans)
            {
                font = span.getFont();
                if (font!=null)
                {
                    fontMetrics = canvas.getFontMetrics(font);
                    result = Math.max(result, fontMetrics.getDescent());
                }
            }
            return result;
        }
        
        public float getMaxAscentOfSpans()
        {
            float result = 0.0f;
            if (tspans.size()<1) return result;
            java.awt.Component canvas = new java.awt.Canvas();
            java.awt.Font font;
            java.awt.FontMetrics fontMetrics;
            for (TSpan span : tspans)
            {
                font = span.getFont();
                if (font!=null)
                {
                    fontMetrics = canvas.getFontMetrics(font);
                    result = Math.max(result, fontMetrics.getAscent());
                }
            }
            return result;
        }
        
        public float getVerticalMarginBeforeLine()
        {
            float marginBeforeCurrent = 0.0f;
            float marginAfterLast = 0.0f;
            Styles.ParagraphStyle paragraphStyle = Styles.getPragraphStyle(getPropertyValue("style-name"));
            
            Shape parentShape = getOriginalParentShape();
            if (parentShape!=null && paragraphStyle!=null && !getPropertyValue("isNewline").equals("true"))
            {
                int indexCurrent = parentShape.getTextParagraphChildren().indexOf(this);
                if (indexCurrent>0)
                {
                    String marginBeforeCurrentString = paragraphStyle.getPropertyValue("margin-top");
                    if (!marginBeforeCurrentString.isEmpty()) marginBeforeCurrent = new Measure(marginBeforeCurrentString).getValueInMm100th();
                    
                    TextParagraph lastParagraph = parentShape.getTextParagraphChildren().get(indexCurrent-1);
                    Styles.ParagraphStyle lastParagraphStyle = Styles.getPragraphStyle(lastParagraph.getPropertyValue("style-name"));
                    if (lastParagraphStyle!=null)
                    {
                        String marginAfterLastString = lastParagraphStyle.getPropertyValue("margin-bottom");
                        if (!marginAfterLastString.isEmpty()) marginAfterLast = new Measure(marginAfterLastString).getValueInMm100th();
                        return Math.max(marginBeforeCurrent, marginAfterLast);
                    }
                }
            }
            return 0.0f; 
        }
        
        public float getMaxFontsizeOfSpans()
        {
            float fontSize=0.0f;
            if (tspans.size()<1)
            {
                Styles.ParagraphStyle paraStyle = Styles.getPragraphStyle(getPropertyValue("style-name"));
                if (paraStyle!=null && !paraStyle.getPropertyValue("font-size").isEmpty())
                {
                    String p_par_font_size = paraStyle.getPropertyValue("font-size");
                    if (!p_par_font_size.isEmpty()) fontSize = Math.max(fontSize, new Measure(p_par_font_size).getValueInMm100th());   
                }
                else // default font-size
                {
                    Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
                    if (standardStyle!=null && !standardStyle.getPropertyValue("font-size").isEmpty())
                    {
                        String p_standard_font_size = standardStyle.getPropertyValue("font-size");
                        if (!p_standard_font_size.isEmpty()) fontSize = Math.max(fontSize, new Measure(p_standard_font_size).getValueInMm100th());   
                    }
                }
            }
            else for (TSpan span : tspans)
            {              
                Styles.TextStyle spanStyle = Styles.getTextStyle(span.getPropertyValue("style-name"));
                // try to get font-size from span style
                if (spanStyle!=null && !spanStyle.getPropertyValue("font-size").isEmpty())
                {
                    String p_span_font_size = spanStyle.getPropertyValue("font-size");
                    if (!p_span_font_size.isEmpty()) fontSize = Math.max(fontSize, new Measure(p_span_font_size).getValueInMm100th());   
                }
                else // font-size from paragraph style
                {
                    Styles.ParagraphStyle paraStyle = Styles.getPragraphStyle(getPropertyValue("style-name"));
                    if (paraStyle!=null && !paraStyle.getPropertyValue("font-size").isEmpty())
                    {
                        String p_par_font_size = paraStyle.getPropertyValue("font-size");
                        if (!p_par_font_size.isEmpty()) fontSize = Math.max(fontSize, new Measure(p_par_font_size).getValueInMm100th());   
                    }
                    else // default font-size
                    {
                        Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
                        if (standardStyle!=null && !standardStyle.getPropertyValue("font-size").isEmpty())
                        {
                            String p_standard_font_size = standardStyle.getPropertyValue("font-size");
                            if (!p_standard_font_size.isEmpty()) fontSize = Math.max(fontSize, new Measure(p_standard_font_size).getValueInMm100th());   
                        }
                    }
                }
                //fontSize = Math.max(fontSize, span.getFontHeight());
            }
            return fontSize;
        }
        
        public ArrayList<TSpan> getTSpans()
        {
            return tspans;
        }

        public boolean hasNonEmptyTSpans() 
        {
            for (TSpan tSpan : tspans)
            {
                if (!(tSpan.getText().isEmpty())) return true;
            }
            return false;
        }
        
        @Override
        public Rectangle getBoundingBox() {
            return null;
        }
        
        public float getLineHeight()
        {
            Shapes.Shape shape = getOriginalParentShape();
            float lineHeight = 0.0f;
            float fontSize;
            if (shape!=null)
            {
                int currentParIndex = shape.getTextParagraphChildren().indexOf(this);
                int numOfParsInParent = shape.getTextParagraphChildren().size();
                boolean lastLineInParent = (currentParIndex >= numOfParsInParent-1);
                
                Styles.ParagraphStyle shapeParagraphStyle = shape.getParagraphStyle();
                Styles.GraphicsStyle standardStyle = Styles.getGraphicsStyle("standard");
                
                if (shapeParagraphStyle!=null)
                {
                    fontSize = shapeParagraphStyle.getFontSize();
                    // but do font size correction if this line contains spans
                    if (lastLineInParent) lineHeight = fontSize;
                    else lineHeight = shapeParagraphStyle.getLineHeight(fontSize); // line height
                }
                else if (standardStyle!=null)
                {
                    fontSize = new Measure(standardStyle.getPropertyValue("font-size")).getValueInMm100th();
                    lineHeight = fontSize;
                }
            }
            return lineHeight;
        }
        
        /**
         * sums up the width estimation of contained spans, also includes letter-spacing style property between the spans.
         * @return
         */
        public float getWidthEstimation()
        {
            if (tspans.size()<1) return 0.0f;
            
            Styles.ParagraphStyle paragraphStyle = getParagraphStyle();
            
            float sum=0.0f;
            for (int i=0; i<tspans.size(); i++)
            {
                TSpan span = tspans.get(i);
                // for non-empty spans, except the first one, insert letter-spacing before (skip empty last, as it may not yet be filled with text
                if (i>0 && !(span.text.isEmpty() && i==tspans.size()-1))
                {
                    if (paragraphStyle!=null)
                    {
                        sum += new Measure(paragraphStyle.getPropertyValue("letter-spacing")).getValueInMm100th();
                    }
                }
                sum+=span.getWidthEstimation();
            }
            
            return sum;
        }

        public TSpan getLatestSpan() 
        {
            if (tspans.size()<1) return null;
            else return tspans.get(tspans.size()-1);            
        }

        public void setOriginalParentShape(Shape parent) 
        {
            originalParentShape = parent;
        }
        
        public Shape getOriginalParentShape()
        {
            return originalParentShape;
        }

        public String getTextOfAllSpans()
        {
            String result = "";
            for (TSpan span : tspans)
            {
                result+=span.getText();
            }
            return result;
        }
        
        /**
         * true for paragraphs within oo draw textboxes or within oo draw shapes, that have a graphic style with "fo:wrap-option" set to "wrap"
         * @return
         */
        public boolean isWrapping() 
        {
            if (originalParentShape instanceof TextBox) 
            {
                TextBox textBox = (TextBox) originalParentShape;
                Styles.GraphicsStyle tBgraphicsStyle = textBox.getGraphicsStyle();
                if (tBgraphicsStyle!=null && tBgraphicsStyle.getPropertyValue("textarea-horizontal-align").isEmpty())
                {
                    return true;
                }
            }
            else if (originalParentShape!=null)
            {
                Styles.GraphicsStyle graphicsStyle = originalParentShape.getGraphicsStyle();
                if (graphicsStyle!=null && 
                        (graphicsStyle.getPropertyValueInclParents("wrap-option").equals("wrap") ||
                         graphicsStyle.getPropertyValueInclParents("fit-to-contour").equals("true") ))
                {
                    return true;
                }
            }
            return false;
        }

        private int getNumWhitespaceChars() {
            String wholeText = getTextOfAllSpans();
            int num = 0;
            for(char c: wholeText.toCharArray())
            {
                if (Character.isWhitespace(c)) num++;
            }
            return num;
        }
    }
    
    public static void setLastAddedTextParagraph(TextParagraph textParagraph)
    {
        m_lastAddedTextParagraph = textParagraph;
    }
    
    public static TextParagraph getLastAddedTextParagraph()
    {
        return m_lastAddedTextParagraph;
    }
}
