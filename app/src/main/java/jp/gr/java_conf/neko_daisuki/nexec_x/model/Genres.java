package jp.gr.java_conf.neko_daisuki.nexec_x.model;

import java.util.HashMap;
import java.util.Map;

import jp.gr.java_conf.neko_daisuki.nexec_x.R;

public class Genres {

    private Map<String, Genre> mGenres = new HashMap<String, Genre>();

    public Genres() {
        add(new Genre("favorite", R.drawable.ic_favorite));

        Genre filer = new Genre("file manager", R.drawable.ic_file_manager);
        filer.add(new Application("MToolsFM", "mtoolsfm",
                                  "Two pane file manager"));
        filer.add(new Application("qtFM",
                                  new String[] { "qtfm", "-geometry",
                                                 "${screen_width}x${screen_height}" },
                                  "File manager"));
        filer.add(new Application("ROX-Filer", "rox", "File manager"));
        filer.add(new Application("Thunar", "thunar", "Lightweight file manager"));
        filer.add(new Application("Worker", "worker", "Two pane file manager"));
        filer.add(new Application("X File Explorer", "xfe", "File manager"));
        add(filer);

        Genre game = new Genre("game", R.drawable.ic_game);
        game.add(new Application("Shisen-sho for X11", "xshisen",
                "Shisen-sho puzzle"));
        game.add(new Application("xmine", "xmine", "Minesweeper"));
        add(game);

        Genre painting = new Genre("painting", R.drawable.ic_painting);
        painting.add(new Application("Tux Paint", "tuxpaint",
                                     "Painting for kids"));
        add(painting);
    }

    public Genre get(String name) {
        return mGenres.get(name);
    }

    private void add(Genre genre) {
        mGenres.put(genre.getName(), genre);
    }
}
