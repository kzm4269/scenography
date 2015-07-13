package jp.plen.scenography;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import jp.plen.scenography.fragments.ConnectionFragment;
import jp.plen.scenography.fragments.NavigationDrawerFragment;
import jp.plen.scenography.fragments.ProgrammingFragment;

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.Callbacks {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String STATE_CURRENT_SECTION_NUMBER = "current page";
    private static final String STATE_FRAGMENT_STATES = "fragment_states";
    private String[] mSectionTitles;
    private Bundle mFragmentStates = new Bundle();

    private int mCurrentSectionNumber = 0;
    private Fragment mCurrentSectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 各ページのタイトル
        mSectionTitles = new String[]{
                getString(R.string.section_title_programming),
                getString(R.string.section_title_connecting)};

        // 状態復元
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // NavigationDrawerの設定
        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                mSectionTitles,
                mCurrentSectionNumber);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mFragmentStates = savedInstanceState.getBundle(STATE_FRAGMENT_STATES);
        mCurrentSectionNumber = savedInstanceState.getInt(STATE_CURRENT_SECTION_NUMBER);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        onSaveCurrentSectionState();

        String title = mSectionTitles[position];

        if (title.equals(getString(R.string.section_title_programming))) {
            mCurrentSectionFragment = ProgrammingFragment.newInstance();
        } else if (title.equals(getString(R.string.section_title_connecting))) {
            mCurrentSectionFragment = ConnectionFragment.newInstance();
        } else {
            throw new AssertionError();
        }

        mCurrentSectionNumber = position;
        mCurrentSectionFragment.setArguments(
                mFragmentStates.getBundle(title));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mCurrentSectionFragment)
                .commit();
    }

    private void onSaveCurrentSectionState() {
        if (mCurrentSectionFragment == null) {
            return;
        }
        Bundle state = new Bundle();
        mCurrentSectionFragment.onSaveInstanceState(state);
        mFragmentStates.putBundle(mSectionTitles[mCurrentSectionNumber], state);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveCurrentSectionState();

        outState.putBundle(STATE_FRAGMENT_STATES, mFragmentStates);
        outState.putInt(STATE_CURRENT_SECTION_NUMBER, mCurrentSectionNumber);
    }
}