package vocabletrainer.heinecke.aron.vocabletrainer.stats

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import vocabletrainer.heinecke.aron.vocabletrainer.R
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.BaseFragment
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


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


        chart.apply {
            // if more than 40 entries are displayed in the chart, no values will be
            // drawn
            setMaxVisibleValueCount(40)

            //setDrawMarkers(true)

            // scaling can be not done on x- and y-axis separately
            setPinchZoom(false)
        }

        chart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            textColor = textColor()
            formSize = 8f
            formToTextSpace = 4f
            xEntrySpace = 6f
        }


        val db = Database(context)
        //db.insertFakeHistory(100,1641249422307L)
        val barEntries = db.getHistoryStats()

        val barDataSet = BarDataSet(barEntries.first, "Contracts")
        barDataSet.apply {
            axisDependency = YAxis.AxisDependency.LEFT
            //        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
            //barDataSet.setColor(getColor("defaultYellow"))#
            valueTextSize = Button(requireContext()).textSize -2
            valueTextColor = textColor()
            highLightColor = highlightColor()
            isHighlightEnabled = false
            colors = listOf(Color.GREEN,Color.RED)
            //barDataSet.highLightColor = Color.RED
            //barDataSet.setValueTextSize(defaultValueTextSize)
            //barDataSet.setValueTextColor(getColor("primaryDark"))
        }

        val barData = BarData(barDataSet)

        //chart.description.text = "Training history";
        //chart.getDescription().setTextSize(12);
        //chart.setMarker(markerView(context));
        //chart.getAxisLeft().addLimitLine(lowerLimitLine(2,"Minimum",2,12,getColor("defaultOrange"),getColor("defaultOrange")));
        //chart.getAxisLeft().addLimitLine(upperLimitLine(5,"Target",2,12,getColor("defaultGreen"),getColor("defaultGreen")));
        //chart.getAxisLeft().setAxisMinimum(0);
        chart.axisLeft.textColor = textColor()

        //chart.animateY(1000);



        chart.xAxis.apply {
            //position = XAxis.XAxisPosition.BOTH_SIDED
            //textColor = textColor()

            //isGranularityEnabled = true
            //granularity = 1.0f
            //labelCount = barDataSet;
            valueFormatter = IndexAxisValueFormatter(barEntries.second)
            //Log.d(TAG,"entryCount ${barDataSet.entryCount}")
        }

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