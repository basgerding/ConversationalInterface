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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;



/**
 * SimpleTTS: Basic app with text to speech synthesis
 * <p/>
 * Simple demo in which the user writes a text in a text field
 * and it is synthesized by the system when pressing a button.
 * <p/>
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 2.0, 02/06/16
 *
 */
public class SimpleTTS extends Activity {

	private int TTS_DATA_CHECK = 12;    // Request code to identify the intent that looks for a TTS Engine in the device

	private TextToSpeech mytts = null;

	private EditText inputText;
	private Button speakButton;


	/**
	 * Sets up the activity initializing the text to speech engine
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simpletts);

		// Set up the speak button
		setButton();

		// Invoke the method to initialize text to speech
		initTTS();

		// Reference the edit text field
		inputText = (EditText) findViewById(R.id.input_text);

	}

	/**
	 * Sets up the listener for the button that the user
	 * must click to hear the obtained the synthesized message
     * @see http://developer.android.com/reference/android/speech/mytts/TextToSpeech.html#speak(java.lang.CharSequence,%20int,%20android.os.Bundle,%20java.lang.String)
	 */
	private void setButton() {
		// Reference the speak button
		speakButton = (Button) findViewById(R.id.speak_button);

		// Set up click listener
		speakButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get the text typed in by the user
				String text = inputText.getText().toString();

				// If there is text, call the method speak() to synthesize it
				if (text != null && text.length() > 0) {

					if (Build.VERSION.SDK_INT >= 21) {
						//For SDK 21 and later, the speak method accepts four parameters:
						//text: the string to be spoken (obtained from the interface)
						//QUEUE_ADD: queuing strategy = this message is added to the end of the playback queue
						//null: we do not indicate any specific synthesis parameters, just use the default
						//"msg": unique identifier for this request
						mytts.speak(text, TextToSpeech.QUEUE_ADD, null, "msg");
					} else {
						//For earlier versions, it accepts three parameters (deprecated)
						mytts.speak(text, TextToSpeech.QUEUE_ADD, null);
					}
				}

			}
		});

	}

	/**
	 * Checks whether there is a TTS Engine in the device, when done, it invokes the
	 * onActivityResult method which actually initializes the TTS
	 */
	private void initTTS() {
		//Disable speak button during the initialization of the text to speech engine
		disableSpeakButton();

		//Check if the engine is installed, when the check is finished, the
		//onActivityResult method is automatically invoked
		Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_DATA_CHECK);
	}

	/**
	 * Callback from check for text to speech engine installed
	 * If positive, then creates a new <code>TextToSpeech</code> instance which will be called when user
	 * clicks on the 'Speak' button
	 * If negative, creates an intent to install a <code>TextToSpeech</code> engine
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Check that the result received is from the TTS_DATA_CHECK action
		if (requestCode == TTS_DATA_CHECK) {

			// If the result of the action is CHECK_VOICE_DATA_PASS, there is a TTS Engine
			//available in the device
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

				// Create a TextToSpeech instance  
				mytts = new TextToSpeech(this, new OnInitListener() {
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {
							// Display Toast				
							Toast.makeText(SimpleTTS.this, "TTS initialized", Toast.LENGTH_LONG).show();

							// Set language to US English if it is available
							if (mytts.isLanguageAvailable(Locale.US) >= 0)
								mytts.setLanguage(Locale.US);
						}
						enableSpeakButton();
					}
				});
			} else {
				// The TTS is not available, we will try to install it:
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

				PackageManager pm = getPackageManager();
				ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

				//If the install can be started automatically we launch it (startActivity), if not, we
				//ask the user to install the TTS from Google Play (toast)
				if (resolveInfo != null) {
					startActivity(installIntent);
				} else {
					Toast.makeText(SimpleTTS.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	/**
	 * Disables the speak button so that the user cannot click it while a message
	 * is being synthesized
	 */
	private void disableSpeakButton() {
		speakButton.setEnabled(false);
	}

	/**
	 * Enables the speak button so that the user can click on it to hear
	 * the synthesized message
	 */
	private void enableSpeakButton() {
		speakButton.setEnabled(true);
	}

	/**
	 * Shuts down the TTS when finished
	 */
	@Override
	public void onDestroy() {
		if (mytts != null) {
			mytts.stop();
			mytts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();


	}

	@Override
	public void onStop() {
		super.onStop();

	}
}

