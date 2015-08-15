package pct.droid.tv.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import pct.droid.base.providers.media.models.Media;
import pct.droid.tv.R;
import timber.log.Timber;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.net.URI;


public class MediaRowPresenter extends Presenter {
    private static Context mContext;
    private static int CARD_WIDTH = 170;
    private static int CARD_HEIGHT = 213;

    static class ViewHolder extends Presenter.ViewHolder {
        private Media media;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }

        public void setMedia(Media m) {
            media = m;
        }

        public Media getMedia() {
            return media;
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(URI uri) {
            Picasso.with(mContext)
                    .load(uri.toString())
                    .resize(CARD_WIDTH, CARD_HEIGHT)
                    .centerCrop()
                    .noFade()
                    .error(mDefaultCardImage)
                    .into(mCardView.getMainImageView());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Timber.d("onCreateViewHolder");
        mContext = parent.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Media media = (Media) item;
        ((ViewHolder) viewHolder).setMedia(media);
        Timber.d("onBindViewHolder");
        ((ViewHolder) viewHolder).mCardView.setTitleText(media.title);
        //((ViewHolder) viewHolder).mCardView.setContentText(movie.getStudio());
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        //((ViewHolder) viewHolder).mCardView.setBadgeImage(mContext.getResources().getDrawable(
        //        R.drawable.videos_by_google_icon));
        ((ViewHolder) viewHolder).updateCardViewImage(URI.create(media.image));
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Timber.d("onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        Timber.d("onViewAttachedToWindow");
    }

}
