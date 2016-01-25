package tud.mci.tangram;

import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.xml.sax.XAttributeList;
import java.util.ArrayList;

/**
 * A Java Implemention of XAttributeList
 * @author Martin.Spindler@tu-dresden.de
 */
public final class XmlAttributeListImpl extends WeakBase
   implements XAttributeList
{

    private class Attribute
    {
        public String name; public String value; public String type;
        public Attribute(String name, String type, String value)
        {
            this.name = name; this.type = type; this.value = value;
        }
    };
    
    private final ArrayList<Attribute> m_attributes;
    
    public XmlAttributeListImpl( )
    {
        m_attributes = new ArrayList<Attribute>();
    };
    
    public XmlAttributeListImpl(XAttributeList xAttribs) {
        m_attributes = new ArrayList<Attribute>();
        SetAttrbiuteList(xAttribs);
    }
    
    /**
     * Constructor for easy createing with already one contained attribute (value is CDATA)
     * @param name
     * @param value
     */
    public XmlAttributeListImpl(String name, String value) {
        m_attributes = new ArrayList<Attribute>();
        m_attributes.add(new Attribute(name, "CDATA", value));
    }
    
    public void AddAttribute(String name, String type, String value)
    {
        m_attributes.add(new Attribute(name, type, value));
    }
    
    public void AddAttribute(String name, String value)
    {
        m_attributes.add(new Attribute(name, "CDATA", value));
    }
    
    public void SetAttribute(String name, String value)
    {
        for (Attribute attribute : m_attributes)
        {
            if (attribute.name.equals(name))
            {
                attribute.value=value;
                return;
            }
        }
        // if not yet set
        AddAttribute(name, value);
    }
    
    public void Clear()
    {
        m_attributes.clear();
    }
    
    public void RemoveAttribute(String name)
    {
        for (Attribute attribute : m_attributes)
        {
            if (attribute.name.equals(name))
            {
                m_attributes.remove(attribute); break;
            }
        }
    }
    
    /**
     * Copy Attributes from other List.
     * @param otherList 
     */
    public void SetAttrbiuteList(XAttributeList otherList)
    {
        Clear();
        AppendAttrbiuteList(otherList);
    }
    
    /**
     * Add copy Attributes from other List.
     * @param otherList 
     */
    public void AppendAttrbiuteList(XAttributeList otherList)
    {
        short size = otherList.getLength();
        for (short i=0; i < size; i++)
        {   
            AddAttribute(
                    otherList.getNameByIndex(i), 
                    otherList.getTypeByIndex(i), 
                    otherList.getValueByIndex(i));
        }
    }
    
    // com.sun.star.xml.sax.XAttributeList:
    @Override
    public short getLength()
    {
        return (short) m_attributes.size();
    }

    @Override
    public String getNameByIndex(short i)
    {
        return m_attributes.get(i).name;
    }

    @Override
    public String getTypeByIndex(short i)
    {
        return m_attributes.get(i).type;
    }

    @Override
    public String getTypeByName(String aName)
    {
        for (Attribute attribute : m_attributes)
        {
            if (attribute.name.equals(aName))
            {
                return attribute.type;
            }
        }
        return null;
    }

    @Override
    public String getValueByIndex(short i)
    {
        return m_attributes.get(i).value;
    }

    @Override
    public String getValueByName(String aName)
    {
        for (Attribute attribute : m_attributes)
        {
            if (attribute.name.equals(aName)) { return attribute.value; }
        }
        return null;
    }

}
