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

package butter.droid.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butter.droid.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OptionPreview extends LinearLayout {

    View view;
    @BindView(android.R.id.text1) TextView text;
    @BindView(android.R.id.icon) AppCompatImageView icon;

    public OptionPreview(Context context) {
        this(context, null);
    }

    public OptionPreview(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.optionPreviewStyle);
    }

    public OptionPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.item_option_selector, this);
        ButterKnife.bind(this);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.OptionPreview, defStyle, 0);

        String str = attr.getString(R.styleable.OptionPreview_op_text);
        if (!TextUtils.isEmpty(str)) {
            text.setText(str);
            setContentDescription(str);
        }

        int res = attr.getResourceId(R.styleable.OptionPreview_op_icon, 0);
        if (res != 0) {
            icon.setImageResource(res);
        }

        attr.recycle();
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setText(String str) {
        text.setText(str);
    }

    public void setText(int strRes) {
        text.setText(strRes);
    }

    public void setIcon(int iconRes) {
        icon.setImageResource(iconRes);
    }

}
