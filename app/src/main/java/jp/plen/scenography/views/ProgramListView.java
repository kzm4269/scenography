package jp.plen.scenography.views;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.plen.scenography.R;
import jp.plen.scenography.models.PlenMotion;
import jp.plen.scenography.views.adapter.ProgramListAdapter;

/**
 * プログラムView
 * Created by kzm4269 on 15/06/14.
 */
public class ProgramListView extends ListView {
    private static final String TAG = ProgramListView.class.getSimpleName();
    private final ProgramListAdapter mAdapter;
    private final List<PlenMotion> mPlenMotionList = new ArrayList<>();
    private final PlenMotion mInvisibleMotion = new PlenMotion(-1, "", "");

    private Runnable mLongClickCallback;

    public ProgramListView(Context context) {
        this(context, null);
    }

    public ProgramListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgramListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAdapter = new ProgramListAdapter(getContext(), mPlenMotionList);
        setAdapter(mAdapter);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        int position = pointToPosition(x, y);

        if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
            mPlenMotionList.add(mInvisibleMotion);
            mAdapter.notifyDataSetChanged();
        } else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
            // ドロップ先に空白行
            if (position >= 0) {
                View view = getChildAt(position - getFirstVisiblePosition());
                double viewCenter = view.getY() + view.getHeight() / 2.;
                if (Math.abs(y - viewCenter) < 10) {
                    mPlenMotionList.remove(mInvisibleMotion);
                    mPlenMotionList.add(position, mInvisibleMotion);
                    mAdapter.notifyDataSetChanged();
                }
            }
            // 自動スクロール
            int firstPosition = getFirstVisiblePosition();
            if (position == firstPosition) {
                smoothScrollToPosition(firstPosition - 1);
            }
            int lastPosition = getLastVisiblePosition();
            if (position == lastPosition) {
                smoothScrollToPosition(lastPosition + 1);
            }
        } else if (event.getAction() == DragEvent.ACTION_DROP) {
            int to = mPlenMotionList.indexOf(mInvisibleMotion);
            mPlenMotionList.remove(mInvisibleMotion);

            ClipData data = event.getClipData();
            for (int i = 0; i < data.getItemCount(); i++) {
                Intent intent = data.getItemAt(i).getIntent();
                PlenMotion motion = (PlenMotion) intent.getSerializableExtra("motion_list");
                mPlenMotionList.add(to, motion);
            }
            mAdapter.notifyDataSetChanged();
        }

        if (event.getAction() == DragEvent.ACTION_DRAG_ENDED ||
                event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
            mPlenMotionList.remove(mInvisibleMotion);
            mAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public List<PlenMotion> getList() {
        return mPlenMotionList;
    }

    public void setList(List<PlenMotion> motions) {
        mPlenMotionList.clear();
        mPlenMotionList.addAll(motions);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_state", super.onSaveInstanceState());
        PlenMotion[] program = mPlenMotionList.toArray(new PlenMotion[mPlenMotionList.size()]);
        bundle.putSerializable("program", program);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle)) {
            return;
        }
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable("super_state"));
        Serializable program = bundle.getSerializable("program");
        if (program instanceof PlenMotion[]) {
            mPlenMotionList.clear();
            mPlenMotionList.addAll(Arrays.asList((PlenMotion[]) program));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Log.d(TAG, event.toString());
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
                        Intent intent = new Intent().putExtra("motion_list", mAdapter.getItem(position).clone());
                        ClipData data = ClipData.newIntent("intent", intent);
                        mPlenMotionList.remove(mAdapter.getItem(position));
                        mAdapter.notifyDataSetChanged();
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
