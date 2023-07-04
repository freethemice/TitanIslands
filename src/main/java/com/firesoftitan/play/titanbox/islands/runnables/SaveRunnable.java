package com.firesoftitan.play.titanbox.islands.runnables;

import com.firesoftitan.play.titanbox.libs.runnables.TitanSaverRunnable;
import com.firesoftitan.play.titanbox.islands.TitanIslands;

public class SaveRunnable extends TitanSaverRunnable {

    public SaveRunnable() {
        super(TitanIslands.instance);
    }

    @Override
    public void run() {
        TitanIslands.instance.saveALL();
    }
}
