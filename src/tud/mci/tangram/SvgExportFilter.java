package tud.mci.tangram;

import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XDrawView;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A XML Export filter, producing a SVG file from Office Draw document
 * @author Martin.Spindler@tu-dresden.de
 */
public final class SvgExportFilter extends com.sun.star.lib.uno.helper.WeakBase
   implements com.sun.star.lang.XServiceName,                   // for getServiceName() to provide the service name "tud.mci.tangram.SvgExportFilter" (to create such an object by a factory)
              com.sun.star.lang.XServiceInfo,                   // provides information regarding the implementation, i.e. which services are implemented and the name of the implementation
              com.sun.star.lang.XTypeProvider,                  // for providing interfaces types of this component
              com.sun.star.lang.XInitialization,                // for getting the current frame on initialization
              com.sun.star.xml.XExportFilter,                   // exporter() call will provide the com.sun.star.document.TypeDetection user data required to perform the filtering correctly 
              com.sun.star.xml.sax.XDocumentHandler             // for generating XML data by outputting sax events, forwards to the filters XExtendedDocumentHandler m_xHandler
              
{

// =============================================================================
// private data members
// =============================================================================
    /**
     * the XDocumentHandler
     */
    private com.sun.star.xml.sax.XExtendedDocumentHandler m_xHandler;
    private final boolean m_bPrettyPrint = true;
    private final com.sun.star.uno.XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_serviceName = "tud.mci.tangram.SvgExportFilter";
    private static final String m_implementationName = SvgExportFilter.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.document.ExportFilter",
        "com.sun.star.xml.XMLExportFilter" };
    private static short pageNumber = 0; // starting with 1
    private static long numPages = 0;
// =============================================================================
// constructor
// =============================================================================
    /**
    * Constructor
    * @param context The context.
    */
    public SvgExportFilter( com.sun.star.uno.XComponentContext context )
    {
        m_xContext = context;
        FillBitmapAssets.LoadTangramToolbarBitmaps(context);
    };

// =============================================================================
// component management / registration by com.sun.star.comp.loader.JavaLoader
// =============================================================================
    /** Gives a factory for creating the service(s).
     * This method is called by the <code>JavaLoader</code>
     * <p>
     * @return Returns a <code>XSingleComponentFactory</code> for creating the
     * component.
     * @see com.sun.star.comp.loader.JavaLoader
     * @param sImplementationName The implementation name of the component.
     */
    public static com.sun.star.lang.XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        com.sun.star.lang.XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = com.sun.star.lib.uno.helper.Factory.createComponentFactory(SvgExportFilter.class, m_serviceNames);
        return xFactory;
    }

    /** Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>.
     * @return returns true if the operation succeeded
     * @see com.sun.star.comp.loader.JavaLoader
     * @see com.sun.star.lib.uno.helper.Factory
     * @param xRegistryKey Makes structural information (except regarding tree
     * structures) of a single registry key accessible.
     */
    public static boolean __writeRegistryServiceInfo( com.sun.star.registry.XRegistryKey xRegistryKey ) {
        return com.sun.star.lib.uno.helper.Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

// =============================================================================
// com.sun.star.lang.XServiceName
// =============================================================================
    @Override
    public String getServiceName()
    {
        return m_serviceName;
    }

// =============================================================================
// com.sun.star.lang.XServiceInfo
// =============================================================================
    @Override
    public String getImplementationName() {
         return m_implementationName;
    }

    @Override
    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    @Override
    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }
    
// =============================================================================
// com.sun.star.lang.XTypeProvider
// =============================================================================
    @Override
    public byte[] getImplementationId() {
        return Integer.toString(this.hashCode()).getBytes();
    } 

    /**
     *
     * @return Sequence of all types (usually interface types) provided by the object.
     */
    @Override
    public com.sun.star.uno.Type[] getTypes() {
        com.sun.star.uno.Type[] typeReturn;
        try {
            typeReturn = new com.sun.star.uno.Type[] {
                new com.sun.star.uno.Type( com.sun.star.lang.XServiceName.class ),
                new com.sun.star.uno.Type( com.sun.star.lang.XServiceInfo.class ), 
                new com.sun.star.uno.Type( com.sun.star.lang.XTypeProvider.class ),
                new com.sun.star.uno.Type( com.sun.star.xml.XExportFilter.class ),
                new com.sun.star.uno.Type( com.sun.star.xml.sax.XDocumentHandler.class )
            };
        } catch( java.lang.Exception exception ) {
            return null;
        }
        return( typeReturn );
    }

// =============================================================================
// com.sun.star.lang.XInitialization
// =============================================================================
    @Override
    public void initialize(Object[] object) throws com.sun.star.uno.Exception 
    {
        if (object.length > 0) {
//            m_xFrame = (com.sun.star.frame.XFrame) UnoRuntime.queryInterface(com.sun.star.frame.XFrame.class, object[0]);
//            XDrawView drawView = (XDrawView) UnoRuntime.queryInterface(XDrawView.class, m_xFrame);
//            com.sun.star.beans.XPropertySet pageProperties = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, drawView.getCurrentPage());
//            short pageNumber = AnyConverter.toShort(pageProperties.getPropertyValue("Number")); // starting with 1
//            long numPages = ((com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface (com.sun.star.drawing.XDrawPagesSupplier.class, m_xFrame.getController().getModel())).getDrawPages().getCount();
            //m_xFrame.getController();
//            UnoRuntime.queryInterface(XTitle.class, );
        }
    }
    
// =============================================================================
// com.sun.star.xml.XExportFilter
// =============================================================================
    @Override
    public boolean exporter(com.sun.star.beans.PropertyValue[] exportMediaDescriptor, String[] msUserData) throws com.sun.star.lang.IllegalArgumentException {
        try {
            String sURL = null;
            String sName;
            boolean sSelectionOnly = false;
            com.sun.star.io.XOutputStream xos = null;

            // get interesting values from sourceData (Media Descriptor)
            for (com.sun.star.beans.PropertyValue mediaDescProperty : exportMediaDescriptor) {
                sName = mediaDescProperty.Name;
                if (sName.equals("OutputStream"))   // a stream to receive the document data.  
                {
                    xos = (com.sun.star.io.XOutputStream) AnyConverter.toObject(com.sun.star.io.XOutputStream.class, mediaDescProperty.Value);
                }
                else if (sName.equals("URL"))       // URL of the document
                {
                    sURL = AnyConverter.toString(mediaDescProperty.Value);
                }
                else if (sName.equals("SelectionOnly"))       // URL of the document
                {
                    sSelectionOnly = AnyConverter.toBoolean(mediaDescProperty.Value);
                }
            }
            
             Object desktopObj = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktopObj);
            if (xDesktop != null)
            {
                m_xFrame = xDesktop.getCurrentFrame();
                XController currentXController = m_xFrame.getController();
                XDrawView drawView = (XDrawView) UnoRuntime.queryInterface(XDrawView.class, currentXController);
                
                XModel xModel = currentXController.getModel();
                XDrawPagesSupplier drawPageSupplier = (XDrawPagesSupplier) UnoRuntime.queryInterface (XDrawPagesSupplier.class, xModel);
                
                XPropertySet pageProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, drawView.getCurrentPage());
                pageNumber = AnyConverter.toShort(pageProperties.getPropertyValue("Number")); // starting with 1
                numPages = drawPageSupplier.getDrawPages().getCount();
                //System.out.println("Page "+pageNumber+"/"+numPages);
            }
            
            
            /*
            String fileUrl = m_xFrame.getController().getModel().getURL();
            XTitle xTitle = UnoRuntime.queryInterface(XTitle.class, m_xFrame.getController());
            */
            
            // get selection
