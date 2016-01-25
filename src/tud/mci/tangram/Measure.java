/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

/**
 * Contains methods for parsing and converting lengths and measures
 * @author Martin.Spindler@tu-dresden.de
 */
public class Measure {
      
    private enum MeasureUnit 
    {
        MM_100TH, MM_10TH, MM, CM, INCH_1000TH, INCH_100TH, INCH_10TH, INCH, 
        POINT, TWIP, M, KM, PICA, FOOT, MILE, PERCENT, PIXEL, APPFONT, SYSFONT
    }
    
    private MeasureUnit m_original_unit;
    private float m_original_value;
    private float m_valueInM; // internally always stored in m
    
    /*
    Name        Short   in m
    Centimilli 	mm_100	0.00001			
    Millimeter	mm	0.001			
    Centimeter	cm	0.01					
    Meter	m	1			
    Kilometer	km	1000			
    Inch	in	0.0254			
    Feet	ft	0.3048			
    Miles	mi	1609.344			
    Pica	pc	0.004233333	127/30000	1/6 inch	12 pt
    Point	pt	0.000352778	127/360000	1/72 inch	1/12 pica

    Em          em	height of current font			
    x-height	ex	height of character 'x' in current font			
    Pixel	px				
    Percent	%	context sensitive			
    */

    /**
     * Create empty measure (value is 0, unit is assumed to be 100th mm.)
     */
    public Measure() {
        m_original_unit = MeasureUnit.MM_100TH;
        m_original_value = 0;
        m_valueInM = 0;
    }
    
    /**
     * Parses a Measure
     * @param measureToParse a float value with some unit suffix. If no suffix than unit of 100th mm is assumed.
     */
    public Measure(String measureToParse) throws NumberFormatException
    {
        m_original_unit = MeasureUnit.MM_100TH;
        m_original_value = 0;
        m_valueInM = 0;
        if (measureToParse==null || measureToParse.isEmpty()) return;

        char c;
        // find first non-numeric character, parse value and unit
        // if no unit is found, MM_100TH is assumed
        for (int i = 0; i < measureToParse.length(); i++)
        {
            c = measureToParse.charAt(i);      
            try {
                if ( c!= '-' && c != '.' && (c < '0' || c > '9'))
                {
                    m_original_value = Float.parseFloat(measureToParse.substring(0,i));
                    m_valueInM = m_original_value;
                    String unitString = measureToParse.substring(i);
                    if (unitString.equals("mm")) 
                    {
                        m_original_unit = MeasureUnit.MM;
                        m_valueInM *= 0.001f;
                    }
                    else if (unitString.equals("cm"))
                    {
                        m_original_unit = MeasureUnit.CM;
                        m_valueInM *= 0.01f;
                    }
                    else if (unitString.equals("m"))
                    {
                        m_original_unit = MeasureUnit.M;
                    }
                    else if (unitString.equals("km"))
                    {
                        m_original_unit = MeasureUnit.KM;
                        m_valueInM *= 1000.0f;
                    }
                    else if (unitString.equals("in"))
                    {
                        m_original_unit = MeasureUnit.INCH;
                        m_valueInM *= 0.0254f;
                    }
                    else if (unitString.equals("ft"))
                    {
                        m_original_unit = MeasureUnit.FOOT;
                        m_valueInM *= 0.3048;
                    }
                    else if (unitString.equals("mi"))
                    {
                        m_original_unit = MeasureUnit.MILE;
                        m_valueInM *= 1609.344f;
                    }
                    else if (unitString.equals("pc"))
                    {
                        m_original_unit = MeasureUnit.PICA;
                        m_valueInM *= 127.0f/30000.0f;
                    }
                    else if (unitString.equals("pt"))
                    {
                        m_original_unit = MeasureUnit.POINT;
                        m_valueInM *= 127.0f/360000.0f;
                    }
                    return;
                }
                // no unit given:
                else if (i == measureToParse.length()-1)
                {
                    m_original_unit = MeasureUnit.MM_100TH;
                    m_original_value = Float.parseFloat(measureToParse);
                    m_valueInM = m_original_value * 0.00001f;
                    return;
                }
            }
            catch (NumberFormatException ex)
            {
                throw ex;
            }
        }
    }
    
    private String getUnit()
    {
        switch(m_original_unit)
        {
            case MM: return "mm";
            case CM: return "cm";
            case M: return "m";
            case KM: return "km";
            case INCH: return "in";
            case FOOT: return "ft";
            case MILE: return "mi";
            case PICA: return "pc";
            case POINT: return "pt";
        }
        return "";
    }
    
    private String getUnit(MeasureUnit m)
    {
        switch(m)
        {
            case MM: return "mm";
            case CM: return "cm";
            case M: return "m";
            case KM: return "km";
            case INCH: return "in";
            case FOOT: return "ft";
            case MILE: return "mi";
            case PICA: return "pc";
            case POINT: return "pt";
        }
        return "";
    }
    
    public float getOriginalValue() {return m_original_value;}
    public float getValueInMm100th() { return m_valueInM / 0.00001f; }
    public float getValueInMm() { return m_valueInM / 0.001f; }
    public float getValueInCm() { return m_valueInM / 0.01f; }
    public float getValueInMeter() { return m_valueInM; }
    public float getValueInKm() { return m_valueInM / 1000.0f; }
    public float getValueInIn() { return m_valueInM / 0.0254f; }
    public float getValueInFt() { return m_valueInM / 0.3048f; }
    public float getValueInMi() { return m_valueInM / 1609.344f; }
    public float getValueInPi() { return m_valueInM / (127.0f/30000.0f); }
    public float getValueInPt() { return m_valueInM / (127.0f/360000.0f); }
    
    @Override
    public String toString() { return m_original_value+getUnit(); }
    public String toStringMm() { return (m_valueInM / 0.001f)+getUnit(MeasureUnit.MM); }
    public String toStringCm() { return (m_valueInM / 0.01f)+getUnit(MeasureUnit.CM); }
    public String toStringM() { return (m_valueInM) + getUnit(MeasureUnit.M); }
    public String toStringKm() { return (m_valueInM / 1000.0f)+getUnit(MeasureUnit.KM); }
    public String toStringIn() { return (m_valueInM / 0.0254f)+getUnit(MeasureUnit.INCH); }
    public String toStringFt() { return (m_valueInM / 0.3048f)+getUnit(MeasureUnit.FOOT); }
    public String toStringMi() { return (m_valueInM / 1609.344f)+getUnit(MeasureUnit.MILE); }
    public String toStringPi() { return (m_valueInM / (127.0f/30000.0f))+getUnit(MeasureUnit.PICA); }
    public String toStringPt() { return (m_valueInM / (127.0f/360000.0f))+getUnit(MeasureUnit.POINT); }
   
}
