package jp.plen.scenography.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import jp.plen.scenography.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.plen.plenconnect.ble.BLEDevice;
//import jp.plen.plenconnect.ble.dummy.BLEDevice;
import jp.plen.scenography.models.PlenMotion;
import jp.plen.scenography.utils.PlenMotionJsonHelper;

/**
 * 送信画面
 * Created by kzm4269 on 15/06/21.
 */
public class ConnectionFragment extends Fragment {
    private static final String TAG = ConnectionFragment.class.getSimpleName();
    private EditText mEditText;
    private BLEDevice mDevice;

    private BLEDevice.BLECallbacks mBleCallbacks = new BLEDevice.BLECallbacks() {
        @Override
        public void onConnected(String deviceName) {
            Toast.makeText(getActivity(), deviceName + "に接続しました", Toast.LENGTH_LONG).show();
        }
    };

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ConnectionFragment newInstance() {
        ConnectionFragment fragment = new ConnectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ConnectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_connection, container, false);
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.section_title_connecting);
        mEditText = (EditText) root.findViewById(R.id.edit_text);
        showProgram();
        Button button = (Button) root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeProgram();
            }
        });
        mDevice = new BLEDevice(getActivity());
        mDevice.setBLECallbacks(mBleCallbacks);
        return root;
    }

    private List<String> openProgram() {
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(ProgrammingFragment.class.getSimpleName(), Context.MODE_PRIVATE);
        String path = preferences.getString(ProgrammingFragment.PREF_CURRENT_FILE_PATH, null);
        Log.d(TAG, "path: " + String.valueOf(path));
        if (path == null) return new ArrayList<>();

        List<PlenMotion> motions;
        try {
            motions = PlenMotionJsonHelper.parseProgramList(new File(path));
        } catch (IOException e) {
            String message = "Cannot open - " + path;
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, message);
            return new ArrayList<>();
        }

        List<String> program = new ArrayList<>();
        for (PlenMotion motion : motions) {
            program.add(String.format("#SC%02X%02X", motion.getNumber(), motion.getLoopCount()));
        }
        program.add("$CR");
        return program;
    }

    private void showProgram() {
        StringBuilder builder = new StringBuilder();
        for (String command : openProgram()) {
            builder.append(command);
            builder.append("\n");
        }
        mEditText.setText(builder.toString());
    }

    private void writeProgram() {
        List<String> program = openProgram();
        for (String command : program) {
            mDevice.write(command);
            Log.d(TAG, "send: " + command);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDevice.close();
    }
}
