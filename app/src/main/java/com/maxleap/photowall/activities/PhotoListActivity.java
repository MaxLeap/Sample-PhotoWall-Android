package com.maxleap.photowall.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.maxleap.LocationCallback;
import com.maxleap.MLGeoPoint;
import com.maxleap.MLLog;
import com.maxleap.MLUser;
import com.maxleap.exception.MLException;
import com.maxleap.photowall.BaseActivity;
import com.maxleap.photowall.R;
import com.maxleap.photowall.fragments.PhotoListFragment;
import com.maxleap.photowall.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PhotoListActivity extends BaseActivity {

    public static final String TAG = PhotoListActivity.class.getName();

    public static final int REQUEST_IMAGE_CAPTURE = 10000;

    private static final String imageFileName = "PhotoWallTemp";

    private File mPhotoFile;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private PageAdapter mPageAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        setUpToolbar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mToolbar.setTitle(getString(R.string.title_photolist));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        final ProgressDialog progressDialog = createProgressDialog();
        progressDialog.show();
        Utils.getCurrentLocation(10000, new LocationCallback() {
            @Override
            public void done(final MLGeoPoint mlGeoPoint, final MLException e) {
                progressDialog.dismiss();
                if (mlGeoPoint == null) {
                    Utils.toast(mContext, getString(R.string.err_geo_error));
                }
                setupTabs();
            }
        });
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Get Location...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    private void setupTabs() {
        mPageAdapter = new PageAdapter(getSupportFragmentManager());
        mPageAdapter.addFragment(PhotoListFragment.newInstance(PhotoListFragment.TYPE_LOCAL), getString(R.string.tab_local));
        mPageAdapter.addFragment(PhotoListFragment.newInstance(PhotoListFragment.TYPE_GLOBAL), getString(R.string.tab_global));
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photowall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_take_picture:
                dispatchTakePictureIntent();
                return true;
            case R.id.action_logout:
                MLUser.logOut();
                startActivity(new Intent(mContext, LoginActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mPhotoFile = getImageFile();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(mPhotoFile));

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                MLLog.e(TAG, e);
            }
        }
    }

    public static File getImageFile() throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(mContext, ImageActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (mPageAdapter == null) return;
        for (PhotoListFragment fragment : mPageAdapter.fragments) {
            fragment.findResult();
        }
    }

    private class PageAdapter extends FragmentStatePagerAdapter {

        private final ArrayList<PhotoListFragment> fragments = new ArrayList<>();
        private final ArrayList<String> fragmentTitles = new ArrayList<>();

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        public void addFragment(PhotoListFragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }
    }

}
