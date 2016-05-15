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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

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
public class CoffeeActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_coffee);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                TextView textView = (TextView) findViewById(R.id.text);

                Intent intent = getIntent();
                //If this is a response to the set alarm intent, it shows a message saying that the coffee will be ready
                //at the time when the alarm has been set
                if (AlarmClock.ACTION_SET_ALARM.equals(intent.getAction())) {
                    if (intent.hasExtra(AlarmClock.EXTRA_HOUR)) {
                        int hour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, 7);
                        int mins = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, 0);
                        //**** The communication with the smart coffee machine will go here with the parameters hour and mins ;)
                        textView.setText("Your coffee will be ready at " + String.format("%02d", hour) +":"+ String.format("%02d", mins));
                    } else
                        textView.setText("Your coffee will be ready");
                }
            }
        });
    }
}