package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import share.fair.fairshare.Alert;
import share.fair.fairshare.R;
import share.fair.fairshare.User;
import share.fair.fairshare.activities.GroupActivity;

/**
 * User context menu dialog.
 * This dialog shows up when the user clicks and hold u user name in the group activity.
 */
public class UserContextMenuDialog extends DialogFragment {
    private User user;
    private double paid;
    private double share;

    public UserContextMenuDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("user", user);
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.user = (User) savedInstanceState.getSerializable("user");
        }

        View dialogLayout = inflater.inflate(R.layout.context_menu_user, container);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().setTitle(user.getUserName());

        //==================================== First Item ==========================================
        final EditText etInput = (EditText) dialogLayout.findViewById(R.id.user_context_menu_et_input);
        etInput.setHint("Amount paid");
        final Button btnDone = (Button) dialogLayout.findViewById(R.id.user_context_menu_btn_done);
        final Button btnNext = (Button) dialogLayout.findViewById(R.id.user_context_menu_btn_next);
        btnNext.setEnabled(false);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etInput.getText().toString().length() > 0) {
                    btnNext.setEnabled(true);
                } else {
                    btnNext.setEnabled(false);
                }
            }

        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                btnDone.setVisibility(View.VISIBLE);
                paid = etInput.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(etInput.getText().toString());
                etInput.setText("");
                etInput.setHint("User's share");
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share = etInput.getText().toString().isEmpty() ? Double.NaN : Double.parseDouble(etInput.getText().toString());
                InputMethodManager imm = (InputMethodManager) getDialog().getContext().getSystemService(getDialog().getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
                ((GroupActivity) getActivity()).payForAll(user, paid, share);
                getDialog().dismiss();
            }
        });
        //==================================== Second Item ==========================================
        Button notifyMe = (Button) dialogLayout.findViewById(R.id.user_context_menu_btn_notify_me);
        //if user is already notified, remove him from the list. else, add him to the list:
        if (user.isNotified()) {
            notifyMe.setText("Remove notifications");
            notifyMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get the list of notified users and remove the user's ID from the list:
                    List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                    for (Alert.NotifiedId notifiedId : notifiedIds) {
                        if (notifiedId.userId.equals(user.getUserId())) {
                            notifiedId.delete();
                            break;
                        }
                    }
                    user.setIsNotified(false);
                    user.save();
                    ((GroupActivity) getActivity()).notifyUserListChanged();
                    getDialog().dismiss();
                }
            });
        } else {
            notifyMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Alert.NotifiedId notifiedId = new Alert.NotifiedId(user.getUserId());
                    notifiedId.save();
                    user.setIsNotified(true);
                    user.save();
                    ((GroupActivity) getActivity()).notifyUserListChanged();
                    getDialog().dismiss();
                }
            });
        }

        //======================================Delete user =================================================
        Button btnDelete = (Button) dialogLayout.findViewById(R.id.user_context_menu_btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GroupActivity) getActivity()).removeUser(user);
                dismiss();
            }
        });
        return dialogLayout;
    }
}
