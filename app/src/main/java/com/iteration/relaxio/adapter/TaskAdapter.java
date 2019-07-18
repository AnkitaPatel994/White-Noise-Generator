package com.iteration.relaxio.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.iteration.relaxio.R;
import com.iteration.relaxio.database.Task;
import com.iteration.relaxio.model.Sound;
import com.iteration.relaxio.model.SoundList;
import com.iteration.relaxio.utility.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private Context context;
    private List<Task> list;
    private onActionLisiner lisiner;

    public TaskAdapter(Context context, List<Task> list, onActionLisiner lisiner) {
        this.context = context;
        this.list = list;
        this.lisiner = lisiner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Task task = list.get(position);
        viewHolder.edtTitle.setText(task.getTask());
        final List<Sound> soundList = new ArrayList<>();
        soundList.addAll(SharedPreferenceUtil.getSoundList(context, task.getRendom_id() + ""));

        int total = 0;
        for (int i = 0; i < soundList.size(); i++) {
            total = total + soundList.get(i).getSoundProgress();
            TextView tv2 = new TextView(context);
            TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, soundList.get(i).getSoundProgress());
            tv2.setLayoutParams(params3);
            tv2.setBackgroundColor(Color.parseColor(soundList.get(i).getS_color()));
            viewHolder.llMain.addView(tv2);
        }
        viewHolder.llMain.setWeightSum(total);
        viewHolder.edtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lisiner.onClick(soundList);
            }
        });
        viewHolder.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lisiner.onDelete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;
        TextView edtTitle;
        ImageView ivClose;

        public ViewHolder(View itemView) {
            super(itemView);

            llMain = (LinearLayout) itemView.findViewById(R.id.llMain);
            edtTitle = (TextView) itemView.findViewById(R.id.edtTitle);
            ivClose = (ImageView) itemView.findViewById(R.id.ivClose);

        }
    }

  public interface onActionLisiner {
        void onDelete(Task task);

        void onClick(List<Sound> list);
    }

}
