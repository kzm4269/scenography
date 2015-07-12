package jp.plen.scenography.views;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public ProgramListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAdapter = new ProgramListAdapter(getContext(), mPlenMotionList);
        setAdapter(mAdapter);

        OnItemLongClickListener listener = new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != INVALID_POSITION) {
                    View child = getChildAt(position - getFirstVisiblePosition());
                    Intent intent = new Intent().putExtra("motion_list", mAdapter.getItem(position).clone());
                    ClipData data = ClipData.newIntent("intent", intent);
                    mPlenMotionList.remove(mAdapter.getItem(position));
                    mAdapter.notifyDataSetChanged();
                    child.startDrag(data, new DragShadowBuilder(child), null, 0);
                }
                return true;
            }
        };
        setOnItemLongClickListener(listener);
    }

    public ProgramListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgramListView(Context context) {
        this(context, null);
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
}
