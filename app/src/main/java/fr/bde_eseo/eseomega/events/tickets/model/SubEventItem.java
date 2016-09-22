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

package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by François L. on 11/01/2016.
 */
public class SubEventItem {

    private String titre, id;
    private double price;
    private boolean available;
    private ArrayList<ShuttleItem> shuttleItems;

    public SubEventItem(JSONObject obj) throws JSONException {

        titre = obj.getString("nom");
        id = obj.getString("id");
        price = obj.getDouble("prix");
        available = obj.getInt("dispo") == 1;
        shuttleItems = new ArrayList<>();
    }

    public String getTitre() {
        return titre;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getEventPriceAsString(){
        return new DecimalFormat("0.00").format(price) + "€";
    }

    public boolean isAvailable() {
        return available;
    }

    public void searchShuttles (ArrayList<ShuttleItem> allShuttles) {
        shuttleItems.clear();
        for (int i=0;i<allShuttles.size();i++) {
            ShuttleItem si = allShuttles.get(i);
            if (si.correspondsToID(id)) {
                shuttleItems.add(si);
            }
        }
    }

    public boolean hasShuttles () {
        return shuttleItems.size() > 0;
    }

    public ArrayList<ShuttleItem> getShuttleItems() {
        return shuttleItems;
    }
}
