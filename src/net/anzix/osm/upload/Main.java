package net.anzix.osm.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements OnClickListener {
	private static final int MENU_SETTINGS = 1;
	Uri uri;
	private SharedPreferences preferences;
	private File file;
	private int REQUEST_SAVE = 2;
	private TextView fileName;
	private Button upload;
	private EditText description;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		upload = (Button) findViewById(R.id.upload);
		description = (EditText) findViewById(R.id.description);

		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (description.getText()==null || description.getText().toString().length()==0){
				toast("Please define a description.");
				} else {
					new UploadTask().execute(new File[] { file });	
				}

			}
		});

		((Button) findViewById(R.id.choose_file))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(),
								FileDialog.class);
						intent.putExtra(FileDialog.START_PATH, "/sdcard");
						startActivityForResult(intent, REQUEST_SAVE);

					}
				});

		fileName = (TextView) findViewById(R.id.file_name);

		if (getIntent() != null && getIntent().getExtras() != null) {
			uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
			String openIntent = "/mimetype/";
			Log.i(getClass().getName(), uri.getPath());
			if (uri.getPath().startsWith(openIntent)) {
				setFile(new File(uri.getPath().substring(openIntent.length())));
			} else {
				setFile(new File(uri.getPath()));
			}
			setFile(file);

		}

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = preferences
				.getString(Preferences.OSM_USER_NAME, null);
		if (userName == null || userName.length() == 0) {
			showDialog(1);
		}

		Spinner s = (Spinner) findViewById(R.id.visibility);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.visibility, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		s.setSelection(0);

	}

	public void setFile(File file) {
		fileName.setText(file.getName());
		upload.setEnabled(true);
		this.file = file;

	}

	@Override
	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (requestCode == REQUEST_SAVE && resultCode == Activity.RESULT_OK) {
			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			if (filePath == null)
				return;
			setFile(new File(filePath));
			Log.d(getClass().getName(),
					"new file path " + file.getAbsolutePath());
		} else {
			Log.w(getClass().getName(), "activity returns with " + resultCode);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please set your OSM credentials!")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(Main.this, Preferences.class);
						startActivity(i);
					}
				});
		return builder.create();
	}

	public void toast(String message) {
		Context context = getApplicationContext();

		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	@Override
	public void onClick(View v) {
		new UploadTask().execute(new File[] { file });

	}

	private class UploadTask extends AsyncTask<File, Integer, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Main.this, "",
					"Uploading. Please wait...", true);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (dialog != null) {
				dialog.cancel();
				dialog = null;
			}
			toast(result);

		}

		@Override
		protected String doInBackground(File... files) {
			File file = files[0];
			if (!file.exists()) {
				return "ERROR: " + file.getAbsolutePath() + " does not exist.";

			}
			try {

				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(
						"http://api.openstreetmap.org/api/0.6/gpx/create");
				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("file", new FileBody(file));
				reqEntity.addPart("description", new StringBody(
						((TextView) findViewById(R.id.description)).getText()
								.toString()));
				reqEntity.addPart("tags", new StringBody(
						((TextView) findViewById(R.id.tags)).getText()
								.toString()));
				reqEntity.addPart("visibility", new StringBody(
						((Spinner) findViewById(R.id.visibility))
								.getSelectedItem().toString()));

				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
						preferences.getString(Preferences.OSM_USER_NAME, ""),
						preferences.getString(Preferences.OSM_PASSWORD, ""));
				postRequest.addHeader(BasicScheme.authenticate(creds,
						"US_ASCII", false));
				postRequest.setEntity(reqEntity);

				HttpResponse response = httpClient.execute(postRequest);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String sResponse;
				StringBuilder s = new StringBuilder();

				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse);
				}
				if (response.getStatusLine().getStatusCode() == 200) {
					return "File uploaded sucessfully";
				} else {
					return "ERROR: " + s + " "
							+ response.getStatusLine().getStatusCode();
				}
			} catch (Exception e) {

				Log.e(e.getClass().getName(), e.getMessage(), e);
				return "ERROR: " + e.getMessage();

			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SETTINGS, 0, R.string.settings)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setShortcut('9', 's');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_SETTINGS) {
			Intent i = new Intent(Main.this, Preferences.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}