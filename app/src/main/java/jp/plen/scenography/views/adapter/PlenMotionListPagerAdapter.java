package jp.plen.scenography.views.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.plen.scenography.models.PlenMotion;
import jp.plen.scenography.views.MotionListView;

public class PlenMotionListPagerAdapter extends PagerAdapter {
    private final List<CharSequence> mTitles;
    private final Map<CharSequence, List<PlenMotion>> mMotionGroups;
    private final List<MotionListView> views;

    private Context mContext;

    public PlenMotionListPagerAdapter(Context context, List<CharSequence> titles, Map<CharSequence, List<PlenMotion>> items) {
        super();
        mTitles = titles;
        mMotionGroups = items;
        mContext = context;

        views = new ArrayList<>();
        for (int position = 0; position < getCount(); position++) {
            MotionListView listView = new MotionListView(mContext);

            List<PlenMotion> motions = mMotionGroups.get(mTitles.get(position));
            PlenMotionListAdapter adapter = new PlenMotionListAdapter(mContext, motions);
            listView.setAdapter(adapter);
            views.add(position, listView);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
