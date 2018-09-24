package vocabletrainer.heinecke.aron.vocabletrainer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.ArrayList;

import vocabletrainer.heinecke.aron.vocabletrainer.R;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.TrainerSettings;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Storage.VList;
import vocabletrainer.heinecke.aron.vocabletrainer.lib.Trainer.Trainer;

import static vocabletrainer.heinecke.aron.vocabletrainer.activity.MainActivity.PREFS_NAME;

/**
 * Trainer settings fragment
 */
public class TrainerSettingsFragment extends BaseFragment {

    /**
     * Ok button handler
     */
    public interface FinishHandler {
        /**
         * Called when ok button pressed
         * @param settings
         */
        void handleFinish(TrainerSettings settings);
    }

    // shared prefs keys
    private final static String P_KEY_TS_TIMES_VOCABLE = "vocable_repeat";
    private final static String P_KEY_TS_TRAIN_MODE = "training_mode";
    private final static String P_KEY_TS_ALLOW_HINTS = "hints_allowed";
    private final static String P_KEY_TS_CASE_SENSITIVE = "case_sensitive";
    private final static String P_KEY_TS_TRIM = "trim_input";
    private final static String P_KEY_TS_ADDITION_AUTO = "addition_auto";

    private static final String TAG = "TrainerSettingsFragment";
    View view;
    public ArrayList<VList> lists;
    CheckBox bHints;
    CheckBox bCaseSensitive;
    CheckBox bTrimSpaces;
    CheckBox bAdditionAuto;
    EditText tTimesVocable;
    private Trainer.TEST_MODE testMode;
    private RadioButton[] rButtons;
    private FinishHandler handler;

    /**
     * Create new TrainerSettingsFragment
     * The attached context has to implement {@link FinishHandler}
     * @return
     */
    public static TrainerSettingsFragment newInstance(){
        return new TrainerSettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        handler = (FinishHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trainer_settings, container, false);
        bHints = view.findViewById(R.id.tSettingsChkAllowTips);
        bCaseSensitive = view.findViewById(R.id.chkTSettingsChkCaseSens);
        bTrimSpaces = view.findViewById(R.id.tSettingsChkTrimSpaces);
        bAdditionAuto = view.findViewById(R.id.tSettingsChkAdditionAuto);
        tTimesVocable = view.findViewById(R.id.tSettingsSolveTimes);
        RadioButton rbA = view.findViewById(R.id.rTSettingsA);
        RadioButton rbB = view.findViewById(R.id.rTSettingsB);
        RadioButton rbR = view.findViewById(R.id.rTSettingsAB);
        rButtons = new RadioButton[]{rbA,rbB,rbR};
        Button bStart = view.findViewById(R.id.tSettingsOkBtn);
        bStart.setOnClickListener(v -> {
            int timesToSolve;
            try {
                timesToSolve = Integer.valueOf(tTimesVocable.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            boolean showHints = bHints.isChecked();
            boolean caseSensitive = bCaseSensitive.isChecked();
            boolean trimSpaces = bTrimSpaces.isChecked();
            boolean additionAuto = bAdditionAuto.isChecked();
            TrainerSettings settings = new TrainerSettings(timesToSolve, testMode,
                    showHints, caseSensitive,trimSpaces, additionAuto);
            handler.handleFinish(settings);
        });

        init();

        return view;
    }

    /**
     * Radio button clicked handler
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        refreshTestMode(view);
    }

    /**
     * Update testmode based on selected view
     * @param view
     */
    private void refreshTestMode(View view){
        switch (view.getId()) {
            case R.id.rTSettingsA:
                testMode = Trainer.TEST_MODE.A;
                break;
            case R.id.rTSettingsB:
                testMode = Trainer.TEST_MODE.B;
                break;
            case R.id.rTSettingsAB:
                testMode = Trainer.TEST_MODE.RANDOM;
                break;
            default:
                Log.w(TAG,"invalid view passed for mode refresh");
        }
    }


    /**
     * Returns position of the currently checked radio button in the list
     * @return
     */
    private int getChecked(){
        for(int i = 0; i < rButtons.length ; i++){
            if (rButtons[i].isChecked())
                return i;
        }
        Log.w(TAG,"no button selected!");
        return 0;
    }


    /**
     * Setup view
     */
    private void init() {
        // Load past values
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        // use string to show the hint at first via empty string
        tTimesVocable.setText(settings.getString(P_KEY_TS_TIMES_VOCABLE, ""));
        bHints.setChecked(settings.getBoolean(P_KEY_TS_ALLOW_HINTS, true));
        bCaseSensitive.setChecked(settings.getBoolean(P_KEY_TS_CASE_SENSITIVE,true));
        bTrimSpaces.setChecked(settings.getBoolean(P_KEY_TS_TRIM,true));
        bAdditionAuto.setChecked(settings.getBoolean(P_KEY_TS_ADDITION_AUTO,true));

        for(RadioButton rb : rButtons)
            rb.setOnClickListener(this::onRadioButtonClicked);

        int cRB = (settings.getInt(P_KEY_TS_TRAIN_MODE, 0));
        RadioButton rbtn;
        if(cRB < rButtons.length){
            rbtn = rButtons[cRB];
        }else{
            rbtn = rButtons[0];
        }
        rbtn.setChecked(true);
        refreshTestMode(rbtn); // update here to init testmode
    }

    @Override
    public void onStop() {
        super.onStop();

        // Save values
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(P_KEY_TS_ALLOW_HINTS, bHints.isChecked());
        editor.putInt(P_KEY_TS_TRAIN_MODE, getChecked());
        editor.putString(P_KEY_TS_TIMES_VOCABLE,tTimesVocable.getText().toString());
        editor.putBoolean(P_KEY_TS_CASE_SENSITIVE,bCaseSensitive.isChecked());
        editor.putBoolean(P_KEY_TS_TRIM,bTrimSpaces.isChecked());
        editor.putBoolean(P_KEY_TS_ADDITION_AUTO, bAdditionAuto.isChecked());
        editor.apply();
    }
}
