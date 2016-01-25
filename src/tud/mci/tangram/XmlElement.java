/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import com.sun.star.xml.sax.XAttributeList;

/**
 * A simple representation of an XML Element: String name and XmlAttrubuteListImpl
 * @author Martin.Spindler@tu-dresden.de
 */
public class XmlElement {
    public String name;
    public XmlAttributeListImpl attributes;

    XmlElement(String aName, XAttributeList xAttribs) {
        name = aName;
        attributes = new XmlAttributeListImpl(xAttribs);
    }

    @Override
    public String toString() 
    {
        String result = "<" + name;
        for (short i=0; i< attributes.getLength(); i++)
        {
            result += attributes.getNameByIndex(i) + "=\"" + 
                      attributes.getValueByIndex(i) + "\"" + 
                      ((i<attributes.getLength()-1) ? " " : "");
        }
        result += ">";
        return result;
    }
}
