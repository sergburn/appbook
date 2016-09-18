package com.burnevsky.appbook;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
{
    final static String TAG = "AppBook";

    final String[] mAppListColumns = {"icon", "label"};
    final int[] mAppListFields = {R.id.ali_imgAppIcon, R.id.ali_txtAppName};

    SimpleAdapter mAppListAdapter;
    List<SortedMap<String, Object>> mAppListData = new ArrayList<>();

    PackageManager mPackageManager;
    List<ResolveInfo> mAllApps = new ArrayList<>();

    GridView mAppGrid;

    public MainActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mPackageManager = getContext().getPackageManager();

        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> activities = mPackageManager.queryIntentActivityOptions(null, null, launcher, PackageManager.GET_RESOLVED_FILTER);

        for (ResolveInfo info: activities)
        {
            mAllApps.add(info);
        }

        mAppGrid = (GridView) getView().findViewById(R.id.gridAppIcons);
        mAppGrid.setNumColumns(4);

        mAppListAdapter = new SimpleAdapter(getContext(), mAppListData, R.layout.app_info_list_item, mAppListColumns, mAppListFields)
        {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                if (view != null)
                {
                    /*
                    TextView frame = (TextView) view.findViewById(R.id.tf_cover);
                    if (frame != null)
                    {
                        frame.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                onTranslationRatingClicked(position);
                            }
                        });
                    }
                    */
                }
                return view;
            }
        };

        mAppListAdapter.setViewBinder(new SimpleAdapter.ViewBinder()
        {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation)
            {
                if (view.getId() == R.id.ali_imgAppIcon)
                {
                    ((ImageView) view).setImageDrawable((Drawable) data);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        mAppGrid.setAdapter(mAppListAdapter);
        mAppGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = mPackageManager.getLaunchIntentForPackage(
                    mAllApps.get(position).activityInfo.applicationInfo.packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        });

        onAppNameFilterChanged("");
    }

    public void onAppNameFilterChanged(CharSequence appNameFilter)
    {
        mAppListData.clear();

        for (ResolveInfo appInfo : mAllApps)
        {
            TreeMap<String, Object> row = new TreeMap<>();
            row.put("label", appInfo.loadLabel(mPackageManager));
            row.put("icon", appInfo.loadIcon(mPackageManager));
            mAppListData.add(row);
        }

        mAppListAdapter.notifyDataSetChanged();
    }
}
