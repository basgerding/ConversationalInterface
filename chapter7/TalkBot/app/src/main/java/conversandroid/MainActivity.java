package conversandroid;

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

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import conversandroid.pandora.PandoraConnection;
import conversandroid.pandora.PandoraErrorCode;
import conversandroid.pandora.PandoraException;
import conversandroid.pandora.PandoraResultProcessor;
import conversandroid.talkbot.R;
import conversandroid.voiceinterface.VoiceActivity;


/**
 * Chatbot/VPA that uses the technology of Pandorabots to understand the user queries and provide information
 * 
 * @author Michael McTear, Zoraida Callejas and David Griol
 * @version 4.0, 05/14/16
 *
 */

public class MainActivity extends VoiceActivity {

    private static final String LOGTAG = "TALKBOT";

    private static Integer ID_PROMPT_QUERY = 0;	//Id chosen to identify the prompts that involve posing questions to the user
    private static Integer ID_PROMPT_INFO = 1;	//Id chosen to identify the prompts that involve only informing the user
	private long startListeningTime = 0; // To skip errors (see processAsrError method)

	//TODO: USE YOUR OWN PARAMETERS TO MAKE IT WORK
	private String host = "aiaas.pandorabots.com";
	private String userKey = "YOUR USER KEY HERE";
	private String appId = "YOUR APP ID HERE";
	private String botName = "YOUR BOT NAME HERE";
	PandoraConnection pandoraConnection = new PandoraConnection(host, appId, userKey, botName);
	