//            if (sSelectionOnly)
//            {
//                Object desktopObj = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
//                XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktopObj);
//                if (xDesktop != null)
//                {
//                    XFrame currentXFrame = xDesktop.getCurrentFrame();
//                    XController currentXController = currentXFrame.getController();
//                    XSelectionSupplier xSelectionSupplier = UnoRuntime.queryInterface(XSelectionSupplier.class, currentXController);
//                    if (xSelectionSupplier != null)
//                    {
//                        Object selectionAny = xSelectionSupplier.getSelection();
//                        XShapes selectedXShapes = (XShapes) AnyConverter.toObject(XShapes.class, selectionAny);
//                        if (selectedXShapes!=null && selectedXShapes.getCount() > 0)
//                        {
//                            System.err.println(selectedXShapes.getCount()+ " shape(s) selected."); 
//                            
//                            PropertyValue[] aNewDescriptor = new PropertyValue[exportMediaDescriptor.length + 1]; 
//                            java.lang.System.arraycopy(exportMediaDescriptor, 0, aNewDescriptor, 0, exportMediaDescriptor.length);
//                            aNewDescriptor[exportMediaDescriptor.length].Name = "ShapeSelection";
//                            aNewDescriptor[exportMediaDescriptor.length].Value = (Object) selectedXShapes;
//                            
//                            for (int i = 0; i < selectedXShapes.getCount(); i++)
//                            {
//                                Object shapeObj = selectedXShapes.getByIndex(i);
//                                XShape xShape = UnoRuntime.queryInterface(XShape.class, shapeObj);
//                                if (xShape != null)
//                                {
//                                    String shapeServiceType = xShape.getShapeType();
//                                    // https://wiki.openoffice.org/wiki/Documentation/DevGuide/Drawings/Shape_Types
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            
            // prepare the XML writer
            if (m_xHandler == null) 
            {
                Object xmlWriter = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.xml.sax.Writer", m_xContext);
                if (xmlWriter != null) 
                {
                    m_xHandler = (com.sun.star.xml.sax.XExtendedDocumentHandler) UnoRuntime.queryInterface(com.sun.star.xml.sax.XExtendedDocumentHandler.class, xmlWriter);
                }
            }
            if (m_xHandler == null) 
            {
                return false;
            }
            
            // Connect the provided output stream to the writer            		        
            com.sun.star.io.XActiveDataSource xADSource = (com.sun.star.io.XActiveDataSource) UnoRuntime.queryInterface(com.sun.star.io.XActiveDataSource.class, m_xHandler);

            if (xADSource != null && xos != null) 
            {
                xADSource.setOutputStream(xos);
            } 
            else 
            {
                return false;
            }
        } catch (com.sun.star.uno.Exception e) {
            return false;
        }

        // done ...
        return true;
    }
