package jp.plen.scenography.views.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import jp.plen.scenography.R;

import java.util.List;

import jp.plen.scenography.models.PlenMotion;

public class ProgramListAdapter extends PlenMotionListAdapter {
    private static final String TAG = ProgramListAdapter.class.getSimpleName();
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public ProgramListAdapter(Context context, List<PlenMotion> objects) {
        super(context, objects);
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 特定の行(position)のデータを得る
        final PlenMotion plenMotion = getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.item_program_list, null);

            final EditText loopCountEdit = (EditText) convertView.findViewById(R.id.loopCountEdit);
            final View view = convertView;
            loopCountEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    PlenMotion motion = (PlenMotion) view.getTag();
                    if ("".equals(s.toString())) {
                        motion.setLoopCount(Integer.parseInt(loopCountEdit.getHint().toString()));
                        return;
                    }

                    int input;
                    try {
                        input = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        loopCountEdit.setText("");
                        return;
                    }
                    int loopCount = Math.min(Math.max(1, input), 255);
                    if (loopCount != input) {
                        loopCountEdit.setText(Integer.toString(loopCount));
                        loopCountEdit.setSelection(Integer.toString(loopCount).length());
                    }
                    motion.setLoopCount(loopCount);
                }
            });
        }
        convertView.setTag(plenMotion);

        // CustomDataのデータをViewの各Widgetにセットする
        TextView nameView;
        nameView = (TextView) convertView.findViewById(R.id.motion_name_view);
        nameView.setText(plenMotion.getName());
        TextView numberView;
        numberView = (TextView) convertView.findViewById(R.id.motion_number_view);
        numberView.setText(String.format("%02X", plenMotion.getNumber()));
        ImageView iconView = (ImageView) convertView.findViewById(R.id.motion_icon_view);
        loadImageResource(iconView, plenMotion.getIconName());
        final EditText loopCountEdit = (EditText) convertView.findViewById(R.id.loopCountEdit);
        loopCountEdit.setText(Integer.toString(plenMotion.getLoopCount()));

        if (plenMotion.getNumber() < 0) {
            convertView.setAlpha(0);
        } else {
            convertView.setAlpha(1);
        }
        return convertView;
    }
}
