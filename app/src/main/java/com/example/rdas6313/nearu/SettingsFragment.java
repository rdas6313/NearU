package com.example.rdas6313.nearu;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{

    private TextView titleView,summaryView,seekbarTextView;
    private SeekBar seekBar;
    private Toolbar toolbar;

    private int maxProgress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_fragment,container,false);
        titleView = (TextView)root.findViewById(R.id.titleBar);
        summaryView = (TextView)root.findViewById(R.id.summary);
        seekbarTextView = (TextView)root.findViewById(R.id.seekbarText);
        seekBar = (SeekBar)root.findViewById(R.id.seekBar);
        toolbar = (Toolbar)root.findViewById(R.id.toolBar);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar.setTitle(R.string.SETTINGS);
        toolbar.setTitleTextColor(Color.WHITE);
        titleView.setText(getString(R.string.SETTINGS_TITLE));
        summaryView.setText(getString(R.string.SETTINGS_SUMMARY));
        seekBar.incrementProgressBy(5);
        seekBar.setOnSeekBarChangeListener(this);
        maxProgress = seekBar.getMax();
        Utility utility = Utility.getInstance();
        int progress = utility.getProgressValueFromSharedPreference(getContext(),getString(R.string.SHARED_PREF_KEY));
        seekBar.setProgress(progress);
        seekBar.setMax(maxProgress);
        seekbarTextView.setText(progress+"/"+maxProgress+" km");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekbarTextView.setText(progress+"/"+maxProgress+" km");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        saveSeekBarData(seekBar.getProgress());
    }

    private void saveSeekBarData(int progress){
        Utility utility = Utility.getInstance();
        utility.saveDataToSharedPreference(getContext(),getString(R.string.SHARED_PREF_KEY),progress);
    }
}
