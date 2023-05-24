package com.thefirstlineofcode.sand.demo.app.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class AppExitMonitor extends Service {
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		ChatClientSingleton.destroy();
		stopSelf();
	}
}
