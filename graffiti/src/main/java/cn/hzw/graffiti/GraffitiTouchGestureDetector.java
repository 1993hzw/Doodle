package cn.hzw.graffiti;

import android.content.Context;

import cn.forward.androids.TouchGestureDetector;
import cn.hzw.graffiti.core.IGraffitiTouchGestureDetector;

public class GraffitiTouchGestureDetector extends TouchGestureDetector implements IGraffitiTouchGestureDetector {
    public GraffitiTouchGestureDetector(Context context, IOnTouchGestureListener listener) {
        super(context, listener);
        this.setScaleSpanSlop(1);
        this.setScaleMinSpan(1);
        this.setIsLongpressEnabled(false);
        this.setIsScrollAfterScaled(false);
    }
}
