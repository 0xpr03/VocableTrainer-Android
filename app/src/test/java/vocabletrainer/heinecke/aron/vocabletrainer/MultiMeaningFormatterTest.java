package vocabletrainer.heinecke.aron.vocabletrainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.CSVCustomFormat;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.MultiMeaningHandler;

import static org.junit.Assert.*;
import static vocabletrainer.heinecke.aron.vocabletrainer.lib.CSV.MultiMeaningHandler.ERROR_MSG_FORMAT;

/**
 * Tests for multi-meaning formatting
 */
public class MultiMeaningFormatterTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void importMultiValueFormatTest_Full() {
        CSVCustomFormat format = CSVCustomFormat.DEFAULT;
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A/1","B\\2","C3"};
        final String meanings = "A\\/1/B\\\\2/C3";
        assertTrue(format.isMultiValueEnabled());
        assertTrue(format.isMVEscapeEnabled());
        String output = mmh.formatMultiMeaning(Arrays.asList(meaningsInput));
        assertEquals(meanings,output);
    }

    @Test
    public void importMultiValueFormatTest_NoEscape() {
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(formatDefault.getFormat(),formatDefault.getMultiValueChar(),null);
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A/1","B\\2","C3"};
        final String meanings = "A/1/B\\2/C3";
        assertTrue(format.isMultiValueEnabled());
        assertFalse(format.isMVEscapeEnabled());
        String output = mmh.formatMultiMeaning(Arrays.asList(meaningsInput));
        assertEquals(meanings,output);
    }

    @Test
    public void importMultiValueFormatTest_NoMultivalue() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(ERROR_MSG_FORMAT);
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(formatDefault.getFormat(),null,null);
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A/1","B\\2","C3"};
        final String meanings = "A\\/1/B\\\\2/C3";
        assertFalse(format.isMultiValueEnabled());
        assertFalse(format.isMVEscapeEnabled());
        mmh.formatMultiMeaning(Arrays.asList(meaningsInput));
    }

    @Test
    public void importMultiValueFormatTest_NoMultivalue2() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(ERROR_MSG_FORMAT);
        CSVCustomFormat formatDefault = CSVCustomFormat.DEFAULT;
        CSVCustomFormat format = new CSVCustomFormat(formatDefault.getFormat(),null,formatDefault.getEscapeMVChar());
        MultiMeaningHandler mmh = new MultiMeaningHandler(format);
        final String[] meaningsInput = {"A/1","B\\2","C3"};
        final String meanings = "A\\/1/B\\\\2/C3";
        assertFalse(format.isMultiValueEnabled());
        assertTrue(format.isMVEscapeEnabled());
        mmh.formatMultiMeaning(Arrays.asList(meaningsInput));
    }
}
