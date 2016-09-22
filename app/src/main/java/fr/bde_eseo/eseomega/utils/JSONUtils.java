/**
 * Copyright (C) 2016 - François LEPAROUX
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.bde_eseo.eseomega.utils;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by François L. on 11/08/2015.
 */
public class JSONUtils {


    public static JSONObject getJSONFromUrl(String url) {

        String result;
        JSONObject obj = null;

        result = ConnexionUtils.getServerData(url);
        if (Utilities.isNetworkDataValid(result)) {
            try {
                obj = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return obj;
    }

    public static JSONArray getJSONArrayFromUrl(String url) {

        String result;
        JSONArray array = null;

        result = ConnexionUtils.getServerData(url);
        if (Utilities.isNetworkDataValid(result)) {
            try {
                array = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return array;
    }

    public static JSONArray getJSONArrayFromUrl2(String url, Context ctx) {

        String result = null;
        JSONArray array = null;

        result = ConnexionUtils.postServerData(url, new HashMap<String, String>(), ctx);
        if (Utilities.isNetworkDataValid(result)) {
            try {
                array = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return array;
    }

    public static String getString(JSONObject obj, String key, String defval){
       if(obj.has(key)){
           try{
               return obj.getString(key);
           }catch (JSONException e) {
               return defval;
           }
       }else{
           return defval;
       }
    }

    public static int getInt(JSONObject obj, String key, int defval){
        if(obj.has(key)){
            try{
                return obj.getInt(key);
            }catch (JSONException e) {
                return defval;
            }
        }else{
            return defval;
        }
    }

    public static ArrayList<Integer> getList(JSONObject obj, String key) throws JSONException{
        ArrayList<Integer> list = new ArrayList<Integer>();
        if(obj.has(key)){
            JSONArray ids = obj.getJSONArray(key);
            for(int i = 0; i < ids.length(); i++){
                list.add(ids.getInt(i));
            }
        }
        return list;
    }

    public static Spanned fromHtml(String html){
        html = html.replace("<ul>","");
        html = html.replace("</ul>","");
        html = html.replace("<li>","- ");
        html = html.replace("</li>","<br/>");
        return Html.fromHtml(html);
    }

}
