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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.model.UiSubItem;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;

public class SubsPickerDialog extends BottomSheetDialogFragment implements SubsPickerView {

    private static final String ARG_MEDIA = "butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog.media";
    private static final String ARG_SELECTED = "butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog.selected";

    @Inject SubsPickerPresenter presenter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private SubsPickerAdapter subsAdapter;

    @NonNull @Override public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new CustomWidthBottomSheetDialog(requireContext(), getTheme());
    }

    @Override public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_subs, container, false);
    }

    @Override public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);


        recyclerView = view.findViewById(R.id.rv_items);
        progressBar = view.findViewById(R.id.progress_indicator);

        final SubsPickerAdapter adapter = new SubsPickerAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        subsAdapter = adapter;

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(view.getContext(),
                (view1, position) -> presenter.onSubsItemSelected(adapter.getItem(position))));

        Bundle arguments = getArguments();
        presenter.onViewCreated(arguments.getParcelable(ARG_MEDIA), arguments.getParcelable(ARG_SELECTED));
    }

    @Override public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override public void showSubtitles(List<UiSubItem> subs) {
        subsAdapter.setItems(subs);

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        FrameLayout bottomSheet = getDialog().findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior<FrameLayout> behaviour = BottomSheetBehavior.from(bottomSheet);
        behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        behaviour.setSkipCollapsed(true);
    }

    @Override public void selfClose() {
        dismiss();
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
        }
    }

    public static SubsPickerDialog newInstance(@NonNull MediaWrapper mediaWrapper, @Nullable Subtitle selected) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEDIA, mediaWrapper);
        args.putParcelable(ARG_SELECTED, selected);

        SubsPickerDialog fragment = new SubsPickerDialog();
        fragment.setArguments(args);

        return fragment;
    }

}
