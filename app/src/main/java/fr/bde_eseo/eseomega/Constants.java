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

package fr.bde_eseo.eseomega;

/**
 * Created by François on 16/04/2015.
 */
public class Constants {

    // Register all constants here
    // Try to get the writing order like this :
    // package.class.NAME
    public static final String PLAY_STORE_APP_ID = "com.android.vending";

    // Store app verification
    public final static String SIGNATURE = "mM6h5mqKyeuhXgBF8SLnMBKc7BE=";

    // Timezone : CET ou GMT+01:00
    // ID Timezone : Europe/Paris
    public static final String TZ_ID_PARIS = "Europe/Paris";

    // Developer mail
    public static final String MAIL_DIALOG = "bde@eseoasis.com";

    // Preferences
    public static final String PREFS_NEWS_KEY = "fragment.NewsFragment.KEY";
    public static final String PREFS_APP_WELCOME = "mainactivity.welcome";
    public static final String PREFS_APP_VERSION = "mainactivity.appversion";
    public static final String PREFS_NEWS_LAST_DOWNLOAD_DATE = "fragment.NewsFragment.LAST_DOWNLOAD_DATE";
    public static final String PREFS_APP_WELCOME_DATA = "mainactivity.welcome.DATA";
    public static final String PREFS_APP_LAST_VERSION = "tutorialactivity.alreadyshown";

    public static final String PREFS_USER_PROFILE_KEY = "model.UserProfile.KEY";
    public static final String PREFS_USER_PROFILE_NAME = "model.UserProfile.NAME";
    public static final String PREFS_USER_PROFILE_ID = "model.UserProfile.ID";
    public static final String PREFS_USER_PROFILE_PASSWORD = "model.UserProfile.PASSWORD";
    public static final String PREFS_USER_PROFILE_MAIL = "model.UserProfile.MAIL";
    public static final String PREFS_USER_PROFILE_EXISTS = "model.UserProfile.EXISTS";
    public static final String PREFS_USER_PROFILE_PICTURE = "model.UserProfile.PICTURE";
    //public static final String PREFS_USER_PROFILE_PUSH_TOKEN = "model.UserProfile.PUSHTOKEN";
    public static final String PREFS_USER_PROFILE_PUSH_TOKEN = "model.UserProfile.PUSHTOKEN_V3";
    //public static final String PREFS_USER_PROFILE_PHONE = "model.UserProfile.PHONE"; // On stocke pas !

    // SharedPref class
    public static final String PREFS_GENERAL_HOMESCREEN = "settings.general.homescreen";
    public static final String PREFS_GENERAL_THEME = "settings.general.theme";
    public static final String PREFS_GENERAL_UPDATE = "settings.general.autoupdate";
    public static final String PREFS_LYDIA_PHONE = "settings.lydia.phone";

    // Notifications
    public static final int NOTIF_GENERAL = 0;
    public static final int NOTIF_NEWS = 1;
    public static final int NOTIF_EVENTS = 2;
    public static final int NOTIF_CLUBS = 3;
    public static final int NOTIF_CAFET = 4;
    public static final int NOTIF_TIPS = 5;
    public static final int NOTIF_UPDATE = 21;
    public static final int NOTIF_CONNECT = 99;
    public static final double NOTIF_VERSION = 1.1; // Notification is valid if V_Push_App >= V_Push_Server
    public static final int NOTIF_UPDATE_FORCE = 147; // local only

    // Fragments ID
    public static final String TAG_FRAGMENT_ORDER_TABS = "fragment.tabs.order";

    // URL's


