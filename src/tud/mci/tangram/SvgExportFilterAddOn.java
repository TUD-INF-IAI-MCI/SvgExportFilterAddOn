package tud.mci.tangram;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.DispatchDescriptor;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XTitle;
import com.sun.star.io.XActiveDataSource;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XServiceName;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lang.XTypeProvider;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.ui.dialogs.XFilePickerControlAccess;
import com.sun.star.ui.dialogs.XFilterManager;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.URL;
import com.sun.star.xml.XExportFilter;
import com.sun.star.xml.sax.SAXException;
import com.sun.star.xml.sax.XAttributeList;
import com.sun.star.xml.sax.XDocumentHandler;
import com.sun.star.xml.sax.XExtendedDocumentHandler;
import com.sun.star.xml.sax.XLocator;

/**
 * @author Martin.Spindler@tu-dresden.de
 */
public final class SvgExportFilterAddOn extends WeakBase
   implements com.sun.star.lang.XServiceName,                   // for getServiceName() to provide the service name "tud.mci.tangram.SvgExportFilterAddOn" (to create such an object by a factory)
              com.sun.star.lang.XServiceInfo,                   // provides information regarding the implementation, i.e. which services are implemented and the name of the implementation
              com.sun.star.lang.XTypeProvider,                  // for providing interfaces types of this component
              com.sun.star.lang.XInitialization,                // for getting the XFrame on initialization, the XFrame is needed for dispatching UI commands
              com.sun.star.frame.XDispatchProvider,             // provides XDispatch interfaces for certain functions which are useful at the UI
              com.sun.star.frame.XDispatch,                     // serves state information of objects which can be connected to controls (e.g. toolbox controls)
              com.sun.star.xml.XExportFilter,                   // exporter() call will provide the com.sun.star.document.TypeDetection user data required to perform the filtering correctly 
              com.sun.star.xml.sax.XDocumentHandler             // for generating XML data by outputting sax events, forwards to the filters XExtendedDocumentHandler m_xHandler
{

// =============================================================================
// private data members
// =============================================================================
    /**
     * the XDocumentHandler
     */
    private XExtendedDocumentHandler m_xHandler;
    private final boolean m_bPrettyPrint = true;
    private final XComponentContext m_xContext;
    private XFrame m_xFrame;
    protected String m_msFilterName;
    protected String[] m_msUserData;
    protected String m_msTemplateName;
    private static final String m_serviceName = "tud.mci.tangram.SvgExportFilterAddOn";
    private static final String m_implementationName = SvgExportFilterAddOn.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.document.ExportFilter",
        "com.sun.star.xml.XMLExportFilter",
        "com.sun.star.frame.ProtocolHandler"};

// =============================================================================
// constructor
// =============================================================================
    /**
    * Constructor
    * @param context The context.
    */
    public SvgExportFilterAddOn( XComponentContext context )
    {
        m_xContext = context;
//        XComponent xComponentMsgBox = null;
//        try {
//            Object oToolkit = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext);
//            Object desktop = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.Desktop",m_xContext);
//            XDesktop xDesktop=(XDesktop)UnoRuntime.queryInterface(XDesktop.class,desktop);
//            XFrame m_xFrame=xDesktop.getCurrentFrame();
//            XWindowPeer xPeer=(XWindowPeer)UnoRuntime.queryInterface(XWindowPeer.class,m_xFrame.getContainerWindow());
//            
//            XMessageBoxFactory xMessageBoxFactory = UnoRuntime.queryInterface(XMessageBoxFactory.class, oToolkit);
//            XMessageBox xMessageBox = xMessageBoxFactory.createMessageBox(xPeer, MessageBoxType.ERRORBOX, com.sun.star.awt.MessageBoxButtons.BUTTONS_OK, "Test Title", "Test Message");
//            xComponentMsgBox = (XComponent) UnoRuntime.queryInterface(XComponent.class, xMessageBox);
//            if (xMessageBox != null){
//                short nResult = xMessageBox.execute();
//            } 
//        } catch (Exception ex) {
//            Logger.getLogger(SvgExportFilterAddOn.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        finally{
//            //make sure always to dispose the component and free the memory!
//            if (xComponentMsgBox != null){
//                xComponentMsgBox.dispose();
//            }
//        }
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
    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(SvgExportFilterAddOn.class, m_serviceNames);
        return xFactory;
    }

