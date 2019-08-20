package com.iteration.relaxio.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iteration.relaxio.R;
import com.iteration.relaxio.database.DatabaseClient;
import com.iteration.relaxio.database.SoundData;
import com.iteration.relaxio.dialog.FullScreenDialog;
import com.iteration.relaxio.listener.CallbackListener;
import com.iteration.relaxio.model.Message;
import com.iteration.relaxio.model.Sound;
import com.iteration.relaxio.model.SoundList;
import com.iteration.relaxio.network.Config;
import com.iteration.relaxio.network.GetProductDataService;
import com.iteration.relaxio.network.RetrofitInstance;
import com.iteration.relaxio.services.BackgroundNotificationService;
import com.iteration.relaxio.utility.Constant;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ArrayList<Sound> SoundListArray = new ArrayList<>();
    LinearLayout llFavorites, llFavoritesList, llBox, llPlayPause, llTimer, llVolume, llTimerLine, llPlayLine;
    LinearLayout llMain, llLeft, llRight;
    TextView tvTimer;
    MediaPlayer[] player;
    List<Sound> selectedList = new ArrayList<>();
    ImageView ivPlay, ivTimer;
    GetProductDataService productDataService;
    String[] listTimer;
    int selectedTimerItem = 0;
    CountDownTimer countDownTimer = null;
    private AudioManager audioManager = null;
    public static final String PROGRESS_UPDATE = "progress_update";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        productDataService = RetrofitInstance.getRetrofitInstance().create(GetProductDataService.class);

        llFavorites = (LinearLayout) findViewById(R.id.llFavorites);
        llPlayPause = (LinearLayout) findViewById(R.id.llPlayPause);
        llPlayLine = (LinearLayout) findViewById(R.id.llPlayLine);
        llTimerLine = (LinearLayout) findViewById(R.id.llTimerLine);
        llTimer = (LinearLayout) findViewById(R.id.llTimer);
        llVolume = (LinearLayout) findViewById(R.id.llVolume);
        llFavoritesList = (LinearLayout) findViewById(R.id.llFavoritesList);
        llBox = (LinearLayout) findViewById(R.id.llBox);
        tvTimer = (TextView) findViewById(R.id.tvTime);
        ivTimer = (ImageView) findViewById(R.id.ivTimer);

        llLeft = (LinearLayout) findViewById(R.id.llLeft);
        llRight = (LinearLayout) findViewById(R.id.llRight);
        ivPlay = (ImageView) findViewById(R.id.ivPlay);

        listTimer = getResources().getStringArray(R.array.timer);

        displayFirebaseRegId();

        registerReceiver();

        playHide();

        if (checkPermission()) {
            getSoundAppi();
        } else {
            requestPermission();
        }


        llFavoritesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenDialog dialog = new FullScreenDialog();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Bundle b = new Bundle();
                b.putSerializable("data", (Serializable) selectedList);
                dialog.setCallbackListener(new CallbackListener() {
                    @Override
                    public void onPlay(List<Sound> list) {
                        selectReleasePlayer();
                        selectedList.clear();
                        selectedList.addAll(list);
                        setSoundList(SoundListArray);
                        selectedPlay();
                        playShow();
                    }
                });
                dialog.setArguments(b);
                dialog.show(ft, "");
            }
        });

        ivPlay.setTag(0);
        llPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSoundPlayPause();
            }
        });

        llTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerDialog();
            }
        });

        llVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeControlDialog();
            }
        });
    }

    private void displayFirebaseRegId() {
        String regId = Config.getToken(MainActivity.this);
        if (!TextUtils.isEmpty(regId)) {
            if (!Config.uploadToken(MainActivity.this)) {
                updateToken(regId);
            }
        } else {
            Toast.makeText(MainActivity.this, "Firebase Reg Id is not received yet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateToken(String token) {
        String getWifiMac = Config.getWifiMacAddress();
        Call<Message> TokenCall = productDataService.getTokenData(getWifiMac,token);
        TokenCall.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                String status = response.body().getStatus();
                String message = response.body().getMessage();
                if (status.equals("1"))
                {
                    Log.e("message",""+message);
                }
                else
                {
                    Log.e("message",""+message);
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getSoundAppi() {

        dialog.setMessage("Please wait for setup sound");
        dialog.setCancelable(false);
        dialog.show();

        GetProductDataService productDataService = RetrofitInstance.getRetrofitInstance().create(GetProductDataService.class);
        Call<SoundList> SoundListCall = productDataService.getSoundData();
        SoundListCall.enqueue(new Callback<SoundList>() {
            @Override
            public void onResponse(Call<SoundList> call, Response<SoundList> response) {
                String status = response.body().getStatus();
                String message = response.body().getMessage();
                if (status.equals("1")) {

                    setDataBase(response.body().getSoundList());
//                    SoundListArray = response.body().getSoundList();
//                    setSoundList(SoundListArray);
                } else {
                    getSound();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SoundList> call, Throwable t) {
                getSound();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setDataBase(ArrayList<Sound> soundList) {
        checkData(soundList);
    }

    private void checkData(final List<Sound> list) {
        class CheckData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                for (int i = 0; i < list.size(); i++) {
                    Sound sound = list.get(i);
                    List<SoundData> data = DatabaseClient.getInstance(MainActivity.this).getAppDatabase().soundDataDao().getSoundData(Integer.parseInt(sound.getS_id()));
                    if (data.size() == 0) {
                        SoundData soundData = new SoundData();
                        soundData.setS_id(Integer.parseInt(sound.getS_id()));
                        soundData.setS_color(sound.getS_color());
                        soundData.setS_img(sound.getS_img());
                        soundData.setS_sound(sound.getS_sound());
                        soundData.setStatus(Constant.PENDDIG);
                        soundData.setLocal_url("");
                        DatabaseClient.getInstance(MainActivity.this).getAppDatabase().soundDataDao().insert(soundData);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getPendding();
            }
        }

        CheckData dt = new CheckData();
        dt.execute();

    }

    private void getSound() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        class GetSound extends AsyncTask<Void, Void, List<SoundData>> {
            @Override
            protected List<SoundData> doInBackground(Void... voids) {
                List<SoundData> data = DatabaseClient.getInstance(MainActivity.this).getAppDatabase().soundDataDao().getCompletedTracks();
                return data;
            }

            @Override
            protected void onPostExecute(List<SoundData> data) {
                super.onPostExecute(data);
                SoundListArray.clear();
                //SoundListArray = response.body().getSoundList();

                for (int i = 0; i < data.size(); i++) {
                    SoundData sound = data.get(i);
                    Sound soundData = new Sound();
                    soundData.setS_id(sound.getS_id() + "");
                    soundData.setS_color(sound.getS_color());
                    soundData.setS_img(sound.getS_img());
                    soundData.setS_sound(sound.getLocal_url());
                    SoundListArray.add(soundData);
                }
                setSoundList(SoundListArray);
            }
        }

        GetSound dt = new GetSound();
        dt.execute();

    }

    private void getPendding() {
        class GetPendingSound extends AsyncTask<Void, Void, List<SoundData>> {
            @Override
            protected List<SoundData> doInBackground(Void... voids) {
                List<SoundData> data = DatabaseClient.getInstance(MainActivity.this).getAppDatabase().soundDataDao().getPendingTracks();
                return data;
            }

            @Override
            protected void onPostExecute(List<SoundData> data) {
                super.onPostExecute(data);
                for (int i = 0; i < data.size(); i++) {
                    SoundData sound = data.get(i);
                    Intent intent = new Intent(MainActivity.this, BackgroundNotificationService.class);
                    intent.putExtra("data", sound);
                    startService(intent);
                }
                if (data.size() == 0) {
                    getSound();
                }
            }
        }
        GetPendingSound dt = new GetPendingSound();
        dt.execute();
    }

    private void getPenddingCheck() {
        class GetPendingSound extends AsyncTask<Void, Void, List<SoundData>> {
            @Override
            protected List<SoundData> doInBackground(Void... voids) {
                List<SoundData> data = DatabaseClient.getInstance(MainActivity.this).getAppDatabase().soundDataDao().getPendingTracks();
                return data;
            }

            @Override
            protected void onPostExecute(List<SoundData> data) {
                super.onPostExecute(data);
                if (data.size() == 0) {
                    getSound();
                }
            }
        }
        GetPendingSound dt = new GetPendingSound();
        dt.execute();
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSoundAppi();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
                break;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void timerDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Set Timer Duration");
        mBuilder.setSingleChoiceItems(listTimer, selectedTimerItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectedTimerItem = i;
                setTimer(i);
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setTimer(int i) {
        switch (i) {
            case 0:
                setTimerTick(0);
                break;
            case 1:
                setTimerTick(5 * 1000 * 60);
                break;
            case 2:
                setTimerTick(10 * 1000 * 60);
                break;
            case 3:
                setTimerTick(15 * 1000 * 60);
                break;
            case 4:
                setTimerTick(20 * 1000 * 60);
                break;
            case 5:
                setTimerTick(30 * 1000 * 60);
                break;
            case 6:
                setTimerTick(40 * 1000 * 60);
                break;
            case 7:
                setTimerTick(1 * 1000 * 60 * 60);
                break;
            case 8:
                setTimerTick(2 * 1000 * 60 * 60);
                break;
            case 9:
                setTimerTick(4 * 1000 * 60 * 60);
                break;
            case 10:
                setTimerTick(8 * 1000 * 60 * 60);
                break;

        }
    }

    private void setTimerTick(int time) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                long totalSec = millisUntilFinished / 1000;
                String time = convertTime(totalSec);
                tvTimer.setText(time);
                tvTimer.setVisibility(View.VISIBLE);
                ivTimer.setVisibility(View.GONE);
            }

            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                ivTimer.setVisibility(View.VISIBLE);
                setSoundPlayPause();
            }
        };
        if (time == 0) {
            tvTimer.setVisibility(View.GONE);
            ivTimer.setVisibility(View.VISIBLE);
            countDownTimer.cancel();
        } else {
            countDownTimer.start();
        }
    }

    private void setSoundPlayPause() {
        if (Integer.parseInt(ivPlay.getTag().toString()) == 0) {
            ivPlay.setImageResource(R.drawable.ic_pause);
            ivPlay.setTag(1);
            selectedPlay();
        } else {
            ivPlay.setImageResource(R.drawable.ic_play);
            ivPlay.setTag(0);
            selectReleasePlayer();
        }
    }

    private void selectedPlay() {
        for (int i = 0; i < selectedList.size(); i++) {
            Sound item = selectedList.get(i);
            //  final Uri uri = Uri.parse(RetrofitInstance.BASE_URL + item.getS_sound());
            final Uri uri = Uri.fromFile(new File(item.getS_sound()));
            try {
                int finalI = item.getFinalI();
                ivPlay.setTag(1);
                ivPlay.setImageResource(R.drawable.ic_pause);
                player[finalI] = new MediaPlayer();
                player[finalI].setAudioStreamType(AudioManager.STREAM_MUSIC);
                player[finalI].setLooping(true);
                player[finalI].setDataSource(MainActivity.this, uri);
                player[finalI].setAudioStreamType(AudioManager.STREAM_MUSIC);
                player[finalI].prepare();
                player[finalI].start();
                float volume = (float) (1 - (Math.log(100 - item.getSoundProgress()) / Math.log(100)));
                player[finalI].setVolume(volume, volume);
                item.setFinalI(finalI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectReleasePlayer() {
        for (int i = 0; i < selectedList.size(); i++) {
            Sound item = selectedList.get(i);
            //     final Uri uri = Uri.parse(RetrofitInstance.BASE_URL + item.getS_sound());
            try {
                int finalI = item.getFinalI();
                if (player[finalI] != null == player[finalI].isPlaying()) {
                    player[finalI].stop();
                    player[finalI].reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void setSoundList(ArrayList<Sound> soundListArray) {
        llRight.removeAllViews();
        llLeft.removeAllViews();
        final int maxVolume = 100;
        player = new MediaPlayer[soundListArray.size()];
        for (int i = 0; i < soundListArray.size(); i++) {
            final Sound item = soundListArray.get(i);
            final String sound_id = soundListArray.get(i).getS_id();
            final String sound_color = soundListArray.get(i).getS_color();
            final String sound_img = soundListArray.get(i).getS_img();
            final String sound = soundListArray.get(i).getS_sound();
            View itemView = LayoutInflater.from(this).inflate(R.layout.sound_list, null);


            LinearLayout llSoundColorBG = (LinearLayout) itemView.findViewById(R.id.llSoundColorBG);
            ImageView ivSoundIcon = (ImageView) itemView.findViewById(R.id.ivSoundIcon);
            final SeekBar sbVolume = (SeekBar) itemView.findViewById(R.id.sbVolume);

            llSoundColorBG.setBackgroundColor(Color.parseColor(sound_color));

            Log.d("Img_path", "" + RetrofitInstance.BASE_URL + sound_img);

            Picasso.with(MainActivity.this).load(RetrofitInstance.BASE_URL + sound_img).into(ivSoundIcon);

            //final Uri uri = Uri.parse(RetrofitInstance.BASE_URL + sound);
            final Uri uri = Uri.fromFile(new File(sound));
            Log.d("uri", "" + uri);
            sbVolume.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            sbVolume.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

            final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            sbVolume.setMax(maxVolume);
            sbVolume.setProgress(maxVolume);

            final int finalI = i;
            player[finalI] = new MediaPlayer();
            sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    float volume = (float) (1 - (Math.log(maxVolume - progress) / Math.log(maxVolume)));
                    player[finalI].setVolume(volume, volume);
                    for (int i = 0; i < selectedList.size(); i++) {
                        if (selectedList.get(i).getFinalI() == finalI) {
                            selectedList.get(i).setSoundProgress(progress);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            for (int j = 0; j < selectedList.size(); j++) {
                if (finalI == selectedList.get(j).getFinalI()) {
                    sbVolume.setVisibility(View.VISIBLE);
                    sbVolume.setProgress(selectedList.get(j).getSoundProgress());
                }
            }
            llSoundColorBG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {

                        if (player[finalI] != null == player[finalI].isPlaying()) {
                            player[finalI].stop();
                            player[finalI].reset();
                            sbVolume.setVisibility(View.INVISIBLE);
                            item.setFinalI(finalI);
                            for (int j = 0; j < selectedList.size(); j++) {
                                if (selectedList.get(j).getFinalI() == finalI) {
                                    selectedList.remove(j);
                                }
                            }
                            if (selectedList.size() == 0) {
                                ivPlay.setTag(0);
                                ivPlay.setImageResource(R.drawable.ic_play);
                                playHide();
                            }
                        } else {
                            ivPlay.setTag(1);
                            ivPlay.setImageResource(R.drawable.ic_pause);
                            playShow();
                            player[finalI] = new MediaPlayer();
                            player[finalI].setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player[finalI].setLooping(true);
                            player[finalI].setDataSource(MainActivity.this, uri);
                            player[finalI].setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player[finalI].prepare();
                            player[finalI].start();
                            float volume = (float) (1 - (Math.log(maxVolume - sbVolume.getProgress()) / Math.log(maxVolume)));
                            player[finalI].setVolume(volume, volume);
                            sbVolume.setVisibility(View.VISIBLE);
                            item.setFinalI(finalI);
                            item.setSoundProgress(sbVolume.getProgress());
                            selectedList.add(item);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            });

            if (i % 2 == 0) {
                llLeft.addView(itemView);
            } else {
                llRight.addView(itemView);
            }
        }


    }

    private void playShow() {
        llTimer.setVisibility(View.VISIBLE);
        llPlayPause.setVisibility(View.VISIBLE);
        llPlayLine.setVisibility(View.VISIBLE);
        llTimerLine.setVisibility(View.VISIBLE);
    }

    private void playHide() {
        llTimer.setVisibility(View.GONE);
        llPlayPause.setVisibility(View.GONE);
        llPlayLine.setVisibility(View.GONE);
        llTimerLine.setVisibility(View.GONE);
    }

    private String convertTime(long totalSecs) {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        String timeString = "";
        if (hours == 0) {
            timeString = String.format("%02d:%02d", minutes, seconds);
        } else {
            timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return timeString;
    }

    public void volumeControlDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.volume_dialog);
        dialog.setTitle("Set size!");
        dialog.setCancelable(true);
//there are a lot of settings, for dialog, check them all out!
        dialog.show();

        SeekBar volumeSeekbar = (SeekBar) dialog.findViewById(R.id.size_seekbar);
        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void closeAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit!");

        //Setting message manually and performing action on button click
        builder.setMessage("Do you want to close this application ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectReleasePlayer();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        closeAppDialog();
    }

    private void registerReceiver() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PROGRESS_UPDATE);
        bManager.registerReceiver(mBroadcastReceiver, intentFilter);

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PROGRESS_UPDATE)) {

                boolean downloadComplete = intent.getBooleanExtra("downloadComplete", false);
                //Log.d("API123", download.getProgress() + " current progress");
                if (downloadComplete) {
                    getPenddingCheck();
                }
            }

        }
    };
}
