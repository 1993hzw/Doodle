package cn.hzw.doodle.core;

/**
 * Created on 19/01/2019.
 */
public interface IDoodleItemListener {
    public int PROPERTY_SCALE = 1;
    public int PROPERTY_ROTATE = 2;
    public int PROPERTY_PIVOT_X = 3;
    public int PROPERTY_PIVOT_Y = 4;
    public int PROPERTY_SIZE = 5;
    public int PROPERTY_COLOR = 6;
    public int PROPERTY_LOCATION = 7;

    /**
     * 属性改变时回调
     * @param property 属性
     */
    void onPropertyChanged(int property);
}
