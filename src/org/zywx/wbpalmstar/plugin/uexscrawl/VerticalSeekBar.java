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
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
//自定义页码进度条
public class VerticalSeekBar extends SeekBar {
    private int mPageCount = 0;
    private OnSeekBarPageChangeListener mBarPageChangeListener;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        //设置系统进度条的监听
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (mBarPageChangeListener != null)
                    //接口回调
                    mBarPageChangeListener.setSeekBarPageChanged( getProgressIsPageIndex(progress, getMax()));
            }
        });

        setMax(1000);
    }

    //进度条改变的接口
    public interface OnSeekBarPageChangeListener {
        public abstract void setSeekBarPageChanged(int page);
    }

    //注册接口
    public void setSeekBarPageChangeListener(OnSeekBarPageChangeListener listener) {
        mBarPageChangeListener = listener;
    }

    //Thumb的变化情况
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    //测量宽度也需要旋转
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    //画布旋转90
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(),0);

        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int i=0;
                //设置y值为进度条的值
                i=getMax() - (int) (getMax() * event.getY() / getHeight());
                setProgress(i);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    //设置进度条页数
    public void setPagesCount(int count) {
        mPageCount = count;
    }

    //分页的进度条的段值
    private int getProgressIsPageIndex(int progress, int max) {
        if (mPageCount < 2) {
            if (progress == 0) return 0;
            else return 1;
        }
        if (progress == 0) return 0;
        // 获取每一块的进度值
        int progressSizeEveryPage = getMax()/(mPageCount-1);
        for (int i = 1; i < mPageCount; i++) {
            if (progress <= (i)*progressSizeEveryPage && progress > ((i-1)*progressSizeEveryPage)) {
                if (progress > (2*i-1)*progressSizeEveryPage/2 ) return i;
                else return i-1;
            }
        }
        return mPageCount-1;
    }

    //外面调用上下按键翻页，需要同时滚动进度条效果
    public void setProgressSpecialPage(int pageIndex, int count) {
        if (count <= 1) {
            setProgress(0);
            return;
        }

        mPageCount = count;
        if (pageIndex < 0 || pageIndex >= mPageCount) return;

        int progressSizeEveryPage = getMax()/(mPageCount-1);

        setProgress( progressSizeEveryPage*pageIndex);
        //设置进度条按钮跟着移动
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}