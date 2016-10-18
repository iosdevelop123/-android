package com.example.secretwang.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.ProgressDialog;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class historyActivity extends Activity {

    private ListView listView = null;

    private String loginStr;
    private String namelist;
    private String name2;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView = (ListView)findViewById(R.id.listView_historyHold);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo",MODE_PRIVATE);
        loginStr = sharedPreferences.getString("login","");
        SharedPreferences name = getSharedPreferences("name", MODE_PRIVATE);
        namelist = name.getString("itemName","");
        name2 = name.getString("itemName2","");
        new Thread(runnable).start();
        //开启网络请求进度条
        progressDialog = ProgressDialog.show(historyActivity.this, "","正在加载,请稍候！");
    }
        Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("value");
            Log.i("sss",string);
            if (string.equals("[]")){
                Toast.makeText(historyActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
            }else {
                try {
                    JSONArray jsonArray = new JSONArray(string);
                    List<Map<String, Object>> listMap = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        JSONObject resultStr = jsonArray.getJSONObject(i);
                        map.put("Symbol", resultStr.getString("Symbol"));
                        if (resultStr.getString("Type").equals("Buy")){
                            map.put("Type", "看多");
                        }else {
                            map.put("Type", "看空");
                        }
                        String data = resultStr.getString("OpenTime").substring(0,10);
                        String time = resultStr.getString("OpenTime").substring(11,16);
                        map.put("DataText",data);
                        map.put("openTimeText",time);
                        map.put("Volume", String.valueOf(resultStr.getInt("Volume")) + "手");
                        map.put("Commission", "手续费" + String.valueOf(resultStr.getInt("Commission")));
                        map.put("Profit", String.valueOf(resultStr.getInt("Profit")));
                        map.put("CloseTimeText", resultStr.getString("CloseTime").substring(11,16));
                        map.put("openPriceText",String.valueOf(resultStr.getInt("OpenPrice")));
                        map.put("closePriceText",String.valueOf(resultStr.getInt("ClosePrice")));
//                        listMap.add(0, map);
                        listMap.add(map);
                    }
                    listView.setAdapter(new historyHoldAdspter(getApplicationContext(), listMap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss(); //关闭进度条
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences s = getSharedPreferences("driverID",MODE_PRIVATE);
            String driverId = s.getString("driver","");
            String method = "TransformData";
            String timeLong = getStrTime(System.currentTimeMillis()/1000);
            String starTime = getStrTime(System.currentTimeMillis()/1000 - 60*60*24);
            JSONObject parma = new JSONObject();
            try {
                parma.put("DriverID",driverId);
                parma.put("UserID","");
                parma.put("TaskGuid","ab8495db-3a4a-4f70-bb81-8518f60ec8bf");
                parma.put("DataType","CloseOrderPages");
                parma.put("LoginID",loginStr);
                parma.put("ProductType","1");
                parma.put("PageCount","100");
                parma.put("PageIndex","1");
                parma.put("PageSize","500");
                parma.put("StratTime",starTime);
                parma.put("EndTime",timeLong);
                parma.put("Symbol","");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String str_json = parma.toString();
            request request = new request();
            SoapObject requestResult = request.getResult(method, str_json);
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("value",requestResult.getProperty(0).toString());
            message.setData(bundle);
            handler.sendMessage(message);
        }
    };

    /**
     * 时间戳转时间
     * @param timeStamp
     * @return
     */
    public static String getStrTime(long timeStamp){
        String timeString = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        timeString = sdf.format(new Date());//单位秒
        return timeString;
    }
}