    //ESEOMEGA URLs
    public static final String JSON_NEWS_ID = "id";
    public static final String JSON_NEWS_TITLE = "title";
    public static final String JSON_NEWS_IMG = "img";
    public final static String JSON_NEWS_SHORTDATE = "date";
    public static final String JSON_NEWS_FULLDATE = "fulldate";
    public static final String JSON_NEWS_PREVIEW = "preview";
    public static final String JSON_NEWS_CONTENT = "content";
    public final static String JSON_EVENT_ID = "id";
    public final static String JSON_EVENT_NAME = "title";
    public final static String JSON_EVENT_DETAIL = "text";
    public final static String JSON_EVENT_DATE = "fulldate";
    public final static String JSON_EVENT_DATEFIN = "fullenddate";
    public final static String JSON_EVENT_SIGNUP = "signup";
    //ESEOASIS URLs
    public final static String JSON_EVENT_LIEU = "place";
    //JSON Reading
    public final static String JSON_EVENT_CLUB = "from";
    public final static String JSON_EVENT_URL = "adr";
    public final static String JSON_EVENT_IMGURL = "imgUrl";
    public final static String JSON_CLUB_ID = "id";
    public final static String JSON_CLUB_NAME = "name";
    public final static String JSON_CLUB_DETAIL = "description";
    public final static String JSON_CLUB_IMG = "img";
    public final static String JSON_CLUB_CONTACTS = "contacts";
    public final static String JSON_CLUB_CONTACTS_WEB = "web";
    public final static String JSON_CLUB_CONTACTS_FB = "fb";
    public final static String JSON_CLUB_CONTACTS_MAIL = "mail";
    public final static String JSON_CLUB_CONTACTS_TEL = "tel";
    public final static String JSON_CLUB_CONTACTS_TWIT = "twitter";
    public final static String JSON_CLUB_CONTACTS_LI = "linkedin";
    public final static String JSON_CLUB_CONTACTS_SNAP = "snap";
    public final static String JSON_CLUB_CONTACTS_INST = "instagram";
    public final static String JSON_CLUB_CONTACTS_YOU = "youtube";
    public final static String JSON_CLUB_BUREAU = "bureau";
    public final static String JSON_CLUB_RELATED = "related";
    public final static String JSON_CLUB_EVENTS = "events";
    public final static String JSON_CLUB_MEMBER_NAME = "name";
    public final static String JSON_CLUB_MEMBER_DETAIL = "role";
    public final static String JSON_CLUB_MEMBER_IMG = "img";
    public final static String JSON_SPONSO_NAME = "name";
    public final static String JSON_SPONSO_DETAIL = "description";
    public final static String JSON_SPONSO_IMG = "img";
    public final static String JSON_SPONSO_URL = "url";
    public final static String JSON_SPONSO_ADR = "address";
    public final static String JSON_SPONSO_AVANTAGES = "perks";
    public static final String JSON_ROOM_NAME = "name";
    public static final String JSON_ROOM_NUM = "num";
    public static final String JSON_ROOM_BAT = "bat";
    public final static String JSON_ROOM_FLO = "floor";
    public static final String JSON_ROOM_NFO = "info";

    public static final String JSON_STUDENT_ID = "id";
    public static final String JSON_STUDENT_NAME = "name";
    public static final String JSON_STUDENT_CHILDREN = "children";
    public static final String JSON_STUDENT_PARENTS = "parents";
    public static final String JSON_STUDENT_PROMO = "promo";
    public static final String JSON_STUDENT_RANK = "rank";
    //API

    public static final String POST_EVENT_SIGNUP_NAME = "name";
    public static final String POST_EVENT_SIGNUP_MAIL = "email";
    public static final String POST_EVENT_SIGNUP_TEL = "tel";
    // Errors
    public static final int ERROR_TIMESTAMP = 1;
    public static final int ERROR_USERREGISTER = 2;
    public static final int ERROR_SERVICE_OUT = 3;
    public static final int ERROR_UNPAID = 4;
    public static final int ERROR_APP_PB = 6;
    public static final int ERROR_USER_BAN = 7;
    //Family
    public static final int ERROR_BAD_VERSION = 8;
    public static final int ERROR_HOTSPOT = -2;
    public static final int ERROR_NETWORK = 0;
    // APP ID
    public static final String APP_ID = "ANDROID";
    // Bundled intents & data
    public static final String KEY_ELEMENT_ID = "lacommande.key.element.id";
    public static final String KEY_ORDER_ID = "lacommande.order_id";
    public static final String KEY_MENU_ID = "lacommande.key.menu.id";
    public static final String KEY_ELEMENT_POSITION = "lacommande.key.element.position";
    public static final String KEY_NEWS_JSON_DATA = "news.json.data";
    public static final String KEY_NEWS_TITLE = "news.title";
    public static final String KEY_NEWS_AUTHOR = "news.author";
    public static final String KEY_NEWS_LINK = "news.link";
    public static final String KEY_NEWS_IMGARRAY = "news.imgarray";
    public static final String KEY_NEWS_HTML = "news.html";
    public static final String KEY_NEWS_DATE = "news.date";
    public static final String KEY_IMG = "activity.wide.img";
    public static final String KEY_CLUB_VIEW = "clubs.viewitem";
    public static final String KEY_MAIN_INTENT = "main.intent.start";
    public static final String KEY_MAIN_TITLE = "main.intent.title";
    public static final String KEY_MAIN_MESSAGE = "main.intent.message";
    public static final String KEY_EVENT_TICKET_SELECTED = "event.ticket.selected";
    // Returns
    public static final int RESULT_LYDIA_KEY = 422;
    public static final int RESULT_SHUTTLES_KEY = 333;
    public static final String RESULT_LYDIA_VALUE = "lydia_return.value";
    public static final String RESULT_SHUTTLES_VALUE = "shuttles_return.value";
    public static final String RESULT_SHUTTLES_NAME = "shuttles_return.name";
    // LYDIA
    public static final String KEY_LYDIA_ORDER_ID = "lydia.order_id";
    public static final String KEY_LYDIA_ORDER_TYPE = "lydia.order_type";
    public static final String KEY_LYDIA_ORDER_ASKED = "lydia.order_status";
    public static final String TYPE_LYDIA_CAFET = "CAFET";
    public static final String TYPE_LYDIA_EVENT = "EVENT";
    public static final String URL_JSON_STUDENT_SEARCH = URL_JSON_FAMILY + "search/";
    public static final String URL_API_EVENT_SIGNUP = URL_JSON_EVENTS + "signup/";
    /**
     * API V3.0 - Since December 2015 ↔ January 2016
     */


