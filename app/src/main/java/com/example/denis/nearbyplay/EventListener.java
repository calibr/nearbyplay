package com.example.denis.nearbyplay;

/**
 * Created by denis on 8/2/18.
 */

public class EventListener {
    public void trigger(int code, String data) {
        this.onEvent(code, data);
    }
    public void onEvent(int code, String data) {

    }
}
