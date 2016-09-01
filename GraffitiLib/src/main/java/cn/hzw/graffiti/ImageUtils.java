package cn.hzw.graffiti;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

public class ImageUtils {

    //系统数据库存放图片的路径
    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;

    /*
     * 添加图片到系统数据库
     */
    public static Uri addImage(final ContentResolver cr, final String path) {
        File file = new File(path);
        String name = file.getName();
        int i = name.lastIndexOf(".");
        String title = name.substring(0, i);// 文件名称
        String filename = title + name.substring(i);
        int[] degree = new int[1];
        return ImageUtils.addImage(cr, title,
                System.currentTimeMillis(), null, file.getParent(),
                filename, degree);
    }

    private static Uri addImage(ContentResolver cr, String title,
                                long dateTaken, Location location, String directory,
                                String filename, int[] degree) {
        // Read back the compressed file size.
        File file = new File(directory, filename);
        long size = file.length();
        ContentValues values = new ContentValues(9);
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, degree[0]);
        values.put(Images.Media.DATA, file.getAbsolutePath());
        values.put(Images.Media.SIZE, size);

        if (location != null) {
            values.put(Images.Media.LATITUDE, location.getLatitude());
            values.put(Images.Media.LONGITUDE, location.getLongitude());
        }

        return cr.insert(STORAGE_URI, values);
    }

    /*
     * 添加视频到系统数据库
     */
    public static Uri addVideo(ContentResolver cr, String title,
                               long dateTaken, Location location, String directory, String filename) {
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(directory, filename);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        // Read back the compressed file size.
        long size = new File(directory, filename).length();
        ContentValues values = new ContentValues(9);
        values.put(Video.Media.TITLE, title);
        values.put(Video.Media.DISPLAY_NAME, filename);
        values.put(Video.Media.DATE_TAKEN, dateTaken);
        values.put(Video.Media.MIME_TYPE, "video/3gpp");
        values.put(Video.Media.DATA, filePath);
        values.put(Video.Media.SIZE, size);

        if (location != null) {
            values.put(Images.Media.LATITUDE, location.getLatitude());
            values.put(Images.Media.LONGITUDE, location.getLongitude());
        }

        return cr.insert(STORAGE_URI, values);
    }

    // 按比例缩放图片，宽最大为maxw，高最大为maxh
    public static Bitmap getResizedBitmap(Bitmap bitmap, int maxW, int maxH,
                                          boolean isRecycle) {
        if (bitmap == null)
            return null;
        int srcW = bitmap.getWidth();
        int srcH = bitmap.getHeight();
        if (srcH * srcW <= maxH * maxW)//原图小于缩放的图则直接返回
            return bitmap;
        if (srcW > srcH) {
            int t = maxW;
            maxW = maxH;
            maxH = t;
        }
        float nw = maxW * 1f / srcW, nh = maxH * 1f / srcH, s = 1;
        if (nw < nh) {
            s = nw;
        } else {
            s = nh;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(s, s);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, srcW, srcH,
                matrix, true);
        if (isRecycle)
            bitmap.recycle();
        return resizedBitmap;
    }

    //旋转图片
    public static Bitmap rotate(Context context, Bitmap bitmap,
                                int degree, boolean isRecycle) {
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            if (isRecycle) bitmap.recycle();
            return bm1;
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static int getBitmapExifRotate(String path) {
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        return digree;
    }

    /*
     * 根据相片的Exif旋转图片
     */
    public static Bitmap rotateBitmapByExif(Bitmap bitmap, String path, boolean isRecycle) {
        int digree = getBitmapExifRotate(path);
        if (digree != 0) {
            // 旋转图片
            bitmap = ImageUtils.rotate(null, bitmap, digree, isRecycle);
        }
        return bitmap;
    }

    public static final Bitmap createBitmapFromPath(String path, Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int screenW = display.getWidth();
        int screenH = display.getHeight();
        return createBitmapFromPath(path, context, screenW * 2, screenH * 2);
    }

    /**
     * 获取一定尺寸范围内的的图片，防止oom。参考系统自带相机的图片获取方法
     *
     * @param path           路径
     * @param maxResolutionX 图片的最大宽
     * @param maxResolutionY 图片的最大高
     * @return 经过按比例缩放的图片
     * @throws IOException
     */
    public static final Bitmap createBitmapFromPath(String path, Context context, int maxResolutionX, int maxResolutionY) {
        Bitmap bitmap = null;
        Options options = null;
        if (path.endsWith(".3gp")) {
            return ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
        } else {
            try {
                options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int width = options.outWidth;
                int height = options.outHeight;
                options.inSampleSize = computeBitmapSimple(width * height, maxResolutionX * maxResolutionY);
                options.inPurgeable = true;
                options.inPreferredConfig = Config.RGB_565;
                options.inDither = false;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (OutOfMemoryError error) {//内容溢出，则再次缩小图片
                options.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    public static final Bitmap createBitmapFromPath(byte[] data, Context context) {
        Bitmap bitmap = null;
        Options options = null;
        try {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            int screenW = display.getWidth();
            int screenH = display.getHeight();

            options = new Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int maxResolutionX = screenW * 2;
            int maxResolutionY = screenH * 2;
            options.inSampleSize = computeBitmapSimple(width * height, maxResolutionX * maxResolutionY);
            options.inPurgeable = true;
            options.inPreferredConfig = Config.RGB_565;
            options.inDither = false;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return bitmap;
        } catch (OutOfMemoryError error) {//内容溢出，则再次缩小图片
            options.inSampleSize *= 2;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 计算bitmap的simple值
     *
     * @param realPixels,图片的实际像素，
     * @param maxPixels,压缩后最大像素
     * @return simple值
     */
    public static int computeBitmapSimple(int realPixels, int maxPixels) {
        try {
            if (realPixels <= maxPixels) {//如果图片尺寸小于最大尺寸，则直接读取
                return 1;
            } else {
                int scale = 2;
                while (realPixels / (scale * scale) > maxPixels) {
                    scale *= 2;
                }
                return scale;
            }
        } catch (Exception e) {
            return 1;
        }
    }


}


