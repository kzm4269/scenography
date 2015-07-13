package jp.plen.scenography.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jp.plen.scenography.R;
import jp.plen.scenography.models.PlenMotion;
import jp.plen.scenography.utils.PlenMotionJsonHelper;
import jp.plen.scenography.views.ProgramListView;
import jp.plen.scenography.views.adapter.PlenMotionListPagerAdapter;

/**
 * プログラム編集画面
 * Created by kzm4269 on 15/06/14.
 */
public class ProgrammingFragment extends Fragment {
    private static final String TAG = ProgrammingFragment.class.getSimpleName();
    private static final String STATE_CURRENT_MOTION_PAGE = "current_motion_page";
    private static final String STATE_PROGRAM_LIST_VIEW = "program_list_view";
    public static final String PREF_ROOT_DIRECTORY = "root_directory";
    public static final String PREF_CURRENT_FILE_PATH = "current_file_path";

    private ProgramListView mProgramListView;
    private ViewPager mMotionListPager;
    private File mProgramDirectory;
    private File mCurrentFile;

    public static ProgrammingFragment newInstance() {
        return new ProgrammingFragment();
    }

    public ProgrammingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_programming, container, false);

        // View生成
        mProgramListView = (ProgramListView) root.findViewById(R.id.program_list_view);
        mMotionListPager = (ViewPager) root.findViewById(R.id.motion_list_pager);

        // Toolbar
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.section_title_programming);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_save_program) {
                    saveProgram(new File(mProgramDirectory, "temp.json"));
                    openProgram(new File(mProgramDirectory, "temp.json"));
                } else if (id == R.id.action_delete_program) {
                    deleteProgram();
                }
                return true;
            }
        });

        // MotionListのsetup
        PlenMotionListPagerAdapter mMotionListPagerAdapter;
        try {
            LinkedHashMap<CharSequence, List<PlenMotion>> motions = PlenMotionJsonHelper.parseMotionList(
                    getActivity().getResources().openRawResource(R.raw.motion_list));
            List<CharSequence> motionGroups = new ArrayList<>(motions.keySet());
            mMotionListPagerAdapter = new PlenMotionListPagerAdapter(getActivity(), motionGroups, motions);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        mMotionListPager.setAdapter(mMotionListPagerAdapter);

        // プログラム保存用ディレクトリ
        File rootDir = getActivity().getExternalFilesDir(null);
        if (rootDir == null) throw new AssertionError();
        mProgramDirectory = new File(rootDir, "src");
        if (!mProgramDirectory.exists() && mProgramDirectory.mkdir()) {
            String message = "Cannot create - " + mProgramDirectory.getPath();
            Log.e(TAG, message);
            Log.e(TAG, mProgramDirectory.exists() + "");
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(ProgrammingFragment.class.getSimpleName(), Context.MODE_PRIVATE);
        preferences.edit()
                .putString(PREF_ROOT_DIRECTORY, rootDir.getAbsolutePath())
                .apply();

        // 状態復元
        onRestoreInstanceState(savedInstanceState != null ? savedInstanceState : getArguments());

        String path = preferences.getString(PREF_CURRENT_FILE_PATH,
                new File(mProgramDirectory, "program.json").getAbsolutePath());
        if (path == null) throw new AssertionError();
        mCurrentFile = new File(path);
        openProgram(mCurrentFile);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_MOTION_PAGE, mMotionListPager.getCurrentItem());
        outState.putParcelable(STATE_PROGRAM_LIST_VIEW, mProgramListView.onSaveInstanceState());
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.containsKey(STATE_CURRENT_MOTION_PAGE)) {
            mMotionListPager.setCurrentItem(savedInstanceState.getInt(STATE_CURRENT_MOTION_PAGE));
        }
        mProgramListView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_PROGRAM_LIST_VIEW));
    }

    private void saveProgram(File file) {
        try {
            PlenMotionJsonHelper.saveProgramList(file, mProgramListView.getList());
            Log.d(TAG, "save: " + file.getPath());
            for (PlenMotion motion: mProgramListView.getList()) {
                Log.d(TAG, motion.toString());
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getActivity(), "ファイルが作成できません - " + file.getPath(), Toast.LENGTH_LONG).show();
        }
    }

    private void openProgram(File file) {
        List<PlenMotion> list;
        try {
            list = PlenMotionJsonHelper.parseProgramList(file);
            Log.d(TAG, "open: " + file.getPath());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Not found - " + e.getMessage());
            return;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getActivity(), "ファイルが開けません - " + file.getPath(), Toast.LENGTH_LONG).show();
            return;
        }
        mProgramListView.setList(list);
        mCurrentFile = file;

        SharedPreferences preferences = getActivity()
                .getSharedPreferences(ProgrammingFragment.class.getSimpleName(), Context.MODE_PRIVATE);
        preferences.edit()
                .putString(PREF_CURRENT_FILE_PATH, mCurrentFile.getAbsolutePath())
                .apply();
    }

    private void deleteProgram() {
        mProgramListView.setList(new ArrayList<PlenMotion>());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveProgram(mCurrentFile);
    }
}
