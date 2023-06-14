package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.services.route.RouteBusWalkItem;
import com.example.myapplication.long2.CwlRepository;
import com.example.myapplication.long2.model.BusLine;
import com.example.myapplication.long2.model.BusStop;
import com.example.myapplication.long2.model.City;
import com.example.myapplication.long2.ui.ProgressDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;

public class YangMainActivity extends AppCompatActivity {
    private SimpleAdapter simpleAdapter;
    private ToggleButton toggleButton;
    private ListView listView1;
    private TextView textView;
    private boolean Iscollected=false;
    public static int routenum=10;
    private String city_str="default";
    private String city="default";
    private String station_N="default";
    private String FUrl="default";
    private String Url="default";
    private String username="mqy";
    private  BusStop stop=null;
    private String[] routeN=new String[50];
    private String[] routeDR1=new String[50];
    private String[] routeDS1=new String[50];
    private String[] routeT1=new String[50];
    private String[] routeDR2=new String[50];
    private String[] routeDS2=new String[50];
    private String[] routeT2=new String[50];

    private ProgressDialogFragment dialogFragment = new ProgressDialogFragment();
    @SuppressLint("HandlerLeak")
    private Handler handler1 = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                if(msg.arg1==1){
                    System.out.println("get message"+msg.arg1);
                    Iscollected=true;
                    toggleButton.setChecked(Iscollected);
                    System.out.println(Iscollected+"iscollected");
                }else {
                    System.out.println("get message"+msg.arg1);
                    Iscollected=false;
                    toggleButton.setChecked(Iscollected);
                    System.out.println(Iscollected+"iscollected");
                }
            }else if (msg.what==2){
                try {
                    int mark=-1;
                    String result=String.valueOf(msg.obj);
                    JSONObject jsonObject=new JSONObject(result);
                    JSONArray jsonArray=jsonObject.getJSONArray("bus_lines");
                    routenum=jsonArray.length();
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject1=jsonArray.getJSONObject(i);
                        routeN[i]=jsonObject1.getString("name");
                        System.out.println(routeN[i]);
                        routeDS1[i]=jsonObject1.getString("end_stop");
                        routeDS2[i]=jsonObject1.getString("start_stop");
                        if(jsonObject1.getString("end_stop")==null||jsonObject1.getString("start_stop")==null){
                            routeDR1[i]=null;
                            routeDR2[i]=null;
                        }else {
                            routeDR1[i]=jsonObject1.getString("start_stop")+"--";
                            routeDR2[i]=jsonObject1.getString("end_stop")+"--";

                        }
                    }
                    simpleAdapter=new SimpleAdapter(YangMainActivity.this,getData(),R.layout.list_view,
                            new String[]{"routename","routedirection1","routedistance1","routetime1","routedirection2","routedistance2","routetime2"},
                            new int[]{R.id.routename,R.id.routedirection1,R.id.routedistance1,R.id.routetime1,R.id.routedirection2,R.id.routedistance2,R.id.routetime2});
                    listView1.setAdapter(simpleAdapter);
                    System.out.println(Arrays.toString(routeN));
                    dialogFragment.dismiss();
                }catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(e.toString());
                }
            }else if(msg.what==3){
                stop = (BusStop) msg.obj;

            }
        }
    };
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_yang);
        toggleButton=findViewById(R.id.toggleButton2);
        textView=findViewById(R.id.textView);
        listView1=findViewById(R.id.ListView1);
        //根据页面跳转设置变量city，station_N，username
        dialogFragment.show(getSupportFragmentManager(),"data loading");
