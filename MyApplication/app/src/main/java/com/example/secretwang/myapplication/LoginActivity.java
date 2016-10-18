
package com.example.secretwang.myapplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    private TextView userName;
    private TextView passWord;
    private Button loginButton;//登录按钮
    private ProgressDialog progressDialog;//请求网络提示
    private CheckBox checkBox;//记住密码勾选框
    static String YES = "yes";
    static String NO = "no";
    static String login, password;//截取用户名和密码字符串
    private String isMemory;//isMemory变量用来判断SharedPreferences有没有数据，包括上面的YES和NO
    private String FILE = "userInfo";//用于保存SharedPreferences的文件
    private SharedPreferences sp = null;//声明一个SharedPreferences
    private List prices = new ArrayList();
    private String driverID;

    private static final String TRANSFORMDATA = "TransformData";//web请求方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createUI();//lable,button
        rememberSecret();//记住密码
        upInsideLoginButton();//登录按钮点击
        new Thread(HBListRunnable).start();

        /**
         * id
         */
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        driverID = telephonyManager.getDeviceId();
        SharedPreferences driverId = getSharedPreferences("driverID",MODE_PRIVATE);
        SharedPreferences.Editor drivereditor = driverId.edit();
        drivereditor.putString("driver",driverID);
        drivereditor.apply();
    }


    /**
     *  remenber方法用于判断是否记住密码，checkBox1选中时，提取出EditText里面的内容，
     *  放到SharedPreferences里面的login和password中
     */
    public void remember(){
        if (checkBox.isChecked()){
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("login",userName.getText().toString());
            editor.putString("password",passWord.getText().toString());
            editor.putString("isMemory",YES);
            editor.apply();
        }else if (!checkBox.isChecked()){
            sp=getSharedPreferences(FILE,MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("isMemory",NO);
            editor.apply();
        }
    }
    /**
     * 创建按钮，label
     */
    private void createUI(){
        userName = (TextView) findViewById(R.id.userName);
        passWord = (TextView) findViewById(R.id.passWord);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        loginButton = (Button) findViewById(R.id.login);
    }
    /**
     * 记住密码
     * 取出存储的密码，直接显示在lable上
     */
    private void rememberSecret(){
        sp = getSharedPreferences(FILE, MODE_PRIVATE);//android轻量级数据存储类
        isMemory = sp.getString("isMemory", NO);
        //进入界面时，这个if用来判断SharedPreferences里面name和password有没有数据，有的话则直接打在EditText上面
        if (isMemory.equals(YES)){
            login = sp.getString("login","");
            password = sp.getString("password","");
            userName.setText(login);
            passWord.setText(password);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(login,userName.getText().toString());
        editor.putString(password,passWord.getText().toString());
        editor.apply();
    }
    /**
     * 登录按钮点击
     * 触击登录按钮，执行remenber方法文本框里的信息重新写入SharedPreferences里面覆盖之前的，
     * 去除掉勾选框，触击登录按钮执行remenber方法就将之前保存到SharedPreferences的数据清除了
     */
    private void upInsideLoginButton(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login = userName.getText().toString();
                password = passWord.getText().toString();
                remember();
                //开启线程
                new Thread(runnable).start();
                loginButton.setClickable(false);
                loginButton.setText("登陆中...");
                loginButton.setBackgroundResource(R.drawable.shape);
                //开启网络请求进度条
                progressDialog = ProgressDialog.show(LoginActivity.this, "", "正在加载,请稍候！");
            }
        });
    }


    /**
     * 登陆请求
     * runnable
     * handler
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("DriverID",driverID);
                parameters.put("UserID","");
                parameters.put("TaskGuid", "b4026263-704e-4e12-a64d-f79cb42962cc");
                parameters.put("DataType", "CheckUser");
                parameters.put("LoginID", userName.getText().toString());
                parameters.put("LoginPass", passWord.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String str_json = parameters.toString();
                request request = new request();//请求服务器类
                SoapObject string = request.getResult(TRANSFORMDATA, str_json);
                String jsonRequest = string.getProperty(0).toString();//解析后的字符串
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("value", jsonRequest);//值传到handler类处理
                message.setData(bundle);
                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("value");
            progressDialog.dismiss(); //关闭进度条
            if ("{}".equals(string)) {
                Toast.makeText(LoginActivity.this, "账户名密码错误!",Toast.LENGTH_LONG).show();
            } else if ("连接超时".equals(string)) {
                Toast.makeText(LoginActivity.this, "请求超时", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putStringArrayList("prices", (ArrayList<String>) prices);
                intent.putExtras(bundle1);
                startActivity(intent);
            }
            loginButton.setText("登录");
            loginButton.setClickable(true);
            loginButton.setBackgroundResource(R.drawable.shape);
        }
    };


    /**
     * 获取货币列表
     */
    Runnable HBListRunnable = new Runnable() {
        @Override
        public void run() {
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("UserID","");
                parameters.put("TaskGuid", "b4026263-704e-4e12-a64d-f79cb42962cc");
                parameters.put("DataType", "HBList");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SoapObject string = request.getResult(TRANSFORMDATA, parameters.toString());
            String jsonRequest = string.getProperty(0).toString();
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("HBListkey", jsonRequest);
            message.setData(bundle);
            HBListhandler.sendMessage(message);
        }
    };
    Handler HBListhandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("HBListkey");
            try {
                JSONArray jsonArray = new JSONArray(string);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int Converts = jsonObject.getInt("Converts");
                    int USD = jsonObject.getInt("USD");
                    int price = Converts * USD;
                    prices.add(price);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 左下角返回按钮点击事件
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //退出程序
            finish();
            // 返回桌面操作
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
