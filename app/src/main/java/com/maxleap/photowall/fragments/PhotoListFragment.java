package com.maxleap.photowall.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.maxleap.*;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.R;
import com.maxleap.photowall.models.Photo;
import com.maxleap.photowall.utils.Utils;

import java.util.List;

public class PhotoListFragment extends ListFragment {

    public static final String TAG = PhotoListFragment.class.getName();

    public static final String EXTRA_TYPE = "type";

    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_GLOBAL = 1;

    private Context mContext;
    private int mType;
    private List<Photo> mPhotos;
    private Apt mApt;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static PhotoListFragment newInstance(int type) {
        PhotoListFragment photoListFragment = new PhotoListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TYPE, type);
        photoListFragment.setArguments(bundle);
        return photoListFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setUpSwipeRefresh(mSwipeRefreshLayout);
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mType = getArguments().getInt(EXTRA_TYPE, TYPE_LOCAL);
        setEmptyText(getString(R.string.empty_list));
        setListShown(false);
        findResult();
    }

    public void findResult() {
        MLQuery<Photo> query = Photo.getQuery();

        if (mType == TYPE_LOCAL) {
            if (Utils.getPreviousLocation() == null) {
                setListShown(true);
                setListAdapter(null);
                return;
            }

            query.whereWithinKilometers(Photo.LOCATION, Utils.getPreviousLocation(), 10);
        } else {
            query.orderByDescending(MLObject.KEY_UPDATED_AT);
        }

        MLQueryManager.findAllInBackground(query, new FindCallback<Photo>() {
            @Override
            public void done(final List<Photo> list, final MLException e) {
                if (getActivity().isFinishing()) {
                    return;
                }
                setListShown(true);

                if (null != e) {
                    if (e.getCode() == MLException.OBJECT_NOT_FOUND) {
                        setListAdapter(null);
                    } else {
                        MLLog.e(TAG, e);
                        Utils.toast(mContext, e.getMessage());
                    }
                    return;
                }
                mPhotos = list;
                mApt = new Apt(mContext);
                for (Photo photo : list) {
                    mApt.add(photo);
                }
                setListAdapter(mApt);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void setUpSwipeRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout == null) return;
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3,
                R.color.refresh_progress_4);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findResult();
            }
        });
    }

    class Apt extends ArrayAdapter<Photo> {

        public Apt(final Context context) {
            super(context, 0);
        }

        @Override
        public Photo getItem(final int position) {
            return mPhotos.get(position);
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.photo_row, null);
            TextView titleTextView = (TextView) view.findViewById(R.id.title);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            Photo photo = getItem(position);
            titleTextView.setText(photo.getTitle());
            Glide.with(PhotoListFragment.this)
                    .load("http://" + photo.getAttachment().getUrl())
                    .crossFade()
                    .placeholder(R.drawable.ic_photo_black_48dp)
                    .into(imageView);
            return view;
        }
    }

    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }

    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }
}
