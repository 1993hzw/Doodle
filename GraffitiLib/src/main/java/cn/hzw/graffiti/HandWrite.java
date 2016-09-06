package cn.hzw.graffiti;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import cn.forward.androids.utils.LogUtil;
import cn.forward.androids.utils.Util;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by huangziwei on 16-9-2.
 */
public class HandWrite extends View {

    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;

    private Paint paint = null;
    private Bitmap originalBitmap = null;
    private float clickX = 0, clickY = 0;
    private float lastX = 0, lastY = 0;
    private float startX = 0, startY = 0;
    private int color = Color.RED;
    private Canvas myCanvas;
    private Context context;
    private float n;
    private int radius;
    private Path earer;
    private boolean hasEarer, hasCopy, hasPaint, hasArrow;
    private Rect srcRect;
    private RectF dstRect;
    private int clickTimes = 1;// 用于判断在copy模式�?
    private Paint locationPaint, redPaint, bluePaint;
    private MyLocation myLocation;
    // private Path mPath;// 画笔路径
    private Bitmap galleryBitmap;

    private int curColor;

    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<MyPath> pathStack = new CopyOnWriteArrayList<MyPath>();
    private CopyOnWriteArrayList<MyPath> pathStackBackup = new CopyOnWriteArrayList<MyPath>();

    private GraffitiListener mGraffitiListener;

    private float scale =1, maxSacle = 3;
    private float transX = 0, transY = 0;
    int lastPaintMode = -1;
    int lastShapeMode = -1;
    public static final int Mode_PAINTER = 1, MODE_COPY = 2, MODE_ARROW = 3,
            MODE_FILL_CIRCLE = 4, MODE_HOLL_CIRCLE = 5, MODE_FILL_RECT = 6,
            MODE_HOLL_RECT = 7, MODE_LINE = 8;
    private boolean isJustDrawOriginal; // 是否只绘制原图
    private boolean isMovingPic = false;



    public HandWrite(Context context, Bitmap bitmap, GraffitiListener listener) {
        super(context);
        this.context = context;
        galleryBitmap = bitmap;
        mGraffitiListener = listener;
        if (mGraffitiListener == null) {
            throw new RuntimeException("GraffitiListener is null!!!");
        }
        if (galleryBitmap == null) {
            throw new RuntimeException("Bitmap is null!!!");
        }
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBG();
    }

    public void setColor(int c) {
        curColor = c;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        paint.setColor(c);
        paint.setStrokeWidth(radius);
    }

    public int getColor() {
        return curColor;
    }

    public void setSize(int s) {
        radius = s;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        paint.setColor(curColor);
        paint.setStrokeWidth(radius);
        invalidate();
    }

    float oldCopyLastX, oldCopyLastY, oldCopyClikX, oldCopyClickY;

    public void setHasArrow(boolean b) {
        if (hasCopy) {
            oldCopyLastX = lastX;
            oldCopyLastY = lastY;
            oldCopyClikX = clickX;
            oldCopyClickY = clickY;
        }
        hasArrow = b;
        hasPaint = !b;
        hasCopy = !b;
        hasEarer = !b;
        isMovingCopy = true;
        invalidate();
        isMovingCopy = false;
    }

    public void setHasEarer(boolean b) {
        if (hasCopy) {
            oldCopyLastX = lastX;
            oldCopyLastY = lastY;
            oldCopyClikX = clickX;
            oldCopyClickY = clickY;
        }
        hasEarer = b;
        hasArrow = !b;
        hasPaint = !b;
        hasCopy = !b;
        isMovingCopy = true;
        invalidate();
        isMovingCopy = false;
    }

    public void setHasCopy(boolean b) {
        lastX = oldCopyLastX;
        lastY = oldCopyLastY;
        clickX = oldCopyClikX;
        clickY = oldCopyClickY;
        hasCopy = b;
        hasArrow = !b;
        hasPaint = !b;
        hasEarer = !b;
        isMovingCopy = true;

        if (myLocation == null) {
            myLocation = new MyLocation();// 在这里初始化是为了第�?次打�?仿制功能�?
            // 能显示mylocation，因为构造方法会调用update
        }

        invalidate();
        isMovingCopy = false;

    }

