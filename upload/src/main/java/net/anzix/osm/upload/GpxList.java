package net.anzix.osm.upload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;

import java.util.List;


public class GpxList extends SherlockListActivity {
    public static final int UPLOAD_ID = 1;
    List<Gpx> gpxes;
    CustomCheckableAdapter adapter;
    GpxUploadApplication app;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GpxUploadApplication) getApplication();
        app.sync();
        setContentView(R.layout.gpx_list);

        fillData();

        registerForContextMenu(getListView());

        receiver = new InsertReceiver();
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_INSERTED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.toggleChecked(i);
                Log.d("osm", "toggle checked " + i + " " + l);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private class InsertReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            fillData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.gpxlist_menu, menu);
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
//            switch (item.getItemId()) {
//                case UPLOAD_ID:
//                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//                    Log.e("OSM", "selected: " + info.id);
//                    Gpx gpx = gpxes.get((int) info.id);
//                    Intent i = new Intent(this, UploadForm.class);
//                    i.putExtra("id", gpx.getId());
//                    startActivityForResult(i, 0);
//                    return true;
//
//            }
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
                    Intent i = new Intent(this, UploadForm.class);
                    if (adapter.getCheckedItems().size() > 0) {
                        int idx = (Integer) adapter.getCheckedItems().iterator().next();
                        Gpx gpx = (Gpx) adapter.getItem(idx);
                        i.putExtra("id", gpx.getId());
                    }
                    startActivity(i);
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
        setListAdapter(adapter = new CustomCheckableAdapter<Gpx>(this, gpxes) {
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