// This method not longer necessary since OOo 3.4 where the component registration
// was changed to passive component registration. For more details see
// http://wiki.services.openoffice.org/wiki/Passive_Component_Registration
    /** Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>.
     * @return returns true if the operation succeeded
     * @see com.sun.star.comp.loader.JavaLoader
     * @see com.sun.star.lib.uno.helper.Factory
     * @param xRegistryKey Makes structural information (except regarding tree
     * structures) of a single registry key accessible.
     */
    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
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
        Type[] typeReturn;
        try {
            typeReturn = new Type[] {
                new Type( XServiceName.class ),
                new Type( XServiceInfo.class ), 
                new Type( XInitialization.class ), 
                new Type( XDispatchProvider.class ), 
                new Type( XDispatch.class ), 
                new Type( XTypeProvider.class ),
                new Type( XExportFilter.class ),
                new Type( XDocumentHandler.class )
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
            m_xFrame = (XFrame) UnoRuntime.queryInterface(XFrame.class, object[0]);
        }
    }
    
// =============================================================================
// com.sun.star.frame.XDispatchProvider
// =============================================================================
    @Override
    public XDispatch queryDispatch(URL aURL, String sTargetFrameName, int iSearchFlags)
    {
        if (aURL.Protocol.startsWith("tud.mci.tangram.svgexportfilteraddon"))
        {
            if (aURL.Path.compareTo("exportSvgCommand") == 0)
            {
                return this;
            }
        }
        return null;
    }

    @Override
    public XDispatch[] queryDispatches( DispatchDescriptor[] seqDescriptors) 
    {
        int nCount = seqDescriptors.length;
        XDispatch[] seqDispatcher = new XDispatch[seqDescriptors.length];

        for (int i = 0; i < nCount; ++i) 
        {
            seqDispatcher[i] = queryDispatch(
                    seqDescriptors[i].FeatureURL,
                    seqDescriptors[i].FrameName,
                    seqDescriptors[i].SearchFlags);
        }
        return seqDispatcher;
    }
    

