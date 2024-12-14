package projet.gestionabsence.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;

public class ChangePasswordDialogFragment extends DialogFragment {

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final EditText oldPasswordInput = new EditText(getActivity());
        oldPasswordInput.setHint("Ancien mot de passe");

        final EditText newPasswordInput = new EditText(getActivity());
        newPasswordInput.setHint("Nouveau mot de passe");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(oldPasswordInput);
        layout.addView(newPasswordInput);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Changer le mot de passe")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    String oldPassword = oldPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                        if (getTargetFragment() instanceof SettingsFragment) {
                            ((SettingsFragment) getTargetFragment()).changePassword(oldPassword, newPassword);
                        }
                    }
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.cancel())
                .create();
    }
}
