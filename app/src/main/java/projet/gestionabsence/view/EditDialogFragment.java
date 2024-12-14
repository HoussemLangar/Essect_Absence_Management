package projet.gestionabsence.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class EditDialogFragment extends DialogFragment {

    private static final String ARG_FIELD_TYPE = "fieldType";
    private static final String ARG_CURRENT_VALUE = "currentValue";
    private static final String ARG_FIELD = "field";

    private String fieldType;
    private String currentValue;
    private String field;

    public static EditDialogFragment newInstance(String fieldType, String currentValue, String field) {
        EditDialogFragment fragment = new EditDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FIELD_TYPE, fieldType);
        args.putString(ARG_CURRENT_VALUE, currentValue);
        args.putString(ARG_FIELD, field);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldType = getArguments().getString(ARG_FIELD_TYPE);
            currentValue = getArguments().getString(ARG_CURRENT_VALUE);
            field = getArguments().getString(ARG_FIELD);
        }
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final EditText input = new EditText(getActivity());
        input.setText(currentValue);

        if ("email".equals(field)) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else if ("phone".equals(field)) {
            input.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("Modifier " + fieldType)
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        if (getTargetFragment() instanceof SettingsFragment) {
                            ((SettingsFragment) getTargetFragment()).updateUserData(field, newValue);
                        }
                    }
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.cancel())
                .create();
    }
}
