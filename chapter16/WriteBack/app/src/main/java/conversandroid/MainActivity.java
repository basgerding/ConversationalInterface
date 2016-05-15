package conversandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import conversandroid.WriteBack.R;

/**
 * This class shows how to use speech recognition capabilities in Android Wear using the VoiceActivity class
 * that we have created as a general purpose class for Android speech interfaces.
 * The behaviour of the app is very simple: when the user clicks the button, the app starts listening,
 * and when the recognizer returns a result, it shows it in a TextView in the GUI.
 * As most smartwatches do not have speech synthesis capabilities, the synthesizer is not used in this app,
 * though developers may implement the corresponding methods if this feature is supported in their
 * wearables.
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 1.0, 02/17/16
 */
public class MainActivity extends VoiceActivity {

    private long startListeningTime = 0; // To skip errors (see processAsrError method)
    private static final String LOGTAG = "WriteBack";
    private Activity ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        ctx =this;
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //Initialize the speech recognizer and synthesizer
                initSpeechInputOutput(ctx);
                //Set up the speech button
                setSpeakButton();
            }
        });
    }

    /**
     * Initializes the search button and its listener. When the button is pressed, the recognition starts
     */
    private void setSpeakButton() {
        // gain reference to speak button
        Button speak = (Button) findViewById(R.id.speech_btn);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonAppearanceToListening();
                startListening();
            }
        });
    }

    /**
     * Explain to the user why we need their permission to record audio on the device
     * See the checkASRPermission in the VoiceActivity class
     */
    public void showRecordPermissionExplanation(){
        Toast.makeText(getApplicationContext(), "WriteBack must access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();
    }

    /**
     * If the user does not grant permission to record audio on the device, a message is shown and the app finishes
     */
    public void onRecordAudioPermissionDenied(){
        Toast.makeText(getApplicationContext(), "Sorry, WriteBack cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
        System.exit(0);
    }

    /**
     * Starts listening for any user input.
     * When it recognizes something, the <code>processAsrResult</code> method is invoked.
     * If there is any error, the <code>onAsrError</code> method is invoked.
     */
    private void startListening(){

        if(deviceConnectedToInternet()){
            try {

				/*Start listening, with the following default parameters:
					* Language = English
					* Recognition model = Free form,
					* Number of results = 1 (we will use the best result to perform the search)
					*/
                startListeningTime = System.currentTimeMillis();
                listen(Locale.ENGLISH, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
            } catch (Exception e) {
                this.runOnUiThread(new Runnable() {  //Toasts must be in the main thread
                    public void run() {
                        Toast.makeText(getApplicationContext(),"ASR could not be started", Toast.LENGTH_SHORT).show();
                        changeButtonAppearanceToDefault();
                    }
                });

                Log.e(LOGTAG,"ASR could not be started");
                ((TextView) findViewById(R.id.txt)).setText("ERROR: Speech recognition could not be started");
            }
        } else {

            this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                public void run() {
                    Toast.makeText(getApplicationContext(),"Please check your Internet connection", Toast.LENGTH_SHORT).show();
                    changeButtonAppearanceToDefault();
                }
            });
            ((TextView) findViewById(R.id.txt)).setText("ERROR: Please check your Internet connection");
        }
    }

    /**
     * Invoked when the ASR is ready to start listening. Provides feedback to the user to show that the app is listening:
     * 		* It changes the color and the message of the speech button
     */
    @Override
    public void processAsrReadyForSpeech() {
        changeButtonAppearanceToListening();
    }

    /**
     * Provides feedback to the user to show that the app is listening:
     * 		* It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToListening(){
        Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
        button.setText(getResources().getString(R.string.speechbtn_listening)); //Changes the button's message to the text obtained from the resources folder
        button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_listening), PorterDuff.Mode.MULTIPLY);  //Changes the button's background to the color obtained from the resources folder
    }

    /**
     * Provides feedback to the user to show that the app is idle:
     * 		* It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToDefault(){
        Button button = (Button) findViewById(R.id.speech_btn); //Obtains a reference to the button
        button.setText(getResources().getString(R.string.speechbtn_default)); //Changes the button's message to the text obtained from the resources folder
        button.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.speechbtn_default),PorterDuff.Mode.MULTIPLY); 	//Changes the button's background to the color obtained from the resources folder
    }

    /**
     * Provides feedback to the user when the ASR encounters an error
     */
    @Override
    public void processAsrError(int errorCode) {

        changeButtonAppearanceToDefault();

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
                ((TextView) findViewById(R.id.txt)).setText(errorMsg);
            }
        }
    }



    /**
     * Shows the best recognition result in the TextView
     */
    @Override
    public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {

        if(nBestList!=null){

            Log.d(LOGTAG, "ASR found " + nBestList.size() + " results");

            if(nBestList.size()>0){
                String bestResult = nBestList.get(0); //We will use the best result
                ((TextView) findViewById(R.id.txt)).setText(bestResult);

                changeButtonAppearanceToDefault();
            }
        }
    }

    /**
     * Checks whether the device is connected to Internet (returns true) or not (returns false)
     * From: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
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
     * TTS not implemented in most smartwatches, if yours has speech synthesis capabilities,
     * implement the behaviour of the following methods:
     */

    @Override
    public void onTTSDone(String uttId) {}

    @Override
    public void onTTSError(String uttId) {}

    @Override
    public void onTTSStart(String uttId) {}
}
