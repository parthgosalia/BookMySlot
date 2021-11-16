package com.e.bookmyslot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.e.bookmyslot.MainActivity.timer;
import static com.e.bookmyslot.Notificationchannel.Channel1;
import static com.e.bookmyslot.Notificationchannel.Channel2;

public class BackgroundService extends Service {
    RequestQueue queue;
    RequestQueue queue_NextDay;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    String str_nextDay;
    Notification notification;
    int j=100;



    NotificationManagerCompat notificationManagerCompat;


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        timer = new Timer();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Intent NotificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, NotificationIntent, 0);


        final String currentDate = date.format(new Date());

        try {
            Calendar cd = Calendar.getInstance();
            cd.setTime(date.parse(currentDate));
            cd.add(Calendar.DATE, 1);
            str_nextDay = date.format(cd.getTime());
            Log.e("TAG", "onResponse: " + str_nextDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);
        final String pinCode = preferences.getString("Name", "");
        final String dose = preferences.getString("dose","");
        Log.e("TAG", "dose: "+dose);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final String currentTime = sdf.format(new Date());
                notification = new NotificationCompat.Builder(BackgroundService.this,Channel2)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText("Online- "+currentTime)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setContentIntent(pendingIntent)
                        .build();
                startForeground(9999,notification);
                notificationManagerCompat.notify(9999,notification);

                String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode=" + pinCode + "&date=" + currentDate;
                Log.e("TAG", "run: " + url);

                queue = Volley.newRequestQueue(BackgroundService.this);
                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( BackgroundService.this);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("time",currentTime);
                                    editor.putString("date",currentDate);
                                    editor.apply();
                                    Log.e("TAG", "timebackg: "+currentTime );
                                    Log.e("TAG", "onResponse: " + response);
                                    // JSONObject json = new JSONObject(response);
                                    JSONArray jsonArray = response.getJSONArray("sessions");
                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject bookingObject = jsonArray.getJSONObject(i);
                                            int age = bookingObject.getInt("min_age_limit");
                                            int captacity = 0;
                                            String vaccineName,placeName;

                                            if (age == 18) {
                                                if (dose.equals("first")) {
                                                    captacity = bookingObject.getInt("available_capacity_dose1");
                                                    vaccineName = bookingObject.getString("vaccine");
                                                    placeName = bookingObject.getString("name");
                                                }else {
                                                    captacity = bookingObject.getInt("available_capacity_dose2");
                                                    vaccineName = bookingObject.getString("vaccine");
                                                    placeName = bookingObject.getString("name");
                                                }
                                                if (captacity>0) {
                                                    notification = new NotificationCompat.Builder(BackgroundService.this, Channel1)
                                                            .setSmallIcon(R.drawable.logo)
                                                            .setContentTitle(getResources().getString(R.string.app_name1))
                                                            .setContentText(captacity + " vaccines of " + vaccineName + " available at " + placeName)
                                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                            .setContentIntent(pendingIntent)
                                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                                    .bigText(captacity + " vaccines of " + vaccineName + " available at " + placeName))
                                                            .build();


                                                    notificationManagerCompat.notify(i, notification);
                                                }
                                            }else{}
                                        }

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        notification = new NotificationCompat.Builder(BackgroundService.this, Channel1)
                                .setSmallIcon(R.drawable.logo)
                                .setContentTitle(getResources().getString(R.string.app_name1))
                                .setContentText("Something went wrong, Please try again later")
                                .setContentIntent(pendingIntent)

                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .build();

                        notificationManagerCompat.notify(1, notification);
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);


            }
        }, 0, 30 * 1000); // InMilli sec

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String url_nextDay = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode=" + pinCode + "&date=" + str_nextDay;
                Log.e("TAG", "run: " + url_nextDay);


                queue_NextDay = Volley.newRequestQueue(BackgroundService.this);
                JsonObjectRequest stringRequest_NextDay = new JsonObjectRequest(Request.Method.GET, url_nextDay, null,

                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.e("TAG", "onResponse: NextDay" + response);
                                    // JSONObject json = new JSONObject(response);
                                    JSONArray jsonArray = response.getJSONArray("sessions");
                                    if (jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            j++;
                                            JSONObject bookingObject = jsonArray.getJSONObject(i);
                                            int age = bookingObject.getInt("min_age_limit");
                                            Log.e("TAG", "age: "+age);
                                            int captacity = 0;
                                            String vaccineName,placeName;
                                            if (age == 18) {
                                                if (dose.equals("first")) {
                                                    captacity = bookingObject.getInt("available_capacity_dose1");
                                                    vaccineName = bookingObject.getString("vaccine");
                                                    placeName = bookingObject.getString("name");
                                                }else {
                                                    captacity = bookingObject.getInt("available_capacity_dose2");
                                                    vaccineName = bookingObject.getString("vaccine");
                                                    placeName = bookingObject.getString("name");
                                                }
                                                if (captacity>0) {
                                                    notification = new NotificationCompat.Builder(BackgroundService.this, Channel1)
                                                            .setSmallIcon(R.drawable.logo)

                                                            .setContentTitle(getResources().getString(R.string.app_name1))
                                                            .setContentText(captacity + " vaccines of " + vaccineName + " available at " + placeName)
                                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                                            .setContentIntent(pendingIntent)
                                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                                    .bigText(captacity + " vaccines of " + vaccineName + " available at " + placeName))
                                                            .build();

                                                    notificationManagerCompat.notify(j, notification);
                                                }
                                            }else{

                                            }

                                        }

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                         notification = new NotificationCompat.Builder(BackgroundService.this, Channel1)
                                // .setSmallIcon(R.drawable.phone_icon)
                                .setSmallIcon(R.drawable.logo)
                                .setContentTitle(getResources().getString(R.string.app_name1))
                                .setContentText("Something went wrong, Please try again later")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .build();

                        notificationManagerCompat.notify(1, notification);
                    }
                });

                queue_NextDay.add(stringRequest_NextDay);


            }
        }, 0, 30000);


        return START_STICKY;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "onDestroy: ");


    }


}