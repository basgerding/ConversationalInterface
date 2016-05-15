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


/**
 * Error code indicating different reasons behind an error during the connection to Pandorabots

 * @author Michael McTear, Zoraida Callejas and David Griol
 * @version 4.0, 02/13/16
 *
 */

public enum PandoraErrorCode {

    UNKNOWN, //The cause of the error is unknown
    NOMATCH, //There is no valid response coded in the AIML for that particular query
    ID,  //App id, user key or robot name are invalid
    IDORHOST,  //Either ID error or the host (pandora website) is not reachable
    CONNECTION, //Internet connection error
    PARSE //Error parsing the robot response

}