//        city="南京";
//        city_str="nanjing";
//        station_N="邮电大学东";
        city = getIntent().getStringExtra("city_name");
        city_str = getIntent().getStringExtra("city_str");
        station_N = getIntent().getStringExtra("station");
        textView.setText(station_N);
        FUrl="https://ljm-python.azurewebsites.net/query_favorite?message="+username;
        Thread thread1=new Thread(){
            @Override
            public void run() {
                String result= gethttpresult(FUrl);
                System.out.println(result);
                if(result==null)
                {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"网络连接错误！",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else
                {
                    int Cflag=0;
                    try {
                        JSONArray jsonArray=new JSONArray(result);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            if(jsonObject.getString("city_name").equals(city) && jsonObject.getString("name").equals(station_N)){
                                Cflag=1;
                                break;
                            }
                            boolean flag=jsonObject.getString("city_name").equals(city) && jsonObject.getString("name").equals(station_N);
                            if(!flag&&i==jsonArray.length()-1){
                                Cflag=0;
                            }
                        }
                        Message message=new Message();
                        message.what=1;
                        message.arg1=Cflag;
                        handler1.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println(e.toString());
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(),"文件解析错误1！",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }

            }
        };
        thread1.start();
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleButton.isChecked()){
                    FUrl="https://ljm-python.azurewebsites.net/add_favorite?city="+city_str+"&favorite_type=1&user="+username+"&name="+station_N;
                    new Thread(){
                        @Override
                        public void run() {
                            String result= gethttpresult(FUrl);
                            System.out.println(result+"添加收藏");
                            if(result==null)
                            {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(),"网络连接错误！",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    }.start();
                }else{
                    FUrl="https://ljm-python.azurewebsites.net/query_favorite?message="+username;
                    new Thread(){
                        @Override
                        public void run() {
                            String result= gethttpresult(FUrl);
                            System.out.println(result+"删除收藏");
                            if(result==null)
                            {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(),"网络连接错误！",Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                            else
                            {
                                String id = null;
                                try {
                                    JSONArray jsonArray=new JSONArray(result);
                                    for(int i=0;i<jsonArray.length();i++){
                                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                                        if(jsonObject.getString("city_name").equals(city) && jsonObject.getString("name").equals(station_N)){
                                            id=jsonObject.getString("id");
                                            break;
                                        }
                                    }
                                    FUrl="https://ljm-python.azurewebsites.net/delete_favorite?id="+id;
                                    String result1=gethttpresult(FUrl);
                                    System.out.println(result1);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    System.out.println(e.toString());
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(),"文件解析错误2！",Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }
                        }
                    }.start();
                }
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    toggleButton.setBackgroundResource(R.drawable.favorite_timage);
                }else {
                    toggleButton.setBackgroundResource(R.drawable.favourite_fimage);
                };
            }
        });
        Url="https://ljm-python.azurewebsites.net/station_line_info?city="+city_str+"&station_name="+station_N;
        new Thread(){
            @Override
            public void run() {
                String result= gethttpresult(Url);
                System.out.println(result+" station detail");
                if(result==null)
                {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"网络连接错误！",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else
                {
                    Object Obj=new Object();
                    Obj=result;
                    Message message=new Message();
                    message.what=2;
                    message.obj=Obj;
                    handler1.sendMessage(message);
                    System.out.println("t"+Obj);
                }

                BusStop stop = CwlRepository.Companion.getInstance().getBusStopByCityAndName(
                        new City(city_str,city),station_N
                );

            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                BusStop stop = CwlRepository.Companion.getInstance().getBusStopByCityAndName(
                    new City(city_str,city),station_N
            );
                Message msg=new Message();
                msg.what=3;
                msg.obj=stop;
                handler1.sendMessage(msg);
            }
        }.start();
        System.out.println("uit"+Arrays.toString(routeN));
        try {
            Thread.sleep(300);
        }catch (InterruptedException ex)
        {
            System.out.println("出现异常");
        }
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //跳转到其他页面
                //调用city_str，city，station_N，route_N传递参数
                Intent intent = new Intent(YangMainActivity.this, Bus_Route_Details.class);
                intent.putExtra("city_str", city_str);
                intent.putExtra("city_name", city);

                intent.putExtra("line", routeN[i].split("\\(")[0]);
                Random random = new Random();

                BusLine line = stop.getBusLines().get(0);
                for(BusLine _line : stop.getBusLines()){
                   if(_line.getName().equals(routeN[i])) {
                       line = _line;
                       break;
                   }
                }

                intent.putExtra("xing_Current_site",line.getBusStopNames().get(
                        new Random().nextInt(line.getBusStopNames().size())));

                intent.putExtra("xing_user",username);
                startActivity(intent);
            }
        });
    }
    private List<? extends Map<String,?>> getData(){
        List<Map<String,Object>> list;
        Map<String,Object> map;
        list=new ArrayList<Map<String,Object>>();
        for(int i=0;i<routenum;i++){
            map =new HashMap<String,Object>();
            map.put("routename",routeN[i]);
            map.put("routedirection1",routeDR1[i]);
            map.put("routedistance1",routeDS1[i]);
            map.put("routetime1",routeT1[i]);
            map.put("routedirection2",routeDR2[i]);
            map.put("routedistance2",routeDS2[i]);
            map.put("routetime2",routeT2[i]);
            list.add(map);
        }
        return list;
    }
    public static String gethttpresult(String urlStr){
        try {
            URL url=new URL(urlStr);
            HttpURLConnection connect=(HttpURLConnection)url.openConnection();
            InputStream input=connect.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String line = null;
            System.out.println(connect.getResponseCode());
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
}