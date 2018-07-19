package cn.hzw.doodle;

import android.content.Context;

import cn.forward.androids.TouchGestureDetector;
import cn.hzw.doodle.core.IDoodleTouchDetector;

public class DoodleTouchDetector extends TouchGestureDetector implements IDoodleTouchDetector {
    public DoodleTouchDetector(Context context, IOnTouchGestureListener listener) {
        super(context, listener);
        this.setScaleSpanSlop(1);
        this.setScaleMinSpan(1);
        this.setIsLongpressEnabled(false);
        this.setIsScrollAfterScaled(false);
    }
}
