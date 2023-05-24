package com.thefirstlineofcode.sand.demo.app.android;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;

import java.net.Inet4Address;
import java.net.InetAddress;

public class ConfigureStreamActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configure_stream);

		StandardStreamConfig streamConfig = Toolkits.getStreamConfig(this);
		EditText etHost = findViewById(R.id.et_host);
		etHost.setText(streamConfig.getHost());
		EditText etPort = findViewById(R.id.et_port);
		etPort.setText(String.valueOf(streamConfig.getPort()));

		CheckBox cbEnableTls = findViewById(R.id.cb_enable_tls);
		cbEnableTls.setChecked(streamConfig.isTlsPreferred());
	}

	public void configureStream(View view) {
		EditText etHost = findViewById(R.id.et_host);
		if (TextUtils.isEmpty(etHost.getText().toString())) {
			runOnUiThread(() -> {
				Toast.makeText(this, getString(R.string.host_cant_be_null), Toast.LENGTH_LONG).show();
				etHost.requestFocus();
			});

			return;
		}

		try {
			InetAddress inetAddress = Inet4Address.getByName(etHost.getText().toString());
			if (!(inetAddress instanceof Inet4Address)) {
				throw new IllegalArgumentException("Not an IPv4 address.");
			}
		} catch (Exception e) {
			runOnUiThread(() -> {
				Toast.makeText(this, getString(R.string.host_must_be_an_ipv4_address), Toast.LENGTH_LONG).show();
				etHost.selectAll();
				etHost.requestFocus();
			});

			return;
		}

		EditText etPort = findViewById(R.id.et_port);
		if (TextUtils.isEmpty(etHost.getText().toString())) {
			runOnUiThread(() -> {
				Toast.makeText(this, getString(R.string.port_cant_be_null), Toast.LENGTH_LONG).show();
				etPort.requestFocus();
			});

			return;
		}

		boolean portIsInvalid = false;
		int port = -1;
		try {
			port = Integer.parseInt(etPort.getText().toString());

			if (port <= 0) {
				portIsInvalid = true;
			}
		} catch (NumberFormatException e) {
			portIsInvalid = true;
		}

		if (portIsInvalid) {
			Toast.makeText(this, getString(R.string.port_must_be_an_positive_integer), Toast.LENGTH_LONG).show();
			etPort.selectAll();
			etPort.requestFocus();

			return;
		}

		CheckBox cbEnableTls = findViewById(R.id.cb_enable_tls);

		StandardStreamConfig streamConfig = new StandardStreamConfig(etHost.getText().toString(), port);
		streamConfig.setTlsPreferred(cbEnableTls.isChecked());
		Toolkits.setStreamConfig(this, streamConfig);

		ChatClientSingleton.destroy();

		finish();
	}
}
