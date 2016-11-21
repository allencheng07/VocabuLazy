package wishcantw.vocabulazy.player.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wishcantw.vocabulazy.R;
import wishcantw.vocabulazy.database.object.OptionSettings;
import wishcantw.vocabulazy.player.activity.PlayerActivity;
import wishcantw.vocabulazy.player.model.PlayerModel;
import wishcantw.vocabulazy.player.view.PlayerOptionDialogView;
import wishcantw.vocabulazy.player.view.PlayerOptionView;
import wishcantw.vocabulazy.widget.DialogFragmentNew;

/**
 * Created by SwallowChen on 11/21/16.
 */

public class PlayerOptionDialogFragment extends DialogFragmentNew implements PlayerOptionDialogView.PlayerOptionEventListener {

    // layout resources
    private static final int LAYOUT_RES_ID = R.layout.view_player_option_dialog;

    // views
    private PlayerOptionDialogView mPlayerOptionDialogView;

    // Model
    private PlayerModel mPlayerModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPlayerOptionDialogView = (PlayerOptionDialogView) inflater.inflate(LAYOUT_RES_ID, container, false);
        mPlayerOptionDialogView.setPlayerOptionEventListener(this);
        return mPlayerOptionDialogView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPlayerModel = ((PlayerActivity) getActivity()).getPlayerModel();

        OptionSettings optionSettings = mPlayerModel.getPlayerOptionSettings();
        mPlayerOptionDialogView.setPlayerOptionModeContent(optionSettings, true);
    }

    @Override
    protected String getGALabel() {
        return null;
    }

    @Override
    public void onPlayerOptionChanged(int optionID, int mode, View v, int value) {
        if (optionID == PlayerOptionView.IDX_OPTION_MODE) {
            // The value is the mode option index that is selected
            int newMode = value;
            OptionSettings optionSettings = mPlayerModel.getOptionSettings().get(newMode);
            mPlayerOptionDialogView.setPlayerOptionModeContent(optionSettings, false);
        }
        // Refresh option settings
        mPlayerModel.updateOptionSettings(optionID, mode, v, value);
        // TODO : notify the service that option settings has changed
        // optionChanged();
    }
}