// =============================================================================
// com.sun.star.xml.sax.XDocumentHandler
// =============================================================================
    
    int m_doc_PageHit;          // incremented, if another (non-master) draw page was hit
    int m_doc_PageFinished; // ture, if page ist finished
    boolean m_doc_DrawDocOkay;  // true, if office:document was hit with attributes office:class="drawing" and office:version >= "1.2"
    ArrayList<XmlElement> m_doc_currentXmlPath; // e.g. list of for hierarchy within original document, e.g {office:document,office:automatic-styles,style:page-master,style:properties}
    int m_doc_currentPathDepthIndex; // depth of current xmlPath (set by startElement / endElement)
    XmlElement m_doc_currentElement;  // the current element (set by startElement / endElement)
    XmlElement m_doc_currentParent;  // the parent of the current element (set by startElement / endElement)
    XmlElement m_doc_currentGrandParent; // the grandParent of the current element
    
    enum SaxNotificationType {
        StartDocument, EndDocument, StartElement, EndElement, Characters, IgnorableWhitespace, ProcessingInstruction, SetDocumentLocator, IgnoredStartElement
    }
    private SaxNotificationType m_lastSaxNotificationType;
    
    @Override
    public void startDocument() throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        System.err.println("startDocument");
        
        // --- initialization --------------------------------------------------
        m_doc_PageHit = 0;
        m_doc_PageFinished = 0;
        m_doc_DrawDocOkay = false;
        m_doc_currentXmlPath = new ArrayList<XmlElement>();
        m_doc_currentElement = null;
        m_doc_currentPathDepthIndex = 0;
        m_doc_currentParent = null;
        m_doc_currentGrandParent = null;
        
        Styles.init();
        Shapes.init();
        Metadata.init();
        
        // --- start document --------------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.StartDocument;
        m_xHandler.startDocument();
    }
    
    @Override
    public void endDocument() throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        System.err.println("endDocument");
        
        // --- end document ----------------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.EndDocument;
        m_xHandler.endDocument();
    }
    
    @Override
    public void startElement(String aName, com.sun.star.xml.sax.XAttributeList xAttribs) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        if (m_lastSaxNotificationType==SaxNotificationType.StartElement ||
            m_lastSaxNotificationType==SaxNotificationType.EndElement)
        {
            System.err.println();
        }
        System.err.print("<"+aName);
        if (xAttribs.getLength()>0) System.err.print(" ");
        for (short i = 0; i<xAttribs.getLength();i++)
        {
            System.err.print(xAttribs.getNameByIndex(i) + "=\"");
            System.err.print(xAttribs.getValueByIndex(i) + "\"");
            if (i < (xAttribs.getLength() - 1)) System.err.print(" ");
        }
        System.err.print(">");
        
        // --- track current xml path ------------------------------------------
        m_doc_currentElement = new XmlElement(aName, xAttribs);
        m_doc_currentXmlPath.add(m_doc_currentElement);
        m_doc_currentPathDepthIndex = m_doc_currentXmlPath.size() - 1;
        m_doc_currentParent = (m_doc_currentPathDepthIndex > 0 ) ?
                m_doc_currentXmlPath.get(m_doc_currentPathDepthIndex-1) : 
                null;
        m_doc_currentGrandParent = (m_doc_currentPathDepthIndex > 1 ) ?
                m_doc_currentXmlPath.get(m_doc_currentPathDepthIndex-2) : 
                null;
        
        // --- start element ---------------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.StartElement;
        
        // --- root: check if right document, else end -------------------------
        if (aName.equals("office:document"))
        {
            if ((xAttribs.getValueByName("office:class").equals("drawing") || xAttribs.getValueByName("office:class").equals("graphics"))
                    &&
                Float.parseFloat(xAttribs.getValueByName("office:version")) >= 1.2)
            {
                 m_doc_DrawDocOkay = true;
            }
            else 
            {
                m_doc_DrawDocOkay = false;
                m_xHandler.endDocument();
            }
        }
        
        // --- metadata --------------------------------------------------------
        else if (aName.equals("dc:title")) // will be handled in characters()
        { }
        else if (aName.equals("dc:description")) // will be handled in characters()
        { }
        else if (aName.equals("dc:subject")) // will be handled in characters()
        { }
        else if (aName.equals("dc:date")) // will be handled in characters()
        { }
        else if (aName.equals("meta:creation-date")) // will be handled in characters()
        { }
        else if (aName.equals("meta:keyword")) // will be handled in characters()
        { }
        else if (aName.equals("meta:user-defined")) // will be handled in characters()
        { }
        
        // --- styles ----------------------------------------------------------
        else if (aName.equals("style:properties"))
        {
            // --- page-master styles ------------------------------------------
            if (m_doc_currentParent.name.equals("style:page-master"))
            {
                Styles.getMasterPageStyles().add(new Styles.MasterPageStyle(
                    m_doc_currentParent.attributes.getValueByName("style:name"),
                    xAttribs,
                    m_doc_currentParent.attributes.getValueByName("style:parent-style-name")
                ));
            }
            // --- default-style -----------------------------------------------
            else if (m_doc_currentParent.name.equals("style:default-style"))
            {
                Styles.DocLanguage = xAttribs.getValueByName("fo:language");
                Styles.DocCountry = xAttribs.getValueByName("fo:country");
            }
            // --- other styles ------------------------------------------------
            else if (m_doc_currentParent.name.equals("style:style"))
            {
                // --- page styles ---------------------------------------------
                if (m_doc_currentParent.attributes.getValueByName("style:family").equals("drawing-page"))
                {
                    Styles.getPageStyles().add(new Styles.PageStyle(
                        m_doc_currentParent.attributes.getValueByName("style:name"),
                        xAttribs,
                        m_doc_currentParent.attributes.getValueByName("style:parent-style-name")
                    ));
                }
                // --- graphics styles -----------------------------------------
                else if (m_doc_currentParent.attributes.getValueByName("style:family").equals("graphics"))
                {
                    Styles.getGraphicsStyles().add(new Styles.GraphicsStyle(
                        m_doc_currentParent.attributes.getValueByName("style:name"),
                        xAttribs,
                        m_doc_currentParent.attributes.getValueByName("style:parent-style-name")
                    ));
                }
                // --- paragraph styles ----------------------------------------
                else if (m_doc_currentParent.attributes.getValueByName("style:family").equals("paragraph"))
                {
                    Styles.getParagraphStyles().add(new Styles.ParagraphStyle(
                        m_doc_currentParent.attributes.getValueByName("style:name"),
                        xAttribs
                    ));
                }
                // --- paragraph styles ----------------------------------------
                else if (m_doc_currentParent.attributes.getValueByName("style:family").equals("text"))
                {
                    Styles.getTextStyles().add(new Styles.TextStyle(
                        m_doc_currentParent.attributes.getValueByName("style:name"),
                        xAttribs
                    ));
                }
            }
        }
        
        // --- font declarations -----------------------------------------------
        else if (aName.equals("style:font-decl"))
        {
            if (m_doc_currentParent.name.equals("office:font-decls"))
            {
                Styles.getFontDeclStyles().add(new Styles.FontDeclStyle(xAttribs.getValueByName("style:name"), xAttribs));
            }
        }
        
        // --- master pages ----------------------------------------------------
        else if (aName.equals("style:master-page"))
        {
            Styles.getMasterPages().add(new Styles.MasterPage(
                xAttribs.getValueByName("style:name"),
                xAttribs.getValueByName("style:page-master-name"),
                xAttribs.getValueByName("draw:style-name")
            ));
        }
        
        // --- stroke dash patterns --------------------------------------------
        else if (aName.equals("draw:stroke-dash"))
        {
            Styles.getStrokeDashStyles().add(new Styles.StrokeDashStyle(
                xAttribs.getValueByName("draw:name"),
                xAttribs
            ));
        }
        
        // --- marker defs -----------------------------------------------------
        else if (aName.equals("draw:marker"))
        {
            Styles.getMarkerStyles().add(new Styles.MarkerStyle(
                xAttribs.getValueByName("draw:name"),
                xAttribs
            ));
        }
        
        // --- layer definitions -----------------------------------------------
        else if (aName.equals("draw:layer"))
        {
            Shapes.addNewLayer(xAttribs.getValueByName("draw:name"));
        }
                // --- shape title -----------------------------------------------------
        else if (aName.equals("svg:title"))
        {
            // will be handled in characters()
        }
        // --- shape desc ------------------------------------------------------
        else if (aName.equals("svg:desc"))
        {
            // will be handled in characters()
        }
        // --- first draw:page: produce svg ------------------------------------
        else if (aName.equals("draw:page") && m_doc_DrawDocOkay)
        {
            m_doc_PageHit++;
        }
        
        // don't continue with shapes from other pages
        else if (m_doc_PageHit>0 && m_doc_PageHit!=pageNumber)
        {
            m_lastSaxNotificationType = SaxNotificationType.IgnoredStartElement;
        }
        
        // --- draw:g -------------------------------------------------------
        else if (aName.equals("draw:g"))
        {
            Shapes.addShape(new Shapes.G("drawG",xAttribs));
        }
        
        // --- draw:rect -------------------------------------------------------
        else if (aName.equals("draw:rect"))
        {
            Shapes.addShape(new Shapes.Rect("drawRect", xAttribs));
        }
        
        // --- draw:line -------------------------------------------------------
        else if (aName.equals("draw:line"))
        {
            Shapes.addShape(new Shapes.Line("drawLine", xAttribs));
        }
        
        // --- draw:path -------------------------------------------------------
        else if (aName.equals("draw:path"))
        {
            Shapes.addShape(new Shapes.Path("drawPath", xAttribs));
        }
        
        // --- draw:connector (as path) ----------------------------------------
        else if (aName.equals("draw:connector"))
        {
            XmlAttributeListImpl connectorAtts = new XmlAttributeListImpl(xAttribs);
            Shapes.addShape(new Shapes.Path("drawConnector", connectorAtts));
        }
        
        // --- draw:polygon ----------------------------------------------------
        else if (aName.equals("draw:polygon"))
        {
            Shapes.addShape(new Shapes.Polygon("drawPolygon", xAttribs));
        }
        
        // --- draw:polyline ---------------------------------------------------
        else if (aName.equals("draw:polyline"))
        {
            Shapes.addShape(new Shapes.Polyline("drawPolyline", xAttribs));
        }
        
        // --- draw:circle -----------------------------------------------------
        else if (aName.equals("draw:circle"))
        {
            Shapes.addShape(new Shapes.Circle("drawCircle", xAttribs));
        }
        
        // --- draw:ellipse ----------------------------------------------------
        else if (aName.equals("draw:ellipse"))
        {
            Shapes.addShape(new Shapes.Ellipse("drawEllipse", xAttribs));
        }
        
        // --- draw:image ------------------------------------------------------
        else if (aName.equals("draw:image"))
        {
            // if style for image defines opacity, set "opacity" property instead of "fill-opacity"
            String graphicStyleName = xAttribs.getValueByName("draw:style-name");
            if (!graphicStyleName.isEmpty())
            {
                Styles.GraphicsStyle graphicsStyle = Styles.getGraphicsStyle(graphicStyleName);
                if (graphicsStyle!=null)
                {
                    String transparencyString = graphicsStyle.getPropertyValue("transparency");
                    if (!transparencyString.isEmpty())
                    {
                        float opacity;
                        if (transparencyString.endsWith("%"))
                        {
                            opacity = 1.0f - Float.parseFloat(transparencyString.substring(0, transparencyString.length()-1))/100.0f;
                        }
                        else
                        {
                            opacity = 1.0f - Float.parseFloat(transparencyString);
                        }
                        graphicsStyle.properties.put("opacity",Float.toString(opacity));
                    }
                }
            }
            Shapes.addShape(new Shapes.Image("drawImage", xAttribs));
        }
        
        // --- office:binary-data ----------------------------------------------
        else if (aName.equals("office:binary-data"))
        {
            // will be handled in characters()
        }
        
        // ---------------------------------------------------------------------
        else if (aName.equals("draw:text-box"))
        {
            Shapes.addShape(new Shapes.TextBox("drawTextBox", xAttribs));
        }
        
        // --- text paragraph --------------------------------------------------
        else if (aName.equals("text:p"))
        {
            Shapes.TextParagraph textParagraph = new Shapes.TextParagraph(xAttribs);
            
            String parentName = m_doc_currentParent.name;
            Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
            // for shapes or textBoxes, add to Shape
            if (
                    (parentName.equals("draw:circle") && lastAddedShape instanceof Shapes.Circle) ||
                    (parentName.equals("draw:ellipse") && lastAddedShape instanceof Shapes.Ellipse) ||
                    (parentName.equals("draw:image") && lastAddedShape instanceof Shapes.Image) ||
                    (parentName.equals("draw:line") && lastAddedShape instanceof Shapes.Line) ||
                    (parentName.equals("draw:path") && lastAddedShape instanceof Shapes.Path) ||
                    (parentName.equals("draw:connector") && lastAddedShape instanceof Shapes.Path) ||
                    (parentName.equals("draw:polygon") && lastAddedShape instanceof Shapes.Polygon) ||
                    (parentName.equals("draw:polyline") && lastAddedShape instanceof Shapes.Polyline) ||
                    (parentName.equals("draw:rect") && lastAddedShape instanceof Shapes.Rect) ||
                    (parentName.equals("draw:text-box") && lastAddedShape instanceof Shapes.TextBox))
            {
                lastAddedShape.addTextParagraph(textParagraph);
            }
            // when outputting svg, non-empty texts that are children of the above shapes will create groups containing the shape and the text(s)
        }
        else if (aName.equals("text:span"))
        {
            if (m_doc_currentParent.name.equals("text:p"))
            {
                Shapes.TSpan tSpan = new Shapes.TSpan(xAttribs);
                Shapes.TextParagraph lastParagraph = Shapes.getLastAddedTextParagraph();
                Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
                if (lastParagraph!=null && lastAddedShape!=null)
                {
                    if (m_doc_currentGrandParent!=null)
                    {
                        // for shapes
                        if (
                            (m_doc_currentGrandParent.name.equals("draw:circle") && lastAddedShape instanceof Shapes.Circle) ||
                            (m_doc_currentGrandParent.name.equals("draw:ellipse") && lastAddedShape instanceof Shapes.Ellipse) ||
                            (m_doc_currentGrandParent.name.equals("draw:image") && lastAddedShape instanceof Shapes.Image) ||
                            (m_doc_currentGrandParent.name.equals("draw:line") && lastAddedShape instanceof Shapes.Line) ||
                            (m_doc_currentGrandParent.name.equals("draw:path") && lastAddedShape instanceof Shapes.Path) ||
                            (m_doc_currentGrandParent.name.equals("draw:connector") && lastAddedShape instanceof Shapes.Path) ||
                            (m_doc_currentGrandParent.name.equals("draw:polygon") && lastAddedShape instanceof Shapes.Polygon) ||
                            (m_doc_currentGrandParent.name.equals("draw:polyline") && lastAddedShape instanceof Shapes.Polyline) ||
                            (m_doc_currentGrandParent.name.equals("draw:rect") && lastAddedShape instanceof Shapes.Rect)||
                            (m_doc_currentGrandParent.name.equals("draw:text-box") && lastAddedShape instanceof Shapes.TextBox))
                        {
                            lastParagraph.addTSpan(tSpan);
                        }
                    }
                }
            }
        }
        else if (aName.equals("text:line-break"))
        {
            Shapes.TextParagraph lastParagraph = Shapes.getLastAddedTextParagraph();
            Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
            if (lastParagraph!=null && lastAddedShape!=null)
            {
                Shapes.TextParagraph textParagraph = new Shapes.TextParagraph();
                // copy properties to new text Paragraph
                Set<String> propertyKeys = lastParagraph.properties.keySet();
                for (String key : propertyKeys)
                {
                    if (!key.isEmpty() && !key.equals("id") && !key.equals("name"))
                        textParagraph.properties.put(key, lastParagraph.properties.get(key));
                }
                textParagraph.properties.put("isNewline", "true");
                lastAddedShape.addTextParagraph(textParagraph);
            }
        }
        
        // --- unknown element -------------------------------------------------
        else
        {
            m_lastSaxNotificationType = SaxNotificationType.IgnoredStartElement;
        }
    }

    @Override
    public void endElement(String aName) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        if (m_lastSaxNotificationType == SaxNotificationType.StartElement ||
            m_lastSaxNotificationType == SaxNotificationType.EndElement)
        {
            System.err.println();
        }
        System.err.print("</"+aName+">");
        
        // --- end element -----------------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.EndElement;
        if (aName.equals("draw:page"))
        {
            if (m_doc_PageHit == pageNumber)
            {
                // get master and style for the page
                Styles.MasterPage masterPage = Styles.getMasterPage(m_doc_currentElement.attributes.getValueByName("draw:master-page-name"));
                Styles.PageStyle pageStyle = Styles.getPageStyle(m_doc_currentElement.attributes.getValueByName("draw:style-name"));

                // setup svg attributes
                XmlAttributeListImpl svgAttributes = new XmlAttributeListImpl();
                svgAttributes.AddAttribute("id", m_doc_currentElement.attributes.getValueByName("draw:name"));
                svgAttributes.AddAttribute("version", "1.1");
                // svgAttributes.AddAttribute("baseProfile", "tiny");
                svgAttributes.AddAttribute("xmlns", "http://www.w3.org/2000/svg");
                svgAttributes.AddAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
                svgAttributes.AddAttribute("xmlns:cc", "http://creativecommons.org/ns#");
                svgAttributes.AddAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                svgAttributes.AddAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
                svgAttributes.AddAttribute("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape");
                svgAttributes.AddAttribute("xml:space", "preserve");
                if (masterPage!=null)
                {
                    //svgAttributes.AddAttribute("width",masterPage.getMasterPageStyle().getWidth().toString());
                    //svgAttributes.AddAttribute("height",masterPage.getMasterPageStyle().getHeight().toString());
                    //svgAttributes.AddAttribute("viewBox", "0 0 "+masterPage.getMasterPageStyle().getWidth().getValueInMm100th()+" "+masterPage.getMasterPageStyle().getHeight().getValueInMm100th());
                    float width = masterPage.getMasterPageStyle().getWidth().getValueInMm100th();
                    float height = masterPage.getMasterPageStyle().getHeight().getValueInMm100th();
                    float top = masterPage.getMasterPageStyle().getTop().getValueInMm100th();
                    float bottom = masterPage.getMasterPageStyle().getBottom().getValueInMm100th();
                    float left = masterPage.getMasterPageStyle().getRight().getValueInMm100th();
                    float right = masterPage.getMasterPageStyle().getBottom().getValueInMm100th();
                    float printWidth = width - right - left;
                    float printHeight = height - top - bottom;
                    svgAttributes.AddAttribute("width",printWidth/1000.0f+"cm");
                    svgAttributes.AddAttribute("height",printHeight/1000.0f+"cm");
                    svgAttributes.AddAttribute("viewBox", Float.toString(left)+" "+Float.toString(top)+" "+Float.toString(printWidth)+" "+Float.toString(printHeight));
                    svgAttributes.AddAttribute("onload", "init();");
                }
                // svg
                m_xHandler.startElement("svg", svgAttributes);

                // metadata
                if (!Metadata.dcTitle.isEmpty())
                {
                    m_xHandler.startElement("title",null); m_xHandler.characters(Metadata.dcTitle); m_xHandler.endElement("title");
                }
                if (!Metadata.dcDescription.isEmpty())
                {
                    m_xHandler.startElement("desc",null); m_xHandler.characters(Metadata.dcDescription); m_xHandler.endElement("desc");
                }
                m_xHandler.startElement("metadata", null);
                m_xHandler.startElement("rdf:RDF", null);
                m_xHandler.startElement("cc:Work", null);
                m_xHandler.startElement("dc:format",null); m_xHandler.characters("image/svg+xml"); m_xHandler.endElement("dc:format");
                m_xHandler.startElement("dc:type",new XmlAttributeListImpl("rdf:resource", "http://purl.org/dc/dcmitype/StillImage")); m_xHandler.endElement("dc:type");
                if (!Metadata.dcTitle.isEmpty())
                {
                    m_xHandler.startElement("dc:title",null); m_xHandler.characters(Metadata.dcTitle); m_xHandler.endElement("dc:title");
                }
                if (!Metadata.dcSubject.isEmpty())
                {
                    m_xHandler.startElement("dc:relation",null); m_xHandler.characters(Metadata.dcSubject); m_xHandler.endElement("dc:relation");
                }
                if (!Metadata.dcDescription.isEmpty())
                {
                    m_xHandler.startElement("dc:description",null); m_xHandler.characters(Metadata.dcDescription); m_xHandler.endElement("dc:description");
                }
                if (!Metadata.dcDate.isEmpty())
                {
                    m_xHandler.startElement("dc:date",null); m_xHandler.characters(Metadata.dcDate); m_xHandler.endElement("dc:date");
                }
                if (!Metadata.metaKeywords.isEmpty())
                {
                    m_xHandler.startElement("dc:subject",null);
                    m_xHandler.startElement("rdf:Bag",null);
                    for (String keyword : Metadata.metaKeywords)
                    {
                        m_xHandler.startElement("rdf:li",null);
                        m_xHandler.characters(keyword);
                        m_xHandler.endElement("rdf:li");
                    }
                    m_xHandler.endElement("rdf:Bag");
                    m_xHandler.endElement("dc:subject");
                }
                m_xHandler.endElement("cc:Work");
                m_xHandler.endElement("rdf:RDF");
                m_xHandler.endElement("metadata");


                // marker defs:
                m_xHandler.startElement("defs", null);
                for (Styles.MarkerStyle markerStyle : Styles.getMarkerStyles())
                {
                    m_xHandler.startElement("marker", markerStyle.getPropertiesAsAttributes());
                    m_xHandler.startElement("path", markerStyle.getPathAttributes());
                    m_xHandler.endElement("path");
                    m_xHandler.endElement("marker");
                }

                // fill images
                for (Styles.FillImage fillImage : Styles.getFillImages())
                {
                    FillBitmapAssets.PatternDefinition patternDefForBitmap = FillBitmapAssets.getPatternDefForBitmap(fillImage.getBase64ImageData());
                    // only if not replacable by shape pattern
                    if (patternDefForBitmap==null)
                    {
                        m_xHandler.startElement("image", fillImage.getPropertiesAsAttributes());
                        m_xHandler.endElement("image");
                    }
                }

                // fill patterns, using images
                for (Styles.Pattern pattern : Styles.getPatterns())
                {
                    m_xHandler.startElement("pattern", pattern.getPropertiesAsAttributes());

                    if (pattern.isImageFill())
                    {
                        svgAttributes = new XmlAttributeListImpl();
                        svgAttributes.AddAttribute("xlink:href", "#"+pattern.getFillImage().getName());
                        m_xHandler.startElement("use",svgAttributes);
                        m_xHandler.endElement("use");
                    }
                    else if (pattern.isPatternDefFill())
                    {
                        FillBitmapAssets.PatternDefinition patternDef = pattern.getPatternDefinition();
                        m_xHandler.startElement(patternDef.backgroundRect.name, patternDef.backgroundRect.attributes);
                        m_xHandler.endElement(patternDef.backgroundRect.name);
                        for (XmlElement patternContentShapeXml : patternDef.listOfShapes)
                        {
                            m_xHandler.startElement(patternContentShapeXml.name, patternContentShapeXml.attributes);
                            m_xHandler.endElement(patternContentShapeXml.name);
                        }
                    }

                    m_xHandler.endElement("pattern");
                }

                m_xHandler.endElement("defs");
                // javascript:
                m_xHandler.startElement("script", new XmlAttributeListImpl("type", "text/javascript"));
                m_xHandler.startCDATA();
                m_xHandler.characters("\n"+
    "\n"+
    "function init(){\n" +
    "	var all = document.getElementsByTagName(\"*\");\n" +
    "	for (var i=0, max=all.length; i < max; i++) \n" +
    "	{\n" +
    "		fitShapeLengthForMarkers(all[i]);\n" +
    "	}\n" +
    "}\n" +
    "\n" +
    "\n" +
    "function fitShapeLengthForMarkers(shape){\n" +
    "	if ((shape.nodeName != \"path\" && shape.nodeName != \"line\" && shape.nodeName != \"polyline\") ||\n" +
    "		(shape.parentNode && shape.parentNode.nodeName != \"g\" && shape.parentNode.nodeName != \"svg\"))\n" +
    "	{\n" +
    "		return;\n" +
    "	}\n" +
    "\n" +
    "	// calc length\n" +
    "	var length = 0;\n" +
    "	if (shape.nodeName == \"path\") length = shape.getTotalLength();\n" +
    "	else if (shape.nodeName == \"line\")  length = Math.sqrt(Math.pow(shape.x2.baseVal.value - shape.x1.baseVal.value, 2) + Math.pow(shape.y2.baseVal.value - shape.y1.baseVal.value, 2));\n" +
    "	else if (shape.nodeName == \"polyline\")  for (i = 1; i < shape.points.numberOfItems; i++ ) { length += Math.sqrt(Math.pow(shape.points.getItem(i).x - shape.points.getItem(i-1).x, 2) + Math.pow(shape.points.getItem(i).y - shape.points.getItem(i-1).y, 2)); } \n" +
    "	\n" +
    "	// get used markers\n" +
    "	var markerStartURL = window.getComputedStyle(shape).markerStart;\n" +
    "	var markerStartName = markerStartURL.substring(markerStartURL.lastIndexOf(\"#\")+1).replace(\")\",\"\").replace(\"\\\"\",\"\").replace(\"\\\"\",\"\");\n" +
    "	var markerStart = document.getElementById(markerStartName);\n" +
    "	var markerEndURL = window.getComputedStyle(shape).markerEnd;\n" +
    "	var markerEndName = markerEndURL.substring(markerEndURL.lastIndexOf(\"#\")+1).replace(\")\",\"\").replace(\"\\\"\",\"\").replace(\"\\\"\",\"\");\n" +
    "	var markerEnd = document.getElementById(markerEndName);\n" +
    "\n" +
    "	// if length > 0 and some marker was used\n" +
    "	if (length > 0 && (markerStart || markerEnd)){\n" +
    "\n" +
    "\n" +
    "		var startWidth = markerStart ? parseFloat(markerStart.getAttribute(\"markerWidth\")) : 0; \n" +
    "		startWidth = startWidth > 10 ? startWidth - 10 : 0;\n" +
    "		var endWidth = markerEnd ? parseFloat(markerEnd.getAttribute(\"markerWidth\")) : 0; \n" +
    "		endWidth = endWidth > 10 ? endWidth - 10 : 0;\n" +
    "\n" +
    "		var rest = length - startWidth - endWidth; \n" +
    "\n" +
    "		//console.log(\"\\n\"+shape.nodeName+\":\\t length: \"+length+\", startWidth: \"+startWidth+\", endWidth: \"+endWidth+\", rest: \"+rest);\n" +
    "		\n" +
    "		var oldDashArray = window.getComputedStyle(shape).strokeDasharray.replace(/px/g,\"\").replace(/ /g,\"\").split(\",\");\n" +
    "		if (oldDashArray[0]==\"none\")\n" +
    "		{\n" +
    "			//            on         off              on            off\n" +
    "			var newDash = 0 + \", \" + startWidth +\",\" + rest +\", \" + endWidth;\n" +
    "			shape.style.strokeDasharray = newDash;\n" +
    "			//console.log(\"new dash: \" + newDash);\n" +
    "		}\n" +
    "		else\n" +
    "		{\n" +
    "			// 0 on, start marker width off\n" +
    "			var newDash = 0 + \", \" + startWidth + \", \";\n" +
    "			// fill \"rest\" with old dash array, will start with an \"on\" value and end with an \"off\" value!\n" +
    "			var currentRest = 0;\n" +
    "			var on = false;	// track if current value is an on or off value\n" +
    "			while (currentRest<rest)\n" +
    "			{\n" +
    "				for (var i=0; i < oldDashArray.length && currentRest<rest; i++)\n" +
    "				{\n" +
    "					on = !on;\n" +
    "					var len = parseFloat(oldDashArray[i]);\n" +
    "					// does len fit?\n" +
    "					if (currentRest + len < rest)\n" +
    "					{\n" +
    "						newDash += len + \", \";\n" +
    "					}\n" +
    "					else // fill up\n" +
    "					{\n" +
    "						newDash += rest - currentRest+ \", \";\n" +
    "					}\n" +
    "					currentRest += len;\n" +
    "					// if not fitting\n" +
    "				}\n" +
    "			}\n" +
    "			// if end was not an off value, insert another:\n" +
    "			if (on) newDash += 0 + \", \";\n" +
    "			// 0 on, end marker width off\n" +
    "			newDash += 0 + \", \" + endWidth;\n" +
    "			shape.style.strokeDasharray = newDash;\n" +
    "			//console.log(\"old dash: \" + oldDashArray)\n" +
    "			//console.log(\"new dash: \" + newDash);\n" +
    "		}\n" +
    "	}\n" +
    "}\n"+
    "\n");
                m_xHandler.endCDATA();
                m_xHandler.endElement("script");

                // css styles:
                m_xHandler.startElement("style", new XmlAttributeListImpl("type", "text/css"));
                m_xHandler.startCDATA();
                m_xHandler.characters(Styles.getCssDefinitions());
                m_xHandler.endCDATA();
                m_xHandler.endElement("style");

                // remove temp group
                Shapes.getLayers().remove(Shapes.getLayerByOriginalName("tud_tangram_mci_Temp"));
                // insert all layers now, containing all the shapes
                for (Shapes.Layer layer : Shapes.getLayers())
                {
                    outputGroup(layer);
                }

                m_xHandler.endElement("svg");
            }
        }
        else if (m_doc_PageHit>0 && m_doc_PageHit!=pageNumber)
        {
            // ignore shapes within other pages
        }
        else if (aName.equals("draw:g"))
        {
            Shapes.endOfCurrentGroup();
        }
        else if (aName.equals("draw:rect"))
        {
        }
        else if (aName.equals("draw:line"))
        {
        }
        else if (aName.equals("draw:path"))
        {
        }
        else if (aName.equals("draw:connector"))
        {
        }
        else if (aName.equals("draw:polygon"))
        {   
        }
        else if (aName.equals("draw:polyline"))
        {
        }
        else if (aName.equals("draw:image"))
        {
        }
        else if (aName.equals("svg:title"))
        {
        }
        else if (aName.equals("svg:desc"))
        {
        }
        else if (aName.equals("text:p"))
        {
        }
        else if (aName.equals("text:span"))
        {
        }
        else if (aName.equals("draw:text-box"))
        {
        }
        
        // --- track current xml path ------------------------------------------
        if (m_doc_currentXmlPath.size()>0) m_doc_currentXmlPath.remove(m_doc_currentXmlPath.size()-1);
        m_doc_currentPathDepthIndex = m_doc_currentXmlPath.size() - 1;
        m_doc_currentElement = (m_doc_currentPathDepthIndex >= 0 ) ?
                m_doc_currentXmlPath.get(m_doc_currentPathDepthIndex):
                null;
        m_doc_currentParent = (m_doc_currentPathDepthIndex > 0 ) ?
                m_doc_currentXmlPath.get(m_doc_currentPathDepthIndex-1) : 
                null;
        m_doc_currentGrandParent = (m_doc_currentPathDepthIndex > 1 ) ?
            m_doc_currentXmlPath.get(m_doc_currentPathDepthIndex-2) : 
            null;
    }
    
    private void outputShape(Shapes.Shape shape)
    {
        try {
            //System.err.println("output "+shape.getOriginClassName()+ " as " + shape.getType()+", style: "+shape.getGraphicsStyleName());
            // start of shape
            if (shape.getType().equals(Shapes.G.TAG) && shape instanceof Shapes.G )
            {
                outputGroup((Shapes.G)shape);
            }
            
            else
            {
                if (shape.getType().equals(Shapes.Rect.TAG) && shape instanceof Shapes.Rect)
                {
                    Shapes.Rect rect = (Shapes.Rect) shape;
                    m_xHandler.startElement(Shapes.Rect.TAG, rect.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Line.TAG) && shape instanceof Shapes.Line)
                {
                    Shapes.Line line = (Shapes.Line) shape;
                    m_xHandler.startElement(Shapes.Line.TAG, line.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Path.TAG) && shape instanceof Shapes.Path)
                {
                    Shapes.Path path = (Shapes.Path) shape;
                    m_xHandler.startElement(Shapes.Path.TAG, path.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Polygon.TAG) && shape instanceof Shapes.Polygon)
                {
                    Shapes.Polygon polygon = (Shapes.Polygon) shape;
                    m_xHandler.startElement(Shapes.Polygon.TAG, polygon.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Polyline.TAG) && shape instanceof Shapes.Polyline)
                {
                    Shapes.Polyline polyline = (Shapes.Polyline) shape;
                    m_xHandler.startElement(Shapes.Polyline.TAG, polyline.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Circle.TAG) && shape instanceof Shapes.Circle)
                {
                    Shapes.Circle circle = (Shapes.Circle) shape;
                    m_xHandler.startElement(Shapes.Circle.TAG, circle.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Ellipse.TAG) && shape instanceof Shapes.Ellipse)
                {
                    Shapes.Ellipse ellipse = (Shapes.Ellipse) shape;
                    m_xHandler.startElement(Shapes.Ellipse.TAG, ellipse.getSvgAttributes());
                }
                else if (shape.getType().equals(Shapes.Image.TAG) && shape instanceof Shapes.Image)
                {
                    Shapes.Image image = (Shapes.Image) shape;
                    m_xHandler.startElement(Shapes.Image.TAG, image.getSvgAttributes());
                }
                if (!shape.title.isEmpty())
                {
                    m_xHandler.startElement("title", null);
                    m_xHandler.characters(shape.title);
                    m_xHandler.endElement("title");
                }
                if (!shape.desc.isEmpty())
                {
                    m_xHandler.startElement("desc", null);
                    m_xHandler.characters(shape.desc);
                    m_xHandler.endElement("desc");
                }
                // end of shape
                m_xHandler.endElement(shape.getType());
            }
        } catch (SAXException ex) {
            Logger.getLogger(SvgExportFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }                
             
    private void outputTextsForShape(Shapes.Shape shape)
    {
        try 
        {                  
            Rectangle shapeBoxBounds = shape.getBoundingBox();
            
            // the actual text area bounding box: get first estimation by shape itself
            Rectangle textBoxBounds = shape.getTextBoundingBox();
            
            // precalculate text block height for vertical alignment offset and block width max linewidth estimation
            float blockHeight = 0.0f;
            float blockWidth = textBoxBounds.width;
            
            // iterate paragraphs
            for (Shapes.TextParagraph textParagraph : shape.getTextParagraphChildren())
            {
                if (textParagraph.hasNonEmptyTSpans())  // skip empty paragraphs
                {
                    // add vertical space between last and current line if new paragraph (but not if just a newline) by respecting marginTop of this or marginBottom of last paragraph
                    blockHeight += textParagraph.getVerticalMarginBeforeLine();
                    // increment y by height of this current line for the next line
                    blockHeight += textParagraph.getLineHeight();
                    // width estimation:
                    blockWidth = Math.max(blockWidth,textParagraph.getWidthEstimation());
                }
            }
            
            // only not null for Line shapes with text
            Line line = (shape instanceof Shapes.Line) ? ((Shapes.Line)shape).getAsLine() : null;
            
            if (line!=null)
            {
                Line normalized = line.getRotationNormalizedLine();
                textBoxBounds.x = Math.min(normalized.x1, normalized.x2);
                textBoxBounds.y = normalized.y1;
                textBoxBounds.width = (normalized.x2-normalized.x1);
                textBoxBounds.height = 0.0f;
            }
            
            // top | middle | bottom | justify
            String vAlign = (shape.getGraphicsStyle()!=null) ? shape.getGraphicsStyle().getPropertyValueInclParents("textarea-vertical-align") : "center";
            if (vAlign.isEmpty() && shape instanceof Shapes.TextBox) vAlign="top";
            // left | center | right | justify  // TODO: center lines, that don't fit, else use hAlign!
            String hAlign = (shape.getGraphicsStyle()!=null) ? shape.getGraphicsStyle().getPropertyValueInclParents("textarea-horizontal-align") : "center";
            
            if (vAlign.equals("top"))
            {
                //if (line!=null) textBoxBounds.y -= shape.getMaxDescentOfFirstLine();
            }
            else if (vAlign.equals("bottom"))
            {
                textBoxBounds.y += (textBoxBounds.height - blockHeight);
            }
            else  // middle
            {
                textBoxBounds.y += (textBoxBounds.height - blockHeight)*0.5f;
            } 
            
            float dy = textBoxBounds.y - shapeBoxBounds.y; // vertical pos for text row
            
            // horizontally center text box if text is wider than textbox
            if (textBoxBounds.width<blockWidth)
            {
                textBoxBounds.x -= (blockWidth - textBoxBounds.width)/2.0f;
            }
            
            textBoxBounds.width = blockWidth;
            textBoxBounds.height = blockHeight;
            
            dy += shape.getMaxLeadingPlusAscentOfFirstLine();
            
            // DEBUG output of text position rectangle:
            //m_xHandler.startElement(Shapes.Rect.TAG, textBoxBounds.getAsSvgRectAttributes());
            //m_xHandler.endElement(Shapes.Rect.TAG);
            
            // the text paragraphs
            for (Shapes.TextParagraph textParagraph : shape.getTextParagraphChildren())
            {
                if (textParagraph.hasNonEmptyTSpans())
                {
                   
                    float dx = textBoxBounds.x - shapeBoxBounds.x;
                    
                    Styles.ParagraphStyle paragraphStyle = Styles.getPragraphStyle(textParagraph.getPropertyValue("style-name"));
                    
                    if (paragraphStyle!=null)
                    {
                        String textAlignString = paragraphStyle.getPropertyValue("text-align");
                                              
                        // horizontal alignment:
                        float diff = textBoxBounds.width - textParagraph.getWidthEstimation();
                        //System.err.println("textBoxBounds.width "+textBoxBounds.width+" textParagraph.getWidthEstimation "+textParagraph.getWidthEstimation());
                        if (diff<0.0f) // center if longer
                        {
                            dx += diff*0.5f;
                        }
                        else
                        {
                            if (textAlignString.equals("center"))
                            {
                                dx += diff*0.5f;
                            }
                            else if (textAlignString.equals("end"))
                            {
                                dx += diff;
                            }
                        }
                        
                    }
                    
                    // add vertical space between last and current line if new paragraph by respecting marginTop of this or marginBottom of last paragraph
                    dy += textParagraph.getVerticalMarginBeforeLine();
                    
                    String shapeTransformString = shape.getPropertyValue("transform");
                    
                    if (line!=null) 
                    {
                        textParagraph.properties.put("transform", "rotate("+line.getAngleInDeg()+" "+line.getCenterX()+" "+line.getCenterY()+")");
                        textParagraph.properties.put("x", Float.toString(shapeBoxBounds.x));
                        textParagraph.properties.put("y", Float.toString(shapeBoxBounds.y));
                    }
                    else if (!(shape instanceof Shapes.G || shape instanceof Shapes.TextBox) && !shapeTransformString.isEmpty())
                    {
                        textParagraph.properties.put("transform", shapeTransformString);
                    }
                    else
                    {
                        textParagraph.properties.put("x", Float.toString(shapeBoxBounds.x));
                        textParagraph.properties.put("y", Float.toString(shapeBoxBounds.y));
                    }
                    textParagraph.properties.put("dx", Float.toString(dx));
                    textParagraph.properties.put("dy", Float.toString(dy));
                    
                    // for testing:
//                    m_xHandler.startElement(Shapes.Line.TAG, new Line(textBoxBounds.x, y, textBoxBounds.x+textBoxBounds.width, y).getAsSvgRectAttributes());
//                    m_xHandler.endElement(Shapes.Line.TAG);
                    
                    // add text paragraph to output
                    XmlAttributeListImpl textAtts = new XmlAttributeListImpl(textParagraph.getSvgAttributes());
                    m_xHandler.startElement(Shapes.TextParagraph.TAG, textAtts);
                    
                    // the span children of a line
                    for (Shapes.TSpan tspan : textParagraph.getTSpans())
                    {
                        if (!tspan.getText().isEmpty())
                        {
                            m_xHandler.startElement(Shapes.TSpan.TAG, tspan.getSvgAttributes());
                            m_xHandler.characters(tspan.getText());
                            m_xHandler.endElement(Shapes.TSpan.TAG);
                        }
                    }
                   
                    m_xHandler.endElement(Shapes.TextParagraph.TAG);
                    
                    // increment dy by height of this current line for the next line
                    dy+=textParagraph.getLineHeight();
                }
            }
        }
        catch (SAXException ex) 
        {
            Logger.getLogger(SvgExportFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void outputGroup(Shapes.G group)
    {
        try 
        {
            // start of group element
            if (group instanceof Shapes.Layer)
            {
                // special treatment for generating layer attributes
                Shapes.Layer layer = (Shapes.Layer) group;
                m_xHandler.startElement("g", layer.getSvgAttributes());
            }
            else
            {
                m_xHandler.startElement("g", group.getSvgAttributes());
            }
            // title and desc output if defined
            if (!group.title.isEmpty())
            {
                m_xHandler.startElement("title", null);
                m_xHandler.characters(group.title);
                m_xHandler.endElement("title");
            }
            if (!group.desc.isEmpty())
            {
                m_xHandler.startElement("desc", null);
                m_xHandler.characters(group.desc);
                m_xHandler.endElement("desc");
            }
            // children
            ArrayList<Shapes.Shape> shapes = group.getChildren();
            for (Shapes.Shape shape : shapes)
            {
                if (shape.getClass() == Shapes.TextBox.class)
                {
                    Shapes.TextBox textBox = (Shapes.TextBox) shape;
                    m_xHandler.startElement(Shapes.TextBox.TAG, textBox.getSvgAttributes());
                    m_xHandler.startElement(Shapes.Rect.TAG, textBox.getRectSvgAttributes());
                    m_xHandler.endElement(Shapes.Rect.TAG);
                    outputTextsForShape(textBox);
                    m_xHandler.endElement(Shapes.TextBox.TAG);
                }
                else if (shape.hasNonEmptyTextParagraphChildren())
                {
                    // make a group
                    XmlAttributeListImpl groupWithTextAtts = new XmlAttributeListImpl("id", shape.getId()+"_grouped_with_text");
                    groupWithTextAtts.AddAttribute("class", "drawShapeWithText"+(shape.getParagraphStyleName().isEmpty()?"":" "+shape.getParagraphStyleName()));
                    m_xHandler.startElement(Shapes.G.TAG, groupWithTextAtts);
                    // containing the shape
                    outputShape(shape);
                    outputTextsForShape(shape);
                    m_xHandler.endElement(Shapes.G.TAG);
                }
                else 
                {
                    outputShape(shape);
                }
            }
            // end of group / layer
            m_xHandler.endElement("g");
        } 
        catch (SAXException ex) 
        {
            Logger.getLogger(SvgExportFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void characters(String aChars) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        System.err.print(aChars);
        
        // --- characters ------------------------------------------------------
        if (m_lastSaxNotificationType != SaxNotificationType.IgnoredStartElement && (m_doc_PageHit<1 || m_doc_PageHit==pageNumber))
        {
            m_lastSaxNotificationType = SaxNotificationType.Characters;
            if (m_doc_currentParent.name.equals("draw:layer"))
            {
                Shapes.Layer lastAddedLayer = Shapes.getLastAddedLayer();
                if (m_doc_currentElement.name.equals("svg:title") && lastAddedLayer!=null)
                {
                    lastAddedLayer.title+=aChars;
                }
                if (m_doc_currentElement.name.equals("svg:desc") && lastAddedLayer!=null)
                {
                    lastAddedLayer.desc+=aChars;
                }
            }
            else if (m_doc_currentParent.name.equals("draw:image") &&
                    m_doc_currentElement.name.equals("office:binary-data"))
            {
                Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
                if (lastAddedShape instanceof Shapes.Image)
                {
                    Shapes.Image lastAddedImage = (Shapes.Image) lastAddedShape;
                    lastAddedImage.appendBase64EncodedImage(aChars);
                }
            }
            else if (m_doc_currentParent.name.equals("draw:fill-image") &&
                    m_doc_currentElement.name.equals("office:binary-data"))
            {
                String imageName = XmlUtils.trimToId(m_doc_currentParent.attributes.getValueByName("draw:name"));
                Styles.FillImage fillImage = Styles.getFillImage(imageName);
                String combined = aChars;
                if (fillImage!=null)
                {
                    combined = fillImage.getBase64ImageData() + combined;
                    Styles.getFillImages().remove(fillImage);
                }
                fillImage = new Styles.FillImage(imageName, combined); 
                Styles.getFillImages().add(fillImage);
            }
            else if (m_doc_currentElement.name.equals("text:p"))    // text, directly in paragraph --> make a new span for it to contain text
            {
                Shapes.TextParagraph lastAddedParagraph = Shapes.getLastAddedTextParagraph();
                Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
                if (lastAddedParagraph!=null && lastAddedShape!=null && m_doc_currentParent!=null)
                {
                    // characters in text elements of shapes or textboxes
                    if (
                        (m_doc_currentParent.name.equals("draw:circle") && lastAddedShape instanceof Shapes.Circle) ||
                        (m_doc_currentParent.name.equals("draw:ellipse") && lastAddedShape instanceof Shapes.Ellipse) ||
                        (m_doc_currentParent.name.equals("draw:image") && lastAddedShape instanceof Shapes.Image) ||
                        (m_doc_currentParent.name.equals("draw:line") && lastAddedShape instanceof Shapes.Line) ||
                        (m_doc_currentParent.name.equals("draw:path") && lastAddedShape instanceof Shapes.Path) ||
                        (m_doc_currentParent.name.equals("draw:connector") && lastAddedShape instanceof Shapes.Path) ||
                        (m_doc_currentParent.name.equals("draw:polygon") && lastAddedShape instanceof Shapes.Polygon) ||
                        (m_doc_currentParent.name.equals("draw:polyline") && lastAddedShape instanceof Shapes.Polyline) ||
                        (m_doc_currentParent.name.equals("draw:rect") && lastAddedShape instanceof Shapes.Rect) ||
                        (m_doc_currentParent.name.equals("draw:text-box") && lastAddedShape instanceof Shapes.TextBox))
                    {
                        // create a tspan containing those characters, without any attributes (when generating output, the class of the text element will be used for formatting)
                        Shapes.TSpan tSpan = new Shapes.TSpan();
                        lastAddedParagraph.addTSpan(tSpan);
                        tSpan.appendText(aChars);
                    }
                }                
            }
            else if (m_doc_currentElement.name.equals("text:span")) // text, in a span --> append text to current span
            {
                Shapes.getLastAddedTextParagraph();
                Shapes.TextParagraph lastAddedParagraph = Shapes.getLastAddedTextParagraph();
                if (lastAddedParagraph!=null)
                {
                    Shapes.TSpan lastAddedSpan = lastAddedParagraph.getLatestSpan();
                    
                    if (lastAddedSpan!=null)
                    {
                        if (m_doc_currentGrandParent.name.equals("draw:circle") ||
                            m_doc_currentGrandParent.name.equals("draw:ellipse") ||
                            m_doc_currentGrandParent.name.equals("draw:image") ||
                            m_doc_currentGrandParent.name.equals("draw:line") ||
                            m_doc_currentGrandParent.name.equals("draw:path") ||
                            m_doc_currentGrandParent.name.equals("draw:connector") ||
                            m_doc_currentGrandParent.name.equals("draw:polygon") ||
                            m_doc_currentGrandParent.name.equals("draw:polyline") ||
                            m_doc_currentGrandParent.name.equals("draw:rect") ||
                            m_doc_currentGrandParent.name.equals("draw:text-box"))
                        {                         
                            lastAddedSpan.appendText(aChars);
                        }
                    }
                }
            }
            else if (m_doc_currentElement.name.equals("svg:title"))
            {
                Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
                if (lastAddedShape!=null) lastAddedShape.title+=aChars;
            }
            else if (m_doc_currentElement.name.equals("svg:desc"))
            {
                Shapes.Shape lastAddedShape = Shapes.getLastAddedShape();
                if (lastAddedShape!=null) lastAddedShape.desc+=aChars;
            }
            else if (m_doc_currentElement.name.equals("dc:title")) Metadata.dcTitle+=aChars;
            else if (m_doc_currentElement.name.equals("dc:description")) Metadata.dcDescription+=aChars;
            else if (m_doc_currentElement.name.equals("dc:subject")) Metadata.dcSubject+=aChars;
            else if (m_doc_currentElement.name.equals("dc:date")) Metadata.dcDate+=aChars;
            else if (m_doc_currentElement.name.equals("meta:creation-date")) Metadata.metaCreationDate+=aChars;
            else if (m_doc_currentElement.name.equals("meta:keyword")) Metadata.metaKeywords.add(aChars);
            else if (m_doc_currentElement.name.equals("meta:user-defined")) 
            {
                String entryName = m_doc_currentElement.attributes.getValueByName("meta:name");
                String valueTypeName = m_doc_currentElement.attributes.getValueByName("meta:value-type");
                Metadata.MetaUserValueType valueType = Metadata.MetaUserValueType.Text;
                if (valueTypeName!=null && !valueTypeName.isEmpty())
                {
                    if (valueTypeName.equals("date")) valueType = Metadata.MetaUserValueType.Date;
                    else if (valueTypeName.equals("time")) valueType = Metadata.MetaUserValueType.Time;
                    else if (valueTypeName.equals("float")) valueType = Metadata.MetaUserValueType.Float;
                    else if (valueTypeName.equals("boolean")) valueType = Metadata.MetaUserValueType.Boolean;
                }
                Metadata.metaUserDefined.add(
                    new Metadata.MetaUserDefined(entryName, valueType, aChars));
            }
            /*

        { }
        else if (aName.equals("meta:user-defined")) // will be handled in characters()
        { }
            */
        }        
    }

    @Override
    public void ignorableWhitespace(String aWhitespaces) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        if (m_bPrettyPrint) System.err.print(aWhitespaces);
        
        // --- ignorableWhitespace ---------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.IgnorableWhitespace;
        if (m_bPrettyPrint) m_xHandler.ignorableWhitespace(aWhitespaces);
    }

    @Override
    public void processingInstruction(String aTarget, String aData) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        System.err.println("processingInstruction: " + aTarget+ " " + aData);
        
        // --- processingInstruction -------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.ProcessingInstruction;
        m_xHandler.processingInstruction(aTarget, aData);
    }

    @Override
    public void setDocumentLocator(com.sun.star.xml.sax.XLocator xLocator) throws SAXException
    {
        // --- DEBUG -----------------------------------------------------------
        System.err.println("setDocumentLocator: " + xLocator.toString());
        
        // --- setDocumentLocator ----------------------------------------------
        m_lastSaxNotificationType = SaxNotificationType.SetDocumentLocator;
        m_xHandler.setDocumentLocator(xLocator);
    }
}
