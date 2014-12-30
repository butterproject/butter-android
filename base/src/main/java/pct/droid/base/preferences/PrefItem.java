package pct.droid.base.preferences;

import android.content.Context;

import pct.droid.base.utils.PrefUtils;

public class PrefItem {

    private Context mContext;
    private int mIconRes;
    private int mTitleRes;
    private String mPrefKey;
    private Object mDefaultValue;
    private OnClickListener mOnClickListener;
    private SubTitleGenerator mSubTitleGenerator;

    public PrefItem(Context context, int iconRes, int titleRes, String prefKey, Object defaultValue, OnClickListener clickListener, SubTitleGenerator subTitleGenerator) {
        this(context, iconRes, titleRes, prefKey, defaultValue);
        mOnClickListener = clickListener;
        mSubTitleGenerator = subTitleGenerator;
    }

    public PrefItem(Context context, int iconRes, int titleRes, String prefKey, Object defaultValue, OnClickListener clickListener) {
        this(context, iconRes, titleRes, prefKey, defaultValue);
        mOnClickListener = clickListener;
    }

    public PrefItem(Context context, int iconRes, int titleRes, String prefKey, Object defaultValue) {
        mContext = context;
        mIconRes = iconRes;
        mTitleRes = titleRes;
        mPrefKey = prefKey;
        mDefaultValue = defaultValue;
    }

    public Object getValue() {
        if (mDefaultValue instanceof Integer) {
            return PrefUtils.get(mContext, mPrefKey, (Integer) mDefaultValue);
        } else if (mDefaultValue instanceof Long) {
            return PrefUtils.get(mContext, mPrefKey, (Long) mDefaultValue);
        } else if (mDefaultValue instanceof Boolean) {
            return PrefUtils.get(mContext, mPrefKey, (Boolean) mDefaultValue);
        } else {
            return PrefUtils.get(mContext, mPrefKey, mDefaultValue.toString());
        }
    }

    public void saveValue(Object value) {
        if (mDefaultValue instanceof Integer) {
            PrefUtils.save(mContext, mPrefKey, (Integer) value);
        } else if (mDefaultValue instanceof Long) {
            PrefUtils.save(mContext, mPrefKey, (Long) value);
        } else if (mDefaultValue instanceof Boolean) {
            PrefUtils.save(mContext, mPrefKey, (Boolean) value);
        } else {
            PrefUtils.save(mContext, mPrefKey, value.toString());
        }
    }

    public void clearValue() {
        PrefUtils.remove(mContext, mPrefKey);
    }

    public int getIconResource() {
        return mIconRes;
    }

    public String getTitle() {
        return mContext.getResources().getString(mTitleRes);
    }

    public String getPrefKey() {
        return mPrefKey;
    }

    public Object getDefaultValue() {
        return mDefaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
    }

    public void setOnClickListener(OnClickListener clickListener) {
        mOnClickListener = clickListener;
    }

    public void setSubTitleGenerator(SubTitleGenerator subTitleGenerator) {
        mSubTitleGenerator = subTitleGenerator;
    }

    public String getSubTitle() {
        if (mSubTitleGenerator != null) {
            return mSubTitleGenerator.get(this);
        }
        return "";
    }

    public void onClick() {
        if (mOnClickListener != null)
            mOnClickListener.onClick(this);
    }

    public interface OnClickListener {
        public void onClick(PrefItem item);
    }

    public interface SubTitleGenerator {
        public String get(PrefItem item);
    }

}
