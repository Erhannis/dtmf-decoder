package wpam.recognizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import pl.polidea.apphance.Apphance;

public class MainActivity extends Activity {

	private static final int REQUEST_MICROPHONE = 100;

	private Button stateButton;
	private Button clearButton;	
	private EditText recognizeredEditText;
	private SpectrumView spectrumView;	
	private NumericKeyboard numKeyboard;
	
	Controller controller;

	@Override
	protected void onResume() {
		super.onResume();
		runtime_permission();
	}

	private String recognizeredText;

	History history;
	
	public static final String APP_KEY = "806785c1fb7aed8a867039282bc21993eedbc4e4";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Apphance.start(this, APP_KEY);
		
		setContentView(R.layout.main);
		
		controller = new Controller(this);

		stateButton = (Button)this.findViewById(R.id.stateButton);
		stateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				controller.changeState();
			}
		});
		
		clearButton = (Button)this.findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				controller.clear();
			}
		});

		spectrumView = new SpectrumView(); 
		spectrumView.setImageView((ImageView) this.findViewById(R.id.spectrum));
				
		recognizeredEditText = (EditText)this.findViewById(R.id.recognizeredText);
		recognizeredEditText.setFocusable(false);
		
		numKeyboard = new NumericKeyboard();		
		numKeyboard.add('0', (Button)findViewById(R.id.button0));
		numKeyboard.add('1', (Button)findViewById(R.id.button1));
		numKeyboard.add('2', (Button)findViewById(R.id.button2));
		numKeyboard.add('3', (Button)findViewById(R.id.button3));
		numKeyboard.add('4', (Button)findViewById(R.id.button4));
		numKeyboard.add('5', (Button)findViewById(R.id.button5));
		numKeyboard.add('6', (Button)findViewById(R.id.button6));
		numKeyboard.add('7', (Button)findViewById(R.id.button7));
		numKeyboard.add('8', (Button)findViewById(R.id.button8));
		numKeyboard.add('9', (Button)findViewById(R.id.button9));
		numKeyboard.add('0', (Button)findViewById(R.id.button0));
		numKeyboard.add('#', (Button)findViewById(R.id.buttonHash));
		numKeyboard.add('*', (Button)findViewById(R.id.buttonAsterisk));
		numKeyboard.add('A', (Button)findViewById(R.id.buttonA));
		numKeyboard.add('B', (Button)findViewById(R.id.buttonB));
		numKeyboard.add('C', (Button)findViewById(R.id.buttonC));
		numKeyboard.add('D', (Button)findViewById(R.id.buttonD));

		setEnabled(false);
		
		recognizeredText = "";
		
		history = new History(this);
		history.load();
	}
	
	public void start()
	{
		stateButton.setText(R.string.stop);
		setEnabled(true);
	}
	
	public void stop()
	{
		history.add(recognizeredText);
		
		stateButton.setText(R.string.start);
		setEnabled(false);
	}
	
	public int getAudioSource()
	{
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		
		if (telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE)
			return MediaRecorder.AudioSource.VOICE_DOWNLINK;
		
		return MediaRecorder.AudioSource.MIC;
	}

	public void drawSpectrum(Spectrum spectrum) {
		spectrumView.draw(spectrum);		
	}
	
	public void clearText() 
	{
		history.add(recognizeredText);
		
		recognizeredText = "";
		recognizeredEditText.setText("");
	}
	
	public void addText(Character c) 
	{
		recognizeredText += c;
		recognizeredEditText.setText(recognizeredText);
	}

	public void setText(String text)
	{
		recognizeredEditText.setText(text);		
	}
	
	public void setEnabled(boolean enabled) 
	{
		recognizeredEditText.setEnabled(enabled);
		numKeyboard.setEnabled(enabled);
	}
	public void setAciveKey(char key)
	{
		numKeyboard.setActive(key);
	}
	
	@SuppressLint("ResourceType")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.history:
	        	showHistory();
	            break;
	        case R.id.send:
	        	sendRecognizeredText();
	        	break;
	        case R.id.about:
	        	showAbout();
	            break;
	    }
	    return true;
	}

	private void showHistory()
	{
		history.add(recognizeredText);
		history.save();		
		
		Intent intent = new Intent(this, HistoryActivity.class);
		startActivity(intent);
	}

	private void sendRecognizeredText() {
		final Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, recognizeredText);
		startActivity(Intent.createChooser(sendIntent, getString(R.string.send)+":"));
	}

	private void showAbout() 
	{
		AlertDialog about = new AlertDialog.Builder(this).create();
		
		about.setTitle(getString(R.string.app_name)+" ("+getVersion()+")");
		about.setIcon(R.drawable.icon);
		about.setMessage(getString(R.string.about_text));
		about.show();
	}
	
	private String getVersion() 
	{
		PackageManager manager = getPackageManager();
		PackageInfo info = null;	
		
		try {	
			info = manager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.wtf("NameNotFoundException", "getVersion() NameNotFoundException");
		}
        	 return info.versionName;
	}
	
	@Override
	protected void onDestroy() 
	{
		history.add(recognizeredText);
		
		history.save();
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		if (controller.isStarted())
			controller.changeState();
		super.onPause();
	}

	private void runtime_permission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == REQUEST_MICROPHONE) {
			boolean allGranted = true;
			for(int grantResult : grantResults) {
				allGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
			}
			if(!allGranted) {
				runtime_permission();
			}
		}
	}
}
