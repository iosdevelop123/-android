package com.example.secretwang.myapplication;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetSocketAddress;
import java.net.Socket;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;



public class MainActivity extends Activity {
    private String[] shoushu = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private List<String> hblist = new ArrayList<String>();//货币英文名
    private List<String> nameList = new ArrayList<String>();//货币中文名
    private List Incrementlist = new ArrayList();
    private static String TaskGuid = "ab8495db-3a4a-4f70-bb81-8518f60ec8bf";
    private Button buyMoreButton;//看多按钮
    private Button buyLessButton;//看空按钮
    private Button chooseNum;//选择手数按钮
    private TextView daojishi;//倒计时数字
    private int timeNum;//倒计时数字
    private Timer daojishiTimer;//倒计时timer
    private TimerTask daojishiTask;//倒计时task
    private TextView mairuduoshaoshouTex;//主界面显示买入多少手
    private WheelView wv;//
//    private WheelView wv2;//手数
    private int number;
    private ImageButton settingBtn;
    private TextView shouTxt;//设置委托手数
    private TextView nametextView;//名称
    private ImageButton userBtn;//个人中心
    private Button holdButton;//持仓
    private TextView PriceTxt;//最新行情
    private String itemName;//储存切换货币时要切换的货币的名字
    private ProgressDialog progressDialog;//刷新提示框
//    public List orderNumbersList = new ArrayList();//订单编号数组
//    private List SymbolNumberSList = new ArrayList();//选中货币的订单编号数组
    private String loginStr;//登录名

    private Button netBtn;
    private String NAME1;//商品名称。
    private String NAME2 = "";
    private String Ip;//手机ip地址
    private int NowHour;//当前时间
//    private int NowMinute;
//    private int profit = 0;//总共盈利
    private String driverId;//手机唯一标识
    private request request = new request();//数据请求
    private Bundle bundle = new Bundle();
    private JSONObject parma = new JSONObject();//请求数据要传入的参数
    private String[] socketDataArray;//socket获取最新行情
    private String dingdanString;
    private int price;//计算获利时乘的参数
    private List usdList = new ArrayList();//获利参数数组
    private double Increment;

    private static final String HOST = "139.196.207.149";//socket请求地址
    private static final int PORT = 2012;//socket
    private static final String SETDATA = "SetData";//web请求方法
    private static final String TRANSFORMDATA = "TransformData";//web请求方法
//    private static final String OpenBuy_New = "OpenBuy-New";
    private Socket socket;


    private LineChart mChart;

    private int shownum=200;//设置每页显示的数据值个数

    private LineDataSet dataSet;
    private boolean Winloss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mChart = (LineChart) findViewById(R.id.spread_line_chart);
        mChart.getLegend().setEnabled(false);
        mChart.setNoDataText("没有数据!");
        mChart.setTouchEnabled(false);
        //mChart.animateX(8000);
        mChart.setDrawBorders(false);
        //mChart.setBorderWidth(2);

        //mChart.setBackgroundColor(Color.WHITE);
        setLineChart(mChart);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setStartAtZero(false);
        yAxis.resetAxisMaxValue();
        yAxis.resetAxisMinValue();
//        yAxis.setEnabled(false);
//        yAxis.setDrawGridLines(true);
//        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
        xAxis.setDrawGridLines(true);

        timeNum = 11;
        /**
         * 创建界面
         * 货币信息获取
         */
        createButton();
        new Thread(HBListRunnable).start();//获取货币列表
        getLoginIdAndHuobiCanshu();
        new Thread(zaicangRunnable).start();//进入主界面根据在仓订单刷新按钮名字

