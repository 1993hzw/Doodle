拓展代码
================

```
/**
 * 使图片填充屏幕
 */
public void centerCrop() {
    RectF rectF = mDoodleView.getDoodleBound();
    float scaleW = mDoodleView.getWidth() / rectF.width();
    float scaleH = mDoodleView.getHeight() / rectF.height();
    float scale = Math.max(scaleW, scaleH);
    mDoodleView.setDoodleScale(scale, mDoodleView.toX(mDoodleView.getWidth() / 2), mDoodleView.toY(mDoodleView.getHeight() / 2));
}
```

```
/**
 * 不跟随缩放变化的文字item
 */
public class FixedSizeDoodleText extends DoodleText {

    float mScaleBefore;

    public FixedSizeDoodleText(IDoodle doodle, String text, float size, IDoodleColor color, float x, float y) {
        super(doodle, text, size, color, x, y);
    }
    @Override
    public void setSize(float size) {
        super.setSize(size);
        if (getDoodle() != null) { // 记录缩放前的倍数
            mScaleBefore = getDoodle().getDoodleScale();
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        float scaleAfter = getDoodle().getDoodleScale();
        setSize(mScaleBefore / scaleAfter * getSize()); // 缩放后重新设置大小
        super.doDraw(canvas);
    }
}
```