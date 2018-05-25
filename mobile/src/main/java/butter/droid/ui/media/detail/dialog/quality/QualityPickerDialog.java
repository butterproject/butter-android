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

package butter.droid.ui.media.detail.dialog.quality;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import butter.droid.R;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.ui.media.detail.dialog.quality.model.UiQuality;
import butterknife.ButterKnife;

public class QualityPickerDialog extends BottomSheetDialogFragment {

    private static final String ARG_ITEMS = "butter.droid.ui.media.detail.dialog.subs.QualityPickerDialog.items";

    @NonNull @Override public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new CustomWidthBottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_quality, container, false);
    }

    @Override public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        QualityPickerCallback callback;
        Fragment parentFragment = getParentFragment();
        FragmentActivity activity = getActivity();
        if (parentFragment != null && parentFragment instanceof QualityPickerCallback) {
            callback = (QualityPickerCallback) parentFragment;
        } else if (activity != null && activity instanceof QualityPickerCallback) {
            callback = (QualityPickerCallback) activity;
        } else {
            throw new IllegalStateException("Parent has to implement SubsPickerCallback");
        }

        QualityPickerAdapter adapter = new QualityPickerAdapter(view.getContext());

        ArrayList<UiQuality> items = getArguments().getParcelableArrayList(ARG_ITEMS);
        adapter.setItems(items);

        RecyclerView recyclerView = view.findViewById(R.id.rv_items);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(view.getContext(),
                (view1, position) -> callback.onQualityItemSelected(position)));
    }

    static class CustomWidthBottomSheetDialog extends BottomSheetDialog {

        CustomWidthBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int width = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
            getWindow().setLayout(width > 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);


            setOnShowListener(dialog -> {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                behaviour.setSkipCollapsed(true);
            });
        }
    }

    public static QualityPickerDialog newInstance(ArrayList<UiQuality> items) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ITEMS, items);

        QualityPickerDialog fragment = new QualityPickerDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public interface QualityPickerCallback {

        void onQualityItemSelected(int position);

    }

}