    public void setHasPaint(boolean b) {
        if (hasCopy) {
            oldCopyLastX = lastX;
            oldCopyLastY = lastY;
            oldCopyClikX = clickX;
            oldCopyClickY = clickY;
        }
        hasPaint = b;
        hasArrow = !b;
        hasCopy = !b;
        hasEarer = !b;
        isMovingCopy = true;
        invalidate();
        isMovingCopy = false;
    }

    public void setJustDrawOriginal(boolean justDrawOriginal) {
        this.isJustDrawOriginal = justDrawOriginal;
    }


    public void init() {
        try {
            originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                    galleryBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (Error e) {
            LogUtil.d(e.getMessage());
            mGraffitiListener.onError(ERROR_INIT, "init error");
            return;
        }
        scale =1;
        myCanvas = new Canvas(originalBitmap);

        setColor(color);
        // earer = new Path();
        locationPaint = new Paint();
        locationPaint.setColor(Color.LTGRAY);
        oldCopyLastX = oldCopyLastY = 200;

        // mPath = new Path();

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        bluePaint = new Paint();
        bluePaint.setColor(Color.CYAN);

        // 初始
        srcRect = new Rect(0, 0, galleryBitmap.getWidth(),
                galleryBitmap.getHeight());
        float x = (startX - lastX) / (n * scale);
        float y = (startY - lastY) / (n * scale);
        dstRect = new RectF(srcRect.left + x, srcRect.top + y,
                srcRect.right + x, srcRect.bottom + y);

        arrowLineSize = Util.dp2px(context, 5);
    }

    public void move(float spanX, float spanY) {
        transX += spanX;
        transY += spanY;
        judgePic();
        invalidate();
    }

    /**
     * 调整图片位置
     */
    public void judgePic() {
        if (scale > 1) { // 当图片放大时，图片偏移的位置不能超过屏幕边缘
            if (transX > 0)
                transX = 0;
            else if (transX + width * scale < width)
                transX = width - width * scale;
            if (transY > 0)
                transY = 0;
            else if (transY + height * scale < height)
                transY = height - height * scale;
        } else { // 当图片缩小时，图片只能在屏幕可见范围内移动
            if (transX + galleryBitmap.getWidth() * n * scale > width)// scale<1是preview.width不用乘scale
                transX = width - galleryBitmap.getWidth() * n * scale;
            else if (transX < 0)
                transX = 0;
            if (transY + galleryBitmap.getHeight() * n * scale > height)
                transY = height - galleryBitmap.getHeight() * n * scale;
            else if (transY < 0)
                transY = 0;
        }
    }

    private int height, width;// 包揽图片的框大小 并非为preview的大�?
    private float centreX, centreY;// 是图片居�?

    private void setBG() {// 不用resize preview
        int w = galleryBitmap.getWidth();
        int h = galleryBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            n = 1 / nw;
            width = getWidth();
            height = (int) (h * n);
        } else {
            n = 1 / nh;
            width = (int) (w * n);
            height = getHeight();
        }
        // 使图片居中
        centreX = (getWidth() - width) / 2f;
        centreY = (getHeight() - height) / 2f;
        invalidate();
    }

    /**
     * 清屏
     */
    public void clear() {
        pathStack.clear();
        pathStackBackup.clear();
        originalBitmap.recycle();
        originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                galleryBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(originalBitmap);
        invalidate();
    }

