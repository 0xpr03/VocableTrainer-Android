package vocabletrainer.heinecke.aron.vocabletrainer.activity

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
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ListPickerFragment
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerSettingsFragment
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerSettingsFragment.FinishHandler
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Database
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.SessionStorageManager
import vocabletrainer.heinecke.aron.vocabletrainer.lib.ViewModel.ListPickerViewModel

/**
 * Trainer settings activity
 */
class TrainerSettingsActivity : FragmentActivity(), FinishHandler,
    ListPickerFragment.FinishListener {
    var viewPagerAdapter: ViewPagerAdapter? = null
    private var viewPager: ViewPager? = null
    private var listPicker: ListPickerFragment? = null
    private var listPickerViewModel: ListPickerViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_settings)
        listPickerViewModel = ViewModelProviders.of(this).get(
            ListPickerViewModel::class.java
        )
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
        listPicker = if (savedInstanceState != null) {
            supportFragmentManager.getFragment(
                savedInstanceState,
                ListPickerFragment.TAG
            ) as ListPickerFragment?
        } else {
            ListPickerFragment.newInstance(true, true, null)
        }
        viewPagerAdapter!!.addFragment(listPicker, R.string.TSettings_Tab_List)
        val settingsFragment = TrainerSettingsFragment.newInstance()
        viewPagerAdapter!!.addFragment(settingsFragment, R.string.TSettings_Tab_Settings)
        viewPager!!.adapter = viewPagerAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, ListPickerFragment.TAG, listPicker!!)
    }

    override fun handleFinish(settings: TrainerSettings) {
        val picked: List<VList> = listPickerViewModel!!.selectedLists
        if (picked.isNotEmpty()) {
            SessionStorageManager.CreateSession(Database(baseContext),settings,picked)
            val intent = Intent(this, TrainerActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(baseContext, R.string.TSettings_Info_missing_lists, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun selectionUpdate(selected: ArrayList<VList>) {}
    override fun cancel() {}
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