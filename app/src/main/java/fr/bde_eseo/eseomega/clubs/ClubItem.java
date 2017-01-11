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

package fr.bde_eseo.eseomega.clubs;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventItem;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by François L. on 31/08/2015.
 *
 */
public class ClubItem {

    public static String NEWSTAG = "news";
    public static String EVENTTAG = "event";
    private String name, desc, img;
    private HashMap<String, String> contacts;
    private ArrayList<SubList> details;
    private int id;
    private boolean updated;

    public ClubItem (JSONObject obj) throws JSONException {
        name = obj.getString(Constants.JSON_CLUB_NAME);
        desc = obj.getString(Constants.JSON_CLUB_DETAIL);
        img = JSONUtils.getString(obj, Constants.JSON_CLUB_IMG, null, true);
        id = obj.getInt(Constants.JSON_CLUB_ID);
        contacts = new HashMap<>();
        JSONObject cont = obj.getJSONObject(Constants.JSON_CLUB_CONTACTS);
        Iterator<String> keys = cont.keys();
        while(keys.hasNext()){
            String k = keys.next();
            contacts.put(k, cont.getString(k));
        }

        details = new ArrayList<>();
        updated = false;
    }

    public void update(Context ctx, JSONArray bureau, JSONArray related, JSONArray events){
        details = new ArrayList<>();
        details.add(new SubList(ctx, ctx.getString(R.string.club_bureau), bureau));
        details.add(new SubList(ctx, ctx.getString(R.string.club_related), related));
        details.add(new SubList(ctx, ctx.getString(R.string.club_event), events));
        updated = true;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getImg() {
        return img;
    }

    public String getContact(String key){
        return contacts.get(key);
    }

    public boolean isUpdated(){
        return updated;
    }

    public ArrayList<SubList> getDetails(){
        return details;
    }

    public boolean hasContact(String key){
        return contacts.containsKey(key) && contacts.get(key).length() > 0;
    }

    public class SubList {
        private String name;
        private ArrayList<ListItem> items;

        public SubList(Context ctx, String name, JSONArray array){
            this.name = name;
            this.items = new ArrayList<>();
            for(int i = 0; i < array.length(); i++){
                try {
                    items.add(new ListItem(ctx, array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getName(){
            return name;
        }

        public ArrayList<ListItem> getItems(){
            return items;
        }

        public int getSize(){
            return items.size();
        }
    }

    public class ListItem {
        private String title, detail, img, type;
        private int id;

        public ListItem (Context ctx, JSONObject obj) throws JSONException {
            if (obj.has(Constants.JSON_CLUB_MEMBER_DETAIL)) {
                //Bureau
                title = obj.getString(Constants.JSON_CLUB_MEMBER_NAME);
                detail = obj.getString(Constants.JSON_CLUB_MEMBER_DETAIL);
                img = JSONUtils.getString(obj, Constants.JSON_CLUB_MEMBER_IMG, null, true);
                id = 0;
            } else if (obj.has(Constants.JSON_EVENT_DATEFIN)) {
                //Event
                title = obj.getString(Constants.JSON_EVENT_NAME);

                detail = EventItem.getShortedDetails(ctx, obj.getString(Constants.JSON_EVENT_DATE), obj.getString(Constants.JSON_EVENT_DATEFIN));
                img = EVENTTAG;
                id = obj.getInt(Constants.JSON_EVENT_ID);
            } else {
                //News
                title = obj.getString(Constants.JSON_NEWS_TITLE);
                detail = obj.getString(Constants.JSON_NEWS_SHORTDATE);
                img = NEWSTAG;
                id = obj.getInt(Constants.JSON_NEWS_ID);
            }
        }

        public String getTitle() {
            return title;
        }

        public String getDetail() {
            return detail;
        }

        public String getImg() {
            return img;
        }

        public int getId(){
            return id;
        }
    }


}
