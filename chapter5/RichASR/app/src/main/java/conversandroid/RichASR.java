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
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import conversandroid.richasr.R;

/**
 * RichASR: App with a rich control of the ASR using the SpeechRecognizer class
 * and implementing the RecognitionListener interface
 * (it is an improvement over the SimpleASR app)
 * 
 * Simple demo in which the user speaks and the recognition results
 * are showed in a list along with their confidence values
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 2.0, 05/13/16
 *
 */

public class RichASR extends Activity implements RecognitionListener{


	private SpeechRecognizer myASR;

	// Default values for the language model and maximum number of recognition results
	// They are shown in the GUI when the app starts, and they are used when the user selection is not valid
	private final static int DEFAULT_NUMBER_RESULTS = 10;
	private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;

	private int numberRecoResults = DEFAULT_NUMBER_RESULTS; 
	private String languageModel = DEFAULT_LANG_MODEL;

	private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;
	private static final String LOGTAG = "RICHASR";
	private static int ASR_CODE = 123;

    private long startListeningTime = 0; // To skip errors (see onError method)
	
	/**
	 * Sets up the activity initializing the GUI and the ASR
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.richasr);

        //Initialize ASR
        initASR();
		
		//Shows in the GUI the default values for the language model and the maximum number of recognition results
		showDefaultValues(); 

		setSpeakButton();
	}

	/*****************************************************************************************
	 * MANAGE ASR
	 *****************************************************************************************/

	/**
	 * Creates the speech recognizer instance if it is available
	 * */
	public void initASR() {

		// find out whether speech recognition is supported
		List<ResolveInfo> intActivities = this.getPackageManager().queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        //Speech recognition does not currently work on simulated devices
        if("generic".equals(Build.BRAND.toLowerCase())){
            Log.e(LOGTAG, "ASR is not supported on virtual devices");
        } else {
            if (intActivities.size() != 0) {
                myASR = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                myASR.setRecognitionListener(this);
            }
        }

        Log.i(LOGTAG, "ASR initialized");
	}

	/**
	 * Starts speech recognition after checking the ASR parameters
	 *
	 * @param language Language used for speech recognition (e.g. Locale.ENGLISH)
	 * @param languageModel Type of language model used (free form or web search)
	 * @param maxResults Maximum number of recognition results
	 */
	public void listen(final Locale language, final String languageModel, final int maxResults)
	{
        Button b = (Button) findViewById(R.id.speech_btn);
        b.setEnabled(false);

		if((languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || languageModel.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)) && (maxResults>=0))
		{
			// Check we have permission to record audio
			checkASRPermission();

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			// Specify the calling package to identify the application
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
			//Caution: be careful not to use: getClass().getPackage().getName());

			// Specify language model
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

			// Specify how many results to receive. Results listed in order of confidence
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResults);

