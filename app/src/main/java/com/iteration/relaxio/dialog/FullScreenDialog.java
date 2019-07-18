package com.iteration.relaxio.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.iteration.relaxio.R;
import com.iteration.relaxio.adapter.TaskAdapter;
import com.iteration.relaxio.database.DatabaseClient;
import com.iteration.relaxio.database.Task;
import com.iteration.relaxio.listener.CallbackListener;
import com.iteration.relaxio.model.Sound;
import com.iteration.relaxio.utility.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FullScreenDialog extends DialogFragment implements TaskAdapter.onActionLisiner {


    public static String TAG = "FullScreenDialog";
    private RecyclerView rvFavoritesList;
    private TextView tvMsg;
    private TaskAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    CallbackListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    public void setCallbackListener(CallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bottom_favrite_dialog, container, false);
        final List<Sound> list = (List<Sound>) getArguments().getSerializable("data");

        tvMsg = view.findViewById(R.id.tvMsg);
        rvFavoritesList = view.findViewById(R.id.rvFavoritesList);

        final LinearLayout llAdd = view.findViewById(R.id.llAdd);
        LinearLayout llMain = view.findViewById(R.id.llMain);
        final EditText edtTitle = view.findViewById(R.id.edtTitle);
        TextView tvSubmit = view.findViewById(R.id.tvSubmit);
        llMain.removeAllViews();
        if (list.size() == 0) {
            llAdd.setVisibility(View.GONE);
        }
        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            total = total + list.get(i).getSoundProgress();
            TextView tv2 = new TextView(getActivity());
            TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, list.get(i).getSoundProgress());
            tv2.setLayoutParams(params3);
            tv2.setBackgroundColor(Color.parseColor(list.get(i).getS_color()));
            llMain.addView(tv2);
        }
        llMain.setWeightSum(total);

        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = edtTitle.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    Toast.makeText(getActivity(), "Please Enter Title!!", Toast.LENGTH_SHORT).show();
                } else {
                    addTask(str, list);
                    llAdd.setVisibility(View.GONE);
                }
            }
        });

        rvFavoritesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TaskAdapter(getActivity(), tasks, this);
        rvFavoritesList.setAdapter(adapter);
        getTasks();
        return view;
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<Task>> {

            @Override
            protected List<Task> doInBackground(Void... voids) {
                List<Task> taskList = DatabaseClient
                        .getInstance(getActivity())
                        .getAppDatabase()
                        .taskDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<Task> taskList) {
                super.onPostExecute(taskList);
                tasks.clear();
                tasks.addAll(taskList);
                adapter.notifyDataSetChanged();
                if (tasks.size() == 0) {
                    tvMsg.setVisibility(View.VISIBLE);
                    rvFavoritesList.setVisibility(View.GONE);
                } else {
                    rvFavoritesList.setVisibility(View.VISIBLE);
                    tvMsg.setVisibility(View.GONE);
                }
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }

    private void deleteTask(final Task task) {
        class DeleteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .taskDao()
                        .delete(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getTasks();
            }
        }

        DeleteTask dt = new DeleteTask();
        dt.execute();

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void cancelUpload() {

    }

    public int rendomNumber() {
        final int min = 1000000;
        final int max = 99999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    public void addTask(final String s, final List<Sound> list) {

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                int random = rendomNumber();
                Task task = new Task();
                task.setTask(s);
                task.setRendom_id(random);

                DatabaseClient.getInstance(getActivity()).getAppDatabase()
                        .taskDao()
                        .insert(task);
                SharedPreferenceUtil.setSoundList(getActivity(), random + "", list);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getTasks();
                Toast.makeText(getActivity(), "Add Successfully!!", Toast.LENGTH_SHORT).show();
            }
        }

        SaveTask saveTask = new SaveTask();
        saveTask.execute();

    }

    @Override
    public void onDelete(Task task) {
        deleteTask(task);
    }

    @Override
    public void onClick(List<Sound> list) {
        listener.onPlay(list);
    }
}