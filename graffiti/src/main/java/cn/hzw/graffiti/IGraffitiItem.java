package cn.hzw.graffiti;

import android.graphics.Canvas;

/**
 * Created on 27/06/2018.
 */

public interface IGraffitiItem {

    public void setGraffiti(IGraffiti graffiti);

    public void draw(IGraffiti graffiti, Canvas canvas);
}
