package vocabletrainer.heinecke.aron.vocabletrainer.stats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarEntry

class StatsViewModel: ViewModel() {
    val historyData: MutableLiveData<ArrayList<BarEntry>?> = MutableLiveData()
}