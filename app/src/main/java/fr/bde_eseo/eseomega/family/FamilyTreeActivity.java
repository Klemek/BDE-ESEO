package fr.bde_eseo.eseomega.family;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utils;

public class FamilyTreeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TreeView trv;
    private HashMap<Integer,StudentItem> family;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getPreferredTheme(getApplicationContext()));

        setContentView(R.layout.activity_family_tree);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);
        trv = (TreeView) findViewById(R.id.tree);
        trv.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        int id = getIntent().getIntExtra("id",-1);
        if(id == -1){
            finish();
        }
        new AsyncJSONFamily(id).execute(Constants.URL_JSON_FAMILY + id);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class AsyncJSONFamily extends AsyncTask<String, String, JSONArray> {

        private int markedid;

        public AsyncJSONFamily(int id){
            this.markedid = id;
        }

        @Override
        protected void onPreExecute() {
            family = new HashMap<>();
        }

        @Override
        protected void onPostExecute(JSONArray array) {
            if (array != null && array.length() > 0) {
                try {
                    // Get / add data
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int id = obj.getInt(Constants.JSON_STUDENT_ID);
                        family.put(id,new StudentItem(obj));
                        if(id == markedid){
                            family.get(id).mark();
                        }
                    }

                    trv.setFamily(family);
                    trv.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray array = JSONUtils.getJSONArrayFromUrl(params[0]);
            return array;
        }
    }
}
