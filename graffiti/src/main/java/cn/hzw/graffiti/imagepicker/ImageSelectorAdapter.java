package cn.hzw.graffiti.imagepicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import cn.hzw.graffiti.R;


/**
 * @author hzw
 * @date 2016/2/17.
 */
public class ImageSelectorAdapter extends BaseAdapter {

    public static final int KEY_IMAGE = -2016;
    public static final int KEY_SELECTED_VIEW = -20161;

    private Context mContext;
    private ArrayList<String> mList;
    private LinkedHashSet<String> mSelectedSet; // 按添加顺序排列


    public ImageSelectorAdapter(Context context, ArrayList<String> list) {
        mList = new ArrayList<String>(list);
        this.mContext = context;
        mSelectedSet = new LinkedHashSet<String>();
    }

    public void refreshPathList(ArrayList<String> list) {
        mList = new ArrayList<String>(list);
        notifyDataSetChanged();
    }


    public Set<String> getSelectedSet() {
        return mSelectedSet;
    }

    public void addSelected(String path) {
        mSelectedSet.add(path);
    }

    public void removeSelected(String path) {
        mSelectedSet.remove(path);
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    int id = 0;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.graffiti_imageselector_item, null);
            holder = new ViewHolder();
            holder.mImage = (ImageView) convertView.findViewById(R.id.graffiti_image);
            holder.mImageSelected = (ImageView) convertView.findViewById(R.id.graffiti_image_selected);
            convertView.setTag(holder);
            holder.mImage.setTag("" + ++id);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setTag(KEY_IMAGE, mList.get(position));
        convertView.setTag(KEY_SELECTED_VIEW, holder.mImageSelected);
        if (mSelectedSet.contains(mList.get(position))) {
            holder.mImageSelected.setVisibility(View.VISIBLE);
        } else {
            holder.mImageSelected.setVisibility(View.GONE);
        }
        display(holder.mImage, mList.get(position));
        return convertView;
    }


    private <T extends View> void display(T container, String uri) {
        ImageLoader.getInstance(mContext).display(container, uri);
    }


    private class ViewHolder {
        ImageView mImage;
        ImageView mImageSelected;
    }
}
