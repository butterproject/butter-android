package pct.droid.tv.presenters;

import android.content.Context;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import pct.droid.tv.R;

public class MoreCardView extends BaseCardView implements View.OnFocusChangeListener {

    private Palette.Swatch mCustomSelectedSwatch;

    private Target mTarget;

    @Bind(R.id.main_image)
    ImageView imageView;

    @Bind(R.id.title_text)
    TextView titleTextView;

    @Bind(R.id.info_field)
    RelativeLayout infoAreaView;

    public MoreCardView(Context context) {
        this(context, null);
    }

    public MoreCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.Widget_Leanback_MoreCardViewStyle);
    }

    public MoreCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.more_card_view, this);

        ButterKnife.bind(this, v);

        setBackgroundResource(R.color.default_background);
        setCardType(BaseCardView.CARD_TYPE_INFO_UNDER);
        setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnFocusChangeListener(this);
    }


    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    /**
     * Sets the title text.
     */
    public void setTitleText(CharSequence text) {
        if (titleTextView == null) {
            return;
        }

        titleTextView.setText(text);
    }

    /**
     * Returns the title text.
     */
    public CharSequence getTitleText() {
        if (titleTextView == null) {
            return null;
        }

        return titleTextView.getText();
    }


    public void setImageResource(int imageResource) {
        imageView.setImageResource(imageResource);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setBackgroundResource(R.color.primary_dark);
        } else {
            setBackgroundResource(R.color.default_background);
        }
    }
}
