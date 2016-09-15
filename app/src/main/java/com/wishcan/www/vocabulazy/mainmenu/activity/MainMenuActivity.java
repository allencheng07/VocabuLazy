package com.wishcan.www.vocabulazy.mainmenu.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.wishcan.www.vocabulazy.R;
import com.wishcan.www.vocabulazy.exam.activity.ExamActivity;
import com.wishcan.www.vocabulazy.mainmenu.fragment.MainMenuFragment;
import com.wishcan.www.vocabulazy.mainmenu.model.MainMenuModel;
import com.wishcan.www.vocabulazy.mainmenu.note.fragment.NoteCreateDialogFragment;
import com.wishcan.www.vocabulazy.mainmenu.note.fragment.NoteDeleteDialogFragment;
import com.wishcan.www.vocabulazy.mainmenu.note.fragment.NoteRenameDialogFragment;
import com.wishcan.www.vocabulazy.mainmenu.note.view.NoteDeleteDialogView;
import com.wishcan.www.vocabulazy.player.activity.PlayerActivity;
import com.wishcan.www.vocabulazy.search.activity.SearchActivity;
import com.wishcan.www.vocabulazy.service.AudioService;
import com.wishcan.www.vocabulazy.storage.Database;

public class MainMenuActivity extends AppCompatActivity implements MainMenuFragment.OnMainMenuEventListener, NoteRenameDialogFragment.OnRenameCompleteListener, NoteDeleteDialogFragment.OnNoteDeleteListener, NoteCreateDialogFragment.OnNoteCreateListener {

    public static final String TAG = "MainMenuActivity";

    private MainMenuFragment mMainMenuFragment;
    private MainMenuModel mMainMenuModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mMainMenuModel == null) {
            mMainMenuModel = new MainMenuModel(getApplicationContext());
        }

        startAudioService();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mMainMenuFragment = (MainMenuFragment) getSupportFragmentManager().findFragmentById(R.id.main_menu_fragment);
        mMainMenuFragment.addOnMainMenuEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Database.getInstance().writeToFile(getApplicationContext());
                return null;
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                /** TODO: for test only */
                Intent intent = new Intent(MainMenuActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_back_to_player:
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTextbookSelected(int bookIndex, int lessonIndex) {
        Intent intent = new Intent(MainMenuActivity.this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.ARG_BOOK_INDEX, bookIndex);
        intent.putExtra(PlayerActivity.ARG_LESSON_INDEX, lessonIndex);
        startActivity(intent);
    }

    @Override
    public void onNoteSelected(int noteIndex) {
        Intent intent = new Intent(MainMenuActivity.this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.ARG_BOOK_INDEX, -1);
        intent.putExtra(PlayerActivity.ARG_LESSON_INDEX, noteIndex);
        startActivity(intent);
    }

    @Override
    public void onNoteRename(int noteIndex, String originalName) {
        // add fragment to mainmenufragment
        NoteRenameDialogFragment fragment = new NoteRenameDialogFragment();
        fragment.addOnRenameCompleteListener(this);
        fragment.setNoteIndex(noteIndex);
        fragment.setOriginalString(originalName);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment, "NoteRenameDialogFragment");
        transaction.addToBackStack("NoteRenameDialogFragment");
        transaction.commit();
    }

    @Override
    public void onRenameCompleted() {
        Log.d(TAG, "Rename completed");
        mMainMenuFragment.updateFragmentsContent();
        mMainMenuFragment.refreshFragments();
    }

    @Override
    public void onNoteDelete(int noteIndex, String noteTitle) {
        NoteDeleteDialogFragment fragment = new NoteDeleteDialogFragment();
        fragment.addOnNoteDeleteListener(this);
        fragment.setNoteIndex(noteIndex);
        fragment.setNoteTitle(noteTitle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment, "NoteDeleteDialogFragment");
        transaction.addToBackStack("NoteDeleteDialogFragment");
        transaction.commit();
    }

    @Override
    public void onNoteDeleted() {
        Log.d(TAG, "Note deleted");
        mMainMenuFragment.updateFragmentsContent();
        mMainMenuFragment.refreshFragments();
    }

    @Override
    public void onNoteCreate() {
        NoteCreateDialogFragment fragment = new NoteCreateDialogFragment();
        fragment.addOnNoteCreateListener(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment, "NoteCreateDialogFragment");
        transaction.addToBackStack("NoteCreateDialogFragment");
        transaction.commit();
    }

    @Override
    public void onNoteCreated() {
        mMainMenuFragment.updateFragmentsContent();
        mMainMenuFragment.refreshFragments();
    }

    @Override
    public void onExamTextbookSelected(int examBookIndex, int examLessonIndex) {
        Intent intent = new Intent(MainMenuActivity.this, ExamActivity.class);
        intent.putExtra(ExamActivity.ARG_BOOK_INDEX, examBookIndex);
        intent.putExtra(ExamActivity.ARG_LESSON_INDEX, examLessonIndex);
        startActivity(intent);
    }

    @Override
    public void onExamNoteSelected(int examNoteIndex) {
        Intent intent = new Intent(MainMenuActivity.this, ExamActivity.class);
        intent.putExtra(ExamActivity.ARG_BOOK_INDEX, -1);
        intent.putExtra(ExamActivity.ARG_LESSON_INDEX, examNoteIndex);
        startActivity(intent);
    }

    public MainMenuModel getModel() {
        return mMainMenuModel;
    }

    private void startAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.START_SERVICE);
        startService(intent);
    }

    private void stopAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.STOP_SERVICE);
        startService(intent);
    }
}
