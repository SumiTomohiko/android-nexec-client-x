package jp.gr.java_conf.neko_daisuki.nexec_x.activity;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;
import jp.gr.java_conf.neko_daisuki.nexec_x.R.layout;
import jp.gr.java_conf.neko_daisuki.nexec_x.R.menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}