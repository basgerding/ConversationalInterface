package conversandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

/**
 * Android Wear app that "prepares coffee" at a certain hour. It responds in two settings:
 *   When the user runs the app explicitly, introduces an hour in the GUI and presses the Set Alarm button
 *   When the user sets and alarm in the wearable using the associated predefined voice command
 *
 * MainActivity: Part of the app that is used to show a GUI when the user runs it explicitly
 * CoffeeActivity: response to the set_alarm intent that shows a text saying that the coffee will be ready (see the intent-filter
 * in the Manifest)
 *
 * @author Zoraida Callejas, Michael McTear and David Griol
 * @version 2.0, 02/15/16
 *
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_main);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //GUI settings must be inside "onLayoutInflated"
                setButton();
            }
        });
    }

    /**
     * Establishes the button behaviour: when it is clicked, it creates an alarm with the
     * hour introduced by the user in the interface
     */
    public void setButton(){
        //Behaviour of the "Set Alarm" button
        Button b = (Button) findViewById(R.id.setButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Reads hour from the TimePicker in the interface
                TimePicker t = (TimePicker) findViewById(R.id.timePicker);
                int hour, mins;
                if (Build.VERSION.SDK_INT >= 23) {
                    hour = t.getHour();
                    mins = t.getMinute();
                } else {
                    hour = t.getCurrentHour();
                    mins = t.getCurrentMinute();
                }
                //Creates and starts the intent to set an Alarm. It is managed by the CoffeeActivity class
                //(see the associated <intent-filter> in the Manifest
                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
                    intent.putExtra(AlarmClock.EXTRA_MINUTES, mins);
                    startActivity(intent);
                }
            }
        });
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
