package wishcantw.vocabulazy.activities.player.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wishcantw.vocabulazy.R;
import wishcantw.vocabulazy.analytics.Analytics;
import wishcantw.vocabulazy.analytics.firebase.FirebaseManager;
import wishcantw.vocabulazy.activities.player.activity.PlayerActivity;
import wishcantw.vocabulazy.activities.player.model.PlayerModel;
import wishcantw.vocabulazy.activities.player.view.PlayerNewNoteDialogView;
import wishcantw.vocabulazy.widget.DialogFragmentNew;
import wishcantw.vocabulazy.widget.DialogViewNew;

/**
 * Created by SwallowChen on 9/6/16.
 */
public class PlayerNewNoteDialogFragment extends DialogFragmentNew implements DialogViewNew.OnYesOrNoClickListener, DialogViewNew.OnBackgroundClickListener {

    // call back interface
    public interface OnNewNoteDialogFinishListener {
        void onNewNoteDone(String string);
    }

    // layout resource id
    private static final int LAYOUT_RES_ID = R.layout.view_player_new_note_dialog;

    // views
    private PlayerNewNoteDialogView mPlayerNewNoteDialogView;

    // listeners
    private OnNewNoteDialogFinishListener mOnDialogFinishListener;

    /** Life cycles **/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPlayerNewNoteDialogView = (PlayerNewNoteDialogView) inflater.inflate(LAYOUT_RES_ID, container, false);
        mPlayerNewNoteDialogView.setOnYesOrNoClickListener(this);
        return mPlayerNewNoteDialogView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // send GA screen event
        FirebaseManager.getInstance().sendScreenEvent(Analytics.ScreenName.CREATE_NOTE);
    }

    /** Abstracts and Interfaces **/

    @Override
    protected String getGALabel() {
        return Analytics.ScreenName.CREATE_NOTE;
    }

    @Override
    public void onYesClick() {
        // get name of new note
        String newNoteString = mPlayerNewNoteDialogView.getNewNoteString();

        // access search model and add new note to database
        PlayerModel playerModel = ((PlayerActivity) getActivity()).getPlayerModel();
        playerModel.addNewNote(newNoteString);

        getActivity().onBackPressed();

        if (mOnDialogFinishListener != null) {
            mOnDialogFinishListener.onNewNoteDone(newNoteString);
        }
    }

    @Override
    public void onNoClick() {
        getActivity().onBackPressed();
    }

    @Override
    public void onBackgroundClick() {
        getActivity().onBackPressed();
    }

    /** Public methods **/

    public void setOnNewNoteDialogFinishListener(OnNewNoteDialogFinishListener listener) {
        mOnDialogFinishListener = listener;
    }
}
