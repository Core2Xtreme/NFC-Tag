package com.ta;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint({ "ParserError", "ParserError" })
public class Daftar extends Activity {
	private NfcAdapter adapter;
	private PendingIntent pendingIntent;
	private IntentFilter writeTagFilters[];
	boolean writeMode;
	private Tag mytag;
	Context ctx;
	private EditText edNIM, edNama;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daftar);
		edNIM = (EditText) findViewById(R.id.editText1); //deklarasi data yang akan ditulis yaitu NIM
		edNama = (EditText) findViewById(R.id.editText2); //deklarasi data yang akan ditulis yaitu Nama
		Button Write = (Button) findViewById(R.id.button1); //tombol yang ditekan akan menulis data
		Write.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				try {
					if(mytag==null){ //membaca kartu, jika tidak ada kartu akan menampilkan pesan
						Toast.makeText(getApplicationContext(), "Tag not found", Toast.LENGTH_LONG ).show();
					}else{	
						String id = null;
						String nama = null;
				        if(edNIM.getText()!=null && edNama.getText()!=null)
				        {
				        	id = edNIM.getText().toString();
				            nama = edNama.getText().toString();
				        }
						write(id+"?"+nama,mytag);
						
				                //konfirmasi kesuksesan
				                Toast.makeText(getApplicationContext(), "Data Pengguna Baru" +
				                		"\nNIM = " + id +
				                        "\nNama = " + nama
				                        , Toast.LENGTH_LONG).show();
				                Intent Home = new Intent (getApplicationContext(), Home.class);
								Home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								Home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(Home);
					
					}
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), "Penulisan Tag Mengalami Error", Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				} catch (FormatException e) {
					Toast.makeText(getApplicationContext(), "Penulisan Tag Mengalami Error" , Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				}
			}
		});

		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };
	
	}
	
	//menulis data tipe String
	private void write(String text, Tag tag) throws IOException, FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage  message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}
	
	//menulis data tipe NdefRecord
	private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
		byte[] textBytes  = text.getBytes();
		int    textLength = textBytes.length;
		byte[] payload    = new byte[1 + textLength];

		payload[0] = 0x01;
		System.arraycopy(textBytes, 0, payload, 1 , textLength);
		
		//menulis mime_type yaitu "proyekTA", jika mime_type berbeda, maka tidak dapat dibaca oleh program
		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA ,
			    "app/bikebdg".getBytes(Charset.forName("US-ASCII")), 
			    new byte[0], payload);

		return recordNFC;
	}	
	
	public void save(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, Home.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.daftar, menu);
		return true;
	}
	@Override
	protected void onNewIntent(Intent intent){
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);  
			Toast.makeText(getApplicationContext(), "Kartu dapat ditulis" +
					"\nTekan Save untuk menulis kartu", Toast.LENGTH_SHORT).show();
	}}
	
	@Override
	public void onPause(){
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume(){
		super.onResume();
		WriteModeOn();
	}

	private void WriteModeOn(){
		writeMode = true;
		adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
	}

	private void WriteModeOff(){
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}

}
