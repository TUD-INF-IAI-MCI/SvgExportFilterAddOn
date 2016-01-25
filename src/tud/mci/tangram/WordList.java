/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tud.mci.tangram;

import java.util.ArrayList;

/**
 * A sequence of words, used for layout / automatic line break calculations
 * Holds a list of strings that are either single whitespace characters or 
 * @author Martin.Spindler@tu-dresden.de
 */
public class WordList {
    private final ArrayList<String> words;

    private WordList(String textToSplit)
    {
        words = new ArrayList<String>();
        if (textToSplit.isEmpty()) return;
        // i, currentChar: iterate characters at index
        for (int i=0; i<textToSplit.length(); i++)
        {
            char currentChar = textToSplit.charAt(i);
            // if current char is whitespace, add as single word
            if (Character.isWhitespace(currentChar))
            {
                words.add(Character.toString(currentChar));
            }
            // for non-whitespace characters
            else
            {
                // start searching after current character for next break or whitespace
                for (int n = i+1; n<textToSplit.length(); n++)
                {
                    // on character at n is whitespace
                    if (Character.isWhitespace(textToSplit.charAt(n)))
                    {
                        // memorze word before
                        words.add(textToSplit.substring(i, n));
                        // continue from found whitespace (-1, because in i will be increased in next cycle)
                        i = n-1;
                        break;  // leave searching for next break, continue outer cycle
                    }
                    // on hyphen or other line breaking char:
                    else if (textToSplit.charAt(n)=='\u002D' || // minus -
                            textToSplit.charAt(n)=='\u007C' ||  // pipe |
                            textToSplit.charAt(n)=='\u00AD' || // soft hyphen
                            textToSplit.charAt(n)=='\u2010' || // hyphen
                            textToSplit.charAt(n)=='\u2012' || // gedankenstrich
                            textToSplit.charAt(n)=='\u2013' || // bindestrich
                            textToSplit.charAt(n)=='\u2013' || // bindestrich
                            textToSplit.charAt(n)=='\u2014' || // gedankenstrich
                            textToSplit.charAt(n)=='\uFE58' || // kleiner gedankenstrich
                            textToSplit.charAt(n)=='?' ||
                            textToSplit.charAt(n)=='!' ||
                            textToSplit.charAt(n)=='´' ||
                            textToSplit.charAt(n)=='%')
                    {
                        // add word, including breaking char
                        words.add(textToSplit.substring(i,n+1));
                        // continue from next, afer breaking char
                        i = n;
                        break; // leave searching for next break, continue outer cycle
                    }
                    // else simply proceed
                    else
                    {
                        // eventually add last word if end of string
                        if (n>=textToSplit.length()-1)
                        {
                            words.add(textToSplit.substring(i));
                            return;
                        }
                    }
                }
            }
        }
    }  
    
    private ArrayList<String> getWords()
    {
        return words;
    }
    
    /**
     * Splits up a text into a String array of words and whitespace chars
     * @param text
     * @return
     */
    public static ArrayList<String> splitWords(String text)
    {
        // convert line breaks in string into spaces
        return new WordList(text.replaceAll("\r\n", " ").replaceAll("\r", " ").replace("\n", " ")).getWords();
    }
}
