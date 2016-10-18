package com.example.secretwang.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import java.util.ArrayList;
import java.util.HashMap;

public class UserActivity extends Activity {
    private Button btn_exit ;//退出按钮
    private ListView lv; //个人中心listview
    private String Balance;//可用余额
    private String loginStr;

    private ProgressDialog progressDialog;
    String[] from={"item0","item1","item2"};  //这里是ListView显示内容每一列的列名
    //这里是ListView显示每一列对应的list_item中控件的id
    int[] to={R.id.user_item0,R.id.user_item1, R.id.user_item2};
    Object[] userItem0={R.mipmap.keyongyue,R.mipmap.xinxi,R.mipmap.shoushi};
    String[] userItem1={"可用余额","昵称","修改密码"}; //这里第一列所要显示的项目
    String[] userItem2={"0.00","nick",""};  //第二列
    ArrayList<HashMap<String,Object>> list=null;
    HashMap<String,Object> map=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //获取存储类里面的用户名
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo",MODE_PRIVATE);
        loginStr = sharedPreferences.getString("login","");
        //加载视图
        loadUI();
        //开启请求余额线程
        new Thread(runnable).start();
  }
    //加载界面
    public void loadUI(){
        //创建ArrayList对象；
        list=new ArrayList<HashMap<String,Object>>();
        //将数据存放进ArrayList对象中，数据安排的结构是，ListView的一行数据对应一个HashMap对象，
        //HashMap对象，以列名作为键，以该列的值作为Value，将各列信息添加进map中，然后再把每一列对应
        //的map对象添加到ArrayList中
        for(int i=0; i<3; i++){
            map=new HashMap<String,Object>();
            map.put("item0", userItem0[i]);
            map.put("item1", userItem1[i]);
            map.put("item2", userItem2[i]);
            list.add(map);
        }
        //创建一个SimpleAdapter对象
        SimpleAdapter adapter=new SimpleAdapter(UserActivity.this,list,R.layout.list_user,from,to);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器
        lv=(ListView) findViewById(R.id.userlistView);
        lv.setAdapter(adapter);
        //退出登录按钮
        btn_exit=(Button)findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserActivity.this)
                        .setTitle("您确定退出吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //退出登录请求服务器
                                new Thread(exitRunnable).start();
                                //移除个人中心视图
                                finish();
                                onBackPressed();
                                //跳转到登陆界面
                                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
            }
        });
        progressDialog = ProgressDialog.show(UserActivity.this, "", "正在加载,请稍候！");
    }
    //退出登录请求
    Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            String method = "SetData";
            JSONObject param = new JSONObject();
            SharedPreferences driver = getSharedPreferences("driverID",MODE_PRIVATE);
            String driverId = driver.getString("driver", "");
            try{
                param.put("TaskGuid","ab8495db-3a4a-4f70-bb81-8518f60ec8bf");
                param.put("DataType","DriverLoginOut");
                param.put("DriverID",driverId);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                String str_json=param.toString();
                request request = new request();
                SoapObject string = request.getResult(method, str_json);
                String jsonRequest = string.getProperty(0).toString();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    /**
     * 请求余额线程
     * //请求结果处理
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String method = "GetData";
            JSONObject param = new JSONObject();
            try{
                param.put("DataType","UserDouBiWeiPan");
                param.put("DataGuid",loginStr);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                String str_json = param.toString();
                request request = new request();
                SoapObject string = request.getResult(method, str_json);
                String jsonRequest = string.getProperty(0).toString();
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("value",jsonRequest);
                message.setData(bundle);
                handler.sendMessage(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("value");
             if (string.equals("连接超时")){
                 Toast.makeText(UserActivity.this, "请求超时,请重新点击查询", Toast.LENGTH_SHORT).show();
             }
            float Rmb = (float) (Float.parseFloat(string) * 6.5);
            //将解析的字符串转换成json对象
            Balance = Rmb + "\n" + "(" + string + "$" + ")";
            String[] userItem3={Balance, loginStr ," "};
            list=new ArrayList<HashMap<String,Object>>();
            for(int i=0; i<3; i++) {
                map = new HashMap<String, Object>();
                map.put("item0", userItem0[i]);
                map.put("item1", userItem1[i]);
                map.put("item2", userItem3[i]);
                list.add(map);
            }
            //创建一个SimpleAdapter对象
            SimpleAdapter adapter=new SimpleAdapter(UserActivity.this,list,R.layout.list_user, from,to);
            lv.setAdapter(adapter);
            progressDialog.dismiss(); //关闭进度条
            }
    };
}
