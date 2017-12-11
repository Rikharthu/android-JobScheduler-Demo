package com.example.rikharthu.jobschedulerdemo;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.network_type_radio_group)
    RadioGroup mNetworkTypeRadioGroup;
    @BindView(R.id.requirement_charging_switch)
    Switch mChargingSwitch;
    @BindView(R.id.requirement_idle_switch)
    Switch mIdleSwitch;
    @BindView(R.id.periodic_job_switch)
    Switch mPeriodicSwitch;
    @BindView(R.id.deadline_label)
    TextView mDeadlineTv;
    @BindView(R.id.deadline_seekbar)
    SeekBar mDeadlineSeekbar;
    private JobScheduler mScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Retrieve JobScheduler from the system
        mScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        mDeadlineSeekbar.setOnSeekBarChangeListener(this);
    }

    @OnClick(R.id.schedule_job_btn)
    void onScheduleJob() {

        // Build a new job request
        Timber.d("Scheduling new job");
        // Configure job constraints
        int selectedNetworkId = mNetworkTypeRadioGroup.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        switch (selectedNetworkId) {
            case R.id.radio_network_default:
                // Will run with/without a network connection (default)
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.radio_network_any:
                // Will run as long as a network is available (wifi/cellular)
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.radio_network_wifi:
                // Will run as long as device is connected to wifi without HotSpot
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        // JobScheduler requires at least one constraint to properly schedule the JobService
        // JobInfo.NETWORK_TYPE_NONE value does not count as constraint in that case
        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
                || mIdleSwitch.isChecked()
                || mChargingSwitch.isChecked()
                || mDeadlineSeekbar.getProgress() > 0;
        if (!constraintSet) {
            Toast.makeText(this, "Please set at least 1 constraint", Toast.LENGTH_SHORT).show();
            return;
        }

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(MyJobService.GREETING_JOB_ID,
                new ComponentName(this, MyJobService.class))
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mIdleSwitch.isChecked())
                .setRequiresCharging(mChargingSwitch.isChecked());
        if (mDeadlineSeekbar.getProgress() > 0) {
            if (mPeriodicSwitch.isChecked()) {
                jobInfoBuilder.setPeriodic(mDeadlineSeekbar.getProgress() * 1000);
            } else {
                // Deadline in millis by which the job must be run
                jobInfoBuilder.setOverrideDeadline(mDeadlineSeekbar.getProgress() * 1000);
            }
        }

        mScheduler.schedule(jobInfoBuilder.build());
    }

    @OnClick(R.id.cancel_jobs_btn)
    void onCancelJobs() {
        mScheduler.cancelAll();
        Toast.makeText(this, "Jobs canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        String progressString = progress > 0 ? " " + progress : " Not Set";
        mDeadlineTv.setText("Override Deadline:" + progressString);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
