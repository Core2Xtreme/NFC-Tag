package com.ta;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Login extends Activity {
	public static final String MIME_TYPE = "app/bikebdg";
	public static final String TAG = "NfcDemo";
	private NfcAdapter mNfcAdapter;
//	private PendingIntent pendingIntent;
//	private IntentFilter writeTagFilters[];
	boolean writeMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "No NFC detected on this device",
					Toast.LENGTH_SHORT).show();
		} else {
			if (!mNfcAdapter.isEnabled()) {
				Toast.makeText(this, "NFC is Disabled", Toast.LENGTH_SHORT)
						.show();
			}
		}
		if (mNfcAdapter != null) {
			handleIntent(getIntent());
		}
	}
	
	public void oke2(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, Home.class);
		startActivity(intent);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// jika ada intent baru
		if (mNfcAdapter != null) {
			handleIntent(intent);

		}
	}
	
	//membaca tipe data ndef yaitu mime_type, jika mime_type beda, gabakal kebaca
	private void handleIntent(Intent intent) { 
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			String type = intent.getType();
			if (MIME_TYPE.equals(type)) {
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

				new NdefReaderTask().execute(tag);

			} else {
				Log.d(TAG, "Wrong mime type: " + type);
			}
		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			// In case we would still use the Tech Discovered Intent

			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String[] techList = tag.getTechList();
			String searchedTech = Ndef.class.getName();
			for (String tech : techList) {
				if (searchedTech.equals(tech)) {
					new NdefReaderTask().execute(tag);
					break;
				}
			}
		}
	}
	
	//memecah tipe NDEF jadi NDEFrecord
	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
		@Override
		protected String doInBackground(Tag... params) {
			Tag tag = params[0];
			Ndef ndef = Ndef.get(tag);
			if (ndef == null) {
				// NDEF is not supported by this Tag.
				return null;
			}
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
					try {

						return readText(ndefRecord);
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, "Unsupported Encoding", e);
					}
				}
			}
			return null;
		}
		
		//membaca NDEFrecord dikonversi jadi text
		private String readText(NdefRecord record)
				throws UnsupportedEncodingException {
			byte[] payload = record.getPayload();
			// Get the Text Encoding
			String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8"
					: "UTF-16";
			// Get the Text
			return new String(payload, textEncoding);
		}

		//membaca tipe string yang sudah dipecah, yaitu "NIM?name"
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				int mark1 = result.indexOf("?");
				final String NIM = result.substring(1, mark1);
				final String name = result.substring(mark1 + 1, result.length());
				Intent tampildata = new Intent(
						getApplicationContext(),
						ShowData.class);
				tampildata.putExtra("NIM", NIM);
				tampildata.putExtra("name", name);
				tampildata.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				tampildata.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(tampildata);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		/**
		 * It's important, that the activity is in the foreground (resumed).
		 * Otherwise an IllegalStateException is thrown.
		 */
		if (mNfcAdapter != null) {
			setupForegroundDispatch(this, mNfcAdapter);
		}
	}

	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is
		 * thrown as well.
		 */
		if (mNfcAdapter != null) {
			stopForegroundDispatch(this, mNfcAdapter);
		}
		super.onPause();
	}

	/**
	 * @param activity
	 *            The corresponding {@link Activity} requesting the foreground
	 *            dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(),
				activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				activity.getApplicationContext(), 0, intent, 0);
		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};
		// Notice that this is the same filter as in our manifest.
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TYPE);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("Check your mime type.");
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	/**
	 * @param activity
	 *            The corresponding {@link BaseActivity} requesting to stop the
	 *            foreground dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

}
