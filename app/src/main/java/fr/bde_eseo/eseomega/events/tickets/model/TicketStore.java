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

import java.util.ArrayList;

import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by François L. on 11/01/2016.
 */
public class TicketStore {

    private static TicketStore instance;

    private TicketStore (){}

    public static TicketStore getInstance() {
        if (instance == null)
            instance = new TicketStore();
        return instance;
    }

    /**
     * The event list
     */
    private ArrayList<EventItem> eventItems;

    /**
     * The ticket list
     */
    private ArrayList<EventTicketItem> eventTicketItems;

    /**
     * Reset function
     */
    public void reset () {
        if (eventItems == null) eventItems = new ArrayList<>();
        eventItems.clear();
        if (eventTicketItems == null) eventTicketItems = new ArrayList<>();
        eventTicketItems.clear();
    }

    /**
     * Token
     */
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Second reset function
     */
    public void resetOrder () {
        token = null;
        if (shuttleItems == null)
            shuttleItems = new ArrayList<>();
        shuttleItems.clear();
        selectedShuttle = null;
    }

    public ArrayList<EventTicketItem> getEventTicketItems() {
        return eventTicketItems;
    }

    public ArrayList<EventItem> getEventItems() {
        return eventItems;
    }

    /**
     * Shuttles items
     */
    private ArrayList<ShuttleItem> shuttleItems;

    public ArrayList<ShuttleItem> getShuttleItems() {
        return shuttleItems;
    }

    /**
     * Assigne aux ID tickets un nom
     */
    public void autoTicketAttributes () {

        // For each ticket
        for (int et=0;et<eventTicketItems.size();et++) {

            // For each event
            for (int ei=0;ei<eventItems.size();ei++) {

                // If event is not a header / content not null
                if (!eventItems.get(ei).isHeader() && eventItems.get(ei).getSubEventItems() != null) {

                    // For each sub-event item
                    for (int si = 0; si < eventItems.get(ei).getSubEventItems().size(); si++) {

                        // If event if not header / non null content
                        if (eventItems.get(ei).getSubEventItems().get(si).getId().equals(eventTicketItems.get(et).getIdevent())) {
                            eventTicketItems.get(et).setLinkedEvent(eventItems.get(ei));
                        }
                    }
                }
            }
        }
    }

    /**
     * Selected ticket
     */
    private SubEventItem selectedTicket;

    public void setSelectedTicket(SubEventItem selectedTicket) {
        this.selectedTicket = selectedTicket;
    }

    public SubEventItem getSelectedTicket() {
        return selectedTicket;
    }

    /**
     * Current shuttle
     */
    private ShuttleItem selectedShuttle;

    public void setSelectedShuttle(ShuttleItem selectedShuttle) {
        this.selectedShuttle = selectedShuttle;
    }
}
