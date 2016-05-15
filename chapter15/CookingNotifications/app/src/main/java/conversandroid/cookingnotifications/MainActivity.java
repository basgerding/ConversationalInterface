package conversandroid.cookingnotifications;

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
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.view.View;
import android.widget.Button;

/**
 * This app shows different ways of creating notifications:
 *  (1) Simple: only a title and a text (notifies the user it is time for lunch)
 *  (2) With action: title, text and associated action (notifies the user there is a new recipe, and
 *          the user can take the action to open its associated web page)
 *  (3) With unrestricted voice input: title, text, and the action associated is that the user
 *          can dictate a response (asks the user how did they like a recipe)
 *  (4) Voice input with grammar: title, text, and the action associated is that the user
 *          can say an option in a grammar (asks the user if they want a recipe today,
 *          they can choose between "no", "yes for lunch" or "yes for dinner"
 *
 * All are shown in the smartphone and also in the smartwatch (make sure that notifications are accepted
 * by the wearable: https://support.google.com/androidwear/answer/6090188?hl=en). The voice input
 * actions can only be performed in the wearable
 *
 * @author Zoraida Callejas, Michael McTear and David Griol
 * @version 1.0, 02/16/16
 *
 */
public class MainActivity extends Activity {

    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Associates the different notifications to the buttons
        ((Button) findViewById(R.id.btn_simple)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleNotification();
            }
        });
        ((Button) findViewById(R.id.btn_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionNotification();
            }
        });
        ((Button) findViewById(R.id.btn_voice)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoiceNotification(false);
            }
        });
        ((Button) findViewById(R.id.btn_voicegrammar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoiceNotification(true);
            }
        });
    }


    /**
     * Simple notification: only title and text, no actions attached
     */
    private void showSimpleNotification(){
        int notificationId = 1;

        //Building notification layout
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.cook)
                        .setContentTitle("Time for lunch!")
                        .setDefaults(Notification.DEFAULT_ALL) //Beware that without some default behaviours
                                                        //the notification may not show up in the wearable
                        .setContentText("Your lunch is ready");

        //Creating an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Building the notification and issuing it with the notification manager
        notificationManager.notify(notificationId, notificationBuilder.build());

    }

    /**
     * Notification with an action (opens recipes web page)
     */
    private void showActionNotification(){

        int notificationId = 2;

        // Build an intent for an action to open a url
        Intent urlIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("http://www.reciperoulette.tv/");
        urlIntent.setData(uri);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, urlIntent, 0);

        //Building notification layout
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.cook)
                        .setContentTitle("Cooking tips")
                        .setContentText("New recipe available")
                        .setDefaults(Notification.DEFAULT_ALL) //Beware that without some default behaviours
                                    //the notification may not show up in the wearable
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .addAction(R.mipmap.cook, "Check recipe", pendingIntent);

        //Creating an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Building the notification and issuing it with the notification manager
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Notification with an action: spoken input
     */
    private void showVoiceNotification(boolean grammar){

        int notificationId = 3;
        NotificationCompat.Action action;

        if(grammar)
            action = getGrammarAction();
        else
            action = getVoiceAction();

        //Building notification layout
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.cook)
                        .setContentTitle("New recipe")
                        .setContentText("There is a tasty new recipe")
                        .setDefaults(Notification.DEFAULT_ALL) //Beware that without some default behaviours
                        //the notification may not show up in the wearable
                        .setAutoCancel(true)
                        .extend(new NotificationCompat.WearableExtender().addAction(action));

        //Creating an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Building the notification and issuing it with the notification manager
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Setting the unrestricted voice reply action
     * See: http://developer.android.com/intl/es/training/wearables/notifications/voice-input.html
     */
    public NotificationCompat.Action getVoiceAction() {

        String replyLabel = "Tell us what you think about it";

        // Creates an intent for the reply action
        Intent replyIntent = new Intent(this, SecondActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(this, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();

        //Creates the reply action and adds the remote input
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.conversandroid,
                        "Tell us what you think", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        return action;
    }

    /**
     * Setting the unrestricted voice reply action
     * See: http://developer.android.com/intl/es/training/wearables/notifications/voice-input.html
     */
    private NotificationCompat.Action getGrammarAction(){

        String replyLabel = "Do you want to try it today?";
        String[] replyChoices = getResources().getStringArray(R.array.grammar); //The grammar is in strings.xml

        // Creates an intent for the reply action
        Intent replyIntent = new Intent(this, SecondActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(this, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .setChoices(replyChoices)
                .build();

       //Creates the reply action and adds the remote input
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.conversandroid,
                        replyLabel, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        return action;

    }


}
