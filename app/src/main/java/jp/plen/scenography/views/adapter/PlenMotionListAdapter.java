package jp.plen.scenography.views.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.plen.scenography.R;
import jp.plen.scenography.models.PlenMotion;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * モーションデータ用アダプタ
 * Created by kzm4269 on 15/06/14.
 */
public class PlenMotionListAdapter extends BaseAdapter {
    private static final String TAG = PlenMotionListAdapter.class.getSimpleName();
    private static final int CACHE_SIZE = 1024 * 1024;  // Byte
    private static final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    @NonNull
    private final Bitmap mDefaultIcon;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<PlenMotion> mList;

    public PlenMotionListAdapter(Context context, List<PlenMotion> objects) {
        super();
        mContext = context;
        mList = objects;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressWarnings("deprecation")
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.no_image);

        if (drawable != null) {
            mDefaultIcon = ((BitmapDrawable) drawable).getBitmap();
        } else {
            throw new AssertionError("cannot load default image");
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PlenMotion getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 特定の行(position)のデータを得る
        PlenMotion plenMotion = getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.item_plen_motion_list, null);
        }

        // CustomDataのデータをViewの各Widgetにセットする
        TextView nameView;
        nameView = (TextView) convertView.findViewById(R.id.motion_name_view);
        nameView.setText(plenMotion.getName());
        TextView numberView;
        numberView = (TextView) convertView.findViewById(R.id.motion_number_view);
        numberView.setText(String.format("%02X", plenMotion.getNumber()));
        ImageView iconView = (ImageView) convertView.findViewById(R.id.motion_icon_view);
        loadImageResource(iconView, plenMotion.getIconName());
        return convertView;
    }

    private Bitmap decodeResourceBitmap(String resourceName) {
        Resources resources = mContext.getResources();
        int id = resources.getIdentifier(resourceName, "drawable", mContext.getPackageName());
        if (id == 0) {
            return mDefaultIcon;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inDensity = displayMetrics.densityDpi;
        options.inSampleSize = 4;
        Bitmap result = BitmapFactory.decodeResource(resources, id, options);
        mCache.put(resourceName, result);
        return result;
    }

    protected void loadImageResource(ImageView imageView, @NonNull final String resourceName) {
        Bitmap cached = mCache.get(resourceName);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        if (resourceName.equals(imageView.getTag())) {
            return;
        }

        final WeakReference<ImageView> imageViewWeakReference = new WeakReference<>(imageView);
        imageView.setImageBitmap(mDefaultIcon);
        imageView.setTag(resourceName);

        Observable
                .create(new Observable.OnSubscribe<Bitmap>() {
                    @Override
                    public void call(Subscriber<? super Bitmap> subscriber) {
                        subscriber.onNext(decodeResourceBitmap(resourceName));
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap progress) {
                        ImageView view = imageViewWeakReference.get();
                        if (view != null && view.getTag() != null) {
                            view.setImageBitmap(progress);
                            view.setTag(null);
                        }
                    }
                });
    }
}
