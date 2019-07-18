package com.iteration.relaxio.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.iteration.relaxio.R;
import com.iteration.relaxio.model.Sound;
import com.iteration.relaxio.network.RetrofitInstance;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class SoundAllListAdapter extends RecyclerView.Adapter<SoundAllListAdapter.ViewHolder> {

    Context context;
    ArrayList<Sound> soundListArray;

    public SoundAllListAdapter(Context context, ArrayList<Sound> soundListArray) {
        this.context = context;
        this.soundListArray = soundListArray;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sound_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        String sound_id = soundListArray.get(position).getS_id();
        String sound_color = soundListArray.get(position).getS_color();
        final String sound_img = soundListArray.get(position).getS_img();
        final String sound = soundListArray.get(position).getS_sound();

        viewHolder.llSoundColorBG.setBackgroundColor(Color.parseColor(sound_color));

        Log.d("Img_path",""+RetrofitInstance.BASE_URL+sound_img);

        Picasso.with(context).load(RetrofitInstance.BASE_URL+sound_img).into(viewHolder.ivSoundIcon);

        final Uri uri = Uri.parse(RetrofitInstance.BASE_URL+sound);
        final MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setLooping(true);

        viewHolder.sbVolume.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        viewHolder.sbVolume.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        viewHolder.sbVolume.setMax(am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        viewHolder.sbVolume.setProgress(am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
//
//        viewHolder.sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//           //     am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, AudioManager.FLAG_PLAY_SOUND);
//                float log1 = (float) (Math.log(50-progress)/Math.log(50));
//           //     player.setVolume(1-log1,1-log1);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        viewHolder.llSoundColorBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (player.isPlaying())
                    {
                        player.stop();
                        player.reset();
                        viewHolder.sbVolume.setVisibility(View.GONE);
                    }
                    else
                    {
                        player.setDataSource(context, uri);
                        player.prepare();
                        player.start();
                        viewHolder.sbVolume.setVisibility(View.VISIBLE);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return soundListArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llSoundColorBG;
        ImageView ivSoundIcon;
        SeekBar sbVolume;

        public ViewHolder(View itemView) {
            super(itemView);

            llSoundColorBG = (LinearLayout)itemView.findViewById(R.id.llSoundColorBG);
            ivSoundIcon = (ImageView) itemView.findViewById(R.id.ivSoundIcon);
            sbVolume = (SeekBar) itemView.findViewById(R.id.sbVolume);

        }
    }
}
