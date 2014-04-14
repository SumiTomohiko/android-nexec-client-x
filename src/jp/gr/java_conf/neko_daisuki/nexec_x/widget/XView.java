package jp.gr.java_conf.neko_daisuki.nexec_x.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.nexec.client.util.NexecClient;

public class XView extends View {

    private NexecClient mClient;

    public XView(Context context) {
        super(context);
    }

    public XView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNexecClient(NexecClient client) {
        mClient = client;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mClient.xDraw();
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
    }
}