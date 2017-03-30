package butter.droid.base.ui.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import butter.droid.base.R;

public class DialogFactory {

    public enum Action {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    public interface ActionCallback {

        void onButtonClick(Dialog which, Action action);
    }

    private DialogFactory() {
        throw new AssertionError("No instances of this class");
    }

    public static AlertDialog createErrorFetchingYoutubeVideoDialog(final Context context, final ActionCallback callback) {
        return new Builder(context)
                .setTitle(R.string.comm_error)
                .setCancelable(false)
                .setMessage(R.string.comm_message)
                .setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onButtonClick(((Dialog) dialog), Action.POSITIVE);
                        }
                    }
                }).create();
    }

}
