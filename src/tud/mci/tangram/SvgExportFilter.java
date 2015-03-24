package tud.mci.tangram;

import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.xml.sax.SAXException;

/**
 * A XML Export filter, producing a SVG file from Office Draw document
 * @author Martin.Spindler@tu-dresden.de
 */
public final class SvgExportFilter extends com.sun.star.lib.uno.helper.WeakBase
   implements com.sun.star.lang.XServiceName,                   // for getServiceName() to provide the service name "tud.mci.tangram.SvgExportFilter" (to create such an object by a factory)
              com.sun.star.lang.XServiceInfo,                   // provides information regarding the implementation, i.e. which services are implemented and the name of the implementation
              com.sun.star.lang.XTypeProvider,                  // for providing interfaces types of this component
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
    private static final String m_serviceName = "tud.mci.tangram.SvgExportFilter";
    private static final String m_implementationName = SvgExportFilter.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.document.ExportFilter",
        "com.sun.star.xml.XMLExportFilter" };

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
            //m_xHandler.allowLineBreak();
            
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
    
    enum SaxNotificationType {
        StartDocument, EndDocument, StartElement, EndElement, Characters, IgnorableWhitespace, ProcessingInstruction, SetDocumentLocator, IgnoredStartElement
    }
    private SaxNotificationType m_lastSaxNotificationType;
    
    @Override
    public void startDocument() throws SAXException
    {
        System.err.println("startDocument");
        
        // TODO: Insert your implementation for "startDocument" here.
        m_lastSaxNotificationType = SaxNotificationType.StartDocument;
        m_xHandler.startDocument();
    }
    
    @Override
    public void endDocument() throws SAXException
    {
        System.err.println("endDocument");
        
        // TODO: Insert your implementation for "endDocument" here.
        m_lastSaxNotificationType = SaxNotificationType.EndDocument;
        m_xHandler.endDocument();
    }
    
    @Override
    public void startElement(String aName, com.sun.star.xml.sax.XAttributeList xAttribs) throws SAXException
    {
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
        
        // 
        if (aName.equals("office:document"))
        {
            XmlAttributeListImpl attributes = new XmlAttributeListImpl();
            attributes.AddAttribute("version", "1.2");
            attributes.AddAttribute("baseProfile", "tiny");
            attributes.AddAttribute("xmlns", "http://www.w3.org/2000/svg");
            attributes.AddAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
            attributes.AddAttribute("xml:space", "preserve");
            m_xHandler.startElement("svg", attributes);
            m_lastSaxNotificationType = SaxNotificationType.StartElement;
        }
        else
        {
            m_lastSaxNotificationType = SaxNotificationType.IgnoredStartElement;
        }
    }

    @Override
    public void endElement(String aName) throws SAXException
    {
        if (m_lastSaxNotificationType == SaxNotificationType.StartElement ||
            m_lastSaxNotificationType == SaxNotificationType.EndElement)
        {
            System.err.println();
        }
        System.err.print("</"+aName+">");
        
        m_lastSaxNotificationType = SaxNotificationType.EndElement;
        if (aName.equals("office:document"))
        {
            m_xHandler.endElement("svg");
        }
    }

    @Override
    public void characters(String aChars) throws SAXException
    {
        System.err.print(aChars);
        
        if (m_lastSaxNotificationType != SaxNotificationType.IgnoredStartElement)
        {
            m_lastSaxNotificationType = SaxNotificationType.Characters;
            m_xHandler.characters(aChars);
        }
    }

    @Override
    public void ignorableWhitespace(String aWhitespaces) throws SAXException
    {
        if (m_bPrettyPrint) System.err.print(aWhitespaces);
        
        // TODO: Insert your implementation for "ignorableWhitespace" here.
        m_lastSaxNotificationType = SaxNotificationType.IgnorableWhitespace;
        if (m_bPrettyPrint) m_xHandler.ignorableWhitespace(aWhitespaces);
    }

    @Override
    public void processingInstruction(String aTarget, String aData) throws SAXException
    {
        System.err.println("processingInstruction: " + aTarget+ " " + aData);
        
        // TODO: Insert your implementation for "processingInstruction" here.
        m_lastSaxNotificationType = SaxNotificationType.ProcessingInstruction;
        m_xHandler.processingInstruction(aTarget, aData);
    }

    @Override
    public void setDocumentLocator(com.sun.star.xml.sax.XLocator xLocator) throws SAXException
    {
        System.err.println("setDocumentLocator: " + xLocator.toString());
        
        // TODO: Insert your implementation for "processingInstruction" here.
        m_lastSaxNotificationType = SaxNotificationType.SetDocumentLocator;
        m_xHandler.setDocumentLocator(xLocator);
    }
}
