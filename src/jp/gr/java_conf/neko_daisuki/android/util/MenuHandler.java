package jp.gr.java_conf.neko_daisuki.android.util;

import android.util.SparseArray;
import android.view.MenuItem;

public class MenuHandler {

    public interface ItemHandler {

        public boolean handle(MenuItem item);
    }

    private SparseArray<ItemHandler> mHandlers;

    public MenuHandler() {
        mHandlers = new SparseArray<ItemHandler>();
    }

    public void put(int id, ItemHandler handler) {
        mHandlers.put(id, handler);
    }

    public boolean handle(MenuItem item) {
        ItemHandler handler = mHandlers.get(item.getItemId());
        return handler != null ? handler.handle(item) : false;
    }
}