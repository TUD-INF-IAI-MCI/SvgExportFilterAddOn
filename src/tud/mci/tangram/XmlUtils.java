/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin.Spindler@tu-dresden.de
 */
public class XmlUtils {
    public static String trimToId(String aName)
    {
        String result = aName;

        // if no name was given, generate an id
        if (result == null || result.isEmpty())
        {
            return "id_" + java.util.UUID.randomUUID().toString();
        }
        // must be at least 2 characters long,must start with a letter
        else if (result.length()<2 || !Character.isLetter(result.charAt(0)))
        {
            result = "id_" + result;
        }

        // capitalize, except first char (lower case)
        String[] words = result.split("\\W+");  // split into words
        result = "";    // reset result and build new
        for (int i=0; i<words.length; i++)
        {
            result += (i==0)?
                    // keep first word as is
                    words[i]:
                    // following words capitalize first char
                    words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }

        // umlauts, whitespaces and periods are not allowed
        result = result.
            replaceAll("\\s","").
            replaceAll("\\.","").
            replaceAll("\\u00c4", "Ae").
            replaceAll("\\u00e4", "ae").
            replaceAll("\\u00d6", "Oe").
            replaceAll("\\u00f6", "oe").
            replaceAll("\\u00dc", "Ue").
            replaceAll("\\u00fc", "ue").
            replaceAll("\\u00df", "ss");

        return result;
    }
    
    /*
     * Decode base64 string to image
     * @param imageString The string to decode
     * @return decoded image or null if no success
     */
    public static java.awt.image.BufferedImage decodeToImage(String imageString) {

        java.awt.image.BufferedImage image = null;
        try 
        {
            String imgString = imageString.startsWith("data:image/") ? imageString.substring(imageString.indexOf(',') + 1) : imageString;
            java.io.InputStream stream = new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(imgString));
            image = javax.imageio.ImageIO.read(stream);
            stream.close();
        } 
        catch (Exception e) {
        }
        return image;
    }
}
