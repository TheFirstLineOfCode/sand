package com.thefirstlineofcode.sand.demo.app.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.chalk.core.stream.NegotiationException;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.core.stream.UsernamePasswordToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

class Toolkits {

	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";

	@SuppressLint("ApplySharedPref")
	static void setStreamConfig(Context context, StandardStreamConfig streamConfig) {
		SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.app_preferences_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(context.getString(R.string.stream_config_host), streamConfig.getHost());
		editor.putInt(context.getString(R.string.stream_config_port), streamConfig.getPort());
		editor.putBoolean(context.getString(R.string.stream_config_enable_tls), streamConfig.isTlsPreferred());
		editor.commit();
	}

	static StandardStreamConfig getStreamConfig(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.app_preferences_name), Context.MODE_PRIVATE);
		String host = preferences.getString(context.getString(R.string.stream_config_host),
				context.getString(R.string.stream_config_default_host));
		int port = preferences.getInt(context.getString(R.string.stream_config_port),
				Integer.parseInt(context.getString(R.string.stream_config_default_port)));
		boolean enableTls = preferences.getBoolean(context.getString(R.string.stream_config_enable_tls),
				Boolean.parseBoolean(context.getString(R.string.stream_config_default_enable_tls)));

		StandardStreamConfig streamConfig = new StandardStreamConfig(host, port);
		streamConfig.setTlsPreferred(enableTls);

		return streamConfig;
	}

	static UsernamePasswordToken getUsernamePasswordToken(Context context) {
		Properties properties = getUserDataProperties(context);
		if (properties == null)
			return null;

		String username = (String)properties.get(KEY_USERNAME);
		String password = (String)properties.get(KEY_PASSWORD);

		if (username == null || password == null)
			return null;

		return new UsernamePasswordToken(username, password);
	}

	private static Properties getUserDataProperties(Context context) {
		File file = new File(context.getFilesDir(), context.getString(R.string.file_name_user_data_properties));
		if (!file.exists())
			return null;

		Properties properties = new Properties();
		try {
			properties.load(new BufferedReader(new FileReader(file)));
		} catch (IOException e) {
			// throw new RuntimeException("Can't read user data properties.", e);
			return null;
		}

		return properties;
	}

	static void rememberUser(Context context, String username, char[] password) {
		Properties properties = getUserDataProperties(context);
		if (properties == null) {
			properties = new Properties();
		}
		properties.setProperty(KEY_USERNAME, username);
		properties.setProperty(KEY_PASSWORD, new String(password));

		File file = new File(context.getFilesDir(), context.getString(R.string.file_name_user_data_properties));
		try {
			properties.store(new BufferedWriter(new FileWriter(file)), null);
		} catch (IOException e) {
			throw new RuntimeException("Can't write user data properties.", e);
		}
	}

	static String getErrorInfo(IError error) {
		if (error.getText() == null)
			return error.getDefinedCondition();
		else
			return String.format("%s - %s", error.getDefinedCondition(), error.getText());
	}

	static NegotiationException findNegotiationException(RuntimeException e) {
		if (e instanceof NegotiationException)
			return (NegotiationException)e;

		Throwable t = e;
		while (t.getCause() != null) {
			if (t.getCause() instanceof NegotiationException)
				return (NegotiationException)t.getCause();

			t = t.getCause();
		}

		return null;
	}

	static void showAlertMessage(Activity activity, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(activity.getString(R.string.app_name));
		builder.setMessage(message);

		builder.show();
	}
}