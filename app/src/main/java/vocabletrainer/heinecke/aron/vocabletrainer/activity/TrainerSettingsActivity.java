package vocabletrainer.heinecke.aron.vocabletrainer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.ListPickerFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.fragment.TrainerSettingsFragment;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.PARAM_TABLES;
import static vocabletrainer.heinecke.aron.vocabletrainer.activity.TrainerActivity.PARAM_TRAINER_SETTINGS;

/**
 * Trainer settings activity
 */
public class TrainerSettingsActivity extends FragmentActivity implements TrainerSettingsFragment.FinishHandler, ListPickerFragment.FinishListener {

    private static final String TAG = "TrainerSettings";

    ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private ListPickerFragment listPicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_settings);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.pager);
        initViewPager(savedInstanceState);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Init ViewPager
     */
    private void initViewPager(Bundle savedInstanceState){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (savedInstanceState != null) {
            listPicker = (ListPickerFragment) getSupportFragmentManager().getFragment(savedInstanceState, ListPickerFragment.TAG);
        } else {
            listPicker = ListPickerFragment.newInstance(true, false, null, false);
        }
        viewPagerAdapter.addFragment(listPicker,R.string.TSettings_Tab_List);
        TrainerSettingsFragment settingsFragment = TrainerSettingsFragment.newInstance();
        viewPagerAdapter.addFragment(settingsFragment,R.string.TSettings_Tab_Settings);

        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, ListPickerFragment.TAG, listPicker);
    }

    @Override
    public void handleFinish(TrainerSettings settings) {
        ArrayList<VList> picked = listPicker.getSelectedItems();
        if(picked.size() > 0) {
            Intent intent = new Intent(this, TrainerActivity.class);
            intent.putExtra(PARAM_TRAINER_SETTINGS, settings);
            intent.putParcelableArrayListExtra(PARAM_TABLES, picked);
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), R.string.TSettings_Info_missing_lists, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void selectionFinished(ArrayList<VList> selected) { }

    @Override
    public void cancel() { }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /**
         * Add Fragment to viewpager
         * @param fragment Fragment
         * @param title Tab-Title string resource
         */
        void addFragment(Fragment fragment,@StringRes int title){
            addFragment(fragment,getString(title));
        }

        /**
         * Add Fragment to viewpager
         * @param fragment Fragment
         * @param title Title
         */
        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
