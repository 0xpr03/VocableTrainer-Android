package vocabletrainer.heinecke.aron.vocabletrainer;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.MultiMeaningHandler;

import static org.junit.Assert.*;

/**
 * Tests for multi-meaning parsing
 */
public class MultiMeaningParserTest {
    @Test
    public void importMultiValueEscapingTest_Full(){
        CSVCustomFormat format = CSVCustomFormat.DEFAULT;
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A\\/1","B\\\\2","C3"};
        final String[] meanings = {"A/1","B\\2","C3"};
        assertTrue(format.isMultiValueEnabled());
        assertTrue(format.isMVEscapeEnabled());
        String input = joinStringArray(meaningsInput,format.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }

    @Test
    public void importMultiValueEscapingTest_Single(){
        CSVCustomFormat format = CSVCustomFormat.DEFAULT;
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A\\/1"};
        final String[] meanings = {"A/1"};
        String input = joinStringArray(meaningsInput,format.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }


    @Test
    public void importMultiValueEscapingTest_No_Escape(){
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(formatDefault.getFormat(),
                formatDefault.getMultiValueChar(),null);
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        assertTrue(format.isMultiValueEnabled());
        assertFalse(format.isMVEscapeEnabled());
        final String[] meaningsInput = {"A\\/1","B\\\\2","C3"};
        final String[] meanings = {"A\\","1","B\\\\2","C3"};
        String input = joinStringArray(meaningsInput,format.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }

    @Test
    public void importMultiValueEscapingTest_No_Multivalue(){
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(formatDefault.getFormat(),
                null,null);
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        assertFalse(format.isMVEscapeEnabled());
        assertFalse(format.isMultiValueEnabled());
        final String[] meaningsInput = {"A\\/1","B\\\\2","C3"};
        final String[] meanings = {"A\\/1/B\\\\2/C3"};
        String input = joinStringArray(meaningsInput,formatDefault.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }

    @Test
    public void importMultiValueEscapingTest_Full_Trim(){
        CSVCustomFormat format = CSVCustomFormat.DEFAULT;
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {" A \\/1 "," B\\\\2 "," C3 "," C \\ /4"};
        final String[] meanings = {"A /1","B\\2","C3","C","4"};
        assertTrue(format.trim());
        assertTrue(format.isMultiValueEnabled());
        assertTrue(format.isMVEscapeEnabled());
        String input = joinStringArray(meaningsInput,format.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }

    @Test
    public void importMultiValueEscapingTest_Full_No_Trim(){
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(
                formatDefault.getFormat().withIgnoreSurroundingSpaces(false));
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {" A \\/1 "," B\\\\2 "," C3 "," C \\ /4"};
        final String[] meanings = {" A /1 "," B\\2 "," C3 "," C  ","4"};
        assertFalse(format.trim());
        assertTrue(format.isMultiValueEnabled());
        assertTrue(format.isMVEscapeEnabled());
        String input = joinStringArray(meaningsInput,format.getMultiValueChar());
        System.out.println(input);
        List<String> result = mmh.parseMultiMeaning(input);
        assertArrayEquals(meanings,result.toArray());
    }

    /**
     * Join String array
     * @param array array of string
     * @param c join char
     * @return
     */
    public static String joinStringArray(@NonNull String[] array, final char c){
        return Arrays.stream(array).reduce("",(x,y) -> {
            if(x.length() > 0) {
                return x + c + y;
            } else {
                return y;
            }
        });
    }
}
