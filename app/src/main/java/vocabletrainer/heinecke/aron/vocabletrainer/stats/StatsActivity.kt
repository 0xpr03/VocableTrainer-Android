package vocabletrainer.heinecke.aron.vocabletrainer.stats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import vocabletrainer.heinecke.aron.vocabletrainer.R
import vocabletrainer.heinecke.aron.vocabletrainer.activity.FragmentActivity
import vocabletrainer.heinecke.aron.vocabletrainer.listpicker.ListPickerFragment
import vocabletrainer.heinecke.aron.vocabletrainer.trainer.TrainerSettingsFragment.FinishHandler
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ListPickerViewModel

/**
 * Statistics Activity
 */
class StatsActivity : FragmentActivity() {
    var viewPagerAdapter: ViewPagerAdapter? = null
    private var viewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        viewPager = findViewById(R.id.pager)
        initViewPager(savedInstanceState)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
    }

    /**
     * Init ViewPager
     */
    private fun initViewPager(savedInstanceState: Bundle?) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager
        )
        val historyFragment = HistoryFragment.newInstance()
        viewPagerAdapter!!.addFragment(historyFragment, R.string.Stats_Tab_History)
        viewPager!!.adapter = viewPagerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
    inner class ViewPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(
        manager!!
    ) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        /**
         * Add Fragment to viewpager
         * @param fragment Fragment
         * @param title Tab-Title string resource
         */
        fun addFragment(fragment: Fragment?, @StringRes title: Int) {
            addFragment(fragment!!, getString(title))
        }

        /**
         * Add Fragment to viewpager
         * @param fragment Fragment
         * @param title Title
         */
        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    companion object {
        private const val TAG = "TrainerSettings"
    }
}