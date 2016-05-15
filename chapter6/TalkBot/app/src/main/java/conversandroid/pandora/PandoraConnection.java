package conversandroid.pandora;

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

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//>> Check the build.gradle file to see how apache http client libraries are considered for compilation

/**
 * Connection to the Pandorabot AI as a Service.
 *
 * This class is based in the one created by Richard Wallace as a Java API to the service mentioned
 * (version 0.0.9) See: <a href="https://developer.pandorabots.com/docs">Pandorabots API Documentation</a><br>).
 * However, significant edition has been performed to adapt it to Android and simplify
 * the methods used.
 *
 * @author Michael McTear, Zoraida Callejas and David Griol
 * @version 4.0, 02/13/16
 *
 */
public class PandoraConnection {

    private static final String LOGTAG = "PANDORA_CONNECT";

    private String host;
    private String userKey;
    private String appId;
    private String botName;

    /**
     * Constructor that initializes the connection parameters
     *
     * @param host
     *            host name of pandorabots API server
     * @param appId
     *            app_id to pandorabots API
     * @param userKey
     *            user_key to pandorabots API
     * @param botName
     *            unique name of the bot within the app with appId
     */
    public PandoraConnection(String host, String appId, String userKey, String botName) {
        this.host = host;
        this.appId = appId;
        this.userKey = userKey;
        this.botName = botName;
    }


    /**
     * Sends the user message to the chatbot and returns the chatbot response
     * It is a simplification and adaptation to Android of the method with the same name in the
     * Pandorabots Java API: https://github.com/pandorabots/pb-java
     * @param input text for conversation
     * @return text of bot's response
     * @throws PandoraException when the connection is not succesful
     */

    public String talk(String input) throws PandoraException {

        String responses = "";
        input = input.replace(" ", "%20");

        URI uri = null;
        try {
            uri = new URI("https://"+host+"/talk/"+appId+"/"+botName+"?input="+input+"&user_key="+userKey);
            Log.d(LOGTAG, "Request to pandorabot: Botname=" + botName + ", input=\"" + input + "\"" + " uri="+ uri);
        } catch (URISyntaxException e) {
            Log.e(LOGTAG, e.getMessage());
            throw new PandoraException(PandoraErrorCode.IDORHOST);
        }


        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy); //Why the strictmode: http://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua

            try {
                Content content = Request.Post(uri).execute().returnContent();
                String response = content.asString();
                JSONObject jObj = new JSONObject(response);
                JSONArray jArray = jObj.getJSONArray("responses");
                for (int i = 0; i < jArray.length(); i++) {
                    responses += jArray.getString(i).trim();
                }
            } catch (JSONException e) {
                Log.e(LOGTAG, e.getMessage());
                throw new PandoraException(PandoraErrorCode.PARSE);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
                throw new PandoraException(PandoraErrorCode.CONNECTION);
            } catch (Exception e){
                throw new PandoraException(PandoraErrorCode.IDORHOST);
            }

        }

        if(responses.toLowerCase().contains("match failed")) {
            Log.e(LOGTAG, "Match failed");
            throw new PandoraException(PandoraErrorCode.NOMATCH);
        }

        Log.d(LOGTAG, "Bot response:" + responses);

        return responses;
    }
}

