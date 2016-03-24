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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ylt on 16/3/23.
 */
public class ScrawlImageView extends ImageView {


    private Paint mPaint;
    private List<LinePath> mHistoryPaths;
    private Canvas mCanvas;

    private Bitmap mImageBitmap;
    private Bitmap mOverlayBitmap;
    private Matrix matrix = new Matrix();
    private Path mPath=new Path();

    private float mX;
    private float mY;

    private int mTouchSlop=5;
    Paint mAlphaPaint;
    private int mStroke=5;
    private boolean isEraserMode=false;

    public ScrawlImageView(Context context) {
        this(context, null);
    }

    public ScrawlImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrawlImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(mStroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(25);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(false);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mHistoryPaths = new ArrayList<LinePath>();
        mAlphaPaint =new Paint();
        mAlphaPaint.setAlpha(255);
    }

    public void setPaintAlpha(int alpha){
        mAlphaPaint.setAlpha(alpha);
        invalidate();
    }

    public void setPaintStrokeWidth(int stroke){
        mStroke=stroke;
        mPaint.setStrokeWidth(stroke);
    }

    public void setPaintColor(int color){
        mPaint.setColor(color);
    }

    public Bitmap save() {
        Bitmap bitmap=Bitmap.createBitmap(mOverlayBitmap.getWidth(),
                mOverlayBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        canvas.drawBitmap(mImageBitmap,new Matrix(),null);
        canvas.drawBitmap(mOverlayBitmap,new Matrix(),null);
        return bitmap;
    }

    public void setEraserMode(boolean eraserMode) {
        isEraserMode = eraserMode;
        if (isEraserMode){
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            mPaint.setStrokeWidth(30);
        }else{
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            mPaint.setStrokeWidth(mStroke);
        }
    }

    public class LinePath {
        Path path;
        Paint paint;
    }


    public void undo() {
        if (mHistoryPaths == null || mHistoryPaths.isEmpty()) {
            return;
        }
        if (mCanvas != null) {

            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            invalidate();

            mHistoryPaths.remove(mHistoryPaths.size() - 1);
            for (LinePath linePath : mHistoryPaths) {
                mCanvas.save();
                mCanvas.drawPath(linePath.path, linePath.paint);
                mCanvas.restore();
            }
            invalidate();
        }
    }

    public void restore() {
        if (mCanvas != null) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            invalidate();
            mHistoryPaths.clear();
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOverlayBitmap !=null){
            int saveCount=canvas.getSaveCount();
            canvas.save();
            canvas.drawBitmap(mOverlayBitmap,getImageMatrix(), mAlphaPaint);
            canvas.restoreToCount(saveCount);

        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mImageBitmap=bm;
        mOverlayBitmap = Bitmap.createBitmap(bm.getWidth(),
                bm.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mOverlayBitmap);
        mCanvas.drawColor(0);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getPointerCount() != 1) {

            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX = x;
                mY = y;
                mPath=new Path();
                mPath.moveTo(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx>mTouchSlop||dy>mTouchSlop){
                    float x1 = (x + mX) / 2;
                    float y1 = (y + mY) / 2;

                    mPath.quadTo(mX, mY, x1, y1);
                    mCanvas.drawPath(mPath, mPaint);
                    invalidate();
                }
                mX = x;
                mY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mPath!=null){
                    LinePath linePath=new LinePath();
                    Paint paint=new Paint(mPaint);
                    linePath.paint=paint;
                    linePath.path=mPath;
                    mHistoryPaths.add(linePath);
                }
                break;


        }

        return true;
    }


    private void pathTo(MotionEvent event) {
        Point pointD = new Point((int) event.getX(), (int) event.getY());
        calculationRealPoint(pointD, matrix);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPath.moveTo(pointD.x, pointD.y);
        } else {
            mPath.lineTo(pointD.x, pointD.y);
        }
    }

    public void calculationRealPoint(Point point, Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        int sX = point.x;
        int sY = point.y;
        point.x = (int) ((sX - values[Matrix.MTRANS_X]) / values[Matrix.MSCALE_X]);
        point.y = (int) ((sY - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_Y]);
    }

    private float getDistOfTowPoints(MotionEvent event) {
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        float lengthX = Math.abs(x0 - x1);
        float lengthY = Math.abs(y0 - y1);
        return (float) Math.sqrt(lengthX * lengthX + lengthY * lengthY);
    }

}
