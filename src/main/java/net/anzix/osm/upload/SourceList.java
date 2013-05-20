package net.anzix.osm.upload;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;
import net.anzix.osm.upload.data.Source;
import net.anzix.osm.upload.data.SourceDao;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SourceList extends ListActivity {

    public static final int DELETE_ID = 1;

    List<Source> sources;

    CustomAdapter adapter;

    GpxUploadApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GpxUploadApplication) getApplication();
        app.sync();
        setContentView(R.layout.source_list);
        fillData();
        setListAdapter(adapter = new CustomAdapter<Source>(this, R.layout.source_list, sources) {
            protected int getItemLayout() {
                return R.layout.source_item;
            }

            protected void fillView(Source custom, ViewHolder holder) {
                String loc = custom.getLocation();
                holder.item1.setText(custom.getType());
                holder.item2.setText(custom.getLocation());
            }

            protected void cacheViews(ViewHolder holder, View v) {
                holder.item1 = (TextView) v.findViewById(R.id.name);
                holder.item2 = (TextView) v.findViewById(R.id.description);
            }
        });
        registerForContextMenu(getListView());

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        switch (item.getItemId()) {
            case DELETE_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Log.e("OSM", "selected: " + info.id);
                Source s = sources.get((int) info.id);
                app.getDaoSession().getSourceDao().delete(s);
                adapter.remove(s);
                adapter.notifyDataSetChanged();
                app.syncNeeded = true;
                return true;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sourcelist_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (!super.onMenuItemSelected(featureId, item)) {
            switch (item.getItemId()) {
                case R.id.newsource:
                    Intent intent = new Intent(getBaseContext(), FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, "/sdcard");
                    intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                    intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                    startActivityForResult(intent, 1);
                    return true;

            }
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            File f = new File(filePath);
            if (!f.isDirectory()) {
                Toast.makeText(this, "It's a file and not a directory!", Toast.LENGTH_SHORT);
            }
            if (f.exists()) {
                Source s = new Source();
                s.setType("dir");
                String loc = f.getAbsolutePath();
                try {
                    loc = f.getCanonicalPath();
                } catch (IOException e) {
                    Log.e("OSM", "Can't get canonical path ", e);
                }
                s.setLocation(loc);
                app.getDaoSession().getSourceDao().insert(s);
                adapter.add(s);
                app.syncNeeded = true;
            }
        }
        fillData();
        adapter.notifyDataSetChanged();
    }

    private void fillData() {
        GpxUploadApplication app = (GpxUploadApplication) getApplication();
        SourceDao dao = app.getDaoSession().getSourceDao();
        sources = dao.queryBuilder().list();

    }
}
