package com.example.studkompas.model;

import android.transition.Transition;

public class ShowUiTransitionListener implements Transition.TransitionListener {
    private final Runnable onEndAction;

    public ShowUiTransitionListener(Runnable onEndAction) {
        this.onEndAction = onEndAction;
    }

    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        onEndAction.run();
    }

    @Override
    public void onTransitionCancel(Transition transition) {
        onEndAction.run();
    }

    @Override
    public void onTransitionPause(Transition transition) {
    }

    @Override
    public void onTransitionResume(Transition transition) {
    }
}