        //网络判断动画
        netAnimation();
        Timer timer = new Timer();
        timer.schedule(task,1000,1000);
        socketOnline();//socket连接
        daojishiTimer = new Timer();


    }

    private void setLineChart(LineChart chart) {
        chart.setDrawGridBackground(false);
        chart.setDescription("");
        //chart.setVisibleXRangeMaximum(9);
        //chart.setVisibleYRangeMaximum(150, YAxis.AxisDependency.LEFT);

        // 为chart添加空数据
        chart.setData(new LineData());

        // 设置x轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setSpaceBetweenLabels(4);

        // 设置左侧坐标轴
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        //leftAxis.setShowOnlyMinMax(true);
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(Color.parseColor("#eac281"));



        // 设置右侧坐标轴
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }





    /**
     * 获取最后一个LineDataSet的索引
     */
    private int getLastDataSetIndex(LineData lineData) {
        int dataSetCount = lineData.getDataSetCount();
        return dataSetCount > 0 ? (dataSetCount - 1) : 0;
    }

    private LineDataSet createLineDataSet() {
        dataSet = new LineDataSet(null, "DataSet 1");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.WHITE);
        dataSet.setDrawValues(false);
        return dataSet;
    }


    /**
     * 倒计时10秒
     * 10秒结束刷新界面
     */
    class MyTask extends TimerTask{
        @Override
        public void run() {
            timeNum--;
            Message mes = new Message();
            mes.what = timeNum;
            hand.sendMessage(mes);
            if (timeNum == 0) {
                timeNum = 11;
                daojishiTask.cancel();
            }
        }
    }
    Handler hand = new Handler(){
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Integer i = message.what;
            daojishi.setText(String.valueOf(i));
            if (i == 0){
                daojishi.setText("");
                buttonCanClick();
            }
        }
    };

    /**
     * 实时获取当前时间
     */
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            getNowTime();
        }
    };

    /**
     * 获取登录名
     * driverId
     * 获利计算参数
     */
    private void getLoginIdAndHuobiCanshu(){
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        loginStr = userInfo.getString("login", "");
        SharedPreferences driver = getSharedPreferences("driverID", MODE_PRIVATE);
        driverId = driver.getString("driver", "");
//        Bundle bundle = this.getIntent().getExtras();
//        pricesList = bundle.getStringArrayList("prices");
    }

    /**
     * socket连接
     */
    private void socketOnline(){
        new Thread(){
            @Override public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(HOST,PORT),10000);
                    BufferedReader bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String s = bff.readLine();
                    Message msg = new Message();
                    bundle.putString("socket",s);
                    msg.setData(bundle);
                    shandler.sendMessage(msg);
                    ReceiveThread mReceiveThread = new ReceiveThread();
                    mReceiveThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    int ic=0;
    // 定义Handler对象
    private Handler shandler = new Handler() {
        @Override
        // 当有消息发送出来的时候就执行Handler的这个方法
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 处理UI
            Bundle bundle = msg.getData();
            String s = bundle.getString("socket");
            String[] strArray = null;
            strArray = s.split("\\r");
            for (int i = 0; i < strArray.length; i++) {
                String[] array = null;
                array = strArray[i].split(",");
                socketDataArray = array;
                if (socketDataArray[1].equals("CloseOrders")){
                    if (socketDataArray[2].equals(loginStr)){
                        dingdanCloseOrder();
                        Toast.makeText(MainActivity.this, "订单关仓!", Toast.LENGTH_SHORT).show();
                    }
                }else if (socketDataArray[1].equals("Change")){
                    if (socketDataArray[2].equals(loginStr)){
                        if (!socketDataArray[3].equals(driverId)){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("其他客户端登录,退出此客户端!")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //退出登录
                                            try {
                                                System.exit(0);
                                                socket.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            onBackPressed();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    }
                }

                if (socketDataArray.length > 4){

                    if (socketDataArray[2].equals(itemName)){
                        yinglijisuan(socketDataArray);
                       // mLineData = getLineData(200,Float.parseFloat(socketDataArray[3]));

                        LineData lineData = mChart.getData();

                        if (lineData != null) {
                            int indexLast = getLastDataSetIndex(lineData);

                            LineDataSet lastSet = lineData.getDataSetByIndex(indexLast);
                            // set.addEntry(...); // can be called as well
//                            lastSet.setColor(Color.rgb(104,241,175));


                            if (lastSet == null) {
                                lastSet = createLineDataSet();
                                lineData.addDataSet(lastSet);
                            }

                            // 这里要注意，x轴的index是从零开始的
                            // 假设index=2，那么getEntryCount()就等于3了
                            int count = lastSet.getEntryCount();
                            // add a new x-value first 这行代码不能少

                            lineData.addXValue(count + "");

//                            float yValues = (float) (Math.random() * 100);
                            float yValues = Float.parseFloat(socketDataArray[3]);
                            // 位最后一个DataSet添加entry


                            lineData.addEntry(new Entry(yValues, ic), indexLast);
                            ic=ic+1;
                            mChart.notifyDataSetChanged();
                            mChart.invalidate();
                            //mChart.setPivotX(10);

                            if(Winloss){
                                dataSet.resetColors();
                                dataSet.setColor(Color.RED);
                            }else{
                                dataSet.setColor(Color.GREEN);
                            }

                            if(ic>shownum){
                                mChart.setVisibleXRangeMaximum(shownum-1);//设置区域显示的最大点个数
                                mChart.moveViewToX(ic-(shownum));//设置x向左边移动到那个xindex,shownum=10
                                mChart.notifyDataSetChanged();
                                mChart.invalidate();

                                lastSet.removeFirst();
                                mChart.notifyDataSetChanged();


                               // mChart.fitScreen();
                            }
                           // */
                        }
                    }
                }
                for (int j = 0; j < hblist.size(); j++) {
                    if (socketDataArray[2].equals(hblist.get(j)) && nametextView.getText().toString().equals(nameList.get(j))){
                        PriceTxt.setText(socketDataArray[3]);
                    }
                }
            }

        }
    };

    /**
     * socket长连接
     */
    public class ReceiveThread extends Thread{
        @Override
        public void run(){
            while (true) {
                try {
                    if (socket != null && socket.isConnected()) {
                        if (!socket.isInputShutdown()) {
                            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String content = inStream.readLine();
                            Log.i("ssss",content);
                            if (content == null) {
                                continue;
                            }
                            Message msg = new Message();
                            bundle.putString("socket", content);
                            msg.setData(bundle);
                            shandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                        try {
                            socket.connect(new InetSocketAddress(HOST, PORT), 10000);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                }
            }
        }
    }


    /**
     * 订单关单
     */
    private void dingdanCloseOrder(){
        new Thread(zaicangRunnable).start();
//        Toast.makeText(MainActivity.this, "订单关仓!", Toast.LENGTH_SHORT).show();
        if (daojishiTimer != null){
            if (daojishiTask != null){
                daojishiTask.cancel();  //将原任务从队列中移除
            }
            daojishiTask = new MyTask();  // 新建一个任务
            daojishiTimer.schedule(daojishiTask, 1000,1000);
        }
    }

    /**
     *  获取当前时间
     */
    private void getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String s = simpleDateFormat.format(new Date());
        NowHour = Integer.parseInt(s.substring(0, 2));
//        NowMinute = Integer.parseInt(s.substring(3, 5));//截取字符串
        if (NowHour>=9 && NowHour <16){
            Message message = new Message();
            message.what = 0;
            timeHandler.sendMessage(message);
        }else {
            Message message = new Message();
            message.what = 2;
            timeHandler.sendMessage(message);
        }
    }
    Handler timeHandler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Integer i = message.what;
            nametextView.setText(nameList.get(i));
            price = (int) usdList.get(i);
            itemName = hblist.get(i);
            Increment = (double)Incrementlist.get(i);
        }
    };


    /**
     * 网络判断
     */
    private void netAnimation() {
        NetWorkUtils net = new NetWorkUtils();
        int type = net.getAPNType(MainActivity.this);
        if (type == 0) {
            netBtn.setText(R.string.网络);
            netBtn.setTextColor(Color.WHITE);
            //netBtn.setBackgroundColor(Color.BLUE);
            //初始化
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            //设置动画时间
            alphaAnimation.setDuration(10000);
            netBtn.startAnimation(alphaAnimation);
            // netBtn.setBackgroundColor(Color.TRANSPARENT);
            netBtn.setVisibility(View.INVISIBLE);
            Ip = net.getIpAddress();
        } else if (type == 1) {
            netBtn.setText("您当前使用的是wifi网络");
            netBtn.setTextColor(Color.WHITE);
            //初始化
            Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            //设置动画时间
            alphaAnimation.setDuration(10000);
            netBtn.startAnimation(alphaAnimation);
            //netBtn.setBackgroundColor(Color.TRANSPARENT);
            netBtn.setVisibility(View.INVISIBLE);
            Ip = net.getLocalIpAddress(MainActivity.this);
        }
        SharedPreferences IP = getSharedPreferences("IP", MODE_PRIVATE);
        SharedPreferences.Editor ipEditor = IP.edit();
        ipEditor.putString("IP", Ip);
        ipEditor.commit();
    }

    /**
     * 获取货币列表
     */
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
                    String Bh = jsonObject.getString("Bh");
                    String Name = jsonObject.getString("Name");
                    double Increment = jsonObject.getDouble("Increment");
                    usdList.add(jsonObject.getInt("USD") * jsonObject.getInt("Converts"));
                    hblist.add(Bh);//协议名字
                    nameList.add(Name);//汉语名字
                    Incrementlist.add(Increment);
                }
                NAME1 = hblist.get(0);
                NAME2 = hblist.get(2);
                itemName = NAME1;//进入主界面的时候默认刷新恒生指数
                SharedPreferences name = getSharedPreferences("name", MODE_PRIVATE);
                SharedPreferences.Editor editor = name.edit();
                editor.putString("itemName", itemName);
                editor.putString("itemName2", NAME2);
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable HBListRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                parma.put("UserID","");
                parma.put("TaskGuid", "b4026263-704e-4e12-a64d-f79cb42962cc");
                parma.put("DataType", "HBList");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SoapObject string = request.getResult(TRANSFORMDATA, parma.toString());
            String jsonRequest = string.getProperty(0).toString();
            Message message = new Message();
            bundle.putString("HBListkey", jsonRequest);
            message.setData(bundle);
            HBListhandler.sendMessage(message);
        }
    };

    /**
     * 盈利计算
     */
    private void yinglijisuan(String[] dataArray){
        try {
            JSONArray jsonArray = new JSONArray(dingdanString);
            int yingli = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double open = jsonObject.getDouble("OpenPrice");
                int Volume = jsonObject.getInt("Volume");
                String buy = jsonObject.getString("Type");
                Double stop = jsonObject.getDouble("StopLoss");
                Double take = jsonObject.getDouble("TakeProfit");
                String Symbol = jsonObject.getString("Symbol");
                if (Symbol.equals(itemName)){

                }
                if (buy.equals("Buy") && dataArray[2].equals(Symbol)){
                    yingli = (int) ((Double.parseDouble(dataArray[3]) - open) * price * Volume );
                    if (stop >= Double.parseDouble(dataArray[3]) || take <= Double.parseDouble(dataArray[3])){
                        dingdanCloseOrder();
                    }
                }else if (buy.equals("Sell") && dataArray[2].equals(Symbol)){
                    yingli = (int) ((open - Double.parseDouble(dataArray[3]) -Increment) * price * Volume );
                    if (stop <= Double.parseDouble(dataArray[3])+Increment || take >= Double.parseDouble(dataArray[3]) + Increment){
                        dingdanCloseOrder();
                    }
                }
            }
            if (yingli < 0) {//根据正负设置颜色
                Winloss=false;
            } else {
                Winloss=true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * 主界面根据在仓订单刷新界面
     */
    Runnable zaicangRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                parma.put("DriverID", driverId);
                parma.put("TaskGuid", TaskGuid);
                parma.put("UserID","");
                parma.put("DataType", "InOrderList");
                parma.put("LoginID", loginStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SoapObject soapObject = request.getResult(TRANSFORMDATA, parma.toString());
            Message message = new Message();
            bundle.putString("zaicangkey", soapObject.getProperty(0).toString());
            message.setData(bundle);
            zaicanghandler.sendMessage(message);
        }
    };


    /**
     * 根据在仓订单改变主界面显示
     */
    Handler zaicanghandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("zaicangkey");
            dingdanString = string;
            int shoushu = 0;
            try {
                JSONArray jsonArray = new JSONArray(string);
//                dingdanArray =  jsonArray;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject js = jsonArray.getJSONObject(i);
                    shoushu = js.getInt("Volume");
//                    profit = js.getInt("Profit");
                }
                mairuduoshaoshouTex.setText(String.valueOf(shoushu));
//                yingliText.setText(String.valueOf(profit));
//                if (profit < 0) {//根据正负设置颜色
////                    yingliText.setTextColor(Color.parseColor("#0069d5"));
//                } else {
////                    yingliText.setTextColor(Color.parseColor("#fe0000"));
//                }
//                profit = 0;//清零，否则会累加
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 界面创建
     */
    private void createButton() {
        //手数和品种
        shouTxt = (TextView) findViewById(R.id.shoushutextView);
        nametextView = (TextView) findViewById(R.id.nametextView);
        //设置按钮
        settingBtn = (ImageButton) findViewById(R.id.setBtn);
        settingBtn.setOnClickListener(openBrawer);
        //个人中心按钮
        userBtn = (ImageButton) findViewById(R.id.usrBtn);
        userBtn.setOnClickListener(userBtnClick);
        //持仓按钮
        holdButton = (Button) findViewById(R.id.button_hold);
        holdButton.setOnClickListener(holdButtonClick);
        // 看多按钮
        buyMoreButton = (Button) findViewById(R.id.button_buyMore);
        buyMoreButton.setOnClickListener(buyMoreClick);
//        看空按钮
        buyLessButton = (Button) findViewById(R.id.button_buyLess);
        buyLessButton.setOnClickListener(buyLessButtonClick);
//        选择手数按钮,透明盖在委托手数上面
        chooseNum = (Button) findViewById(R.id.chooseNum);
        chooseNum.setOnClickListener(settingBtnClick);
        //最新行情数据
        PriceTxt = (TextView) findViewById(R.id.textView_priceText);
        //网络button
        netBtn = (Button) findViewById(R.id.netImgBtn);
        //主界面盈利的number
//        yingliText = (TextView) findViewById(R.id.textView10);
//        显示买入多少手
        mairuduoshaoshouTex = (TextView) findViewById(R.id.textView6);
//        倒计时
        daojishi = (TextView) findViewById(R.id.daojishi);
    }

    /**
     * 打开浏览器
     */
    View.OnClickListener openBrawer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//
            final Uri uri = Uri.parse("https://xidue.com/hd/hd.html");
            final Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        }
    };
    /**
     * 设置按钮允许点击
     */
    private void buttonCanClick() {
        buyLessButton.setClickable(true);
        buyLessButton.setBackgroundResource(R.drawable.shape);
        buyMoreButton.setClickable(true);
        buyMoreButton.setBackgroundResource(R.drawable.buymore);
    }


    /**
     *  设置按钮不允许被点击
     */
    private void buttonCanNotClick() {
        buyLessButton.setClickable(false);
        buyLessButton.setBackgroundResource(R.drawable.huise);
        buyMoreButton.setClickable(false);
        buyMoreButton.setBackgroundResource(R.drawable.huise);
    }


    /**
     *  持仓按钮点击事件，并传递数据
     */
    View.OnClickListener holdButtonClick = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, holdActivity.class);
            String name = itemName;
            intent.putExtra("name", name);//把本界面选择的货币传递给持仓界面
            startActivityForResult(intent, 0);
        }
    });


    /**
     * 跳转个人中心界面
     */
    View.OnClickListener userBtnClick = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        }
    });

    /**
     * 跳转设置界面
     * 选择货币，和选择手数
     */
    View.OnClickListener settingBtnClick = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View outerView = LayoutInflater.from(MainActivity.this).inflate(R.layout.wheel_view, null);
            wv = (WheelView) outerView.findViewById(R.id.wheel_view_wv);
            wv.setOffset(2);
            wv.setItems(Arrays.asList(shoushu));
            //     保存上次选择的手数
            wv.setSeletion(number);
            wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(int selectedIndex, String item) {
                    shouTxt.setText(item);
                    number = selectedIndex - 2;
                }
            });
            /**
            wv2 = (WheelView) outerView.findViewById(R.id.wheel_view_wv2);
            wv2.setOffset(2);
            wv2.setItems(nameList);
            wv2.setSeletion(category);
            wv2.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(int selectedIndex, String item1) {
                    nametextView.setText(item1);
                    itemName = hblist.get(selectedIndex - 2);
                    category = selectedIndex - 2;
                    price = (int) pricesList.get(selectedIndex - 2);
                    PriceTxt.setText("0.00");
                }
            });
             */

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("设置您委托的产品类型和手数")
                    .setView(outerView)
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    });


    /**
     * 看多按钮点击事件
     */
    View.OnClickListener buyMoreClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                buyMoreButtonClick();

        }
    };


    /**
     * 看多买入
     * 判断是否可以下单
     * 判断是否在交易时间
     */
    private void buyMoreButtonClick() {
         new Thread(kanduoRunnable).start();
         progressDialog = ProgressDialog.show(MainActivity.this, "", "下单中...");
    }

    /**
     * 看多数据请求
     * Runnable
     * handler
     */
    Runnable kanduoRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                parma.put("DriverID", driverId);
                parma.put("UserID","");
                parma.put("TaskGuid", TaskGuid);
                parma.put("DataType", "InOrder");
                parma.put("Login", loginStr);
                parma.put("Symbol", itemName);
                parma.put("Volume", shouTxt.getText().toString());
                parma.put("StopLoss", "8");
                parma.put("TakeProfit", "8");
                parma.put("ProductType","1");
                parma.put("Comment", "Android");
                parma.put("Type", "Buy");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SoapObject soapObject = request.getResult(SETDATA, parma.toString());
            Message message = new Message();
            bundle.putString("buyMore", soapObject.getProperty(0).toString());
            message.setData(bundle);
            buyMoreHandler.sendMessage(message);
        }
    };
    Handler buyMoreHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("buyMore");
            try {
                JSONObject jsonObject = new JSONObject(string);
                String s = jsonObject.getString("OrderNumber");
                if (s.isEmpty()){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(jsonObject.getString("Comment"))
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }else {
                    Toast.makeText(MainActivity.this, "买入成功!", Toast.LENGTH_SHORT).show();
                    buttonCanNotClick();
                    new Thread(zaicangRunnable).start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
//            buttonCanClick();
        }
    };

    /**
     * 看空按钮点击事件
     */
    View.OnClickListener buyLessButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                buyLessButtonClick();
        }
    };
    private void buyLessButtonClick() {
        new Thread(kankongRunnable).start();
        progressDialog = ProgressDialog.show(MainActivity.this, "", "下单中...");
    }


    /**
     * 看空请求数据
     * runnable
     * handler
     */
    Runnable kankongRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                parma.put("DriverID", driverId);
                parma.put("UserID","");
                parma.put("TaskGuid", TaskGuid);
                parma.put("DataType", "InOrder");
                parma.put("Login", loginStr);
                parma.put("Symbol", itemName);
                parma.put("Volume", shouTxt.getText().toString());
                parma.put("StopLoss", "8");
                parma.put("TakeProfit", "8");
                parma.put("ProductType","1");
                parma.put("Comment", "Android");
                parma.put("Type", "Sell");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SoapObject soapObject = request.getResult(SETDATA, parma.toString());
            Message message = new Message();
            bundle.putString("buyLess", soapObject.getProperty(0).toString());
            message.setData(bundle);
            kankonghandle.sendMessage(message);
        }
    };
    Handler kankonghandle = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            Bundle bundle = message.getData();
            String string = bundle.getString("buyLess");
            try {
                JSONObject jsonObject = new JSONObject(string);
                String s = jsonObject.getString("OrderNumber");
                if (s.isEmpty()){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(jsonObject.getString("Comment"))
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }else {
                    Toast.makeText(MainActivity.this, "买入成功!", Toast.LENGTH_SHORT).show();
                    buttonCanNotClick();
                    new Thread(zaicangRunnable).start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
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
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("是否退出登录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //退出登录
                            try {
                                finish();
                                socket.close();
                                socket.isClosed();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setCancelable(false)
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

