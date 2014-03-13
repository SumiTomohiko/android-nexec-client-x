package jp.gr.java_conf.neko_daisuki.nexec_x.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;

public class MainActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}