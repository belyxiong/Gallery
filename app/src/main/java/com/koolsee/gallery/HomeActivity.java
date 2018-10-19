package com.koolsee.gallery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koolsee.gallery.adapter.RecommendAdapter;
import com.koolsee.gallery.model.Recommend;
import com.koolsee.gallery.utils.TakePhotoUtil;
import com.koolsee.gallery.widget.GalleryFlow;

/**
 * 首页
 * @author zengxiaotao
 */
public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity";

    private static final int RECOMMEND_SCROLL_INTERVAL = 5000;

    private GalleryFlow mRecommends;

    private RecommendAdapter mRecommendAdapter;

    private Button mButton;

    private TextView mRecommendName;

    private List<Recommend> mGalleryCommends;

    private ImageView mLayerTopBg;

    private int mCurrentRecommend;

    private Timer mTimer;

    private TimerTask mAutoScrollTask;

    private boolean isActivityShown = true;

    private boolean mRecommendFocus = false;

    private final static int FLAG_TAKEPICTURE = 1111;
    private final static int FLAG_PICKPICTURE = 2222;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // 初始化推荐位
        initRecommends();
    }

    @Override
    protected void onResume() {
        // 构建内容更新视图
        isActivityShown = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isActivityShown = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mAutoScrollTask = null;
            mTimer = null;
        }
        super.onDestroy();
    }

    /**
     * 初始化推荐位
     */
    private void initRecommends() {
        mButton = (Button) findViewById(R.id.btn_add);
        mLayerTopBg = (ImageView) findViewById(R.id.layer_top_bg);
        mRecommendName = (TextView) findViewById(R.id.home_recommend_name);
        mRecommends = (GalleryFlow) findViewById(R.id.home_recommend);
        findGalleryCommend();
        mRecommendAdapter = new RecommendAdapter(this, mGalleryCommends);
        mRecommends.setAdapter(mRecommendAdapter);
        mRecommends.setAnimationDuration(1000);
        // 没有推荐时，隐藏顶层推荐的背景
        if (mGalleryCommends == null || mGalleryCommends.isEmpty()) {
            mRecommendName.setVisibility(View.GONE);
            mLayerTopBg.setVisibility(View.GONE);
        }
        mCurrentRecommend = firstSelect();
        mRecommends.setSelection(mCurrentRecommend);
        //释放焦点
        mRecommends.clearFocus();
        mButton.requestFocus();

        // 推荐位点击事件监听器
        mRecommends.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recommend recommend = (Recommend) mRecommendAdapter.getItem(position);

                //Toast.makeText(HomeActivity.this, recommend.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, CardDetail.class);
                startActivity(intent);

            }
        });
        // 推荐位选中事件监听器
        mRecommends.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Recommend recommend = (Recommend) mRecommendAdapter.getItem(position);
                mRecommendName.setText(recommend.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mRecommends.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((mLayerTopBg != null) && (event.getAction() == MotionEvent.ACTION_DOWN)) {
                    mRecommends.setFocusable(true);
                    mRecommends.setFocusableInTouchMode(true);
                    mRecommends.requestFocus();
                    mRecommendFocus = true;
                    mLayerTopBg.setImageResource(R.drawable.layer_top_bg_sel);
                }
                return false;
            }
        });
        recommendAutoScroll();
        // 焦点处理
        focusHandler(mButton, mRecommends);
    }

    /**
     * 设置首页推荐位在没有焦点的情况下自动滚动
     */
    private void recommendAutoScroll() {
        mTimer = new Timer();
        mAutoScrollTask = new TimerTask() {

            @Override
            public void run() {
                if (isActivityShown) {
                    // 选中推荐位中的下一项
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (!mRecommendFocus) {
                                mCurrentRecommend++;
                                if (mCurrentRecommend >= mRecommendAdapter.getCount()) {
                                    mCurrentRecommend = firstSelect();
                                    mRecommends.setSelection(mCurrentRecommend);
                                } else {
                                    mRecommends.moveNext();
                                }
                            }
                        }
                    });
                }
            }
        };
        mTimer.schedule(mAutoScrollTask, RECOMMEND_SCROLL_INTERVAL, RECOMMEND_SCROLL_INTERVAL);
    }

    /**
     * 计算推荐位集合的第一个
     *
     * @return
     */
    private int firstSelect() {
        if ((mRecommends == null) || mGalleryCommends == null || mGalleryCommends.isEmpty()) {
            return 0;
        }
        int count = mRecommends.getCount();
        int selection = ((count % 2) == 0) ? count / 2 : (count / 2) + 1;
        int size = mGalleryCommends.size();
        if (size < GalleryFlow.MIN_CYCLE_NUMS) {
            return 0;
        }
        int y = selection % size;
        if (y != 0) {
            selection = selection + (size - y);
        }
        return selection;
    }

    /**
     * 查询画廊数据
     *
     * @return
     */
    private void findGalleryCommend() {
        mGalleryCommends = new ArrayList<Recommend>();
        Recommend recomends1 = new Recommend(R.drawable.poster1, "飞屋环游记");
        Recommend recomends2 = new Recommend(R.drawable.poster2, "世界之窗");
        Recommend recomends3 = new Recommend(R.drawable.poster3, "爱到天昏地暗");
        Recommend recomends4 = new Recommend(R.drawable.poster4, "我与阿斗二三事");
        Recommend recomends5 = new Recommend(R.drawable.poster5, "神奇大中华");
        Recommend recomends6 = new Recommend(R.drawable.poster6, "环游地球三十年");
        Recommend recomends7 = new Recommend(R.drawable.poster7, "肖申克的救赎");
        Recommend recomends8 = new Recommend(R.drawable.poster8, "勇敢的心");
        mGalleryCommends.add(recomends1);
        mGalleryCommends.add(recomends2);
        mGalleryCommends.add(recomends3);
        mGalleryCommends.add(recomends4);
        mGalleryCommends.add(recomends5);
        mGalleryCommends.add(recomends6);
        mGalleryCommends.add(recomends7);
        mGalleryCommends.add(recomends8);
    }

    private OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (v.getId() == R.id.home_recommend) {
                //推荐位向下，媒资菜单获得焦点并选中之前选中的子项
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                    Log.d(TAG, "onKey------推荐位向下");
                    mCurrentRecommend = mRecommends.getSelectedItemPosition();
                    mRecommendFocus = false;
                    mLayerTopBg.setImageResource(R.drawable.layer_top_bg_nor);
                    mButton.requestFocus();
                    return true;
                }
            } else if (v.getId() == R.id.btn_add) {
                //按钮向上，推荐位获得焦点
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                    Log.d(TAG, "onKey------按钮向上");
                    mRecommends.requestFocus();
                    mRecommendFocus = true;
                    mLayerTopBg.setImageResource(R.drawable.layer_top_bg_sel);
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * 修正焦点，一次可传入多个View
     *
     * @param views
     */
    private void focusHandler(View... views) {
        for (View v : views) {
            focusHandler(v);
        }
    }

    /**
     * 修正焦点
     *
     * @param view
     */
    private void focusHandler(View view) {
        view.setOnKeyListener(onKeyListener);
    }

    public void onAddBtnClicked(View v) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    TakePhotoUtil.takePhoto(HomeActivity.this, FLAG_TAKEPICTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ab.setNegativeButton(R.string.album, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pickImageFromAlbum2();
            }
        });

        ab.show();
    }

    public void onDisplayModeClicked(View v) {
    }

    public void onPasswdClicked(View v) {
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
            case FLAG_PICKPICTURE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(HomeActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Uri imageUri = data.getData();
                    Log.e("TAG", imageUri.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FLAG_TAKEPICTURE:
                if (resultCode == RESULT_CANCELED) {

                    Toast.makeText(HomeActivity.this, "点击取消  拍照", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Uri imageUri = data.getData();
                    //如果拍照图片过大会无法显示
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);



                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case 444:
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                Bitmap photo = data.getParcelableExtra("data");


                break;
            case 555:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(HomeActivity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }

                Uri imageUri = data.getData();
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), getOptions(imageUri.getPath()));



                break;
            default:
                break;

        }
    }

    public void pickImageFromAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 111);

    }

    public void pickImageFromAlbum2() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, FLAG_PICKPICTURE);

    }

    /**
     * 获取压缩图片的options
     *
     * @return
     */
    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 4;      //此项参数可以根据需求进行计算
        options.inJustDecodeBounds = false;

        return options;
    }
}