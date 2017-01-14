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

package fr.bde_eseo.eseomega.sponsors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by François L. on 11/08/2015.
 */
class SponsorItem {

    private String name, detail, img, url, adr;
    private ArrayList<String> avantages;

    public SponsorItem(JSONObject obj) throws JSONException{
        this.name = obj.getString(Constants.JSON_SPONSO_NAME);
        this.detail = obj.getString(Constants.JSON_SPONSO_DETAIL);
        this.img = JSONUtils.getString(obj, Constants.JSON_SPONSO_IMG, null, true);
        this.url = JSONUtils.getString(obj, Constants.JSON_SPONSO_URL, "");
        this.adr = JSONUtils.getString(obj, Constants.JSON_SPONSO_ADR, "");
        this.avantages = new ArrayList<>();
        JSONArray avantagesJSON = obj.getJSONArray(Constants.JSON_SPONSO_AVANTAGES);
        for (int a = 0; a < avantagesJSON.length(); a++) {
            avantages.add(avantagesJSON.getString(a));
        }

    }

    public String getAdr() {
        return adr;
    }

    public String getDetail() {
        return detail;
    }

    public String getImg() {
        return img;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public boolean hasAdr(){
        return !adr.equals("") && adr.length()>0;
    }

    public boolean hasUrl(){
        return !url.equals("") && url.length()>0;
    }

    public ArrayList<String> getAvantages() {
        return avantages;
    }

    @Override
    public String toString() {
        return "SponsorItem{" +
                "name='" + name + '\'' +
                ", detail='" + detail + '\'' +
                ", img='" + img + '\'' +
                ", adr='" + url + '\'' +
                ", adr='" + adr + '\'' +
                ", avantages=" + avantages +
                '}';
    }
}
