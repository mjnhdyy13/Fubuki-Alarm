package com.hfad.alarmclock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static java.util.Calendar.AM;
import static java.util.Calendar.AM_PM;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.PM;

public class CreateAlarm extends AppCompatActivity {

    TimePicker timePicker;
    TextView textView;
    Boolean ischeckagain=false;
    Switch switchre;
    int mhour,mmMin;
    AlertDialog alert;
    boolean started = false;





    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);
        Log.i("In on create creatalarm", "Ohhhhhhh");

        //check permission**************************************************
        if (isMyServiceRunning(MyService2.class)){
            started = true;
        }
        start_stop();
        //*************************************************************



        //T???o notify  channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("noty","noty",NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        switchre = (Switch) findViewById(R.id.switchrepeat);
        //b???t ho???c t???t l???p l???i cho b??o th???c m???i
        switchre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ischeckagain = true;
                } else {
                    ischeckagain= false;

                }
            }
        });

        Date date1 = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);




        mhour = cal.get(HOUR_OF_DAY);
        mmMin = cal.get(MINUTE);
        timePicker = (TimePicker) findViewById(R.id.timePicker);


        //set th???i gian cho alarm m???i
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mhour = hourOfDay;
                mmMin = minute;
                Log.i(String.valueOf(mhour)+":"+String.valueOf(mmMin)+"inchange", "Ohhhhhhh");


            }
        });
        Log.i(String.valueOf(mhour)+":"+String.valueOf(mmMin), "Ohhhhhhh");





    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //t???o alarm m???i
    public void setTimer(View v){

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Date date = new Date();
        Calendar cal_alarm = Calendar.getInstance();//th???i gian s??? b??o th???c
        Calendar cal_now = Calendar.getInstance();//th???i gian hi???n t???i

        //set th???i gian cho c??? hai
        cal_now.setTime(date);
        cal_alarm.setTime(date);


        Log.i(String.valueOf(mhour)+":"+String.valueOf(mmMin)+"on if", "Ohhhhhhh");
        //set th???i gian b??o th???c
        cal_alarm.set(HOUR_OF_DAY,mhour);
        cal_alarm.set(Calendar.MINUTE,mmMin);
        cal_alarm.set(Calendar.SECOND, 0);




        //k??ch ho???t khi th???i gian b??o th???c ???? qua v?? k??ch ho???t v??o ng??y mai
        if(cal_alarm.before(cal_now)){
            cal_alarm.add(Calendar.DATE,1);
        }
        //l???y requestcode c???a pending intent
        final int id = (int) System.currentTimeMillis();


        //l???y v??? tr?? c???a b??o th???c m???i
        Intent intenta = getIntent();
        int size = intenta.getIntExtra("posL",0);
        Log.i(String.valueOf(size)+"on create", "zzzz");

        //g???i v??? tr?? c???a b??o th???c cho broadcast
        Intent intentS = new Intent(this,Broadcast2.class);
        intentS.putExtra("My_User",size);



        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),id,intentS,PendingIntent.FLAG_UPDATE_CURRENT);

        String idd = String.valueOf(id);

        //set l???p l???i cho b??o th???c n???u c??
        if(ischeckagain){

            Data mydata = new Data.Builder().putInt("hour",mhour).putInt("minute",mmMin).build();

            Constraints constraints = new Constraints.Builder().build();
            PeriodicWorkRequest saveReq  =
                    new PeriodicWorkRequest.Builder(schelduleAlarm.class, 23, TimeUnit.HOURS).setInitialDelay(5,TimeUnit.SECONDS)
                            .addTag("pero")
                            .setInputData(mydata)
                            .setConstraints(constraints)
                            .build();
            Log.i("in checkagain", "Ohhhhhhh");
            WorkManager.getInstance().enqueueUniquePeriodicWork("pero", ExistingPeriodicWorkPolicy.REPLACE,saveReq);
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal_alarm.getTimeInMillis(), pendingIntent);
            }else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal_alarm.getTimeInMillis(), pendingIntent);
            }
        }
        //G???i c??c thu???c t??nh c???a alarm cho ListAlarm
        Intent intent = new Intent(CreateAlarm.this,ListAlarm.class);
        String time;
        String hour1 = String.valueOf(mhour);
        String min1 = String.valueOf(mmMin);
        if(mhour<10){
            hour1 = "0"+String.valueOf(mhour);
        }
        if(mmMin<10){
            min1 = "0"+String.valueOf(mmMin);
        }
        time = hour1+":"+min1;
        /*if(mmMin!=0 || mhour!=0) {
            String hour1 = String.valueOf(mhour);
            String min1 = String.valueOf(mmMin);
            if(mhour<10){
                hour1 = "0"+String.valueOf(mhour);
            }
            if(mmMin<10){
                min1 = "0"+String.valueOf(mmMin);
            }
            time = hour1+":"+min1;
        }else {

            String s = String.valueOf(java.time.LocalTime.now());
            String[] output = s.split(":");



            time = String.valueOf(cal_now.get(HOUR_OF_DAY))+":"+output[1];
        }*/


        intent.putExtra("time",time);
        intent.putExtra("check",true);
        intent.putExtra("id",idd);
        if(ischeckagain){
            intent.putExtra("repeat","L???p l???i");
        }else {
            intent.putExtra("repeat", "M???t l???n");
        }
        startActivity(intent);

    }


    @Override
    protected void onStop() {
        Log.i("in stop", "Ohhhhhhh");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("in destroy createAlarm", "Ohhhhhhh");

        super.onDestroy();
    }
    //**************************check permission
    public void start_stop() {
        if (checkPermission()) {
            if (started) {
                started = false;
            }
        }else {
            reqPermission();
        }

    }

    //ki???m tra li???u c?? ???????c ph??p hay ch??a
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            if (checkPermission()) {
                start_stop();
            } else {
                reqPermission();
            }
        }
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                reqPermission();
                return false;
            }
            else {
                return true;
            }
        }else{
            return true;
        }

    }

    //Hi???n th??? th??ng b??o y??u c???u permission
    private void reqPermission(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Screen overlay detected");
        alertBuilder.setMessage("Enable 'Draw over other apps' in your system setting.");
        alertBuilder.setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,RESULT_OK);
            }
        });

        alert = alertBuilder.create();
        alert.show();


    }


    @Override
    protected void onRestart() {
        super.onRestart();
        alert.dismiss();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
