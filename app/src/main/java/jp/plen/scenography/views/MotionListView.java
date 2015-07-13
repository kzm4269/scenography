package jp.plen.scenography.views;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.io.Serializable;

import jp.plen.scenography.R;

/**
 * モーションリスト
 * Created by kzm4269 on 15/06/14.
 */
public class MotionListView extends ListView {

    private static final String TAG = MotionListView.class.getSimpleName();
    private Runnable mLongClickCallback;

    public MotionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MotionListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MotionListView(Context context) {
        this(context, null);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int position = getChildIndexFromPosition(x, y);
            if (position != INVALID_POSITION) {
                if (mLongClickCallback != null)
                    removeCallbacks(mLongClickCallback);

                final View target = getChildAt(position - getFirstVisiblePosition());
                mLongClickCallback = new Runnable() {
                    @Override
                    public void run() {
                        View child = getChildAt(position - getFirstVisiblePosition());
                        Intent intent = new Intent().putExtra("motion_list", (Serializable) getAdapter().getItem(position));
                        ClipData data = ClipData.newIntent("intent", intent);
                        if (child == target) {
                            child.setPressed(true);
                            child.startDrag(data, new DragShadowBuilder(child), null, 0);
                        }
                    }
                };
                postDelayed(mLongClickCallback, getContext().getResources().getInteger(R.integer.motion_list_long_press_msec));
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mLongClickCallback != null)
                removeCallbacks(mLongClickCallback);
            mLongClickCallback = null;
        }
        return super.onTouchEvent(event);
    }

    private int getChildIndexFromPosition(int x, int y) {
        int firstPosition = getFirstVisiblePosition();
        int lastPosition = getLastVisiblePosition();
        for (int position = firstPosition; position <= lastPosition; position++) {
            View child = getChildAt(position - firstPosition);
            if (child == null) continue;
            Rect rect = new Rect();
            child.getHitRect(rect);
            if (rect.contains(x, y))
                return position;
        }
        return INVALID_POSITION;
    }
}