			// Specify recognition language
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);

            Log.i(LOGTAG, "Going to start listening...");
            this.startListeningTime = System.currentTimeMillis();
			myASR.startListening(intent);

		}
		else {
			Log.e(LOGTAG, "Invalid params to listen method");
			((TextView) findViewById(R.id.feedbackTxt)).setText("Error: invalid parameters");
		}

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
		if (android.support.v4.content.ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
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
				Toast.makeText(getApplicationContext(), "Sorry, RichASR cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Stops listening to the user
	 */
	public void stopListening(){
        myASR.stopListening();
        Log.i(LOGTAG, "Stopped listening");
        Button b = (Button) findViewById(R.id.speech_btn);
        b.setEnabled(true);
	}

	/********************************************************************************************************
	 * Process ASR events
	 * ******************************************************************************************************
	 */

	/*
	 * (non-Javadoc)
	 *
	 * Invoked when the ASR provides recognition results
	 *
	 * @see android.speech.RecognitionListener#onResults(android.os.Bundle)
	 */
	@Override
	public void onResults(Bundle results) {
		if(results!=null){
            Log.i(LOGTAG, "ASR results received ok");
            ((TextView) findViewById(R.id.feedbackTxt)).setText("Results ready :D");

			//Retrieves the N-best list and the confidences from the ASR result
			ArrayList<String> nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] nBestConfidences = null;

			if (Build.VERSION.SDK_INT >= 14)  //Checks the API level because the confidence scores are supported only from API level 14
				nBestConfidences = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

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

		}
		else{
            Log.e(LOGTAG, "ASR results null");
			//There was a recognition error
			((TextView) findViewById(R.id.feedbackTxt)).setText("Error");
        }

        stopListening();

	}

	/*
     * (non-Javadoc)
     *
     * Invoked when the ASR is ready to start listening
     *
     * @see android.speech.RecognitionListener#onReadyForSpeech(android.os.Bundle)
     */
	@Override
	public void onReadyForSpeech(Bundle arg0) {
		((TextView) findViewById(R.id.feedbackTxt)).setText("Ready to hear you speak!");
	}

	/*
     * (non-Javadoc)
     *
     * Invoked when the ASR encounters an error
     *
     * @see android.speech.RecognitionListener#onError(int)
     */
	@Override
	public void onError(final int errorCode) {

        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        }
        else {
            String errorMsg = "";
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = "Unknown client side error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = "Network related error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = "Network operation timed out";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = "No recognition result matched";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = "RecognitionService busy";
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = "Server sends error status";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = "No speech input";
                default:
                    errorMsg = "";
            }
            if (errorCode == 5 && errorMsg == "") {
                //Log.e(LOGTAG, "Going to ignore the error");
                //Another frequent error that is not really due to the ASR
            } else {
                ((TextView) findViewById(R.id.feedbackTxt)).setText("Error :( " + errorMsg);
                Log.e(LOGTAG, "Error -> " + errorMsg);
                stopListening();
            }
        }
	}

	/*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
	@Override
	public void onBeginningOfSpeech() {
		((TextView) findViewById(R.id.feedbackTxt)).setText("You have started talking ;)");
	}

	/*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBufferReceived(byte[])
     */
	@Override
	public void onBufferReceived(byte[] buffer) {
		((TextView) findViewById(R.id.feedbackTxt)).setText("Buffer received");
	}

	/*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onBeginningOfSpeech()
     */
	@Override
	public void onEndOfSpeech() {
		((TextView) findViewById(R.id.feedbackTxt)).setText("You have stopped talking");
	}

	/*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onEvent(int, android.os.Bundle)
     */
	@Override
	public void onEvent(int arg0, Bundle arg1) {}

	/*
     * (non-Javadoc)
     * @see android.speech.RecognitionListener#onPartialResults(android.os.Bundle)
     */
	@Override
	public void onPartialResults(Bundle arg0) {
		((TextView) findViewById(R.id.feedbackTxt)).setText("Parcial results received");
	}

	/*
 	* (non-Javadoc)
 	* @see android.speech.RecognitionListener#onRmsChanged(float)
 	*/
	@Override
	public void onRmsChanged(float arg0) {}



	/*****************************************************************************************
     * GUI
     *****************************************************************************************/

	/**
	 * Sets up the listener for the button that the user
	 * must click to start talking
	 */
	private void setSpeakButton() {
		//Gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);

		//Set up click listener
		speak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                setRecognitionParams(); //Read speech recognition parameters from GUI
                listen(Locale.ENGLISH, languageModel, numberRecoResults); 				//Set up the recognizer with the parameters and start listening
			}
		});
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
	 * Includes the recognition results in the list view
	 * @param nBestView list of matches
	 */
	private void setListView(ArrayList<String> nBestView){
		
		// Instantiates the array adapter to populate the listView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nBestView);
    	ListView listView = (ListView) findViewById(R.id.nbest_listview);
    	listView.setAdapter(adapter);

	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        myASR.destroy();
        Log.i(LOGTAG, "ASR destroyed");
    }

}