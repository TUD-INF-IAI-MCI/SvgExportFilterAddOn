/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import java.util.ArrayList;

/**
 *
 * @author Spindler
 */
public class Metadata {
    public static String dcTitle = "";
    public static String dcDescription = "";
    public static String dcSubject = "";
    public static String metaCreationDate = "";
    public static String dcDate = "";
    public static ArrayList<String> metaKeywords = new ArrayList<String>();
    public static ArrayList<MetaUserDefined> metaUserDefined = new ArrayList<MetaUserDefined>();
    
    public static void init()
    {
        dcTitle = "";
        dcDescription = "";
        dcSubject = "";
        dcDate = "";
        metaCreationDate = "";
        metaKeywords.clear();
        metaUserDefined.clear();
    }
    
    public static boolean isEmpty()
    {
        return (dcTitle.isEmpty() && dcDescription.isEmpty() && dcSubject.isEmpty() && metaCreationDate.isEmpty() && dcDate.isEmpty() && metaKeywords.isEmpty() && metaUserDefined.isEmpty());
    }
    
    public static enum MetaUserValueType {
        Text, Date, Time, Float, Boolean
    }
    
    public static class MetaUserDefined {

        public String entryName;
        public MetaUserValueType valueType;
        public String valueString;
        
        public MetaUserDefined(String entryName, String valueString) {
            this(entryName, MetaUserValueType.Text, valueString);
        }
            
        public MetaUserDefined(String entryName, MetaUserValueType valueType, String valueString) {
            this.entryName = entryName;
            this.valueType = valueType;
            this.valueString = valueString;
        }
    }
}
