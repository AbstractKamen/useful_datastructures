package com.abstractkamen.datastructures.impl;

public class StopWatch {

    private long beginTime;
    private final String message;

    public StopWatch() {
        this("");
    }

    public StopWatch(String message) {
        reset();
        this.message = message;
    }

    public void reset() {
        this.beginTime = System.currentTimeMillis();
    }

    /**
     * Get time since {@code this} instance was created or since last {@link #reset()}.
     *
     * @return formatted time like dd/hh/mm/ss/sss
     */
    public String getFormattedTime() {
        final String readableFormat = "%02d:%02d:%02d.%03d";
        long millis = System.currentTimeMillis() - beginTime;
        int sec = (int) (millis / 1000) % 60;
        int min = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        return String.format(readableFormat, hours, min, sec, millis % 1000);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message + getFormattedTime();
    }
}