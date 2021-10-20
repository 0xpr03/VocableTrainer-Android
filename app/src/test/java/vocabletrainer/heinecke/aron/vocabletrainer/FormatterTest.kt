package vocabletrainer.heinecke.aron.vocabletrainer

import org.junit.Assert
import org.junit.Test
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Formatter
import java.text.DecimalFormatSymbols

class FormatterTest {
    @Test
    fun format_isCorrect() {
        val fmt = Formatter()
        fmt.changeSI(true)
        val bytes_1024: Long = 1024
        val bytes_mib: Long = 1048576
        val bytes_1000: Long = 1000
        val bytes_mb: Long = 1000000
        Assert.assertEquals("1 KiB", fmt.formatBytes(bytes_1024))
        Assert.assertEquals("1 MiB", fmt.formatBytes(bytes_mib))
        val dfs = DecimalFormatSymbols.getInstance()
        Assert.assertEquals("1${dfs.groupingSeparator}000 B", fmt.formatBytes(bytes_1000))
        Assert.assertEquals("976${dfs.decimalSeparator}6 KiB", fmt.formatBytes(bytes_mb))
    }
}