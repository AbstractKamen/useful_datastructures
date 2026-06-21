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
	this.beginTime = System.nanoTime();
  }

  /**
   * Get time since {@code this} instance was created or since last {@link #reset()}.
   *
   * @return formatted time like dd/hh/mm/ss/sss
   */
  public String getFormattedTime() {
	final String readableFormat = "%02d:%02d:%02d.%03d.%03d";
	final long nanos = System.nanoTime() - beginTime;
	final long micro = (nanos) / 1000;
	final long millis = nanos / 1000_000;
	final int sec = (int) (millis / 1000) % 60;
	final int min = (int) ((millis / (1000 * 60)) % 60);
	final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
	return String.format(readableFormat, hours, min, sec, millis % 1000, micro % 1000);
  }

  public String getMessage() {
	return message;
  }

  @Override
  public String toString() {
	return message + getFormattedTime();
  }
}