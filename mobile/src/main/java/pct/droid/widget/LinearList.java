package pct.droid.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinearList extends LinearLayout
{
    private Adapter mAdapter;
    private Observer mObserver = new Observer(this);
    private OnClickListener mOnItemClickListener;

    public LinearList(Context context)
    {
        super(context);
        setOrientation(VERTICAL);
    }

    public LinearList(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public LinearList(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
    }

    public void setAdapter(Adapter adapter)
    {
        if (mAdapter != null)
            mAdapter.unregisterDataSetObserver(mObserver);

        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mObserver);
        mObserver.onChanged();
    }

    // Should be called before setAdapter
    public void setOnItemClickListener(OnClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private class Observer extends DataSetObserver
    {
        LinearList mContext;

        public Observer(LinearList context)
        {
            mContext = context;
        }

        @Override
        public void onChanged()
        {
            List<View> oldViews = new ArrayList<>(mContext.getChildCount());

            for (int i = 0; i < mContext.getChildCount(); i++)
                oldViews.add(mContext.getChildAt(i));

            Iterator<View> iter = oldViews.iterator();

            mContext.removeAllViews();

            for (int i = 0; i < mContext.mAdapter.getCount(); i++)
            {
                View convertView = iter.hasNext() ? iter.next() : null;
                convertView = mContext.mAdapter.getView(i, convertView, mContext);
                convertView.setOnClickListener(mOnItemClickListener);
                mContext.addView(convertView);
            }

            super.onChanged();
        }

        @Override
        public void onInvalidated()
        {
            mContext.removeAllViews();
            super.onInvalidated();
        }
    }
}