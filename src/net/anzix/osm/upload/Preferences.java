package net.anzix.osm.upload;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity {

    public static final String OSM_PASSWORD = "osm_password";
    public static final String OSM_USER_NAME = "osm_user_name";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        // Inline preferences 
        PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
        inlinePrefCat.setTitle("OSM credentials");
        root.addPreference(inlinePrefCat);

        // Edit text preference
        EditTextPreference userName = new EditTextPreference(this);
        userName.setDialogTitle("OSM user name");
        userName.setKey(OSM_USER_NAME);
        userName.setTitle("OSM user name");
        userName.setSummary("User name to use OSM API");
        inlinePrefCat.addPreference(userName);

        EditTextPreference password = new EditTextPreference(this);
        password.setDialogTitle("Password");
        password.setKey(OSM_PASSWORD);
        password.setTitle("OSM password");
        password.setSummary("Password to use OSM API");
        
        inlinePrefCat.addPreference(password);

      
        return root;
    
}
}
