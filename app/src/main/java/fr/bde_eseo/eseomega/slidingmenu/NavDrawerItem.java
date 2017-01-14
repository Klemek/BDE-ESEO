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

package fr.bde_eseo.eseomega.slidingmenu;

// Model for our sidelist
public class NavDrawerItem {

    private String title;
    private String id;
	private String moreData;

	// TODO in a int description type (FL)
	private boolean isProfile;
	private boolean isDivider;
    private boolean isOption;

	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;

    /**
     * Item
     */
	public NavDrawerItem(String title, int icon){
		this.title = title;
		this.icon = icon;
        isProfile = false;
        isDivider = false;
        isOption = false;
	}

    /**
     * Profile
     */
    public NavDrawerItem(String title, String id){
        this.title = title;
        this.id = id;
        isProfile = true;
        isDivider = false;
        isOption = false;
    }

    /**
     * Divider
     */
    public NavDrawerItem() {
        isProfile = false;
        isDivider = true;
        isOption = false;
    }

    public NavDrawerItem(String title) {
        this.title = title;
        isProfile = false;
        isDivider = false;
        isOption = true;
    }

    /**
     * All / not used now
     */
	public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}

	public String getMoreData() {
		return moreData;
	}

	public void setMoreData(String moreData) {
		this.moreData = moreData;
	}

	public boolean isEmpty() {
		return title.length() == 0;
	}
	
	public String getTitle(){
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIcon(){
		return this.icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getCount(){
        return this.count;
	}

	public void setCount(String count) {
		this.count = count;
		isCounterVisible = !count.equals("0");
	}
	
    public boolean isProfile() {
        return this.isProfile;
    }

    public boolean isDivider() {
        return isDivider;
    }

    public boolean isOption() {
        return isOption;
    }

	public boolean getCounterVisibility() {
		return this.isCounterVisible;
	}
	
	public void setCounterVisibility(boolean isCounterVisible){
		this.isCounterVisible = isCounterVisible;
	}
}