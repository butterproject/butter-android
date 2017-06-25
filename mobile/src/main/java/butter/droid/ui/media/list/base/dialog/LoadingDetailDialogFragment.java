/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.media.list.base.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.provider.base.module.Media;
import java.util.ArrayList;
import javax.inject.Inject;

public class LoadingDetailDialogFragment extends DialogFragment {

    public static final String ARG_MEDIA = "butter.droid.ui.media.list.base.dialog.butter.droid.ui.media.list.base.dialog.media_item";

    @Inject ProviderManager providerManager;

    private Callback callback;

    private Boolean savedInstanceState = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.Theme_Butter)).inflate(R.layout
                .fragment_loading_detail, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        this.savedInstanceState = false;
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Transparent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // TODO
        // providerManager.getCurrentMediaProvider().cancel();
    }

    @Override public void onAttach(final Context context) {
        super.onAttach(context);

        Fragment targetFragment = getTargetFragment();
        if (targetFragment == null) {
            throw new IllegalArgumentException("target fragment must be set");
        } else if (targetFragment instanceof Callback) {
            callback = (Callback) targetFragment;
        } else {
            throw new IllegalArgumentException("target fragment must implement callbacks");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Media> currentList = callback.getCurrentList();
        int position = getArguments().getInt(ARG_MEDIA);
        final Media media = currentList.get(position);
        // TODO
        /*providerManager.getCurrentMediaProvider().getDetail(currentList, position, new MediaProvider.Callback() {
                    @Override
                    public void onSuccess(MediaProvider.Filters filters, ArrayList<Media> items, boolean changed) {
                        if (!isAdded() || items.size() <= 0) {
                            return;
                        }

                        final Media item = items.get(0);
                        item.color = media.color;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onDetailLoadSuccess(item);
                                if (!LoadingDetailDialogFragment.this.savedInstanceState) {
                                    dismiss();
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailure(Exception ex) {
                        if (!ex.getMessage().equals("Canceled")) {
                            ex.printStackTrace();
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onDetailLoadFailure();
                                    if (!LoadingDetailDialogFragment.this.savedInstanceState) {
                                        dismiss();
                                    }
                                }
                            });
                        }
                    }
                }

        );*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        savedInstanceState = true;
    }

    public static LoadingDetailDialogFragment newInstance(Integer position) {
        Bundle args = new Bundle();
        args.putInt(ARG_MEDIA, position);

        LoadingDetailDialogFragment fragment = new LoadingDetailDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callback {
        void onDetailLoadFailure();

        void onDetailLoadSuccess(Media item);

        ArrayList<Media> getCurrentList();
    }


}
