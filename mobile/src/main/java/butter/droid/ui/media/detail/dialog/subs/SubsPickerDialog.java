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

package butter.droid.ui.media.detail.dialog.subs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butter.droid.R;
import butter.droid.ui.media.detail.model.UiSubItem;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubsPickerDialog extends BottomSheetDialogFragment {

    private static final String ARG_ITEMS = "butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog.items";

    @BindView(R.id.items_layout) LinearLayout itemsLayout;

    @NonNull @Override public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new CustomWidthBottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_subs, container, false);
    }

    @Override public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        SubsPickerCallback callback;
        Fragment parentFragment = getParentFragment();
        FragmentActivity activity = getActivity();
        if (parentFragment != null && parentFragment instanceof SubsPickerCallback) {
            callback = (SubsPickerCallback) parentFragment;
        } else if (activity != null && activity instanceof SubsPickerCallback) {
            callback = (SubsPickerCallback) activity;
        } else {
            throw new IllegalStateException("Parent has to implement SubsPickerCallback");
        }

        SubsPickerAdapter subsPickerAdapter = new SubsPickerAdapter(itemsLayout, callback);

        ArrayList<UiSubItem> items = getArguments().getParcelableArrayList(ARG_ITEMS);
        subsPickerAdapter.setData(items);
    }

    static class CustomWidthBottomSheetDialog extends BottomSheetDialog {

        public CustomWidthBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int width = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
            getWindow().setLayout(width > 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public static SubsPickerDialog newInstance(List<UiSubItem> items) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ITEMS, new ArrayList<>(items));

        SubsPickerDialog fragment = new SubsPickerDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public interface SubsPickerCallback {

        void onSubsItemSelected(int position, UiSubItem item);

    }

}
