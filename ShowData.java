package com.ta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class ShowData extends Activity {
	private String nim, name;
	public static final String MIME_TYPE = "app/bikebdg";
	public static final String TAG = "NfcDemo";
	boolean writeMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_data);
		AlertDialog alertDialog = new AlertDialog.Builder(ShowData.this).create();
		alertDialog.setTitle("Proyek TA");
		alertDialog.setIcon(0);
		//alertDialog.setMessage("Selamat Datang di Proyek TA !");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		alertDialog.show();
		Intent intent = getIntent();
		nim = intent.getStringExtra("NIM");
		name = intent.getStringExtra("name");
		
		TextView ID = (TextView) findViewById(R.id.show_ID);
		TextView nama = (TextView) findViewById(R.id.show_name);
		
		ID.setText("NIM   : " + nim);
		nama.setText("Nama : " + name);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_data, menu);
		return true;
	}

}
