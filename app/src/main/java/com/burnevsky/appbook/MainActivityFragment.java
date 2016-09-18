package com.burnevsky.appbook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
{
    final static String TAG = "AppBook";

    private static final String[] mAppListColumns = {"icon", "label"};
    private static final int[] mAppListFields = {R.id.ali_imgAppIcon, R.id.ali_txtAppName};

    private static final String[] TRAINER_KEYBOARD_LINES = {
            "qwertyuiopå",
            "asdfghjklöä",
            "__zxcvbnm__"
    };

    private static final int TRAINER_KEYBOARD_LINE_LENGTH = 11;
    private static final int TRAINER_KEYBOARD_LINES_NUM = TRAINER_KEYBOARD_LINES.length;

    SimpleAdapter mAppListAdapter;
    List<SortedMap<String, Object>> mAppListData = new ArrayList<>();

    PackageManager mPackageManager;
    List<ResolveInfo> mAllApps = new ArrayList<>();

    GridView mAppGrid;
    GridLayout mKeyboard;
    ArrayList<Button> mKeys = new ArrayList<>();
    Drawable mDefaultIcon;

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
        mDefaultIcon = mPackageManager.getDefaultActivityIcon();

        Intent launcher = new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);

        ComponentName me = new ComponentName(
            getActivity().getApplicationContext().getPackageName(),
            MainActivity.class.getName());

        List<ResolveInfo> activities = mPackageManager.queryIntentActivityOptions(
                me,
                null,
                launcher,
                PackageManager.GET_RESOLVED_FILTER);

        for (ResolveInfo info: activities)
        {
            mAllApps.add(info);
        }

        Collections.sort(mAllApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                return lhs.loadLabel(mPackageManager).toString().compareTo(
                        rhs.loadLabel(mPackageManager).toString());
            }
        });

        mKeyboard = (GridLayout) getView().findViewById(R.id.gridAlphabet);
        makeKeyboard(findAllCaps());

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

    private void makeKeyboard(CharSequence caps)
    {
        DisplayMetrics display = getResources().getDisplayMetrics();

        final int BUTTON_HEIGHT_DP = 42;
        final int BUTTON_WIDTH_DP = 30;
        final int BUTTON_ROW_GAP_DP = 10;

        final int BUTTON_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BUTTON_WIDTH_DP, display);

        final int pxWidth = Math.min(BUTTON_WIDTH, display.widthPixels / TRAINER_KEYBOARD_LINE_LENGTH); // no padding anywhere
        final int pxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BUTTON_HEIGHT_DP, display);

        int row = 0;
        int col = 0;
        GridLayout.Spec rowSpec = null;
        for (int c = 0; c < caps.length(); c++)
        {
            if (col == 0)
            {
                rowSpec = GridLayout.spec(row, 1.0f);
            }

            char cap = caps.charAt(c);
            if (Character.isLetter(cap))
            {
                GridLayout.Spec colSpec = GridLayout.spec(col, 1.0f);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                //params.bottomMargin = (r < TRAINER_KEYBOARD_LINES_NUM - 1) ? BUTTON_ROW_GAP_DP : (BUTTON_ROW_GAP_DP / 2);

                Button k = new Button(getActivity(), null, android.R.attr.buttonStyle);
                k.setMinHeight(pxHeight);
                k.setMinWidth(pxWidth);
                k.setMinimumHeight(pxHeight);
                k.setMinimumWidth(pxWidth);
                k.setPadding(0, 0, 0, 0);
                //k.setOnClickListener(mKeyBoardListener);
                k.setText(String.valueOf(cap));
                k.setTypeface(k.getTypeface(), 1); // bold
                k.setTextSize(22); // sp
                //k.setGravity(Gravity.CENTER);
                //k.setAllCaps(false);

                mKeyboard.addView(k, params);
                mKeys.add(k);
            }

            if (++col == TRAINER_KEYBOARD_LINE_LENGTH)
            {
                col = 0;
                row++;
            }
        }
    }

    public void onAppNameFilterChanged(CharSequence appNameFilter)
    {
        mAppListData.clear();

        for (ResolveInfo appInfo : mAllApps)
        {
            TreeMap<String, Object> row = new TreeMap<>();
            row.put("label", appInfo.loadLabel(mPackageManager));
            row.put("icon", appInfo.loadIcon(mPackageManager));
            //row.put("icon", mDefaultIcon);
            mAppListData.add(row);
        }

        mAppListAdapter.notifyDataSetChanged();
    }

    private String findAllCaps()
    {
        TreeSet<Character> caps = new TreeSet<>();

        for (ResolveInfo info: mAllApps)
        {
            CharSequence label = info.loadLabel(mPackageManager);
            caps.add(label.charAt(0));
        }

        StringBuilder capsString = new StringBuilder();
        for (Character c: caps)
        {
            capsString.append(c);
        }

        return capsString.toString();
    }
}
