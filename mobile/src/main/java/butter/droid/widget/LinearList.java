package butter.droid.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinearList extends LinearLayout {

    private Adapter adapter;
    private Observer observer = new Observer(this);
    private OnClickListener onItemClickListener;

    public LinearList(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public LinearList(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public LinearList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
    }

    public void setAdapter(Adapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }

        this.adapter = adapter;
        this.adapter.registerDataSetObserver(observer);
        observer.onChanged();
    }

    // Should be called before setAdapter
    public void setOnItemClickListener(OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private class Observer extends DataSetObserver {

        LinearList context;

        public Observer(LinearList context) {
            this.context = context;
        }

        @Override
        public void onChanged() {
            List<View> oldViews = new ArrayList<>(context.getChildCount());

            for (int i = 0; i < context.getChildCount(); i++) {
                oldViews.add(context.getChildAt(i));
            }

            Iterator<View> iter = oldViews.iterator();

            context.removeAllViews();

            for (int i = 0; i < context.adapter.getCount(); i++) {
                View convertView = iter.hasNext() ? iter.next() : null;
                convertView = context.adapter.getView(i, convertView, context);
                convertView.setOnClickListener(onItemClickListener);
                context.addView(convertView);
            }

            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            context.removeAllViews();
            super.onInvalidated();
        }
    }
}
