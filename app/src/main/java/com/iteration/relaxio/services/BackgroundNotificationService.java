package com.iteration.relaxio.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.iteration.relaxio.R;
import com.iteration.relaxio.activity.MainActivity;
import com.iteration.relaxio.database.DatabaseClient;
import com.iteration.relaxio.database.SoundData;
import com.iteration.relaxio.network.DownloadInterface;
import com.iteration.relaxio.utility.Constant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;


public class BackgroundNotificationService extends IntentService {
    SoundData track;
    String local_file_path = "";

    public BackgroundNotificationService() {
        super("Service");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;


    @Override
    protected void onHandleIntent(Intent intent) {
        track = (SoundData) intent.getSerializableExtra("data");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("id", "an", NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription("no sound");
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        notificationBuilder = new NotificationCompat.Builder(this, "id")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Sound " + track.getS_id())
                .setDefaults(0)
                .setAutoCancel(true);
        notificationManager.notify(track.getS_id(), notificationBuilder.build());

        initRetrofit();

    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://sound.iterationtechnology.com/")
                .build();
        DownloadInterface retrofitInterface = retrofit.create(DownloadInterface.class);

        Call<ResponseBody> request = retrofitInterface.downloadImage(track.getS_sound());
        try {
            downloadImage(request.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            onDownloadComplete(true, track, local_file_path);
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void downloadImage(ResponseBody body) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream inputStream = new BufferedInputStream(body.byteStream(), 1024 * 8);
        String fileName = track.getS_sound().substring(track.getS_sound().lastIndexOf('/') + 1);
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Relexo";
        File dir = new File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outputFile = new File(fullPath, track.getS_id() + "_" + fileName);
        String local_file_path = outputFile.getAbsolutePath();
        OutputStream outputStream = new FileOutputStream(outputFile);
        long total = 0;
        boolean downloadComplete = false;
        //int totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));

        while ((count = inputStream.read(data)) != -1) {
            total += count;
            int progress = (int) ((double) (total * 100) / (double) fileSize);
            updateNotification(progress);
            outputStream.write(data, 0, count);
            downloadComplete = true;
        }
        onDownloadComplete(downloadComplete, track, local_file_path);
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    private void updateNotification(int currentProgress) {
        notificationBuilder.setProgress(100, currentProgress, false);
        notificationBuilder.setContentText(currentProgress + "%");
        notificationManager.notify(track.getS_id(), notificationBuilder.build());
    }

    private void sendProgressUpdate(boolean downloadComplete) {
        Intent intent = new Intent(MainActivity.PROGRESS_UPDATE);
        intent.putExtra("downloadComplete", downloadComplete);
        LocalBroadcastManager.getInstance(BackgroundNotificationService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete(boolean downloadComplete, SoundData trackDownload, String local_file_path) {
        notificationManager.cancel(track.getS_id());
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText(getString(R.string.download_completed));
        notificationManager.notify(track.getS_id(), notificationBuilder.build());
        if (downloadComplete) {
            updataData(track,local_file_path);
        }
        sendProgressUpdate(downloadComplete);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(track.getS_id());
    }


    public void updataData(final SoundData data, final String local_file_path) {
        class GetSound extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {

                if (local_file_path.equals("")) {
                    data.setStatus(Constant.ERROR);
                } else {
                    data.setStatus(Constant.COMPLETE);
                }
                data.setLocal_url(local_file_path);
                DatabaseClient.getInstance(BackgroundNotificationService.this).getAppDatabase().soundDataDao().update(data);
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
            }
        }
        GetSound dt = new GetSound();
        dt.execute();

    }
}