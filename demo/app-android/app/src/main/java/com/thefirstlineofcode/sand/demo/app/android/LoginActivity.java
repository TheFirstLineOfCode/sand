package com.thefirstlineofcode.sand.demo.app.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.stream.NegotiationException;
import com.thefirstlineofcode.chalk.core.stream.UsernamePasswordToken;
import com.thefirstlineofcode.chalk.network.ConnectionException;

public class LoginActivity extends AppCompatActivity {
	public static final int PERMISSIONS_REQUEST_CODE = 1;
	
	private View.OnClickListener onClickListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		UsernamePasswordToken token = Toolkits.getUsernamePasswordToken(this);
		
		EditText etUserName = findViewById(R.id.et_user_name);
		if (token != null)
			etUserName.setText(token.getUsername());
		
		EditText etPassword = findViewById(R.id.et_password);
		if (token != null)
			etPassword.setText(new String(token.getPassword()));
		
		Button btLogin = findViewById(R.id.bt_login);
		onClickListener = new View.OnClickListener() {
			public void onClick(View view) {
				EditText etUserName = findViewById(R.id.et_user_name);
				String userName = etUserName.getText().toString();
				if (TextUtils.isEmpty(userName)) {
					runOnUiThread(() -> {
						Toast.makeText(LoginActivity.this, getString(R.string.user_name_cant_be_null), Toast.LENGTH_LONG).show();
						etUserName.requestFocus();
					});
					
					return;
				}
				
				EditText etPassword = findViewById(R.id.et_password);
				String password = etPassword.getText().toString();
				if (TextUtils.isEmpty(password)) {
					runOnUiThread(() -> {
						Toast.makeText(LoginActivity.this, getString(R.string.password_cant_be_null), Toast.LENGTH_LONG).show();
						etPassword.requestFocus();
					});
					
					return;
				}
				
				IChatClient chatClient = ChatClientSingleton.get(LoginActivity.this);
				if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
					if (!chatClient.isConnected() && !connect(etUserName, userName, password, chatClient))
						return;
				} else {
					requestPermissions(
							new String[] {
								Manifest.permission.INTERNET,
								Manifest.permission.ACCESS_NETWORK_STATE,
								Manifest.permission.CHANGE_NETWORK_STATE,
								Manifest.permission.CAMERA,
								Manifest.permission.RECORD_AUDIO,
								Manifest.permission.MODIFY_AUDIO_SETTINGS,
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
								Manifest.permission.READ_EXTERNAL_STORAGE
							}, PERMISSIONS_REQUEST_CODE);
				}
				
				finish();
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
			}
		};
		btLogin.setOnClickListener(onClickListener);
		
		if (token == null)
			return;
		
		Intent intent = getIntent();
		if (intent != null && intent.getBooleanExtra(getString(R.string.auto_login), true))
			onClickListener.onClick(btLogin);
	}

	public void startRegisterActivity(View view) {
		startActivity(new Intent(this, RegisterActivity.class));
	}

	public void startConfigureStreamActivity(View view) {
		startActivity(new Intent(this, ConfigureStreamActivity.class));
	}

	private boolean connect(EditText etUserName, String userName, String password, IChatClient chatClient) {
		try {
			chatClient.connect(new UsernamePasswordToken(userName, password));
		} catch (ConnectionException e) {
			runOnUiThread(() -> Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_LONG).show());
			return false;
		} catch (AuthFailureException e) {
			runOnUiThread(() -> {
				Toast.makeText(this, getString(R.string.incorrect_user_name_or_password), Toast.LENGTH_LONG).show();
				etUserName.selectAll();
				etUserName.requestFocus();
			});
			
			ChatClientSingleton.destroy();
			
			return false;
		} catch (RuntimeException e) {
			NegotiationException ne = Toolkits.findNegotiationException(e);
			if (ne != null && ne.getAdditionalErrorInfo() instanceof IError) {
				IError error = (IError)ne.getAdditionalErrorInfo();
				runOnUiThread(() -> Toast.makeText(this, getString(R.string.unknown_error,
						Toolkits.getErrorInfo(error)), Toast.LENGTH_LONG).show());
			} else {
				runOnUiThread(() -> Toast.makeText(this, getString(R.string.unknown_error, e.getClass().getName()), Toast.LENGTH_LONG).show());
			}

			return false;
		}

		Toolkits.rememberUser(this, userName, password.toCharArray());
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_CODE && allPermissionsGranted(grantResults)) {
			onClickListener.onClick(findViewById(R.id.bt_login));
		} else {
			new AlertDialog.Builder(this).
					setTitle("Error").
					setMessage("User denied permissions request. App will exit.").
					setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}
			).create().show();
		}
	}
	
	private boolean allPermissionsGranted(int[] grantResults) {
		for (int grantResult : grantResults) {
			if (grantResult != PackageManager.PERMISSION_GRANTED)
				return false;
		}
		
		return true;
	}
}
