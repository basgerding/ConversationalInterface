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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;



/**
 * RichTTS: App with a rich control of text to speech synthesis using the TextToSpeech class
 * and implementing the OnInitListener
 * (it is an improvement over the SimpleTTS app)
 * <p/>
 * Simple demo in which the user writes a text in a text field
 * and it is synthesized by the system when pressing a button.
 * <p/>
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 2.0, 02/07/16
 *
 */
public class RichTTS extends Activity implements android.speech.tts.TextToSpeech.OnInitListener{

    private final static int TTS_DATA_CHECK = 12;    // Request code to identify the intent that looks for a TTS Engine in the device
    private final static String LOGTAG = "RichTTS";

    private String language;

    private TextToSpeech mytts = null;

    private EditText inputText;
    private Button speakButton;


    /**
     * Sets up the activity initializing the text to speech engine
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.richtts);

        // Set up the speak button
        setButton();

        // Set up the listview adapter
        setLanguageListView();

        // Invoke the method to initialize text to speech
        initTTS();

        // Reference the edit text field
        inputText = (EditText) findViewById(R.id.input_text);

    }

    private void setLanguageListView(){

        final String[] languages = getResources().getStringArray(R.array.languages);
        final ListView langList = (ListView) findViewById(R.id.lang_list);
        langList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages));

        // ListView on item selected listener.
        langList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                language =  languages[position];
            }
        });

    }


    /**
     * Sets up the listener for the button that the user
     * must click to hear the obtained the synthesized message
     *
     * http://developer.android.com/reference/android/speech/mytts/TextToSpeech.html#speak(java.lang.CharSequence,%20int,%20android.os.Bundle,%20java.lang.String)
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

                    //Set the language selected from the listView (see the itemClickListener in setLanguageListView)
                    if(language!=null)
                        mytts.setLanguage(new Locale(language));

                    //Read volume from seekbar. The seekbar allows choosing integer values from 0 to 10,
                    //we must translate them to a float from 0 to 1
                    int vol= ((SeekBar) findViewById(R.id.volumeBar)).getProgress();
                    float volumeLevel = (float) vol/10;

                    if (Build.VERSION.SDK_INT >= 21) {
                        //For SDK 21 and later, the speak method accepts four parameters:
                        //text: the string to be spoken (obtained from the interface)
                        //QUEUE_ADD: queuing strategy = this message is added to the end of the playback queue
                        //tts_params: a bundle with tts parameters, in our case the volume level
                        //"msg": unique identifier for this request
                        Bundle tts_params = new Bundle();
                        tts_params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumeLevel);
                        mytts.speak(text, TextToSpeech.QUEUE_ADD, tts_params, "msg");

                    } else {
                        //For earlier versions, it accepts three parameters (deprecated)
                        HashMap<String,String> tts_params = new HashMap<String, String>();
                        tts_params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME,Float.toString(volumeLevel));
                        mytts.speak(text, TextToSpeech.QUEUE_ADD, tts_params);
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

                // Create the TextToSpeech instance
                setTTS();
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
                    Toast.makeText(RichTTS.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * Starts the TTS engine. It is work-around to avoid being a subclass of the UtteranceProgressListener abstract class.
     *
     * @author Method by Greg Milette (comments incorporated by us). Source: https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/voiceaction/VoiceActionExecutor.java
     * See the problem here: http://stackoverflow.com/questions/11703653/why-is-utteranceprogresslistener-not-an-interface
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setTTS() {
        mytts = new TextToSpeech(this, (OnInitListener) this);

		/*
		 * The listener for the TTS events varies depending on the Android version used:
		 * the most updated one is UtteranceProgressListener, but in SKD versions
		 * 15 or earlier, it is necessary to use the deprecated OnUtteranceCompletedListener.
		 *
		 * For the newer UtteranceProgressListener, from version 21 and later the onError
		 * method receives two parameters (a String utterance id and an integer error code),
		 * while in earlier versions the deprecated onError method had only a String parameter
		 * with the utterance id.
		 */
        if (Build.VERSION.SDK_INT >=21) {
            mytts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) //TTS finished synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Mission accomplished ;)", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String utteranceId, int errorCode) //TTS encountered an error while synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Oops! there was an error :S", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String utteranceId) //TTS encountered an error while synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Oops! there was an error :S", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onStart(String utteranceId) //TTS has started synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "TTS started!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        else if (Build.VERSION.SDK_INT >= 15) {
            mytts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) //TTS finished synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Mission accomplished ;)", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String utteranceId) //TTS encountered an error while synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Oops! there was an error :S", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onStart(String utteranceId) //TTS has started synthesizing
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "TTS started!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } else {
            mytts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(final String utteranceId) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(RichTTS.this, "Mission accomplished ;)", Toast.LENGTH_LONG).show(); //Earlier SDKs only consider the onTTSDone event
                        }
                    });
                }
            });
        }
    }


    /**
     * Stops the synthesizer if it is speaking
     */
    public void stop() {
        if (mytts.isSpeaking())
            mytts.stop();
    }

    /**
     * Stops the speech synthesis engine. It is important to call it, as
     * it releases the native resources used.
     */
    public void shutdown() {
        mytts.stop();
        mytts.shutdown();
        mytts = null;			/*
		 						This is necessary in order to force the creation of a new TTS instance after shutdown.
		 						It is useful for handling runtime changes such as a change in the orientation of the device,
		 						as it is necessary to create a new instance with the new context.
		 						See here: http://developer.android.com/guide/topics/resources/runtime-changes.html
							*/
    }

    /*
     * A <code>TextToSpeech</code> instance can only be used to synthesize text once
     * it has completed its initialization.
     * (non-Javadoc)
     * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
     */
    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            Toast.makeText(RichTTS.this, "TTS initialized", Toast.LENGTH_LONG).show();
        } else {
            Log.e(LOGTAG, "Error initializing the TTS");
        }

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