    public void undo() {
        if (pathStack.size() > 0) {
            pathStack.remove(pathStack.size() - 1);
            originalBitmap.recycle();
            originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                    galleryBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            myCanvas = new Canvas(originalBitmap);
            drawItself(pathStackBackup);
            invalidate();
        } else if (pathStackBackup.size() > 0) {
            pathStackBackup.remove(pathStackBackup.size() - 1);
            originalBitmap.recycle();
            originalBitmap = Bitmap.createBitmap(galleryBitmap.getWidth(),
                    galleryBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            myCanvas = new Canvas(originalBitmap);
            drawItself(pathStackBackup);
            invalidate();
        }
    }

    public boolean isModified() {
        return pathStack.size() != 0 || pathStackBackup.size() != 0;
    }

    public int getShape() {
        return lastShapeMode;
    }

    public void setShape(int shape) {
        lastShapeMode = shape;
    }

    public int getMode() {
        return lastPaintMode;
    }

    public void setMode(int mode) {
        lastPaintMode = mode;
    }

    public void setMovingPic(boolean moving) {
        isMovingPic = moving;
    }

    public boolean isMovingPic() {
        return isMovingPic;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        handWriting(canvas);
    }

    private void drawItself(CopyOnWriteArrayList<MyPath> list) {
        for (MyPath p : list) {
            if (p.type == MyPath.TYPE_EARER) {
                myCanvas.save();
                myCanvas.clipPath(p.path);
                myCanvas.drawBitmap(galleryBitmap, 0, 0, null);
                myCanvas.restore();
            } else if (p.type == MyPath.TYPE_PAINT) {
                myCanvas.drawPath(p.path, p.paint);
            } else if (p.type == MyPath.TYPE_COPY) {
                myCanvas.save();
                myCanvas.clipPath(p.path);
                if (!galleryBitmap.isRecycled())
                    myCanvas.drawBitmap(galleryBitmap, p.srcRect,
                            p.dstRect, null);
                myCanvas.restore();
            } else if (p.type == MyPath.TYPE_ARROW) {
                switch (p.shapeMode) {
                    case MODE_FILL_CIRCLE:
                        myCanvas.drawCircle(p.src.x, p.src.y,
                                (float) Math.sqrt((p.src.x - p.dst.x) * (p.src.x - p.dst.x) + (p.src.y - p.dst.y) * (p.src.y - p.dst.y)),
                                p.paint);
                        break;
                    case MODE_HOLL_CIRCLE:
                        myCanvas.drawCircle(p.src.x, p.src.y,
                                (float) Math.sqrt((p.src.x - p.dst.x) * (p.src.x - p.dst.x) + (p.src.y - p.dst.y) * (p.src.y - p.dst.y)),
                                p.paint);
                        break;
                    case MODE_FILL_RECT:
                        myCanvas.drawRect(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                p.paint);
                        break;
                    case MODE_HOLL_RECT:
                        myCanvas.drawRect(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                p.paint);
                        break;
                    case MODE_LINE:
                        myCanvas.drawLine(p.src.x, p.src.y, p.dst.x, p.dst.y,
                                p.paint);
                        break;
                    case MODE_ARROW:
                        drawArrow(myCanvas, p.src.x, p.src.y, p.dst.x, p.dst.y,
                                p.paint, p.arrowSize);
                        break;
                }
            }
        }
    }

    public void drawArrow(Canvas canvas, float sx, float sy, float ex,
                          float ey, Paint paint, float arrowSize) {
        double H = arrowSize; // 箭头高度
        double L = arrowSize / 2; // 底边的一�?

        double awrad = Math.atan(L / 2 / H); // 箭头角度
        double arraow_len = Math.sqrt(L / 2 * L / 2 + H * H) - 5; // 箭头的长�?
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true,
                arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true,
                arraow_len);
        float x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        float y_3 = (float) (ey - arrXY_1[1]);
        float x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        float y_4 = (float) (ey - arrXY_2[1]);
        // 画线
        Path linePath = new Path();
        linePath.moveTo(sx, sy);
        linePath.lineTo(x_3, y_3);
        linePath.lineTo(x_4, y_4);
        linePath.close();
        canvas.drawPath(linePath, paint);

        awrad = Math.atan(L / H); // 箭头角度
        arraow_len = Math.sqrt(L * L + H * H); // 箭头的长�?
        arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        x_3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第�?端点
        y_3 = (float) (ey - arrXY_1[1]);
        x_4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端�?
        y_4 = (float) (ey - arrXY_2[1]);
        Path triangle = new Path();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x_3, y_3);
        triangle.lineTo(x_4, y_4);
        triangle.close();
        canvas.drawPath(triangle, paint);
    }

    // 计算 向量（px,py�? 旋转ang角度后的新长�?
    public double[] rotateVec(float px, float py, double ang,
                              boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度�?�新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
        }
        mathstr[0] = vx;
        mathstr[1] = vy;
        return mathstr;
    }

    public void handWriting(Canvas canvas) {
        if (galleryBitmap.isRecycled())
            return;

        if (!isMovingCopy && !isMovingPic) {
            if (isEarering || isPainting || isCopying || isShapping) {
                if (isPainting && isJustClickOnce) {
                    isJustClickOnce = false;
                    paintPath.quadTo(clickX / (n * scale), clickY
                            / (n * scale), (clickX + radius / 22 + 1)
                            / (n * scale), clickY / (n * scale));
                }
                drawItself(pathStack);
            }
        }
        canvas.scale(n * scale, n * scale);
        if (!originalBitmap.isRecycled()) {
            // bg
            canvas.drawBitmap(galleryBitmap, (centreX + transX)
                    / (n * scale), (centreY + transY) / (n * scale), null);
            if (!isJustDrawOriginal) { // 是否只绘制原图
                canvas.drawBitmap(originalBitmap, (centreX + transX)
                        / (n * scale), (centreY + transY) / (n * scale), null);
            }
        }
        if (isShapping && srcPoint != null) {
            float sx = srcPoint.x + (centreX + transX) / (n * scale);
            float sy = srcPoint.y + (centreY + transY) / (n * scale);
            float dx = clickX
                    / (n * scale) + (centreX + transX)
                    / (n * scale);
            float dy = clickY / (n * scale)
                    + (centreY + transY) / (n * scale);
            switch (lastShapeMode) {
                case MODE_FILL_CIRCLE:
                    canvas.drawCircle(sx,
                            sy,
                            (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)),
                            shapePaint);
                    break;
                case MODE_HOLL_CIRCLE:
                    canvas.drawCircle(sx,
                            sy,
                            (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)),
                            shapePaint);
                    break;
                case MODE_FILL_RECT:
                    canvas.drawRect(sx, sy, dx, dy, shapePaint);
                    break;
                case MODE_HOLL_RECT:
                    canvas.drawRect(sx, sy, dx, dy, shapePaint);
                    break;
                case MODE_LINE:
                    canvas.drawLine(sx, sy, dx, dy, shapePaint);
                    break;
                case MODE_ARROW:
                    drawArrow(canvas,
                            sx,
                            sy, dx, dy, shapePaint,
                            radius);
                    break;
            }

        }
        if (hasCopy)
            myLocation.draw(canvas, (centreX + transX) / (n * scale),
                    (centreY + transY) / (n * scale));

    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return x * x + y * y;
    }

    private float oldDist, newDist, toucheCentreXOnWorld,
            toucheCentreYOnWorld, touchCentreX, touchCentreY;// 双指距离
    boolean isJustClickOnce = false;// 单击屏幕 画笔
    float lastMoveX, lastMoveY, clickMoveX, clickMoveY;
    private int mode;
    boolean isDoubleTouchScaling = false;
    // 是否正在移动copy坐标
    boolean isMovingCopy = false, isCopying = false, isPainting = false,
            isEarering = false, isReLocate = true, isShapping = false;
    float oldScale = 1;
    private Path paintPath;
    private PointF srcPoint;
    private int arrowLineSize;
    private Paint shapePaint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isCopying = isEarering = isPainting = isShapping = false;
                if (pathStack.size() > 3) {// 当前栈大�?3，则拷贝到备份栈
                    pathStackBackup.addAll(pathStack);
                    pathStack.clear();
                }
                mode = 1;
                if (isDoubleTouchScaling) {
                    isDoubleTouchScaling = false;
                }
                if (hasCopy && !isMovingPic && myLocation.isInIt(event)) {// 移动盖章
                    isMovingCopy = true;
                    isReLocate = true;
                    clickX = event.getX() - transX - centreX;
                    clickY = event.getY() - transY - centreY;// no -100
                    startX = startY = lastX = lastY = 0;
                    oldScale = scale;
                    invalidate();
                } else {
                    if (hasCopy) {
                        isCopying = true;
                        isReLocate = false;
                    }
                    if (hasPaint) {
                        isPainting = true;
                    }
                    if (hasEarer)
                        isEarering = true;
                    if (hasArrow)
                        isShapping = true;

                    if (hasCopy && !isMovingPic && clickTimes == 1) {// 记下盖章位置
                        startX = event.getX() - transX - centreX;
                        startY = event.getY() - transY - centreY;
                        clickTimes++;
                        float x = (startX - lastX) / (n * scale);
                        float y = (startY - lastY) / (n * scale);
                        dstRect = new RectF(srcRect.left + x, srcRect.top + y,
                                srcRect.right + x, srcRect.bottom + y);
                        oldScale = scale;

                        clickX = event.getX() - transX - centreX;
                        clickY = event.getY() - transY - centreY;
                        earer = new Path();
                        pathStack.add(new MyPath(earer, null, MyPath.TYPE_COPY,
                                srcRect, dstRect));
                        earer.addCircle(clickX / (n * scale), clickY
                                / (n * scale), radius / 2, Path.Direction.CCW);

                        invalidate();
                    } else if (isMovingPic) {
                        lastMoveX = event.getX();// 不用减trans
                        lastMoveY = event.getY();
                    } else {
                        clickX = event.getX() - transX - centreX;
                        clickY = event.getY() - transY - centreY;
                        // mPath.reset();
                        if (isPainting) {
                            paintPath = new Path();
                            pathStack.add(new MyPath(paintPath, paint,
                                    MyPath.TYPE_PAINT));
                            paintPath.moveTo(clickX / (n * scale), clickY
                                    / (n * scale));
                        } else if (isEarering) {
                            earer = new Path();
                            pathStack.add(new MyPath(earer, null,
                                    MyPath.TYPE_EARER));
                            earer.addCircle(clickX / (n * scale), clickY
                                            / (n * scale), radius / 2,
                                    Path.Direction.CCW);
                        } else if (isCopying) {
                            earer = new Path();
                            pathStack.add(new MyPath(earer, null,
                                    MyPath.TYPE_COPY, srcRect, dstRect));
                            earer.addCircle(clickX / (n * scale), clickY
                                            / (n * scale), radius / 2,
                                    Path.Direction.CCW);

                        } else if (isShapping) {
                            srcPoint = new PointF(clickX / (n * scale), clickY
                                    / (n * scale));
                            shapePaint = new Paint();
                            shapePaint.setAntiAlias(true);
                            shapePaint.setColor(paint.getColor());
                            switch (lastShapeMode) {
                                case MODE_HOLL_CIRCLE:
                                case MODE_HOLL_RECT:
                                case MODE_LINE:
                                    shapePaint.setStyle(Paint.Style.STROKE);
                                    shapePaint.setStrokeWidth(radius);
                                    break;
                                case MODE_FILL_CIRCLE:
                                case MODE_FILL_RECT:
                                case MODE_ARROW:
                                    shapePaint.setStyle(Paint.Style.FILL);
                                    shapePaint.setStrokeWidth(arrowLineSize);
                                    break;
                            }


                        }
                        isJustClickOnce = true;
                        invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                mode = 0;
                if (isMovingCopy) {
                    lastX = event.getX() - transX - centreX;
                    lastY = event.getY() - transY - centreY;
                    clickX = clickY = 0;
                    clickTimes = 1;// 标记已经选择好盖章位�?
                    isMovingCopy = false;
                } else if (isShapping && !isMovingPic) {
                    PointF dstPoint = new PointF(
                            (event.getX() - transX - centreX) / (n * scale),
                            (event.getY() - transY - centreY) / (n * scale));
                    if ((srcPoint.x - dstPoint.x) * (srcPoint.x - dstPoint.x)
                            + (srcPoint.y - dstPoint.y)
                            * (srcPoint.y - dstPoint.y) > 10) {
                        pathStack.add(new MyPath(MyPath.TYPE_ARROW, shapePaint,
                                radius, lastShapeMode, srcPoint, dstPoint));
                    }
                    srcPoint = null;
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);// 两点按下时的距离
                if (isMovingPic) {
                    touchCentreX = event.getX(0) + event.getX(1);// 不用减trans
                    touchCentreY = event.getX(0) + event.getX(1);
                }
                toucheCentreXOnWorld = (touchCentreX - 2 * (transX + centreX))
                        / 2 / (n * scale);
                toucheCentreYOnWorld = (touchCentreY - 2 * (transY + centreY))
                        / 2 / (n * scale);
                mode += 1;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mode < 2) {// 单点滑动
                    if (isDoubleTouchScaling) {
                        return true;
                    }
                    if (isMovingCopy) {
                        clickX = event.getX() - transX - centreX;
                        clickY = event.getY() - transY - centreY;
                        isReLocate = true;
                        invalidate();
                    } else if (isMovingPic) {
                        clickMoveX = event.getX();// 不用减trans！！！！
                        clickMoveY = event.getY();
                        float spanX = clickMoveX - lastMoveX;
                        float spanY = clickMoveY - lastMoveY;
                        if (Math.abs(spanX) > 10 || Math.abs(spanY) > 10) {
                            move(spanX, spanY);
                        }
                        lastMoveX = clickMoveX;
                        lastMoveY = clickMoveY;
                    } else {
                        if (hasCopy && clickTimes < 2)
                            return true;
                        if (isPainting) {
                            paintPath.quadTo(clickX / (n * scale), clickY
                                    / (n * scale), (event.getX() - transX
                                    - centreX + clickX)
                                    / 2 / (n * scale), (event.getY() - transY
                                    - centreY + clickY)
                                    / 2 / (n * scale));
                        }
                        clickX = event.getX() - transX - centreX;
                        clickY = event.getY() - transY - centreY;
                        if (isEarering) {
                            earer.addCircle(clickX / (n * scale), clickY
                                            / (n * scale), radius / 2,
                                    Path.Direction.CCW);
                        } else if (isCopying) {
                            earer.addCircle(clickX / (n * scale), clickY
                                            / (n * scale), radius / 2,
                                    Path.Direction.CCW);
                        }
                        invalidate();
                    }
                } else {// 多点
                    if (isMovingPic) {
                        newDist = spacing(event);// 两点滑动时的距离
                        if (Math.abs(newDist - oldDist) >= 2000) {
                            isDoubleTouchScaling = true;
                            if (newDist - oldDist > 0) {// 拉大
                                scale += 0.05f;
                                if (scale > maxSacle) {
                                    scale = maxSacle;
                                }
                                // toucheCentreXOnWorld=(touchCentreX- 2*(transX
                                // +centreX) ) / 2 / (n * scale);
                                // toucheCentreYOnWorld=(touchCentreY-
                                // 2*(transY+centreY)) / 2/ (n * scale);
                                // 由上式推导出下式，是图片以两点中心缩�?
                                transX = (touchCentreX - toucheCentreXOnWorld
                                        * (2 * n * scale))
                                        / 2 - centreX;
                                transY = (touchCentreY - toucheCentreYOnWorld
                                        * (2 * n * scale))
                                        / 2 - centreY;
                                judgePic();
                                invalidate();
                            } else {// 拉小
                                scale -= 0.05f;
                                if (scale < 0.5f) {
                                    scale = 0.5f;
                                }
                                transX = (touchCentreX - toucheCentreXOnWorld
                                        * (2 * n * scale))
                                        / 2 - centreX;
                                transY = (touchCentreY - toucheCentreYOnWorld
                                        * (2 * n * scale))
                                        / 2 - centreY;
                                judgePic();
                                invalidate();
                            }
                        }
                    }
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }

    public void save() {
        try {
            originalBitmap.recycle();
            try {
                originalBitmap = galleryBitmap.copy(Bitmap.Config.RGB_565, true);
//				   originalBitmap = galleryBitmap;//这样会报错，因为gallertBitmap时Immutable，不可修改的
            } catch (OutOfMemoryError error) {
                mGraffitiListener.onError(ERROR_SAVE, "save error");
            }
            myCanvas = new Canvas(originalBitmap);
            drawItself(pathStackBackup);
            drawItself(pathStack);
            // 释放图片
           /* originalBitmap.recycle();
            galleryBitmap.recycle();*/
        } catch (Throwable e) {//异常 �? error
            e.printStackTrace();
            mGraffitiListener.onError(ERROR_SAVE, "save error");
            return;
        }
        mGraffitiListener.onSaved(originalBitmap);
    }

    private class MyLocation {
        float x, y;

        public MyLocation() {
            updateLocation();
        }

        public void updateLocation() {
            x = (lastX * scale / oldScale + clickX - startX * scale
                    / oldScale)
                    / (n * scale);// 和其他不同这里要加transX
            y = (lastY * scale / oldScale + clickY - startY * scale
                    / oldScale)
                    / (n * scale);
        }

        public void draw(Canvas c, float tx, float ty) {
//            if (!isMovingPic && !isScaling && (isCopying || isMovingCopy))
            updateLocation();
            c.drawCircle(x + tx, y + ty, radius / 2, locationPaint);
            if (isReLocate)
                c.drawCircle(x + tx, y + ty, radius / 4, redPaint);
            else
                c.drawCircle(x + tx, y + ty, radius / 4, bluePaint);
        }

        public boolean isInIt(MotionEvent e) {
            if (((e.getX() - transX - centreX) / (n * scale) - x)
                    * ((e.getX() - transX - centreX) / (n * scale) - x)
                    + ((e.getY() - transY - centreY) / (n * scale) - y)
                    * ((e.getY() - transY - centreY) / (n * scale) - y) <= radius
                    * radius)
                return true;
            return false;
        }
    }

    private static class MyPath {
        public static final int TYPE_PAINT = 1, TYPE_EARER = 0, TYPE_COPY = 3,
                TYPE_ARROW = 4;
        public Path path;
        public Paint paint;
        public int type = -1;
        public Rect srcRect;
        public RectF dstRect;
        public PointF src, dst;
        public float arrowSize = 1;
        public int shapeMode;

        public MyPath(Path path, Paint paint, int type) {
            this.path = path;
            this.paint = paint;
            this.type = type;
        }

        public MyPath(Path path, Paint paint, int type, Rect srcRect,
                      RectF dstRect) {
            this(path, paint, type);
            this.srcRect = srcRect;
            this.dstRect = dstRect;
        }

        public MyPath(int type, Paint paint, float arrowSize, int shapeMode, PointF src,
                      PointF dst) {
            this.src = src;
            this.dst = dst;
            this.type = type;
            this.paint = paint;
            this.arrowSize = arrowSize;
            this.shapeMode = shapeMode;
        }
    }

    public interface GraffitiListener {
        void onSaved(Bitmap bitmap);

        void onError(int i, String msg);
    }
}