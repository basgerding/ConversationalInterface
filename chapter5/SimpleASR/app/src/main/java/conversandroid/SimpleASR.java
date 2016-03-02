/*
 *  Copyright 2016 Zoraida Callejas, Michael McTear and David Griol
 *
 *  This file is part of the Conversandroid Toolkit, from the book:
 *  The Conversational Interface, Michael McTear, Zoraida Callejas and David Griol
 *  Springer 2016 <https://github.com/zoraidacallejas/ConversationalInterface/>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */

package conversandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import conversandroid.simpleasr.R;

/**
 * SimpleASR: Basic app with ASR using a RecognizerIntent
 * 
 * Simple demo in which the user speaks and the recognition results
 * are showed in a list along with their confidence values
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 2.0, 02/23/16
 *
 */

public class SimpleASR extends Activity {

	// Default values for the language model and maximum number of recognition results
	// They are shown in the GUI when the app starts, and they are used when the user selection is not valid
	private final static int DEFAULT_NUMBER_RESULTS = 10;
	private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM; 
	

	private int numberRecoResults = DEFAULT_NUMBER_RESULTS; 
	private String languageModel = DEFAULT_LANG_MODEL;

	private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
	private static final String LOGTAG = "SIMPLEASR";
	private static int ASR_CODE = 123;
	
	/**
	 * Sets up the activity initializing the GUI
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simpleasr);
		
		//Shows in the GUI the default values for the language model and the maximum number of recognition results
		showDefaultValues(); 

		setSpeakButton();
	}
	
	/**
	 * Initializes the speech recognizer and starts listening to the user input
	 */
	private void listen()  {

		// Check we have permission to record audio
		checkASRPermission();

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify language model
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

		// Specify mx number of recognition results
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);  

		// Start listening
		startActivityForResult(intent, ASR_CODE);
    }

	/**
	 * Checks whether the user has granted permission to the microphone. If the permission has not been provided,
	 * it is requested. The result of the request (whether the user finally grants the permission or not)
	 * is processed in the onRequestPermissionsResult method.
	 *
	 * This is necessary from Android 6 (API level 23), in which users grant permissions to apps
	 * while the app is running. In previous versions, the permissions were granted when installing the app
	 * See: http://developer.android.com/intl/es/training/permissions/requesting.html
	 */
	public void checkASRPermission() {
		if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {

			// If  an explanation is required, show it
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
				Toast.makeText(getApplicationContext(), "SimpleASR must access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();

			// Request the permission.
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
					MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
		}
	}

	/**
	 * Processes the result of the record audio permission request. If it is not granted, the
	 * abstract method "onRecordAudioPermissionDenied" method is invoked. Such method must be implemented
	 * by the subclasses of VoiceActivity.
	 * More info: http://developer.android.com/intl/es/training/permissions/requesting.html
	 * */
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if(requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				Log.i(LOGTAG, "Record audio permission granted");
			else {
				Log.i(LOGTAG, "Record audio permission denied");
				Toast.makeText(getApplicationContext(), "Sorry, SimpleASR cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/**
	 * Shows in the GUI the default values for the language model (checks radio button)
	 * and the maximum number of recognition results (shows the number in the text field)
	 */
	private void showDefaultValues() {
		//Show the default number of results in the corresponding EditText
		((EditText) findViewById(R.id.numResults_editText)).setText(""+DEFAULT_NUMBER_RESULTS);
		
		//Show the language model
		if(DEFAULT_LANG_MODEL.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM))
			((RadioButton) findViewById(R.id.langModelFree_radio)).setChecked(true);
		else
			((RadioButton) findViewById(R.id.langModelFree_radio)).setChecked(true);
	}
	
	/**
	 * Reads the values for the language model and the maximum number of recognition results
	 * from the GUI
	 */
	private void setRecognitionParams()  {
		String numResults = ((EditText) findViewById(R.id.numResults_editText)).getText().toString();
		
		//Converts String into int, if it is not possible, it uses the default value
		try{
			numberRecoResults = Integer.parseInt(numResults);
		} catch(Exception e) {	
			numberRecoResults = DEFAULT_NUMBER_RESULTS;	
		}
		//If the number is <= 0, it uses the default value
		if(numberRecoResults<=0)
			numberRecoResults = DEFAULT_NUMBER_RESULTS;
		
		
		RadioGroup radioG = (RadioGroup) findViewById(R.id.langModel_radioGroup);
		switch(radioG.getCheckedRadioButtonId()){
			case R.id.langModelFree_radio:
				languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
				break;
			case R.id.langModelWeb_radio:
				languageModel = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH;
				break;
			default:
				languageModel = DEFAULT_LANG_MODEL;
				break;
		}
	}
	
	/**
	 * Sets up the listener for the button that the user
	 * must click to start talking
	 */
	@SuppressLint("DefaultLocale")
	private void setSpeakButton() {
		//Gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);

        final PackageManager packM = getPackageManager();

		//Set up click listener
		speak.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					//Speech recognition does not currently work on simulated devices,
					//it the user is attempting to run the app in a simulated device
					//they will get a Toast
					if("generic".equals(Build.BRAND.toLowerCase())){
						Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
						toast.show();
						Log.d(LOGTAG, "ASR attempt on virtual device");						
					}
					else{
                        // find out whether speech recognition is supported
                        List<ResolveInfo> intActivities = packM.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
                        if (intActivities.size() != 0) {
							setRecognitionParams(); //Read speech recognition parameters from GUI
							listen();                //Set up the recognizer with the parameters and start listening
						}
                        else
                        {
                            Toast toast = Toast.makeText(getApplicationContext(),"ASR not supported", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d(LOGTAG, "ASR not supported");
                        }
					}
				}
			});
	}

	/**
	 *  Shows the formatted best of N best recognition results (N-best list) from
	 *  best to worst in the <code>ListView</code>. 
	 *  For each match, it will render the recognized phrase and the confidence with 
	 *  which it was recognized.
	 */
	@SuppressLint("InlinedApi")
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE)  {
            if (resultCode == RESULT_OK)  {            	
            	if(data!=null) {
	            	//Retrieves the N-best list and the confidences from the ASR result
	            	ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            	float[] nBestConfidences = null;
	            	
	            	if (Build.VERSION.SDK_INT >= 14)  //Checks the API level because the confidence scores are supported only from API level 14
	            		nBestConfidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
	            	
					//Creates a collection of strings, each one with a recognition result and its confidence
	            	//following the structure "Phrase matched (conf: 0.5)"
					ArrayList<String> nBestView = new ArrayList<String>();
					
					for(int i=0; i<nBestList.size(); i++){
						if(nBestConfidences!=null){
							if(nBestConfidences[i]>=0)
								nBestView.add(nBestList.get(i) + " (conf: " + String.format("%.2f", nBestConfidences[i]) + ")");
							else
								nBestView.add(nBestList.get(i) + " (no confidence value available)");
						}
						else
							nBestView.add(nBestList.get(i) + " (no confidence value available)");
					}
					
					//Includes the collection in the ListView of the GUI
					setListView(nBestView);
					
					Log.i(LOGTAG, "There were : "+ nBestView.size()+" recognition results");
            	}
            }
            else {       	
	    		//Reports error in recognition error in log
	    		Log.e(LOGTAG, "Recognition was not successful");
            }
        }
	}
	
	/**
	 * Includes the recognition results in the list view
	 * @param nBestView list of matches
	 */
	private void setListView(ArrayList<String> nBestView){
		
		// Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nBestView);
    	ListView listView = (ListView) findViewById(R.id.nbest_listview);
    	listView.setAdapter(adapter);

	}
}