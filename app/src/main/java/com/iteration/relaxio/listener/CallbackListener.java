package com.iteration.relaxio.listener;

import com.iteration.relaxio.model.Sound;

import java.util.List;

public interface CallbackListener {

    public void onPlay(List<Sound> list);
}
