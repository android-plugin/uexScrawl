/*
 * Copyright (c) 2016.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.plugin.uexscrawl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.uexScrawl.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


/**
 * Created by ylt on 16/3/23.
 */
public class PhotoScrawlActivity extends FragmentActivity implements View.OnClickListener {

    public static final String KEY_INTENT_IMAGE_PATH="image_path";
    private ScrawlImageView mScrawlImageView;
    private RelativeLayout mImageContentLayout;
    String mPath;

    private RelativeLayout mCloseLayout;
    private RelativeLayout mUndoLayout;
    private RelativeLayout mRestoreLayout;
    private RelativeLayout mSaveLayout;
    private RelativeLayout mEraserLayout;
    private RelativeLayout mBrushLayout;
    private BrushPreviewView mPreviewView;
    private int mScreenWidth;// 手机屏幕的宽（像素）
    private int mScreenHeight;
    private int mCurrentAlpha = 255;
    private int mCurrentStroke = 15;
    private int mCurrnetColor=Color.RED;

    private final int[] colors = {Color.RED,
            Color.parseColor("#fd7f32"),
            Color.parseColor("#ffe00d"),
            Color.parseColor("#85e81b"),
            Color.parseColor("#1792f9"),
            Color.parseColor("#b00ecd")
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_uexscrawl_photo_edit_layout);
        initViews();
        Intent intent = getIntent();
        mPath = intent.getStringExtra(KEY_INTENT_IMAGE_PATH);
        ViewTreeObserver vto2 = mImageContentLayout.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT<16) {
                    mImageContentLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }else{
                    mImageContentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                resizeBitmap();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initViews() {
        mScrawlImageView = (ScrawlImageView) findViewById(R.id.drawView);
        mImageContentLayout = (RelativeLayout) findViewById(R.id.imageContentLayout);
        mCloseLayout = (RelativeLayout) findViewById(R.id.close_layout);
        mUndoLayout = (RelativeLayout) findViewById(R.id.undo_layout);
        mRestoreLayout = (RelativeLayout) findViewById(R.id.restore_layout);
        mSaveLayout = (RelativeLayout) findViewById(R.id.save_layout);
        mEraserLayout = (RelativeLayout) findViewById(R.id.eraser_layout);
        mBrushLayout = (RelativeLayout) findViewById(R.id.brush_layout);
        mPreviewView = (BrushPreviewView) findViewById(R.id.brush_preview_view);
        mCloseLayout.setOnClickListener(this);
        mUndoLayout.setOnClickListener(this);
        mRestoreLayout.setOnClickListener(this);
        mSaveLayout.setOnClickListener(this);
        mBrushLayout.setSelected(true);
        initColorContentLayout();
    }

    private void resizeBitmap() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels; // 屏幕宽度（像素）
        mScreenHeight=metric.heightPixels;
        Bitmap bit = BitmapFactory.decodeFile(mPath);

        Bitmap resizeBmp = compressionFiller(bit, mImageContentLayout);

        mScrawlImageView.setImageBitmap(resizeBmp);


    }

    public Bitmap compressionFiller(Bitmap bitmap, View contentView)
    {

        float scale = 0f;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();
        scale = bitmapHeight > bitmapWidth
                ? contentView.getHeight() / (bitmapHeight * 1f)
                : mScreenWidth / (bitmapWidth * 1f);
        Bitmap resizeBmp;
        if (scale != 0)
        {
            int bitmapheight = bitmap.getHeight();
            int bitmapwidth = bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 长和宽放大缩小的比例
            resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmapwidth,
                    bitmapheight, matrix, true);
        } else
        {
            resizeBmp = bitmap;
        }
        return resizeBmp;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_layout) {
            finish();

        } else if (i == R.id.undo_layout) {
            mScrawlImageView.undo();

        } else if (i == R.id.restore_layout) {
            mScrawlImageView.restore();

        } else if (i == R.id.save_layout) {
            Bitmap bitmap = mScrawlImageView.save();
            String path = getSaveImagePath();
            saveBitmapTofile(bitmap, path);
            BDebug.d("path", path);
            Intent intent = new Intent();
            intent.putExtra(KEY_INTENT_IMAGE_PATH, path);
            setResult(RESULT_OK, intent);
            finish();

        }
    }

    private String getSaveImagePath(){
        File file=new File(mPath);
        File targetFile=new File(file.getParentFile(),"temp_"+file.getName());
        return targetFile.getAbsolutePath();
    }

    private boolean saveBitmapTofile(Bitmap bmp, String path) {
        if (bmp == null || path == null)
            return false;
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
        } catch (FileNotFoundException e) {

        }
        return bmp.compress(format, quality, stream);
    }

    public void onThinClick(View view) {
        VerticalSeekBar seekBar = new VerticalSeekBar(this);
        seekBar.setMax(50);
        seekBar.setProgress(mCurrentStroke);
        seekBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, EUExUtil.dipToPixels(120)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentStroke = progress;
                mScrawlImageView.setPaintStrokeWidth(mCurrentStroke);
                mPreviewView.setStroke(mCurrentStroke);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        showPopWindow(seekBar, view);
    }


    public void onTransClick(View view) {
        VerticalSeekBar seekBar = new VerticalSeekBar(this);
        seekBar.setMax(255);
        seekBar.setProgress(mCurrentAlpha);
        seekBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, EUExUtil.dipToPixels(120)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentAlpha = progress;
                mScrawlImageView.setPaintAlpha(mCurrentAlpha);
                mPreviewView.setAlpha(mCurrentAlpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        showPopWindow(seekBar, view);

    }

    private void showPopWindow(SeekBar seekBar, View relativeView) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        seekBar.measure(w, h);
        PopupWindow popupWindow = new PopupWindow(seekBar);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(seekBar.getMeasuredWidth());
        popupWindow.setHeight(EUExUtil.dipToPixels(120));
        popupWindow.showAtLocation(mImageContentLayout, Gravity.BOTTOM, relativeView.getLeft() - mImageContentLayout.getWidth()
                / 2 + relativeView.getWidth() / 2, mCloseLayout.getHeight() + relativeView.getHeight() + 50);
    }

    private void showColorChoosePop() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout
                .LayoutParams.WRAP_CONTENT);
        int margin = EUExUtil.dipToPixels(8);
        lp.setMargins(margin / 2, margin, margin / 2, margin);
        linearLayout.setLayoutParams(lp);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int color : colors) {
            linearLayout.addView(getColorImageView(color));
        }
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout
                        .LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(scrollLp);
        scrollView.addView(linearLayout);

        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        scrollView.measure(w, h);
        PopupWindow popupWindow = new PopupWindow(scrollView);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(mImageContentLayout.getWidth());
        popupWindow.setHeight(scrollView.getMeasuredHeight());
        popupWindow.showAtLocation(mImageContentLayout, Gravity.BOTTOM, 0, mCloseLayout.getHeight() + mBrushLayout
                .getHeight()+popupWindow.getHeight());

    }

    private void initColorContentLayout() {

    }

    public void onColorPickClick(View view) {
        showColorChoosePop();
    }

    private View getColorImageView(int color) {
        final ImageView imageView = new ImageView(this);
        int width = EUExUtil.dipToPixels(32);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
        int margin = EUExUtil.dipToPixels(4);
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        imageView.setLayoutParams(lp);
        imageView.setTag(color);
        GradientDrawable colorDrawable = new GradientDrawable();
        colorDrawable.setColor(color);
        colorDrawable.setCornerRadius(8);
        if (Build.VERSION.SDK_INT < 16) {
            imageView.setBackgroundDrawable(colorDrawable);
        } else {
            imageView.setBackground(colorDrawable);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrnetColor= (int) imageView.getTag();
                mScrawlImageView.setPaintColor(mCurrnetColor);
                mPreviewView.setColor(mCurrnetColor);
            }
        });
        return imageView;
    }

    public void onBrushClick(View view) {
        view.setSelected(true);
        mEraserLayout.setSelected(false);
        mScrawlImageView.setEraserMode(false);
    }

    public void onEraserClick(View view) {
        mBrushLayout.setSelected(false);
        view.setSelected(true);

        mScrawlImageView.setEraserMode(true);
    }
}
