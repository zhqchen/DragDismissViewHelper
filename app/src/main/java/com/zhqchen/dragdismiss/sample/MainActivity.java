package com.zhqchen.dragdismiss.sample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhqchen.dragdismiss.DragDismissViewHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * demo activity
 * Created by zhqchen on 2016-01-16.
 */
public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE_ALERT_SETTING = 1;

    ListView lvRedPoint;
    Toolbar toolbar;

    private List<UnreadItem> unreadItems;
    private RedPointAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.tb_main);
        lvRedPoint = (ListView) findViewById(R.id.lv_red_point);
        toolbar.setTitle("Demo");

        unreadItems = new ArrayList<>();
        mAdapter = new RedPointAdapter(this, unreadItems);
        lvRedPoint.setAdapter(mAdapter);

        for (int i = 0; i < 20; i++) {
            UnreadItem item = new UnreadItem("item" + i, i + 1);
            unreadItems.add(item);
        }
        mAdapter.notifyDataSetChanged();
        requestPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_ALERT_SETTING);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_SETTING) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(MainActivity.this, "SYSTEM_ALERT_WINDOW permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    static class RedPointAdapter extends BaseAdapter {

        private Context context;
        private List<UnreadItem> contents;

        public RedPointAdapter(Context context, List<UnreadItem> contents) {
            this.context = context;
            this.contents = contents;
        }

        @Override
        public int getCount() {
            return contents.size();
        }

        @Override
        public Object getItem(int position) {
            return contents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_common_text, null);
                holder = new ItemHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }
            UnreadItem item = contents.get(position);
            holder.tvContent.setText(item.content);
            holder.tvUnread.setText(String.valueOf(item.unreadCount));
            holder.tvUnread.setVisibility(item.unreadCount > 0 ? View.VISIBLE : View.GONE);
            DragDismissViewHelper helper;//将helper与被拖动的view绑定
            if (holder.tvUnread.getTag() == null) {
                helper = new DragDismissViewHelper(context, holder.tvUnread);
                helper.setPaintColor(Color.RED);
                helper.setFarthestDistance(180);
                holder.tvUnread.setTag(helper);
            } else {
                helper = (DragDismissViewHelper) holder.tvUnread.getTag();
            }

            helper.setDragStateListener(new DragDismissViewHelper.DragStateListener() {

                @Override
                public void onOutFingerUp(View view) {
                    contents.get(position).unreadCount = 0;
                    notifyDataSetChanged();
                }

                @Override
                public void onInnerFingerUp(View view) {
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

    }

    static class ItemHolder {
        TextView tvUnread;
        TextView tvContent;

        ItemHolder(View convertView) {
            tvUnread = (TextView) convertView.findViewById(R.id.tv_unread);
            tvContent = (TextView) convertView.findViewById(R.id.tv_content);
        }
    }

    static class UnreadItem {
        public UnreadItem(String content, int unreadCount) {
            this.content = content;
            this.unreadCount = unreadCount;
        }

        public String content;
        public int unreadCount;
    }
}
