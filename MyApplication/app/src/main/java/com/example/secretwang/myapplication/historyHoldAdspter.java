package com.example.secretwang.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by Secret Wang on 2015/12/7.
 */
public class historyHoldAdspter extends BaseAdapter {
    private List<Map<String,Object>>data;
    private LayoutInflater layoutInflater;
    private Context context;
    public historyHoldAdspter(Context context,List<Map<String,Object>>data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public final class HistoryHold {
        public TextView nameText;
        public TextView shoNumText;
        public TextView MoreOrLessText;
        public TextView feesText;
        public TextView DataText;
        public TextView openTimeText;
        public TextView CloseTimeText;
        public TextView priceText;
        public TextView openPriceText;
        public TextView closePriceText;

    }
    @Override
    public int getCount() {
        return data.size();
    }
    /**
     * 获得某一位置的数据
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    /**
     * 获得唯一标识
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryHold historyHold = null;
        if (convertView == null){
            historyHold = new HistoryHold();
            convertView = layoutInflater.inflate(R.layout.hold_history,null);
            historyHold.nameText = (TextView)convertView.findViewById(R.id.textView11);
            historyHold.shoNumText = (TextView)convertView.findViewById(R.id.textView18);
            historyHold.MoreOrLessText = (TextView)convertView.findViewById(R.id.textView15);
            historyHold.feesText = (TextView)convertView.findViewById(R.id.textView20);
            historyHold.DataText = (TextView)convertView.findViewById(R.id.textView21);
            historyHold.openTimeText = (TextView)convertView.findViewById(R.id.textView24);
            historyHold.CloseTimeText = (TextView)convertView.findViewById(R.id.textView25);
            historyHold.priceText = (TextView)convertView.findViewById(R.id.textView27);
            historyHold.openPriceText = (TextView)convertView.findViewById(R.id.textView30);
            historyHold.closePriceText = (TextView)convertView.findViewById(R.id.textView28);
            convertView.setTag(historyHold);
        }else {
            historyHold = (HistoryHold)convertView.getTag();
        }
        historyHold.nameText.setText((String)data.get(position).get("Symbol"));
        historyHold.shoNumText.setText((String)data.get(position).get("Volume"));
        historyHold.MoreOrLessText.setText((String)data.get(position).get("Type"));
        historyHold.feesText.setText((String)data.get(position).get("Commission"));
        historyHold.DataText.setText((String)data.get(position).get("DataText"));
        historyHold.openTimeText.setText((String)data.get(position).get("openTimeText"));
        historyHold.CloseTimeText.setText((String)data.get(position).get("CloseTimeText"));
        historyHold.priceText.setText((String)data.get(position).get("Profit"));
        historyHold.openPriceText.setText((String)data.get(position).get("openPriceText"));
        historyHold.closePriceText.setText((String)data.get(position).get("closePriceText"));
        if (historyHold.MoreOrLessText.getText().toString().equals("看多")){
            historyHold.MoreOrLessText.setBackgroundResource(R.drawable.shape2);
        }else {
            historyHold.MoreOrLessText.setBackgroundResource(R.drawable.shape1);
        }
        if (historyHold.priceText.getText().toString().startsWith("-")){
            historyHold.priceText.setTextColor(Color.parseColor("#0069d5"));
        }else {
            historyHold.priceText.setTextColor(Color.parseColor("#ff0000"));
        }
        return convertView;
    }

}
