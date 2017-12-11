package com.example.rikharthu.jobschedulerdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import timber.log.Timber;

public class MyJobService extends JobService {

    public static final int GREETING_JOB_ID = 1000;

    /*
    Register in XML with BIND_JOB_SERVICE permission

    Call jobFinished(<current_job>,<do_reschedule_job>) when your service or thread has finished working on the job
    (if you return false in onStartJob())
     */

    // Called by the system when it is time for your job to execute
    // Runs on the main thread!
    // Return true if there is something left to do (job will be rescheduled). Return false otherwise.
    // What will automaticallyCall jobFinished()
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Timber.d("Starting job");

        // Do something according to passed job parameters
        Toast.makeText(this, "Executing a job #" + jobParameters.getJobId(), Toast.LENGTH_SHORT).show();

        //Set up the notification content intent to launch the app when clicked
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.job_service))
                .setContentText(getString(R.string.job_running))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        manager.notify(0, builder.build());

        Timber.d("Finishing job");
        return false; // Answers the question: "Is there still work going on?"
    }

    // Called by the system if the job is cancelled before it finishes
    // Return true if you want yor job to be re-schedule. False if you don't care
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // Cancel ongoing job
        Timber.d("Stopping job");
        return false; // Answers the question: "Should this job be retried?"
    }
}