    private static final String URL_SERVER2 = "https://web59.secure-secure.co.uk/francoisle.fr/"; // secure way
    public static final String URL_ASSETS = URL_SERVER2 + "lacommande/assets/";
    public static final String URL_JSON_INGENEWS = URL_SERVER2 + "eseonews/jsondata/ingenews_data/ingenews.php";
    private static final String URL_API = "https://api.eseoasis.com/";
    public static final String URL_JSON_APP_INFO = URL_API + "app_info";
    //News
    public static final String URL_JSON_NEWS = URL_API + "news?smtype=ANDROID&";
    public static final String URL_JSON_NEWS_SINGLE = URL_API + "news/";
    //Event
    public static final String URL_JSON_EVENTS = URL_API + "events/";
    //Clubs
    public static final String URL_JSON_CLUBS = URL_API + "clubs/";
    //Sponsos
    public static final String URL_JSON_SPONSORS = URL_API + "sponsors/";
    //Salles
    public static final String URL_JSON_PLANS = URL_API + "rooms/";
    public static final String URL_JSON_FAMILY = URL_API + "family/";
    // Base URL
    private static final String URL_API_BASE = URL_SERVER2 + "lacommande/api/";
    // Connexion
    public static final String URL_API_CLIENT_CONNECT = URL_API_BASE + "client/connect.php";
    // Push notifications
    public static final String URL_API_PUSH_REGISTER = URL_API_BASE + "push/register.php";
    public static final String URL_API_PUSH_UNREGISTER = URL_API_BASE + "push/unregister.php";
    // LaCommande
    public static final String URL_API_ORDER_ITEMS = URL_API_BASE + "order/items.php";
    public static final String URL_API_ORDER_LIST = URL_API_BASE + "order/list.php";
    public static final String URL_API_ORDER_RESUME = URL_API_BASE + "order/resume.php";
    public static final String URL_API_ORDER_PREPARE = URL_API_BASE + "order/prepare.php";
    public static final String URL_API_ORDER_SEND = URL_API_BASE + "order/send.php";
    // General & Specific Informations
    public static final String URL_API_INFO_SERVICE = URL_API_BASE + "info/service.php";
    public static final String URL_API_INFO_VERSION = URL_API_BASE + "info/version.php";
    // Lydia
    public static final String URL_API_LYDIA_ASK = URL_API_BASE + "lydia/ask.php";
    public static final String URL_API_LYDIA_CHECK = URL_API_BASE + "lydia/check.php";
    // Events
    public static final String URL_API_EVENT_ITEMS = URL_API_BASE + "event/items.php";
    public static final String URL_API_EVENT_LIST = URL_API_BASE + "event/list.php";
    public static final String URL_API_EVENT_PREPARE = URL_API_BASE + "event/prepare.php";
    public static final String URL_API_EVENT_SEND = URL_API_BASE + "event/send.php";
    public static final String URL_API_EVENT_MAIL = URL_API_BASE + "event/mail.php";

    // External Intents
    // public static final String EXTERNAL_INTENT_LYDIA_CAFET = "fr.bde_eseomega.eseomega.LYDIA_CAFET"; // Android VIEW now

}
