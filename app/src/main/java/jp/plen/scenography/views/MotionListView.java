package jp.plen.scenography.views;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;

/**
 * モーションリスト
 * Created by kzm4269 on 15/06/14.
 */
public class MotionListView extends ListView {

    public MotionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        OnItemLongClickListener listener = new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != INVALID_POSITION) {
                    View child = getChildAt(position - getFirstVisiblePosition());
                    Intent intent = new Intent().putExtra("motion_list", (Serializable) getAdapter().getItem(position));
                    ClipData data = ClipData.newIntent("intent", intent);
                    child.startDrag(data, new DragShadowBuilder(child), null, 0);
                }
                return true;
            }
        };
        setOnItemLongClickListener(listener);
    }

    public MotionListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MotionListView(Context context) {
        this(context, null);
    }
}