// =============================================================================
// com.sun.star.frame.XDispatch
// =============================================================================
    @Override
    public void dispatch(URL aURL, PropertyValue[] aArguments) 
    {
        if (aURL.Protocol.startsWith("tud.mci.tangram.svgexportfilteraddon")) 
        {

            if (aURL.Path.compareTo("exportSvgCommand") == 0) 
            {
                String sStorePath = ""; 
                XComponent filePickerXComponent = null;

                try {
                    // see https://wiki.openoffice.org/wiki/Documentation/DevGuide/GUI/File_Picker
                    // the filepicker is instantiated with the global Multicomponentfactory...
                    // openoffice dialog: "com.sun.star.ui.dialogs.OfficeFilePicker"
                    // system dialog: "com.sun.star.ui.dialogs.FilePicker"
                    Object oFilePicker = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.ui.dialogs.OfficeFilePicker", m_xContext);
                    XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, oFilePicker);

                    xFilePicker.setTitle("SVG Export");
                    
                    // get URL / title of current document
                    String fileUrl = m_xFrame.getController().getModel().getURL();
                    XTitle xTitle = UnoRuntime.queryInterface(XTitle.class, m_xFrame.getController());
                    String fileName = xTitle.getTitle();
                    if (!fileUrl.isEmpty())
                    {
                        fileName = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
                        // set same path as original document
                        xFilePicker.setDisplayDirectory(fileUrl.substring(0, fileUrl.lastIndexOf("/")));
                    }
                    if (fileName.endsWith(".odg")||fileName.endsWith(".sxf")||fileName.endsWith(".otg")||fileName.endsWith(".std"))
                    {
                        fileName = fileName.substring(0, fileName.length()-4);
                    }
                            
                    // the defaultname is the initially proposed filename..
                    xFilePicker.setDefaultName(fileName+".svg");
                    

                    // set the filters of the dialog.
                    XFilterManager xFilterManager = (XFilterManager) UnoRuntime.queryInterface(XFilterManager.class, xFilePicker);
                    xFilterManager.appendFilter("Scalable Vector Graphics", ".svg");

                    // choose the template that defines the capabilities of the filepicker dialog
                    XInitialization xInitialize = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, xFilePicker);
                    Short[] listAny = new Short[] { com.sun.star.ui.dialogs.TemplateDescription.FILESAVE_AUTOEXTENSION_SELECTION};
                    xInitialize.initialize(listAny);

                    // add a control to the dialog to add the extension automatically to the filename...
                    XFilePickerControlAccess xFilePickerControlAccess = (XFilePickerControlAccess) UnoRuntime.queryInterface(XFilePickerControlAccess.class, xFilePicker);
                    xFilePickerControlAccess.setValue(com.sun.star.ui.dialogs.ExtendedFilePickerElementIds.CHECKBOX_AUTOEXTENSION, (short) 0, true);

                    filePickerXComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);

                    // execute the dialog...
                    XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);
                    short nResult = xExecutable.execute();

                    // query the resulting path of the dialog...
                    if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK) {
                        String[] sPathList = xFilePicker.getFiles();
                        if (sPathList.length > 0){
                            sStorePath = sPathList[0]; 
                        }
                    }
                    else
                    {
                        return;
                    }
                    
                    // set up arguments set for output filtering
                    String[] userData = {
                        "tud.mci.tangram.SvgExportFilterAddOn",
                        "",
                        "",
                        "com.sun.star.comp.Draw.XMLExporter"
                    };
                    PropertyValue[] args1 = new PropertyValue[4];
                    args1[0] = new PropertyValue();
                    args1[0].Name = "URL";  
                    args1[0].Value = (Object) sStorePath; //"file:///F:/Users/Spindler/Desktop/Unbenannt%201.svg";
                    args1[1] = new PropertyValue();
                    args1[1].Name = "FilterName"; 
                    args1[1].Value = (Object) "tud_mci_tangram_SvgExportFilter";
                    args1[2] = new PropertyValue();
                    args1[2].Name = "FilterData";  
                    args1[2].Value = (Object) userData;
                    args1[3] = new PropertyValue();
                    args1[3].Name = "SelectionOnly"; 
                    args1[3].Value = (Object) false;
                    
                    Object dispatcherObj = m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.DispatchHelper", m_xContext);
                    XDispatchHelper xDispatcher = UnoRuntime.queryInterface(XDispatchHelper.class, dispatcherObj);
                    XDispatchProvider xDispatchProvider = UnoRuntime.queryInterface(XDispatchProvider.class, m_xFrame);
                    
                    // call filter dispatch
                    xDispatcher.executeDispatch(
                            xDispatchProvider,
                            ".uno:ExportTo", // URL
                            "",              // TargetFrameName
                            com.sun.star.frame.FrameSearchFlag.AUTO, // SearchFlags for finding the frame if no special TargetFrameName was used
                            args1); // Arguments 
                } 
                catch (Exception ex) 
                {
                    System.err.println("ex");
                }
                finally
                {
                    //make sure always to dispose the component and free the memory!
                    if (filePickerXComponent != null){ filePickerXComponent.dispose(); }
                }
            } 
        }
    }

    @Override
    public void addStatusListener(com.sun.star.frame.XStatusListener xControl,
            com.sun.star.util.URL aURL) {
    }

    @Override
    public void removeStatusListener(com.sun.star.frame.XStatusListener xControl,
            com.sun.star.util.URL aURL) {
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
            XOutputStream xos = null;

            // get interesting values from sourceData (Media Descriptor)
            for (PropertyValue mediaDescProperty : exportMediaDescriptor) {
                sName = mediaDescProperty.Name;
                if (sName.equals("OutputStream"))   // a stream to receive the document data.  
                {
                    xos = (XOutputStream) AnyConverter.toObject(XOutputStream.class, mediaDescProperty.Value);
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
                    m_xHandler = (XExtendedDocumentHandler) UnoRuntime.queryInterface(XExtendedDocumentHandler.class, xmlWriter);
                }
            }
            if (m_xHandler == null) 
            {
                return false;
            }
            //m_xHandler.allowLineBreak();
            
            // Connect the provided output stream to the writer            		        
            XActiveDataSource xADSource = (XActiveDataSource) UnoRuntime.queryInterface(XActiveDataSource.class, m_xHandler);

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
    public void startElement(String aName, XAttributeList xAttribs) throws SAXException
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
    public void setDocumentLocator(XLocator xLocator) throws SAXException
    {
        System.err.println("setDocumentLocator: " + xLocator.toString());
        
        // TODO: Insert your implementation for "processingInstruction" here.
        m_lastSaxNotificationType = SaxNotificationType.SetDocumentLocator;
        m_xHandler.setDocumentLocator(xLocator);
    }
}
