package net.anzix.osm.upload;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.TextView;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;

import java.io.File;
import java.util.Date;
import java.util.List;


public class GpxList extends ListActivity {
    public static final int UPLOAD_ID = 1;
    List<Gpx> gpxes;
    CustomAdapter adapter;
    GpxUploadApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GpxUploadApplication) getApplication();
        app.sync();
        setContentView(R.layout.gpx_list);

        fillData();

        registerForContextMenu(getListView());


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, UPLOAD_ID, 0, "Upload");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.gpxlist_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (app.syncNeeded) {
            app.sync();
            fillData();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == Window.FEATURE_CONTEXT_MENU) {
            switch (item.getItemId()) {
                case UPLOAD_ID:
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    Log.e("OSM", "selected: " + info.id);
                    Gpx gpx = gpxes.get((int) info.id);
                    Intent i = new Intent(this, UploadForm.class);
                    i.putExtra("id", gpx.getId());
                    startActivityForResult(i, 0);
                    return true;

            }
        } else if (featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id.delete_all:
                    app.getDaoSession().getGpxDao().deleteAll();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    return true;
                case R.id.sources:
                    startActivity(new Intent(this, SourceList.class));
                    return true;
                case R.id.preferences:
                    startActivity(new Intent(this, Preferences.class));
                    return true;
                case R.id.upload:
                    startActivity(new Intent(this, UploadForm.class));
                    return true;
            }
        }
        return false;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillData();
        adapter.notifyDataSetChanged();
    }*/

    private void fillData() {

        GpxDao dao = app.getDaoSession().getGpxDao();
        gpxes = dao.queryBuilder().orderDesc(GpxDao.Properties.Created).orderAsc(GpxDao.Properties.Created).list();
        setListAdapter(adapter = new CustomAdapter<Gpx>(this, R.layout.gpx_list, gpxes) {
            protected int getItemLayout() {
                return R.layout.gpx_item;
            }

            protected void fillView(Gpx custom, ViewHolder holder) {
                String loc = custom.getLocation();
                String name = loc.substring(loc.lastIndexOf("/") + 1);
                holder.item1.setText(name);
                if (custom.getUploaded() != null) {
                    holder.item1.setTextColor(Color.GREEN);
                    holder.item3.setVisibility(View.VISIBLE);
                } else {
                    holder.item1.setTextColor(defaultColor);
                    holder.item3.setVisibility(View.INVISIBLE);
                }
                GpxUploadApplication app = (GpxUploadApplication) getApplication();
                holder.item2.setText(app.getSourceHandle(custom.getType()).getDisplayableLocation(custom));
            }

            protected void cacheViews(ViewHolder holder, View v) {
                holder.item1 = (TextView) v.findViewById(R.id.name);
                holder.item2 = (TextView) v.findViewById(R.id.description);
                holder.item3 = (TextView) v.findViewById(R.id.uploaded);
            }
        });
    }


}
