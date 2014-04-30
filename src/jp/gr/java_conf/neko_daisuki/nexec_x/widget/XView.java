package jp.gr.java_conf.neko_daisuki.nexec_x.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class XView extends View {

    private interface ActionUpProc {

        public void run();
    }

    private interface TouchEventHandler {

        public class Nop implements TouchEventHandler {

            @Override
            public boolean run(MotionEvent event) {
                return false;
            }
        }

        public boolean run(MotionEvent event);
    }

    private class ActionDownHandler implements TouchEventHandler {

        @Override
        public boolean run(MotionEvent event) {
            xMotionNotify(event);
            mDownTime = SystemClock.uptimeMillis();
            return true;
        }
    }

    private class ActionUpHandler implements TouchEventHandler {

        private class NopActionUpProc implements ActionUpProc {

            @Override
            public void run() {
            }
        }

        private class ClickingActionUpProc implements ActionUpProc {

            @Override
            public void run() {
                mClient.xLeftButtonPress();
                mClient.xLeftButtonRelease();
            }
        }

        private ActionUpProc mNop = new NopActionUpProc();
        private ActionUpProc mClickingProc = new ClickingActionUpProc();

        @Override
        public boolean run(MotionEvent event) {
            long deltaT = mDownTime - SystemClock.uptimeMillis();
            ActionUpProc proc = deltaT < 400 ? mClickingProc : mNop;
            proc.run();
            return true;
        }
    }

    private class ActionMoveHandler implements TouchEventHandler {

        @Override
        public boolean run(MotionEvent event) {
            xMotionNotify(event);
            return true;
        }
    }

    private NexecClient mClient;
    private long mDownTime;

    // helpers
    private SparseArray<TouchEventHandler> mHandlers;
    private TouchEventHandler mNopHandler = new TouchEventHandler.Nop();

    public XView(Context context) {
        super(context);
        initialize();
    }

    public XView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public XView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void setNexecClient(NexecClient client) {
        mClient = client;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TouchEventHandler handler = mHandlers.get(event.getActionMasked());
        return (handler != null ? handler : mNopHandler).run(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mClient.xDraw();
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
    }

    private void initialize() {
        mHandlers = new SparseArray<TouchEventHandler>();
        mHandlers.put(MotionEvent.ACTION_DOWN, new ActionDownHandler());
        mHandlers.put(MotionEvent.ACTION_MOVE, new ActionMoveHandler());
        mHandlers.put(MotionEvent.ACTION_UP, new ActionUpHandler());
    }

    private void xMotionNotify(MotionEvent event) {
        mClient.xMotionNotify((int)event.getX(), (int)event.getY());
    }
}