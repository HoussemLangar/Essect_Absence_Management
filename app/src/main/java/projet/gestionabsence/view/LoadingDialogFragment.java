package projet.gestionabsence.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import androidx.fragment.app.DialogFragment;

import projet.gestionabsence.R;

public class LoadingDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);

        return dialog;
    }
}
