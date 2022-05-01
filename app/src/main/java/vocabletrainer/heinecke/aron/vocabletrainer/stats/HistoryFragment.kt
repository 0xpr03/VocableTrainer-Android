package vocabletrainer.heinecke.aron.vocabletrainer.stats

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import vocabletrainer.heinecke.aron.vocabletrainer.R
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.BaseFragment
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database


/**
 * History statistics overview
 */
class HistoryFragment: BaseFragment() {
    private val TAG = "HistoryFragment"

    private lateinit var chart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        chart = view.findViewById(R.id.chart_history)


        // if more than 40 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40)

        // scaling can be not done on x- and y-axis separately
        chart.setPinchZoom(false)

        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.textColor = textColor()
        l.formSize = 8f
        l.formToTextSpace = 4f
        l.xEntrySpace = 6f


//        val barEntries = ArrayList<BarEntry>()
//
//        barEntries.add(BarEntry(0f, floatArrayOf(1.0f,2f)))
//        barEntries.add(BarEntry(1f, floatArrayOf(2f,7f)))
//        barEntries.add(BarEntry(2f, floatArrayOf(4f,5f)))
//        barEntries.add(BarEntry(3f, floatArrayOf(6f,3f)))
//        barEntries.add(BarEntry(4f, floatArrayOf(5f,1f)))
//        barEntries.add(BarEntry(5f, floatArrayOf(7f,4f)))

        val db = Database(context)
        db.insertFakeHistory(100,1641249422307L)
        val barEntries = db.getHistoryStats()

        val barDataSet = BarDataSet(barEntries, "Contracts")
        barDataSet.axisDependency = YAxis.AxisDependency.LEFT
        //        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
        //barDataSet.setColor(getColor("defaultYellow"))#
        barDataSet.valueTextSize = Button(requireContext()).textSize
        barDataSet.valueTextColor = textColor()
        barDataSet.highLightColor = highlightColor()
        barDataSet.isHighlightEnabled = true
        barDataSet.colors = listOf(Color.GREEN,Color.RED)
        //barDataSet.highLightColor = Color.RED
        //barDataSet.setValueTextSize(defaultValueTextSize)
        //barDataSet.setValueTextColor(getColor("primaryDark"))

        val barData = BarData(barDataSet)

        //chart.description.text = "Training history";
        //chart.getDescription().setTextSize(12);
        chart.setDrawMarkers(true);
        //chart.setMarker(markerView(context));
        //chart.getAxisLeft().addLimitLine(lowerLimitLine(2,"Minimum",2,12,getColor("defaultOrange"),getColor("defaultOrange")));
        //chart.getAxisLeft().addLimitLine(upperLimitLine(5,"Target",2,12,getColor("defaultGreen"),getColor("defaultGreen")));
        //chart.getAxisLeft().setAxisMinimum(0);
        chart.xAxis.position = XAxis.XAxisPosition.BOTH_SIDED

        // color fixes
        chart.xAxis.textColor = textColor()
        chart.axisLeft.textColor = textColor()

        val labels = ArrayList<String> ();

        labels.add( "JAN");
        labels.add( "FEB");
        labels.add( "MAR");
        labels.add( "APR");
        labels.add( "MAY");
        labels.add( "JUN");


        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels);
        //chart.animateY(1000);

        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        //chart.getXAxis().setLabelCount(barDataSet.getEntryCount());

        chart.data = barData
    }

    private fun textColor(): Int = color(R.color.textColor)
    private fun highlightColor(): Int = color(R.color.colorAccent)

    private fun color(@ColorRes id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requireContext().getColor(id)
        } else {
            ContextCompat.getColor(requireContext(),id)
        }
    }

    companion object {
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}