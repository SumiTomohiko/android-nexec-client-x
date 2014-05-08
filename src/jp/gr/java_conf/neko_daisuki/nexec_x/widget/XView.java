package jp.gr.java_conf.neko_daisuki.nexec_x.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class XView extends View {

    private class OnGestureListener implements GestureDetector.OnGestureListener {

        private abstract class LongPressedHandler {

            public abstract void run();
        }

        private class PressingLongPressedHandler extends LongPressedHandler {

            @Override
            public void run() {
                mClient.xLeftButtonPress();
                mLongPressedHandler = mReleasingLongPressedHandler;
            }
        }

        private class ReleasingLongPressedHandler extends LongPressedHandler {

            @Override
            public void run() {
                mClient.xLeftButtonRelease();
                mLongPressedHandler = mPressingLongPressedHandler;
            }
        }

        private LongPressedHandler mPressingLongPressedHandler;
        private LongPressedHandler mReleasingLongPressedHandler;
        private LongPressedHandler mLongPressedHandler;

        public OnGestureListener() {
            mPressingLongPressedHandler = new PressingLongPressedHandler();
            mReleasingLongPressedHandler = new ReleasingLongPressedHandler();
            mLongPressedHandler = mPressingLongPressedHandler;
        }

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
            mLongPressedHandler.run();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
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

    private NexecClient mClient;
    private int mScale;

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
        return mGestureDetector.onTouchEvent(event);
    }

    public void zoomIn() {
        setScale(mScale + 1);
        postInvalidate();
    }

    public void zoomOut() {
        setScale(1 < mScale ? mScale - 1 : 1);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mClient.xDraw();
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, mMatrix, null);
    }

    private void setScale(int scale) {
        mScale = scale;
        mMatrix = new Matrix();
        mMatrix.postScale(mScale, mScale);
    }

    private void initialize(Context context) {
        mGestureDetector = new GestureDetector(context,
                                               new OnGestureListener());
        setScale(1);
    }

    private void xMotionNotify(MotionEvent event) {
        int x = (int)event.getX() / mScale;
        int y = (int)event.getY() / mScale;
        mClient.xMotionNotify(x, y);
    }
}