	/**
	 * Sets up the activity initializing the GUI, ASR and TTS
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Set layout
		setContentView(R.layout.main);
		
		//Initialize the speech recognizer and synthesizer
		initSpeechInputOutput(this);
		
		//Set up the speech button and progress circle
		setSpeakButton();
        showProgressBar(false);
	}

	/**
	 * Initializes the search button and its listener. When the button is pressed, a feedback is shown to the user
	 * and the recognition starts
	 */
	private void setSpeakButton() {
		this.setDefaultButtonAppearance();
		// gain reference to speak button
		Button speak = (Button) findViewById(R.id.speech_btn);
		speak.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                    //Start ASR
                    indicateListening();
					startListening();
			}
		});
	}

	/**
	 * Explain to the user why we need their permission to record audio on the device
	 * See the checkASRPermission in the VoiceActivity class
	 */
	public void showRecordPermissionExplanation(){
		Toast.makeText(getApplicationContext(), "TalkBot needs your permission to access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();
	}

	/**
	 * If the user does not grant permission to record audio on the device, a message is shown and the app finishes
	 */
	public void onRecordAudioPermissionDenied(){
		Toast.makeText(getApplicationContext(), "Sorry, TalkBot cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
		System.exit(0);
	}
	
	/**
	 * Starts listening for any user input.
	 * When it recognizes something, the <code>processAsrResult</code> method is invoked. 
	 * If there is any error, the <code>processAsrError</code> method is invoked.
	 */
	private void startListening(){
		
		if(deviceConnectedToInternet()){
			try {
				
				/*Start listening, with the following default parameters:
					* Recognition model = Free form, 
					* Number of results = 1 (we will use the best result to perform the search)
					*/
				startListeningTime = System.currentTimeMillis();
				listen(Locale.ENGLISH, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
			} catch (Exception e) {
				Log.e(LOGTAG, e.getMessage());
			}	
		} else {
				Log.e(LOGTAG, "Device not connected to Internet");	
		}
	}

	/**
	 * Provides feedback to the user to show that the app is listening:
	 * 		* It changes the color and the message of the speech button
	 *      * It synthesizes a voice message
	 */
	private void indicateListening() {
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
		button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_listening),PorterDuff.Mode.MULTIPLY); //Changes the button's background to the color obtained from the resources folder
    }
	
	/**
	 * Provides feedback to the user to show that the app is idle:
	 * 		* It changes the color and the message of the speech button
	 */	
	private void setDefaultButtonAppearance(){
		Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
		button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
		button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_default),PorterDuff.Mode.MULTIPLY);	//Changes the button's background to the color obtained from the resources folder
    }

    /**
     * Shows or hides a progress circle to tell the user that the app is processing their command
     * @param show true=show, false=hide
     */
    private void showProgressBar(Boolean show){
        if(show)
            findViewById(R.id.listeningCircle).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.listeningCircle).setVisibility(View.GONE);
    }
	
	/**
	 * Provides feedback to the user when the ASR encounters an error
	 */
	@Override
	public void processAsrError(int errorCode) {
		setDefaultButtonAppearance();
        showProgressBar(false);

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
					errorMsg = ""; //Another frequent error that is not really due to the ASR, we will ignore it
			}
			if (errorMsg != "") {
				this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
					public void run() {
						Toast.makeText(getApplicationContext(), "Speech recognition error", Toast.LENGTH_LONG).show();
					}
				});

				Log.e(LOGTAG, "Error when attempting to listen: " + errorMsg);
				try { speak(errorMsg,"EN", ID_PROMPT_INFO); } catch (Exception e) { Log.e(LOGTAG, "TTS not accessible"); }
			}
		}

    }


	@Override
	public void processAsrReadyForSpeech() {}

	/**
	 * Initiates interaction with Pandorabots with the results of the recognition
	 */
	@Override
	public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {
        setDefaultButtonAppearance();
        runOnUiThread(new Runnable() {
            public void run() {
                showProgressBar(true); //the app has started to process the user input, so it shows the progress circle
            }
        });

		if(nBestList!=null){
			if(nBestList.size()>0){
				String userQuery = nBestList.get(0); //We will use the best result

				try {
					String response = pandoraConnection.talk(userQuery); //Query to pandorabots
					processBotResults(response); //Process the bot response
				} catch (PandoraException e) {
                    processBotErrors(e.getErrorCode());
                }
			}
		}

	}

	/**
	 * Informs the user about errors with the connection to Pandorabots
	 * @param errorCode Reason behind the error
	 */
	private void processBotErrors(PandoraErrorCode errorCode){
		String errormsg="";

		switch(errorCode)
		{
			case ID: //Problem with app id, user key or robot name (parameters required by Pandorabots)
				errormsg = getResources().getString(R.string.iderror_prompt);
				break;

			case NOMATCH: //There is no response available for the query
				errormsg =  getResources().getString(R.string.iderror_prompt);
				break;

			case IDORHOST: //Problem with ID or the host name indicted for the Pandora service
				errormsg =  getResources().getString(R.string.iderror_prompt);
				break;

			case CONNECTION: //Internet connection error
				errormsg = getResources().getString(R.string.connectionerror_prompt);
				break;

			case PARSE: //Error parsing the robot response
				errormsg = getResources().getString(R.string.iderror_prompt);
				break;

			default:
				errormsg = getResources().getString(R.string.connectionerror_prompt);
				break;
		}

		try {
			speak(errormsg, "EN", ID_PROMPT_INFO);
			Log.e(LOGTAG, getResources().getString(R.string.connectionerror_prompt));
		} catch (Exception e) {
			Log.e(LOGTAG, "The message '" + errormsg + "' could not be synthesized");
		}

	}


	/**
	 * Processes the response from Pandorabots ALICE2v. This response can be a simple text with simple HTML tags, or a more complex
	 * text with <oob> tags that must be further processed.
	 * 
	 * @param result response from Pandorabots
	 */
	public void processBotResults(String result){

		Log.d(LOGTAG, "Response, contents of that: "+result);

		// Speak out simple text from Pandorabots after removing any HTML content
		if(!result.contains("<oob>")){
			result = removeTags(result);
			try {
				speak(result,"EN",ID_PROMPT_INFO);
			} catch (Exception e) {
				Log.e(LOGTAG, "The message '"+result+"' could not be synthesized");
                showProgressBar(false);
			}

		}
		// Send responses with <oob> for further processing
		else{
			try {
                PandoraResultProcessor oob=new PandoraResultProcessor(this, ID_PROMPT_INFO);
				oob.processOobOutput(result);
			} catch (Exception e) {
				Log.d(LOGTAG, e.getMessage());
			}

		}
	}
	
	
	/**
	 * Removes HTML tags from a string
	 * 
	 * @author http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
	 * @param string text with html tags
	 * @return text without html tags
	 */
	private String removeTags(String string) {
		Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

	    if (string == null || string.length() == 0) {
	        return string;
	    }

	    Matcher m = REMOVE_TAGS.matcher(string);
	    return m.replaceAll("");
	}
	
	
	/**
	 * Checks whether the device is connected to Internet (returns true) or not (returns false)
	 * 
	 * @author http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
	 */
	public boolean deviceConnectedToInternet() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
	}
	
	/**
	 * Shuts down the TTS engine when finished
	 */   
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdown();
	}

	/**
	 * Invoked when the TTS has finished synthesizing.
	 * 
	 * In this case, it starts recognizing if the message that has just been synthesized corresponds to a question (its id is ID_PROMPT_QUERY),
	 * and does nothing otherwise.
	 * 
	 * @param uttId identifier of the prompt that has just been synthesized (the id is indicated in the speak method when the text is sent
	 * to the TTS engine)
	 */
	@Override
	public void onTTSDone(String uttId) {
		if(uttId.equals(ID_PROMPT_QUERY.toString()))
			startListening();
		
	}

	/**
	 * Invoked when the TTS encounters an error.
	 * 
	 * In this case it just writes in the log.
	 */
	@Override
	public void onTTSError(String uttId) {
		Log.e(LOGTAG, "TTS error");
	}

	/**
	 * Invoked when the TTS starts synthesizing
	 * 
	 * In this case it just writes in the log.
	 */
	@Override
	public void onTTSStart(String uttId) {
        runOnUiThread(new Runnable() {
            public void run() {
                showProgressBar(false); //the app has finished processing the user input, so it hides the progress circle
            }
        });
        Log.e(LOGTAG, "TTS starts speaking");
	}
} 

