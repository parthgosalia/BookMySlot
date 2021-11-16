package com.e.bookmyslot;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Activity";
    private EditText edAmount;
    private Button btn_Proceed,btn_supportUs,btn_Share;
    TextView tv_time;
    public static Timer timer = new Timer();
    AlertDialog dialog;
    String dose="";
    RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edAmount = findViewById(R.id.ed_amount);
        btn_Proceed = findViewById(R.id.btn_proceed);
        tv_time = findViewById(R.id.tv_time);
        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);


        btn_Proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edAmount.getText().toString().equals("")||dose.equals("")){
                    showAlert("My Vaccine Turn","Please enter PinCode and Select a Dose ",MainActivity.this);
                }
                else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Name",edAmount.getText().toString());
                    editor.putString("dose",dose);
                    editor.apply();

                    showAlert(getResources().getString(R.string.app_name),"Success, Now you can keep the app in background",MainActivity.this);


                }

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(this,
                BackgroundService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        timer.cancel();

        stopService(new Intent(MainActivity.this, BackgroundService.class));
        SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String time = preferences1.getString("time", "");
        final String date = preferences1.getString("date","");
        Log.e(TAG, "time: "+time );
        Log.e(TAG, "date: "+date );
        tv_time.setText("Last Search Date & Time:- "+date+" "+time);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart: ");
       // BackgroundService.timer.cancel();

        stopService(new Intent(MainActivity.this, BackgroundService.class));
    }


    public void showAlert(String title, String msg, final Context context) {
        AlertDialog.Builder builder;


        builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                dialog.cancel();
            }
        });
        dialog = builder.create();
        dialog.show();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Activity onDestroy: " );
        timer.cancel();
        stopService(new Intent(MainActivity.this, BackgroundService.class));

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_firstDose:
                if (checked)
                    dose = "first";
                    break;
            case R.id.radio_secondDose:
                if (checked)
                    dose = "second";
                    break;
        }
    }
}