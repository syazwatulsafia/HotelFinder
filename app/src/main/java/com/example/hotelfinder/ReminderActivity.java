package com.example.hotelfinder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        Button btnSet = findViewById(R.id.btnSetAlarm);
        ImageView btnBack = findViewById(R.id.btn_back_arrow);

        btnBack.setOnClickListener(v -> finish());

        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        timePicker.setIs24HourView(true);

        btnSet.setOnClickListener(v -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedCalendar.set(Calendar.MINUTE, timePicker.getMinute());
            } else {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                selectedCalendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            }
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            if (selectedCalendar.before(Calendar.getInstance())) {
                vibrateError();
                Toast.makeText(this, "Please select a time in the future!", Toast.LENGTH_SHORT).show();
            } else {
                startAlarm(selectedCalendar);
            }
        });
    }

    private void vibrateError() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Two short pulses for an "error" feel
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 50, 100, 50}, -1));
            } else {
                vibrator.vibrate(200); // Standard vibration for old versions
            }
        }
    }

    private void startAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Reminder scheduled!", Toast.LENGTH_SHORT).show();
    }
}