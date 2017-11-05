package butter.droid.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @deprecated Shoucl move to something like {@link OptionPreview}
 */
@Deprecated
public class OptionSelector extends LinearLayout {

    View view;
    @BindView(android.R.id.text1) TextView text;
    @BindView(android.R.id.icon) AppCompatImageView icon;

    private FragmentManager fragmentManager;
    private String[] data = new String[0];
    private int defaultOption = -1;
    private int title;
    private SelectorListener listener;

    public OptionSelector(Context context) {
        super(context);
    }

    public OptionSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, android.R.style.Widget_Button);
    }

    public OptionSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        setClickable(true);
        setFocusable(true);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.item_option_selector, this);
        ButterKnife.bind(this, view);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.OptionSelector, defStyle, 0);

        String str = attr.getString(R.styleable.OptionSelector_optionText);
        if (!TextUtils.isEmpty(str)) {
            text.setText(str);
            setContentDescription(str);
        }

        int res = attr.getResourceId(R.styleable.OptionSelector_optionIcon, R.mipmap.ic_launcher);
        icon.setImageResource(res);

        setOnClickListener(onClickListener);

        attr.recycle();
    }

    public void setText(String str) {
        text.setText(str);
    }

    public void setText(int strRes) {
        text.setText(strRes);
    }

    public void setTitle(int strRes) {
        title = strRes;
    }

    public void setIcon(int iconRes) {
        icon.setImageResource(iconRes);
    }

    public void setListener(SelectorListener listener) {
        this.listener = listener;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public void setDefault(int defaultOption) {
        this.defaultOption = defaultOption;
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fragmentManager == null) {
                return;
            }

            StringArraySelectorDialogFragment.showSingleChoice(fragmentManager, title, data, defaultOption,
                    (dialog, position) -> {
                        if (listener != null) {
                            listener.onSelectionChanged(position, data[position]);
                        }
                        defaultOption = position;
                        setText(data[position]);
                        dialog.dismiss();
                    }
            );
        }
    };

    public interface SelectorListener {
        void onSelectionChanged(int position, String value);
    }

}
