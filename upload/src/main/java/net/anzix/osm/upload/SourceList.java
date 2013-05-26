package net.anzix.osm.upload;

import android.widget.AdapterView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;
import net.anzix.osm.upload.data.Source;
import net.anzix.osm.upload.data.SourceDao;
import net.anzix.osm.upload.source.SourceHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceList extends SherlockListActivity {

    public static final int DELETE_ID = 1;

    List<Item> sources = new ArrayList<Item>();

    CustomAdapter adapter;

    GpxUploadApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GpxUploadApplication) getApplication();
        setContentView(R.layout.source_list);

        setListAdapter(adapter = new CustomAdapter<Item>(this, sources) {
            protected int getItemLayout() {
                return R.layout.source_item;
            }

            protected void fillView(Item custom, ViewHolder holder) {
                holder.item1.setText(custom.handler.getName());
                holder.item2.setText("Location: " + GpxUploadApplication.getDisplayPath(custom.source.getLocation()));
            }

            protected void cacheViews(ViewHolder holder, View v) {
                holder.item1 = (TextView) v.findViewById(R.id.name);
                holder.item2 = (TextView) v.findViewById(R.id.description);
            }
        });
        fillData();
        registerForContextMenu(getListView());

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        switch (item.getItemId()) {
            case DELETE_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Log.e("OSM", "selected: " + info.id);
                Source s = sources.get((int) info.id).source;
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
        getSupportMenuInflater().inflate(R.menu.sourcelist_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (!super.onMenuItemSelected(featureId, item)) {
            switch (item.getItemId()) {
                case R.id.newsource:
                    Intent i = new Intent(this, SourceChooser.class);
                    startActivityForResult(i, 0);
                    return true;

            }
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillData();
        adapter.notifyDataSetChanged();
    }

    private void fillData() {
        GpxUploadApplication app = (GpxUploadApplication) getApplication();
        SourceDao dao = app.getDaoSession().getSourceDao();
        adapter.clear();
        for (Source s : dao.queryBuilder().list()) {
            adapter.add(new Item(s, app.getSourceHandle(s.getType())));
        }

        adapter.notifyDataSetChanged();

    }

    private class Item {
        public Source source;
        public SourceHandler handler;

        private Item(Source source, SourceHandler handler) {
            this.source = source;
            this.handler = handler;
        }
    }
}
