package jp.gr.java_conf.neko_daisuki.nexec_x.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class XView extends View {

    private class OnGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            xMotionNotify(e);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mClient.xRightButtonPress();
            mClient.xRightButtonRelease();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            if (e2.getPointerCount() == 2) {
                mScrolling = true;
                /*
                 * When I move my finger to top left corner, distanceX and
                 * distanceY are greater than zero.
                 */
                mOffset.x -= distanceX;
                mOffset.y -= distanceY;
                updateMatrix();
                return true;
            }

            xMotionNotify(e2);
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mClient.xLeftButtonPress();
            mClient.xLeftButtonRelease();
            return true;
        }
    }

    private static class Point {

        public float x;
        public float y;
    }

    private NexecClient mClient;
    private int mScale;

    /*
     * Position of screen's top left corner. The offsets are zero or less than
     * zero.
     */
    private Point mOffset = new Point();

    private boolean mScrolling = false;

    // helpers
    private GestureDetector mGestureDetector;
    private Matrix mMatrix;

    public XView(Context context) {
        super(context);
        initialize(context);
    }

    public XView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public XView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public int getScale() {
        return mScale;
    }

    public void setNexecClient(NexecClient client) {
        mClient = client;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScrolling && (event.getActionMasked() == MotionEvent.ACTION_UP)) {
            mScrolling = false;
            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    public void zoomIn() {
        setScale(mScale + 1);
    }

    public void zoomOut() {
        setScale(1 < mScale ? mScale - 1 : 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mClient.xDraw(), mMatrix, null);
    }

    private void setScale(int scale) {
        mScale = scale;
        updateMatrix();
    }

    private void initialize(Context context) {
        mGestureDetector = new GestureDetector(context,
                                               new OnGestureListener());
        setScale(1);
    }

    private void xMotionNotify(MotionEvent event) {
        int x = (int)(event.getX() / mScale - mOffset.x);
        int y = (int)(event.getY() / mScale - mOffset.y);
        mClient.xMotionNotify(x, y);
    }

    private void updateMatrix() {
        adjustOffset();
        mMatrix = new Matrix();
        mMatrix.postTranslate(mOffset.x, mOffset.y);
        mMatrix.postScale(mScale, mScale);
        postInvalidate();
    }

    private void adjustOffset() {
        mOffset.x = Math.max(Math.min(0.0f, mOffset.x),
                             - getWidth() / mScale * (mScale - 1));
        mOffset.y = Math.max(Math.min(0.0f, mOffset.y),
                             - getHeight() / mScale * (mScale - 1));
    }
}