package vocabletrainer.heinecke.aron.vocabletrainer;

import org.junit.Test;

import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formatter;

import static org.junit.Assert.assertEquals;

public class FormatterTest {
    @Test
    public void format_isCorrect() {
        Formatter fmt = new Formatter();
        fmt.changeSI(true);
        long bytes_1024 = 1024;
        long bytes_mib = 1048576;
        long bytes_1000 = 1000;
        long bytes_mb = 1000000;
        assertEquals("1 KiB", fmt.formatBytes(bytes_1024));
        assertEquals("1 MiB", fmt.formatBytes(bytes_mib));
        assertEquals("1,000 B", fmt.formatBytes(bytes_1000));
        assertEquals("976.6 KiB", fmt.formatBytes(bytes_mb));
    }
}