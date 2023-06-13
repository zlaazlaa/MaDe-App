package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.core.AMapException;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import overlay.BusLineOverlay;
import util.ToastUtil;

public class Bus_Route_Details extends Activity implements AMap.OnMarkerClickListener,
        AMap.InfoWindowAdapter, AdapterView.OnItemSelectedListener, BusLineSearch.OnBusLineSearchListener,
        View.OnClickListener {

    private RelativeLayout bottomSheet;
    private ImageView arrowImageView;
    private TextView top_message;
    private BusRouteMapView busRouteMapView;

    private Button button_collect;
    private Button reversing;

    private List<String> stopNames;
    private int numStops;
    private int Current_i = 0;
    private List<Double> bus_Time = new LinkedList<>();
    private List<Double> station_time = new LinkedList<>();
    private List<FavoriteItem> favoriteItems = new LinkedList<>();
    private String Current_Favorite_id;
    private int all_ms;

    private String xing_json;

    private static final String xing_city = "city_name";
    private static final String xing_busNumber = "line";
    private static final String xing_Current_site = "xing_Current_site";
    private static final String xing_user = "xing_user";
    private static final String xing_city_code = "xing_city_code";

    private String city = "nanjing";
    private String busNumber = "331";
    private String Current_site = "赣州汽车站";
    private String user = "mqy";
    private String favorite_type = "0";
    private String city_code = "021";
    private String HTTPHOST = "https://ljm-python.azurewebsites.net";
    private String Add_favorite_http = "https://ljm-python.azurewebsites.net/add_favorite";
    private String Query_favorite_http = "https://ljm-python.azurewebsites.net/query_favorite";
    private String Delete_favorite_http = "https://ljm-python.azurewebsites.net/delete_favorite";

    private String httpResult = "OK";
    private BusRoute busRoute;

    private TextView bus_route_id;
    private TextView origin_station;
    private TextView end_station;
    private TextView all_miles;
    private Button next_bus_1;
    private Button next_bus_2;
    private TextView next_bus_message_1;
    private TextView next_bus_message_2;
    private TextView next_bus_number;
    private TextView Current_size_view;
    private MapView mapView;

    private ImageView loadingImage;
    private Handler handler;
    private boolean isClickable = true;
    private ProgressBar progressBar;

    private int revering_switch = 0;

    //定时查询公交车到达时间的间隔
    private static final long ONE_MINUTE = 60 * 1000;

    private Runnable runnable;

    private String BUS_NUM;
    private String CITY_ID;
    private AMap aMap;
    private ProgressDialog progDialog = null;// 进度框
    private EditText searchName;// 输入公交线路名称
    private Spinner selectCity;// 选择城市下拉列表
    private String[] itemCitys = { "北京-010", "郑州-0371", "上海-021" };
    private String cityCode = "";// 城市区号
    private int currentpage = 0;// 公交搜索当前页，第一页从0开始
    private BusLineResult busLineResult;// 公交线路搜索返回的结果
    private List<BusLineItem> lineItems = null;// 公交线路搜索返回的busline
    private BusLineQuery busLineQuery;// 公交线路查询的查询类

    private BusLineSearch busLineSearch;// 公交线路列表查询

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route_details);

        Intent intent = getIntent();
        if(intent != null){
            if(intent.getStringExtra(xing_city) != null){
                city = intent.getStringExtra(xing_city);
            }
            if(intent.getStringExtra(xing_busNumber) != null){
                busNumber = intent.getStringExtra(xing_busNumber);
            }
            if(intent.getStringExtra(xing_user) != null){
                user = intent.getStringExtra(xing_user);
            }
            if(intent.getStringExtra(xing_Current_site) != null){
                Current_site = intent.getStringExtra(xing_Current_site);
            }
            if(intent.getStringExtra(xing_city_code) != null){
                city_code = intent.getStringExtra(xing_city_code);
            }
        }

        bus_route_id = findViewById(R.id.bus_route_id);
        button_collect = findViewById(R.id.button_collect);
        reversing = findViewById(R.id.reversing);
        end_station = findViewById(R.id.end_station);
        top_message = findViewById(R.id.top_message);
        origin_station = findViewById(R.id.origin_station);
        all_miles = findViewById(R.id.all_miles);
        next_bus_1 = findViewById(R.id.next_bus_1);
        next_bus_2 = findViewById(R.id.next_bus_2);
        next_bus_message_1 = findViewById(R.id.next_bus_message_1);
        next_bus_message_2 = findViewById(R.id.next_bus_message_2);
        next_bus_number = findViewById(R.id.next_bus_number);
        loadingImage = findViewById(R.id.loading_image);
        progressBar = findViewById(R.id.progress_bar);
        Current_size_view = findViewById(R.id.Current_site);

        // Adjust the size and position of the components based on the screen size

// Get the display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

// Get the screen width and height in pixels
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

// Determine the screen size category based on the screen width
        int screenSizeCategory;
        if (screenWidth < 480) {
            screenSizeCategory = 0; // Small screens
        } else if (screenWidth < 720) {
            screenSizeCategory = 1; // Normal screens
        } else if (screenWidth < 1080) {
            screenSizeCategory = 2; // Large screens
        } else {
            screenSizeCategory = 3; // Extra-large screens
        }

// Apply scaling factors and position offsets based on the screen size category
        float scaleFactor;
        int positionOffset;
        switch (screenSizeCategory) {
            case 0: // Small screens
                scaleFactor = 0.75f;
                positionOffset = 20; // Example position offset
                break;
            case 1: // Normal screens
                scaleFactor = 1.0f;
                positionOffset = 30; // Example position offset
                break;
            case 2: // Large screens
                scaleFactor = 1.25f;
                positionOffset = 40; // Example position offset
                break;
            case 3: // Extra-large screens
                scaleFactor = 1.0f;
                positionOffset = 0; // Example position offset
                break;
            default:
                scaleFactor = 1.0f; // Default scaling factor
                positionOffset = 10; // Default position offset
                break;
        }
        Log.d("scaleFactor",scaleFactor+"");
        Log.d("positionOffset",positionOffset+"");

// Adjust the size and position of the components
        TextView topMessageTextView = findViewById(R.id.top_message);
        ImageView arrowImageView = findViewById(R.id.arrowImageView);
        TextView busRouteIdTextView = findViewById(R.id.bus_route_id);
        Button collectButton = findViewById(R.id.button_collect);
        TextView originStationTextView = findViewById(R.id.origin_station);
        ImageView arrowLeftImageView = findViewById(R.id.ic_arrow_left);
        TextView endStationTextView = findViewById(R.id.end_station);
        Button reversingButton = findViewById(R.id.reversing);
        TextView allMilesTextView = findViewById(R.id.all_miles);
        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        TextView currentSiteTextView = findViewById(R.id.Current_site);
        TextView nextBusNumberTextView = findViewById(R.id.next_bus_number);
        Button nextBus1Button = findViewById(R.id.next_bus_1);
        TextView nextBusMessage1TextView = findViewById(R.id.next_bus_message_1);
        Button nextBus2Button = findViewById(R.id.next_bus_2);
        TextView nextBusMessage2TextView = findViewById(R.id.next_bus_message_2);
        BusRouteMapView busRouteMapTextView = findViewById(R.id.busRouteMapView);

// Adjust the size and position of the components
        ViewGroup.LayoutParams topMessageLayoutParams = topMessageTextView.getLayoutParams();
        topMessageLayoutParams.width = (int) (topMessageLayoutParams.width * scaleFactor);
        topMessageLayoutParams.height = (int) (topMessageLayoutParams.height * scaleFactor);
        topMessageTextView.setLayoutParams(topMessageLayoutParams);

        ViewGroup.LayoutParams arrowImageLayoutParams = arrowImageView.getLayoutParams();
        arrowImageLayoutParams.width = (int) (arrowImageLayoutParams.width * scaleFactor);
        arrowImageLayoutParams.height = (int) (arrowImageLayoutParams.height * scaleFactor);
        arrowImageView.setLayoutParams(arrowImageLayoutParams);

        ViewGroup.LayoutParams busRouteIdLayoutParams = busRouteIdTextView.getLayoutParams();
        busRouteIdLayoutParams.width = (int) (busRouteIdLayoutParams.width * scaleFactor);
        busRouteIdLayoutParams.height = (int) (busRouteIdLayoutParams.height * scaleFactor);
        busRouteIdTextView.setLayoutParams(busRouteIdLayoutParams);

// Adjust the position of the components
        RelativeLayout.LayoutParams collectButtonLayoutParams = (RelativeLayout.LayoutParams) collectButton.getLayoutParams();
        collectButtonLayoutParams.leftMargin += positionOffset;
        collectButtonLayoutParams.topMargin += positionOffset;
        collectButton.setLayoutParams(collectButtonLayoutParams);

        ConstraintLayout.LayoutParams originStationLayoutParams = (ConstraintLayout.LayoutParams) originStationTextView.getLayoutParams();
        originStationLayoutParams.leftMargin += positionOffset;
        originStationLayoutParams.topMargin += positionOffset;
        originStationTextView.setLayoutParams(originStationLayoutParams);

        ConstraintLayout.LayoutParams arrowLeftImageLayoutParams = (ConstraintLayout.LayoutParams) arrowLeftImageView.getLayoutParams();
        arrowLeftImageLayoutParams.leftMargin += positionOffset;
        arrowLeftImageLayoutParams.topMargin += positionOffset;
        arrowLeftImageView.setLayoutParams(arrowLeftImageLayoutParams);

        ConstraintLayout.LayoutParams endStationLayoutParams = (ConstraintLayout.LayoutParams) endStationTextView.getLayoutParams();
        endStationLayoutParams.leftMargin += positionOffset;
        endStationLayoutParams.topMargin += positionOffset;
        endStationTextView.setLayoutParams(endStationLayoutParams);

        ConstraintLayout.LayoutParams reversingButtonLayoutParams = (ConstraintLayout.LayoutParams) reversingButton.getLayoutParams();
        reversingButtonLayoutParams.leftMargin += positionOffset;
        reversingButtonLayoutParams.topMargin += positionOffset;
        reversingButton.setLayoutParams(reversingButtonLayoutParams);

// Adjust the size of the components
        LinearLayout.LayoutParams allMilesLayoutParams = (LinearLayout.LayoutParams) allMilesTextView.getLayoutParams();
        allMilesLayoutParams.width = (int) (allMilesLayoutParams.width * scaleFactor);
        allMilesLayoutParams.height = (int) (allMilesLayoutParams.height * scaleFactor);
        allMilesTextView.setLayoutParams(allMilesLayoutParams);

        ConstraintLayout.LayoutParams frameLayoutParams = (ConstraintLayout.LayoutParams) frameLayout.getLayoutParams();
        frameLayoutParams.width = (int) (frameLayoutParams.width * scaleFactor);
        frameLayoutParams.height = (int) (frameLayoutParams.height * scaleFactor);
        frameLayout.setLayoutParams(frameLayoutParams);

        FrameLayout.LayoutParams currentSiteLayoutParams = (FrameLayout.LayoutParams) currentSiteTextView.getLayoutParams();
        currentSiteLayoutParams.width = (int) (currentSiteLayoutParams.width * scaleFactor);
        currentSiteLayoutParams.height = (int) (currentSiteLayoutParams.height * scaleFactor);
        currentSiteTextView.setLayoutParams(currentSiteLayoutParams);

        FrameLayout.LayoutParams nextBusNumberLayoutParams = (FrameLayout.LayoutParams) nextBusNumberTextView.getLayoutParams();
        nextBusNumberLayoutParams.width = (int) (nextBusNumberLayoutParams.width * scaleFactor);
        nextBusNumberLayoutParams.height = (int) (nextBusNumberLayoutParams.height * scaleFactor);
        nextBusNumberTextView.setLayoutParams(nextBusNumberLayoutParams);

        FrameLayout.LayoutParams nextBus1LayoutParams = (FrameLayout.LayoutParams) nextBus1Button.getLayoutParams();
        nextBus1LayoutParams.width = (int) (nextBus1LayoutParams.width * scaleFactor);
        nextBus1LayoutParams.height = (int) (nextBus1LayoutParams.height * scaleFactor);
        nextBus1Button.setLayoutParams(nextBus1LayoutParams);

        FrameLayout.LayoutParams nextBusMessage1LayoutParams = (FrameLayout.LayoutParams) nextBusMessage1TextView.getLayoutParams();
        nextBusMessage1LayoutParams.width = (int) (nextBusMessage1LayoutParams.width * scaleFactor);
        nextBusMessage1LayoutParams.height = (int) (nextBusMessage1LayoutParams.height * scaleFactor);
        nextBusMessage1TextView.setLayoutParams(nextBusMessage1LayoutParams);

        FrameLayout.LayoutParams nextBus2LayoutParams = (FrameLayout.LayoutParams) nextBus2Button.getLayoutParams();
        nextBus2LayoutParams.width = (int) (nextBus2LayoutParams.width * scaleFactor);
        nextBus2LayoutParams.height = (int) (nextBus2LayoutParams.height * scaleFactor);
        nextBus2Button.setLayoutParams(nextBus2LayoutParams);

        FrameLayout.LayoutParams nextBusMessage2LayoutParams = (FrameLayout.LayoutParams) nextBusMessage2TextView.getLayoutParams();
        nextBusMessage2LayoutParams.width = (int) (nextBusMessage2LayoutParams.width * scaleFactor);
        nextBusMessage2LayoutParams.height = (int) (nextBusMessage2LayoutParams.height * scaleFactor);
        nextBusMessage2TextView.setLayoutParams(nextBusMessage2LayoutParams);

        RelativeLayout.LayoutParams busRouteMapLayoutParams = (RelativeLayout.LayoutParams) busRouteMapTextView.getLayoutParams();
        busRouteMapLayoutParams.width = (int) (busRouteMapLayoutParams.width * scaleFactor);
        busRouteMapLayoutParams.height = (int) (busRouteMapLayoutParams.height * scaleFactor);
        busRouteMapTextView.setLayoutParams(busRouteMapLayoutParams);


        Current_size_view.setText(Current_site);

        SharedPreferences mSharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        busRouteMapView = findViewById(R.id.busRouteMapView);
        try {
            initData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                xing_json = mSharedPreferences.getString("BusRouteData", "");
                BusRoute temp = new BusRoute();
                try {
                    busRoute = temp.fromJson(xing_json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(busRoute != null && busRoute.getName().equals(busNumber)){
                    this.cancel();
                }
            }
        },0,500);
        if(xing_json == null){
            xing_json = "{\"id\": \"900000170163\", \"name\": \"311路(民生医院--潮阳高铁站)\", \"type\": \"普通公交\", \"polyline\": \"116.45908,23.248602;116.458012,23.248346;116.457891,23.248316;116.457704,23.24826;116.457643,23.248242;116.457387,23.248164;116.456923,23.24799;116.456636,23.24786;116.455751,23.247452;116.455582,23.247378;116.455477,23.247322;116.455074,23.247144;116.455043,23.247127;116.454913,23.247066;116.454609,23.246927;116.454457,23.246853;116.454249,23.246758;116.453928,23.246606;116.453364,23.246328;116.452878,23.246094;116.452561,23.245946;116.452374,23.245859;116.452296,23.245825;116.451732,23.245586;116.451072,23.245291;116.449965,23.244757;116.449783,23.244674;116.449444,23.244518;116.449214,23.244418;116.448785,23.244227;116.448785,23.244223;116.448685,23.244184;116.448116,23.243967;116.447396,23.243746;116.447018,23.24365;116.446176,23.243472;116.44582,23.243394;116.445013,23.243229;116.444236,23.24306;116.444201,23.243056;116.44332,23.242878;116.442439,23.242674;116.441745,23.24253;116.44138,23.242457;116.440942,23.242365;116.440712,23.242313;116.439635,23.242088;116.439566,23.242075;116.439427,23.242049;116.438628,23.241888;116.438624,23.241888;116.438464,23.241853;116.437947,23.241801;116.437691,23.241797;116.437196,23.241832;116.436871,23.241888;116.436146,23.242049;116.435937,23.242101;116.43526,23.242261;116.434696,23.2424;116.43424,23.242513;116.43342,23.242721;116.433103,23.242799;116.432756,23.242878;116.432448,23.242982;116.432222,23.243095;116.431927,23.243268;116.431597,23.243481;116.430825,23.243971;116.430278,23.244323;116.42974,23.244661;116.429475,23.244822;116.428902,23.245195;116.427743,23.245968;116.42727,23.246302;116.426888,23.246558;116.426424,23.246801;116.426098,23.246957;116.425855,23.247066;116.42497,23.247491;116.424262,23.247817;116.423312,23.248242;116.422526,23.248589;116.422092,23.248793;116.421424,23.249093;116.421311,23.249145;116.420946,23.249314;116.420252,23.249627;116.419831,23.249813;116.419605,23.249918;116.419397,23.250013;116.419058,23.250169;116.418568,23.250382;116.417821,23.250694;116.417348,23.250838;116.417253,23.250868;116.416762,23.250946;116.416328,23.25099;116.415812,23.251033;116.415386,23.251063;116.415052,23.251089;116.414323,23.251155;116.414128,23.251172;116.414017,23.251182;116.41385,23.251198;116.413346,23.251241;116.412548,23.251306;116.412231,23.251332;116.411875,23.251372;116.411806,23.251493;116.41181,23.251545;116.411866,23.251667;116.412144,23.251849;116.41214,23.251845;116.412444,23.252044;116.412835,23.252354;116.413069,23.252539;116.413398,23.25276;116.413685,23.252951;116.414032,23.253181;116.414076,23.253216;116.414674,23.25362;116.414766,23.253676;116.415295,23.254028;116.415851,23.254371;116.416198,23.254588;116.416532,23.254787;116.416711,23.254891;116.417079,23.255104;116.41717,23.255156;116.417166,23.255152;116.417378,23.255278;116.417574,23.255382;116.41776,23.25549;116.418173,23.255725;116.418511,23.25592;116.418594,23.255964;116.41921,23.256302;116.419766,23.256589;116.420161,23.256762;116.420855,23.257044;116.422743,23.257734;116.422986,23.257817;116.423524,23.25799;116.42352,23.25799;116.423715,23.258056;116.4249,23.258464;116.425755,23.258837;116.427192,23.259605;116.427192,23.259601;116.427396,23.259714;116.427448,23.259735;116.428763,23.260339;116.430473,23.261102;116.430694,23.261207;116.430656,23.261504;116.430621,23.261775;116.430621,23.261771;116.430543,23.262331;116.430486,23.262769;116.430139,23.265421;116.430074,23.26592;116.430039,23.266289;116.430022,23.266393;116.429848,23.267422;116.429653,23.268776;116.429627,23.269041;116.429575,23.269583;116.429549,23.269818;116.429536,23.269944;116.42947,23.270508;116.429401,23.271133;116.429397,23.271194;116.429345,23.271528;116.429319,23.271723;116.429319,23.271719;116.429201,23.272604;116.429102,23.273377;116.428889,23.274444;116.428915,23.274627;116.428958,23.274661;116.429028,23.274766;116.429588,23.275477;116.430104,23.276137;116.430382,23.276476;116.430438,23.276541;116.430434,23.276541;116.430807,23.276984;116.430942,23.277118;116.43151,23.277695;116.431606,23.277747;116.432101,23.277925;116.432826,23.278194;116.433103,23.278294;116.433126,23.278302;116.433481,23.278429;116.433477,23.278429;116.433633,23.278481;116.433919,23.278589;116.434492,23.278811;116.434701,23.278889;116.435152,23.279062;116.435252,23.279102;116.435629,23.279249;116.435734,23.279288;116.436801,23.279705;116.437643,23.280026;116.438212,23.280234;116.43885,23.280473;116.439349,23.280703;116.439475,23.280777;116.439596,23.280872;116.439705,23.281042;116.439939,23.281732;116.440056,23.282083;116.440473,23.283338;116.440929,23.284666;116.441259,23.285794;116.441263,23.285911;116.441246,23.286003;116.441176,23.286055;116.441363,23.286137;116.442509,23.286541;116.442878,23.286701;116.442956,23.286732;116.442986,23.286745;116.443316,23.286979;116.443872,23.28737;116.443945,23.287418;116.444214,23.2876;116.445122,23.288238;116.445473,23.288494;116.445794,23.288724;116.44579,23.288724;116.446706,23.289392;116.446953,23.289575;116.447721,23.290135;116.448364,23.290599;116.448932,23.291011;116.4499,23.291714;116.45013,23.291884;116.450126,23.291884;116.451376,23.292791;116.45168,23.293012;116.451701,23.293064;116.451276,23.293411;116.450868,23.293746;116.44997,23.294436;116.447539,23.296424;116.447422,23.296519;116.447244,23.296671;116.447127,23.296801;116.446875,23.297135;116.446753,23.297326;116.446545,23.297795;116.446332,23.298451;116.446185,23.298889;116.446124,23.299076;116.44592,23.299692;116.445747,23.300117;116.445495,23.30056;116.445334,23.300786;116.444306,23.302001;116.443798,23.302587;116.443728,23.302665;116.44319,23.303286;116.442691,23.303863;116.44178,23.304909;116.441632,23.305074;116.441506,23.30523;116.440742,23.306107;116.440013,23.306949;116.439939,23.307036;116.43895,23.308168;116.437852,23.309444;116.437786,23.309518;116.4377,23.309618;116.436697,23.310764;116.435885,23.31171;116.435816,23.311784;116.435716,23.311901;116.43533,23.312344;116.43477,23.312986;116.43447,23.313333;116.433937,23.313958;116.433555,23.314392;116.433103,23.314905;116.433095,23.314918;116.4324,23.315712;116.432326,23.315799;116.43201,23.316207;116.431658,23.31671;116.431484,23.316979;116.430916,23.317852;116.430725,23.318142;116.429974,23.319306;116.429916,23.319382;116.429653,23.319727;116.429497,23.319926;116.429188,23.320234;116.42553,23.323086;116.425451,23.323134;116.424965,23.32352;116.424861,23.323602;116.424388,23.323971;116.421957,23.325868;116.421836,23.325955;116.421766,23.326007;116.420734,23.326806;116.419353,23.327904;116.418937,23.328216;116.419262,23.328255;116.419258,23.328255;116.41934,23.328264;116.419991,23.328268;116.420807,23.328199;116.422049,23.328069;116.422326,23.328056;116.422487,23.32809;116.423125,23.328325;116.42349,23.328516;116.42316,23.32941;116.422882,23.330009;116.422808,23.330182;116.422622,23.330629;116.422539,23.330855;116.422335,23.331302;116.422261,23.331467;116.422127,23.33178;116.421944,23.332192;116.42171,23.332717;116.421419,23.333351;116.421354,23.333498;116.421324,23.333572;116.421306,23.333607;116.421215,23.333837;116.420924,23.334501;116.420764,23.334957;116.420629,23.335334;116.420595,23.335447;116.420546,23.335591;116.420512,23.33569;116.42049,23.335742;116.42046,23.335855;116.420291,23.336463;116.420234,23.33668;116.419779,23.338585;116.419696,23.338976;116.419618,23.339336;116.419457,23.339978;116.419414,23.340156;116.419323,23.340612;116.419284,23.340786;116.419171,23.341285;116.419049,23.341901;116.419023,23.342005;116.418898,23.342569;116.418741,23.343038;116.418668,23.34322;116.41855,23.343446;116.418398,23.343663;116.418212,23.343885;116.417648,23.344479;116.417569,23.344596;116.417101,23.345017;116.41691,23.345139;116.416623,23.34526;116.416376,23.345295;116.416107,23.345291;116.415556,23.345226;116.415373,23.34523;116.415165,23.345286;116.415109,23.345317;116.414896,23.345443;116.414614,23.345642;116.414119,23.345946;116.413659,23.346233;116.412934,23.346662;116.412804,23.346766;116.412765,23.346819;116.412721,23.346931;116.412721,23.346927;116.41253,23.347378;116.412422,23.347569;116.412361,23.347652;116.412261,23.347721;116.411992,23.347869;116.411866,23.347908;116.411211,23.348095;116.411128,23.348116;116.411059,23.348138;116.409826,23.348472;116.409748,23.348494;116.40967,23.348516;116.409466,23.348568;116.409219,23.348659;116.409123,23.348715;116.409032,23.348785;116.408954,23.348893;116.408659,23.349748;116.408251,23.350951;116.408116,23.351285;116.408103,23.351306;116.408043,23.351424;116.407821,23.351723;116.407799,23.352222;116.407782,23.352595;116.407786,23.352682;116.407804,23.353003;116.407799,23.35339;116.407799,23.35365;116.407821,23.353958;116.407821,23.354002;116.407843,23.354319;116.407847,23.354362;116.407873,23.35503;116.408021,23.355894;116.408142,23.356584;116.408238,23.356931;116.408368,23.3574;116.408885,23.357339;116.40921,23.357305;116.409206,23.357305;116.409444,23.357279;116.410317,23.357187;116.411128,23.35704;116.41122,23.357023;116.411736,23.356931;116.411892,23.356905;116.412318,23.356832;116.412921,23.356727;116.413103,23.356688;116.414397,23.356428;116.41559,23.356181;116.416888,23.355855;116.41773,23.355655;116.417969,23.355595;116.418841,23.355391;116.419227,23.355317;116.419557,23.355247;116.420265,23.355104;116.42059,23.356189;116.42079,23.356845;116.420825,23.356931;116.420937,23.357244;116.421141,23.357891;116.421137,23.357891;116.421172,23.357999;116.421393,23.358702;116.421532,23.359301;116.421571,23.359692;116.421576,23.360156;116.42158,23.36036;116.421584,23.360464;116.421584,23.36046;116.421654,23.361914;116.421801,23.363134;116.421866,23.363585;116.421984,23.364436;116.421988,23.36447;116.421988,23.364466;116.422148,23.365365;116.422192,23.365543;116.422435,23.366506;116.4226,23.367205;116.422661,23.367522;116.422734,23.367917;116.422821,23.368338;116.422947,23.368906;116.42303,23.369184;116.423507,23.37076;116.423641,23.371194;116.423663,23.371341;116.423655,23.37145;116.423646,23.371567;116.423598,23.371736;116.423872,23.372031;116.423872,23.372027;116.423989,23.372157;116.424345,23.372587;116.424835,23.373433;116.424883,23.373524;116.424883,23.37352;116.425924,23.37549;116.426528,23.37661;116.426571,23.376684;116.426497,23.376775;116.426437,23.376897;116.426393,23.376988;116.425525,23.378902;116.425438,23.379093;116.425356,23.379271;116.425265,23.379327;116.424961,23.3799;116.424505,23.38089;116.424353,23.381003;116.42421,23.381055;116.424167,23.38105;116.424163,23.381055;116.424066,23.381055;116.423815,23.381054\", \"all_station\": [{\"sequence\": \"1\", \"id\": \"BV11631848\", \"name\": \"民生医院\", \"location\": \"116.45908,23.248602\"}, {\"sequence\": \"2\", \"id\": \"BV11521246\", \"name\": \"四海工业城\", \"location\": \"116.448785,23.244227\"}, {\"sequence\": \"3\", \"id\": \"BV11538973\", \"name\": \"金光路口站\", \"location\": \"116.421424,23.249093\"}, {\"sequence\": \"4\", \"id\": \"BV11467197\", \"name\": \"汕尾村\", \"location\": \"116.417348,23.250838\"}, {\"sequence\": \"5\", \"id\": \"BV11467177\", \"name\": \"粤运客运站\", \"location\": \"116.414017,23.251182\"}, {\"sequence\": \"6\", \"id\": \"BV11538974\", \"name\": \"广祥路口站\", \"location\": \"116.412835,23.252354\"}, {\"sequence\": \"7\", \"id\": \"BV10423727\", \"name\": \"环美路口\", \"location\": \"116.416711,23.254891\"}, {\"sequence\": \"8\", \"id\": \"BV10423774\", \"name\": \"环山路口\", \"location\": \"116.42352,23.25799\"}, {\"sequence\": \"9\", \"id\": \"BV10425986\", \"name\": \"南里学校\", \"location\": \"116.427192,23.259601\"}, {\"sequence\": \"10\", \"id\": \"BV11587908\", \"name\": \"峡华路\", \"location\": \"116.430656,23.261504\"}, {\"sequence\": \"11\", \"id\": \"BV11587793\", \"name\": \"义英\", \"location\": \"116.430022,23.266393\"}, {\"sequence\": \"12\", \"id\": \"BV11587918\", \"name\": \"义华路\", \"location\": \"116.42947,23.270508\"}, {\"sequence\": \"13\", \"id\": \"BV11587910\", \"name\": \"北环路口\", \"location\": \"116.429319,23.271719\"}, {\"sequence\": \"14\", \"id\": \"BV11587907\", \"name\": \"练江大桥\", \"location\": \"116.430434,23.276541\"}, {\"sequence\": \"15\", \"id\": \"BV11587804\", \"name\": \"铜盂镇政府\", \"location\": \"116.433126,23.278302\"}, {\"sequence\": \"16\", \"id\": \"BV11587915\", \"name\": \"铜盂医院\", \"location\": \"116.435252,23.279102\"}, {\"sequence\": \"17\", \"id\": \"BV09070690\", \"name\": \"港口(招呼站)\", \"location\": \"116.44579,23.288724\"}, {\"sequence\": \"18\", \"id\": \"BV10423178\", \"name\": \"铜盂路口\", \"location\": \"116.451276,23.293411\"}, {\"sequence\": \"19\", \"id\": \"BV10423174\", \"name\": \"桶盘村\", \"location\": \"116.446185,23.298889\"}, {\"sequence\": \"20\", \"id\": \"BV10241129\", \"name\": \"洋美\", \"location\": \"116.443798,23.302587\"}, {\"sequence\": \"21\", \"id\": \"BV10241128\", \"name\": \"河陇\", \"location\": \"116.439939,23.307036\"}, {\"sequence\": \"22\", \"id\": \"BV10242182\", \"name\": \"新桥\", \"location\": \"116.4324,23.315712\"}, {\"sequence\": \"23\", \"id\": \"BV10241544\", \"name\": \"灵山寺路口\", \"location\": \"116.429916,23.319382\"}, {\"sequence\": \"24\", \"id\": \"BV10423181\", \"name\": \"新兴\", \"location\": \"116.421324,23.333572\"}, {\"sequence\": \"25\", \"id\": \"BV10910893\", \"name\": \"前进路口\", \"location\": \"116.420546,23.335591\"}, {\"sequence\": \"26\", \"id\": \"BV11587917\", \"name\": \"华侨医院\", \"location\": \"116.419696,23.338976\"}, {\"sequence\": \"27\", \"id\": \"BV10684926\", \"name\": \"华光\", \"location\": \"116.419023,23.342005\"}, {\"sequence\": \"28\", \"id\": \"BV10748679\", \"name\": \"茂业电讯\", \"location\": \"116.409748,23.348494\"}, {\"sequence\": \"29\", \"id\": \"BV10800936\", \"name\": \"阳光百汇\", \"location\": \"116.408103,23.351306\"}, {\"sequence\": \"30\", \"id\": \"BV10423198\", \"name\": \"谷华路\", \"location\": \"116.409206,23.357305\"}, {\"sequence\": \"31\", \"id\": \"BV10423195\", \"name\": \"上堡中学路口\", \"location\": \"116.421137,23.357891\"}, {\"sequence\": \"32\", \"id\": \"BV10800917\", \"name\": \"官田路口\", \"location\": \"116.421988,23.364466\"}, {\"sequence\": \"33\", \"id\": \"BV10423172\", \"name\": \"新坡\", \"location\": \"116.423655,23.37145\"}, {\"sequence\": \"34\", \"id\": \"BV11587805\", \"name\": \"仙波中学\", \"location\": \"116.424883,23.37352\"}, {\"sequence\": \"35\", \"id\": \"BV11587919\", \"name\": \"高铁站路口\", \"location\": \"116.426393,23.376988\"}, {\"sequence\": \"36\", \"id\": \"BV10800934\", \"name\": \"潮阳高铁站\", \"location\": \"116.423815,23.381054\"}]}";
            BusRoute temp = new BusRoute();
            try {
                busRoute = temp.fromJson(xing_json);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            stopNames = new LinkedList<>();
            for (BusStation allStation : busRoute.getAllStations()) {
                stopNames.add(allStation.getName());
            }
            numStops = stopNames.size();

            busRouteMapView.setBusRouteData(numStops, stopNames, Current_site);
            xing_json = gson.toJson(busRoute);
            mEditor.putString("BusRouteData", xing_json);
            mEditor.commit();
        }
        busRoute = gson.fromJson(xing_json,BusRoute.class);

        if(busRoute != null){
            stopNames = new LinkedList<>();
            for (BusStation allStation : busRoute.getAllStations()) {
                stopNames.add(allStation.getName());
            }
            numStops = stopNames.size();
            Collections.reverse(stopNames);
            if(revering_switch%2 == 1){
                Collections.reverse(stopNames);
            }
            revering_switch++;

            for (int i=0; i<busRoute.getAllStations().size(); i++) {
                if(busRoute.getAllStations().get(i).getName().equals(Current_site)){
                    Current_i = i;
                    break;
                }
            }

            String tmp = end_station.getText().toString();
            end_station.setText(origin_station.getText().toString());
            origin_station.setText(tmp);

            busRouteMapView.setBusRouteData(numStops, stopNames, Current_site);
        }

        bus_route_id.setText(busRoute.getName()+"路");

        if(busRoute.getAllStations().get(busRoute.getAllStations().size()-1).getName().length() >= 4){
            end_station.setText(busRoute.getAllStations().get(busRoute.getAllStations().size()-1).getName().substring(0,4));
        }else {
            end_station.setText(busRoute.getAllStations().get(busRoute.getAllStations().size()-1).getName());
        }
        if(busRoute.getAllStations().get(0).getName().length() >= 4){
            origin_station.setText(busRoute.getAllStations().get(0).getName().substring(0,4));
        }else {
            origin_station.setText(busRoute.getAllStations().get(0).getName());
        }

        initBus();
        all_miles.setText("首05:00 末21:30 | 全程:"+all_ms+"公里 | 票价:2元");

        // Create a TimerTask that will be executed every minute
        TimerTask task = new TimerTask() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                initBus();
                List<Double> buses = new LinkedList<>();
                double tmp = 0;
                if(Current_i == 0){
                    Current_size_view.setText("您已在起点站");
                    next_bus_number.setText("最近0班");

                    next_bus_1.setText("--分钟");
                    next_bus_message_1.setText("还有--公里");
                    next_bus_2.setText("--分钟");
                    next_bus_message_2.setText("还有--公里");
                    return;
                }
                for (int i=0; i<Current_i ; i++) {
                    tmp += station_time.get(i);
                }
                for (Double aDouble : bus_Time) {
                    if(aDouble%tmp < tmp+1){
                        buses.add((aDouble%tmp)*10);
                    }
                }
                // Create a DecimalFormat instance with the desired format pattern
                DecimalFormat decimalFormat = new DecimalFormat("#0.0");
                if(buses.size() == 0){
                    next_bus_number.setText("最近0班");

                    next_bus_1.setText("--分钟");
                    next_bus_message_1.setText("还有--公里");
                    next_bus_2.setText("--分钟");
                    next_bus_message_2.setText("还有--公里");

                }
                if(buses.size()==1){
                    next_bus_number.setText("最近1班");

                    // Format the result to two decimal places
                    String formattedResult = decimalFormat.format(tmp-buses.get(buses.size()-1));
                    next_bus_1.setText(formattedResult+"分钟");
                    next_bus_message_1.setText("还有"+decimalFormat.format((Double.parseDouble(formattedResult)*Math.random()/2+0.1))+"公里");
                    next_bus_2.setText("--分钟");
                    next_bus_message_2.setText("还有--公里");
                }
                if(buses.size()>=2){
                    next_bus_number.setText("最近2班");

                    // Format the result to two decimal places
                    String formattedResult = decimalFormat.format(Math.floor(Math.abs(tmp-buses.get(buses.size()-1))));
                    next_bus_1.setText(formattedResult.substring(0,formattedResult.length()-2)+"分钟");
                    next_bus_message_1.setText("还有"+decimalFormat.format(Double.parseDouble(formattedResult)*Math.random()/2+0.1)+"公里");
                    // Format the result to two decimal places
                    formattedResult = decimalFormat.format(Math.floor(Math.abs(tmp-buses.get(buses.size()-2))));
                    next_bus_2.setText(formattedResult.substring(0,formattedResult.length()-2)+"分钟");
                    next_bus_message_2.setText("还有"+decimalFormat.format(Double.parseDouble(formattedResult)*Math.random()/2+0.1)+"公里");
                }
            }
        };

        // Schedule the TimerTask to run every minute starting from now
        timer.schedule(task, 0, ONE_MINUTE);

        reversing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopNames = new LinkedList<>();
                for (BusStation allStation : busRoute.getAllStations()) {
                    stopNames.add(allStation.getName());
                }
                numStops = stopNames.size();
                if(revering_switch%2 == 1){
                    Collections.reverse(stopNames);
                }
                revering_switch++;

                String tmp = end_station.getText().toString();
                end_station.setText(origin_station.getText().toString());
                origin_station.setText(tmp);

                busRouteMapView.setBusRouteData(numStops, stopNames, Current_site);
            }
        });

        int[] buttonColors = {Color.parseColor("#f80eff"), Color.parseColor("#1da905")};
        int[] textColors = {Color.WHITE, Color.WHITE};

        int[][] states = {
                new int[]{android.R.attr.state_selected}, // Selected state
                new int[]{} // Default state
        };

        ColorStateList colorStateList = new ColorStateList(states, buttonColors);
        ColorStateList textStateList = new ColorStateList(states, textColors);

        button_collect.setBackgroundTintList(colorStateList);

        button_collect.setTextColor(textStateList);

        String[] buttonTexts = {"已收藏", "收藏"};
        button_collect.setText(buttonTexts[1]); // Set initial text


        query_favorite();
        Current_Favorite_id = mSharedPreferences.getString("Current_Favorite_id", "");
        String isCollected = mSharedPreferences.getString("isCollected", "");
        if(isCollected.equals("1")){
            button_collect.setSelected(true);
        }else {
            button_collect.setSelected(false);
        }
        button_collect.setBackgroundTintList(colorStateList);
        button_collect.setTextColor(textStateList);
        button_collect.setText(buttonTexts[button_collect.isSelected() ? 0 : 1]);

        button_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!button_collect.isSelected()){
                    Add_Favorite();
                    Current_Favorite_id = mSharedPreferences.getString("Current_Favorite_id", "");
                }else {
                    Delete_Favorite();
                }

                button_collect.setSelected(!button_collect.isSelected());
                button_collect.setBackgroundTintList(colorStateList);
                button_collect.setTextColor(textStateList);
                button_collect.setText(buttonTexts[button_collect.isSelected() ? 0 : 1]);
            }

            private void Add_Favorite() {
                new Add_Favorite_RequestTask().execute(Add_favorite_http, city, favorite_type, user, Current_site);
            }

            class Add_Favorite_RequestTask extends AsyncTask<String, Void, String> {

                @Override
                protected String doInBackground(String... params) {
                    String url = params[0];
                    String city = params[1];
                    int favoriteType = Integer.parseInt(params[2]);
                    String user = params[3];
                    String name = params[4];

                    // Perform your network request here and return the result
                    String result = "";

                    try {
                        // Create the URL object with the given parameters
                        String requestUrl = url + "?city=" + city + "&favorite_type=" + favoriteType + "&user=" + user + "&name=" + name;
                        URL requestURL = new URL(requestUrl);

                        // Open a connection to the URL
                        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
                        connection.setRequestMethod("GET");

                        // Read the response from the connection
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();
                            result = response.toString();
                        }

                        // Disconnect the connection
                        connection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return result;
                }

                @Override
                protected void onPostExecute(String result) {
                    // Handle the result here
                    Log.d("test", "Response: " + result);

                    try {
                        JSONArray jsonArray = new JSONArray(result); // jsonData is the JSON data string

                        List<FavoriteItem> favoriteItems = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            FavoriteItem favoriteItem = new FavoriteItem();
                            favoriteItem.setId(jsonObject.getInt("id"));
                            favoriteItem.setFavoriteType(jsonObject.getInt("favorite_type"));
                            favoriteItem.setName(jsonObject.getString("name"));
                            favoriteItem.setCityStr(jsonObject.getString("city_str"));
                            favoriteItem.setUser(jsonObject.getString("user"));
                            favoriteItem.setCityName(jsonObject.getString("city_name"));

                            if (jsonObject.has("index") && !jsonObject.isNull("index")) {
                                favoriteItem.setIndex(jsonObject.getInt("index"));
                            }

                            favoriteItems.add(favoriteItem);
                        }

                        boolean InCollected = false;
                        for (FavoriteItem favoriteItem : favoriteItems) {
                            if(favoriteItem.name.equals(Current_site)){
                                InCollected = true;
                                mEditor.putString("Current_Favorite_id", favoriteItem.getId()+"");
                                break;
                            }
                        }

                        if(InCollected){
                            mEditor.putString("isCollected", "1");
                            mEditor.commit();
                        }else {
                            mEditor.putString("isCollected", "0");
                            mEditor.commit();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        bottomSheet = findViewById(R.id.bottomSheet);
        BottomSheetBehavior<RelativeLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(bottomSheet.getHeight()+190);
        bottomSheetBehavior.setState(bottomSheetBehavior.STATE_DRAGGING);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // Bottom sheet is collapsed
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // Bottom sheet is expanded
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Bottom sheet is hidden
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                // Calculate the threshold at which the arrow changes direction
                float threshold = 0.5f;

                if (slideOffset < threshold) {
                    top_message.setText("上拉查看详细信息");
                    // Show the down arrow when the slide offset is less than the threshold
                    arrowImageView.setImageResource(R.drawable.ic_arrow_up);
                } else {
                    top_message.setText("下拉查看地图");
                    // Show the up arrow when the slide offset is greater than or equal to the threshold
                    arrowImageView.setImageResource(R.drawable.ic_arrow_down);
                }
            }
        });
        startProgressAnimation();

        CITY_ID = city;
        BUS_NUM = busNumber;
        cityCode = city_code;
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        searchLine();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置marker的监听和信息窗口的监听
     */
    private void setUpMap() {
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 公交线路搜索
     */
    public void searchLine() {
        currentpage = 0;// 第一页默认从0开始
        //showProgressDialog();
        String search = BUS_NUM;
        if ("".equals(search)) {
            search = "641";
            searchName.setText(search);
        }
        busLineQuery = new BusLineQuery(search, BusLineQuery.SearchType.BY_LINE_NAME,
                cityCode);// 第一个参数表示公交线路名，第二个参数表示公交线路查询，第三个参数表示所在城市名或者城市区号
        busLineQuery.setPageSize(10);// 设置每页返回多少条数据
        busLineQuery.setPageNumber(currentpage);// 设置查询第几页，第一页从0开始算起
        try {
            busLineSearch = new BusLineSearch(this, busLineQuery);// 设置条件
            busLineSearch.setOnBusLineSearchListener(this);// 设置查询结果的监听
            busLineSearch.searchBusLineAsyn();// 异步查询公交线路名称
        } catch (AMapException e) {
            e.printStackTrace();
        }

        // 公交站点搜索事例
        /*
         * BusStationQuery query = new BusStationQuery(search,cityCode);
         * query.setPageSize(10); query.setPageNumber(currentpage);
         * BusStationSearch busStationSearch = new BusStationSearch(this,query);
         * busStationSearch.setOnBusStationSearchListener(this);
         * busStationSearch.searchBusStationAsyn();
         */
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索:\n");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 提供一个给默认信息窗口定制内容的方法
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 提供一个个性化定制信息窗口的方法
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 点击marker回调函数
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;// 点击marker时把此marker显示在地图中心点
    }

    /**
     * 选择城市
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        String cityString = itemCitys[position];
        cityCode = cityString.substring(cityString.indexOf("-") + 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        cityCode = "010";
    }

    /**
     * BusLineDialog ListView 选项点击回调
     */
    interface OnListItemlistener {
        public void onListItemClick(Bus_Route_Details.BusLineDialog dialog, BusLineItem item);
    }

    /**
     * 所有公交线路显示页面
     */
    class BusLineDialog extends Dialog implements View.OnClickListener {

        private List<BusLineItem> busLineItems;
        private BusLineAdapter busLineAdapter;
        private Button preButton, nextButton;
        private ListView listView;
        protected Bus_Route_Details.OnListItemlistener onListItemlistener;

        public BusLineDialog(Context context, int theme) {
            super(context, theme);
        }

        public void onListItemClicklistener(
                Bus_Route_Details.OnListItemlistener onListItemlistener) {
            this.onListItemlistener = onListItemlistener;

        }

        public BusLineDialog(Context context, List<BusLineItem> busLineItems) {
            this(context, android.R.style.Theme_NoTitleBar);
            this.busLineItems = busLineItems;
            busLineAdapter = new BusLineAdapter(context, busLineItems);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.busline_dialog);
            preButton = (Button) findViewById(R.id.preButton);
            nextButton = (Button) findViewById(R.id.nextButton);
            listView = (ListView) findViewById(R.id.listview);
            listView.setAdapter(busLineAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    onListItemlistener.onListItemClick(Bus_Route_Details.BusLineDialog.this,
                            busLineItems.get(arg2));
                    dismiss();

                }
            });
            preButton.setOnClickListener(this);
            nextButton.setOnClickListener(this);
            if (currentpage <= 0) {
                preButton.setEnabled(false);
            }
            if (currentpage >= busLineResult.getPageCount() - 1) {
                nextButton.setEnabled(false);
            }

        }

        @Override
        public void onClick(View v) {
            this.dismiss();
            if (v.equals(preButton)) {
                currentpage--;
            } else if (v.equals(nextButton)) {
                currentpage++;
            }
            //showProgressDialog();
            busLineQuery.setPageNumber(currentpage);// 设置公交查询第几页
            busLineSearch.setOnBusLineSearchListener(Bus_Route_Details.this);
            busLineSearch.searchBusLineAsyn();// 异步查询公交线路名称
        }
    }


    /**
     * 公交线路查询结果回调
     */
    @Override
    public void onBusLineSearched(BusLineResult result, int rCode) {
        //dissmissProgressDialog();
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null
                    && result.getQuery().equals(busLineQuery)) {
                if (result.getQuery().getCategory() == BusLineQuery.SearchType.BY_LINE_NAME) {
                    if (result.getPageCount() > 0
                            && result.getBusLines() != null
                            && result.getBusLines().size() > 0) {
                        busLineResult = result;
                        lineItems = result.getBusLines();
                        if(lineItems != null) {
                            busLineQuery = new BusLineQuery(lineItems.get(0).getBusLineId(), BusLineQuery.SearchType.BY_LINE_ID,
                                    cityCode);// 第一个参数表示公交线路id，第二个参数表示公交线路id查询，第三个参数表示所在城市名或者城市区号
                            BusLineSearch busLineSearch = null;
                            try {
                                busLineSearch = new BusLineSearch(
                                        Bus_Route_Details.this, busLineQuery);
                                busLineSearch.setOnBusLineSearchListener(Bus_Route_Details.this);
                                busLineSearch.searchBusLineAsyn();// 异步查询公交线路id
                            } catch (AMapException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (result.getQuery().getCategory() == BusLineQuery.SearchType.BY_LINE_ID) {
                    aMap.clear();// 清理地图上的marker
                    busLineResult = result;
                    lineItems = busLineResult.getBusLines();
                    if(lineItems != null && lineItems.size() > 0) {
                        BusLineOverlay busLineOverlay = new BusLineOverlay(this,
                                aMap, lineItems.get(0));
                        busLineOverlay.removeFromMap();
                        busLineOverlay.addToMap();
                        busLineOverlay.zoomToSpan();
                    }
                }
            } else {
                ToastUtil.show(Bus_Route_Details.this, "no result");
            }
        } else {
            ToastUtil.showerror(Bus_Route_Details.this, rCode);
        }
    }

    /**
     * 查询公交线路
     */
    @Override
    public void onClick(View v) {
        searchLine();
    }

    // Method to start the progress animation
    private void startProgressAnimation() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                isClickable = false; // Disable clicks
                progressBar.setVisibility(View.VISIBLE); // Show progress bar
                blinkLoadingImage(); // Start blinking loading image
            }
        };

        handler.postDelayed(runnable, 0);
    }

    // Method to stop the progress animation
    private void stopProgressAnimation() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable); // Cancel the execution of the runnable
        }
        isClickable = true; // Enable clicks
        progressBar.setVisibility(View.GONE); // Hide progress bar
        loadingImage.clearAnimation(); // Stop blinking loading image
        loadingImage.setVisibility(View.GONE); // Hide loading image
    }

    private void blinkLoadingImage() {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); // Blinking duration (500 milliseconds)
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        loadingImage.startAnimation(anim);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return !isClickable || super.dispatchTouchEvent(event);
    }

    private void Delete_Favorite(){
        new Delete_Favorite_RequestTask().execute(Delete_favorite_http, Current_Favorite_id);
    }

    private class Delete_Favorite_RequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String id = params[1];

            // Perform your network request here and return the result
            String result = "";

            // Create the URL object with the given parameters
            String requestUrl = url + "?id="+ id;

            try {
                URL requestURL = new URL(requestUrl);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
                connection.setRequestMethod("GET");

                // Read the response from the connection
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = response.toString();
                }

                // Disconnect the connection
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result here
            Log.d("test", "Response: " + "delete favorite successful!\n"+result);
        }
    }

    private void query_favorite(){
        new Query_Favorite_RequestTask().execute(Query_favorite_http, user);
    }

    private class Query_Favorite_RequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String message = params[1];

            // Perform your network request here and return the result
            String result = "";

            // Create the URL object with the given parameters
            String requestUrl = url + "?message="+ message;

            try {
                URL requestURL = new URL(requestUrl);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
                connection.setRequestMethod("GET");

                // Read the response from the connection
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = response.toString();
                }

                // Disconnect the connection
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result here
            Log.d("test", "Response: " + "query_favorite successful!");

            SharedPreferences mSharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();

            try {
                JSONArray jsonArray = new JSONArray(result); // jsonData is the JSON data string

                List<FavoriteItem> favoriteItems = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    FavoriteItem favoriteItem = new FavoriteItem();
                    favoriteItem.setId(jsonObject.getInt("id"));
                    favoriteItem.setFavoriteType(jsonObject.getInt("favorite_type"));
                    favoriteItem.setName(jsonObject.getString("name"));
                    favoriteItem.setCityStr(jsonObject.getString("city_str"));
                    favoriteItem.setUser(jsonObject.getString("user"));
                    favoriteItem.setCityName(jsonObject.getString("city_name"));

                    if (jsonObject.has("index") && !jsonObject.isNull("index")) {
                        favoriteItem.setIndex(jsonObject.getInt("index"));
                    }

                    favoriteItems.add(favoriteItem);
                }

                boolean InCollected = false;
                for (FavoriteItem favoriteItem : favoriteItems) {
                    if(favoriteItem.name.equals(Current_site)){
                        InCollected = true;
                        mEditor.putString("Current_Favorite_id", favoriteItem.getId()+"");
                        break;
                    }
                }

                if(InCollected){
                    mEditor.putString("isCollected", "1");
                    mEditor.commit();
                }else {
                    mEditor.putString("isCollected", "0");
                    mEditor.commit();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class FavoriteItem {
        private int id;
        private int favoriteType;
        private String name;
        private String cityStr;
        private String user;
        private String cityName;
        private Integer index;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFavoriteType() {
            return favoriteType;
        }

        public void setFavoriteType(int favoriteType) {
            this.favoriteType = favoriteType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCityStr() {
            return cityStr;
        }

        public void setCityStr(String cityStr) {
            this.cityStr = cityStr;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }
    }

    private void initData() throws JSONException {
        getData();
    }

    public class NetworkRequestTask extends AsyncTask<Void, Void, String> {

        private String city;
        private String bus;

        public NetworkRequestTask(String city, String bus) {
            this.city = city;
            this.bus = bus;
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Perform your network request here and return the result
            String result = ""; // Initialize an empty result string
            String httpUrl = HTTPHOST+"/bus_line_info?city="+city+"&bus="+bus;

            try {
                URL url = new URL(httpUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = response.toString();
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            httpResult = result;
            BusRoute temp = new BusRoute();
            try {
                busRoute = temp.fromJson(httpResult);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            stopNames = new LinkedList<>();
            for (BusStation allStation : busRoute.getAllStations()) {
                stopNames.add(allStation.getName());
            }
            numStops = stopNames.size();
            busRouteMapView.setBusRouteData(numStops, stopNames, Current_site);

            SharedPreferences mSharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(busRoute);
            mEditor.putString("BusRouteData", json);
            mEditor.commit();

            stopProgressAnimation();
        }
    }

    private void getData(){
        NetworkRequestTask task = new NetworkRequestTask(city,busNumber);
        task.execute();

    }

    public class BusRoute {
        private String routeId;
        private String name;
        private String type;
        private List<BusStation> allStations;

        public BusRoute(){}

        public BusRoute(String routeId, String name, String type, List<BusStation> allStations) {
            this.routeId = routeId;
            this.name = name;
            this.type = type;
            this.allStations = allStations;
        }

        public String getRouteId() {
            return routeId;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<BusStation> getAllStations() {
            return allStations;
        }

        public BusRoute fromJson(String jsonData) throws JSONException {
            JSONObject json = new JSONObject(jsonData);

            int routeId = json.getInt("id");
            String name = json.getString("name");
            String type = json.getString("type");

            JSONArray stationArray = json.getJSONArray("all_station");
            List<BusStation> allStations = new ArrayList<>();
            for (int i = 0; i < stationArray.length(); i++) {
                JSONObject stationObj = stationArray.getJSONObject(i);
                int sequence = stationObj.getInt("sequence");
                String stationId = stationObj.getString("id");
                String stationName = stationObj.getString("name");
                String location = stationObj.getString("location");

                BusStation station = new BusStation(sequence, stationId, stationName, location);
                allStations.add(station);
            }

            return new BusRoute(String.valueOf(routeId), name, type, allStations);
        }
    }

    public class BusStation {
        private int sequence;
        private String stationId;
        private String name;
        private String location;

        public BusStation(int sequence, String stationId, String name, String location) {
            this.sequence = sequence;
            this.stationId = stationId;
            this.name = name;
            this.location = location;
        }

        public int getSequence() {
            return sequence;
        }

        public String getStationId() {
            return stationId;
        }

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }
    }

    private void initBus(){
        List<Double> tmp = new LinkedList<>();
        List<BusStation> busStations = busRoute.getAllStations();
        for (int i=0; i<=busStations.size()-2; i++) {
            BusStation busStation = busStations.get(i);
            double distance = 0;
            String[] tmp1 = busStation.getLocation().split(",");
            String[] tmp2 = busStations.get(i+1).getLocation().split(",");
            double newX = Double.parseDouble(tmp1[0]);
            double newY = Double.parseDouble(tmp1[1]);
            double nextX = Double.parseDouble(tmp2[0]);
            double nextY = Double.parseDouble(tmp2[1]);
            distance = calculateDistance(newX,newY,nextX,nextY);
            station_time.add(distance);
            tmp.add(distance);
            if(distance < 1){
                distance = 1;
            }
            all_ms += distance;
        }
        double[] travelTimes = new double[tmp.size()];
        int i=0;
        for (double integer : tmp) {
            travelTimes[i++] = integer;
        }
        //int currentTime = 360; // Current time in minutes (e.g., 6:00 AM)
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        double[] busTimings = calculateBusTimings(travelTimes, currentHour*60+currentMinute);

        // Print the number of minutes for all buses traveling on the bus route
        for (int j = 0; j < busTimings.length; j++) {
            bus_Time.add(busTimings[j]%all_ms);
        }
    }

    public double[] calculateBusTimings(double[] travelTimes, int currentTime) {
        int interval = 10; // Time interval between buses in minutes
        int startHour = 5; // Starting hour of the first bus
        int startMinute = 0; // Starting minute of the first bus

        int totalBuses = travelTimes.length; // Total number of buses

        // Calculate the number of minutes elapsed since the first bus started
        int elapsedMinutes = (currentTime / 60 - startHour) * 60 + (currentTime % 60 - startMinute);

        // Calculate the number of buses that have already departed
        int departedBuses = elapsedMinutes / (interval * totalBuses);

        // Calculate the index of the next bus that will depart
        int nextBusIndex = (departedBuses + 1) % totalBuses;

        // Calculate the number of minutes for all buses traveling on the bus route
        double[] busTimings = new double[totalBuses];
        for (int i = 0; i < totalBuses; i++) {
            // Calculate the minutes for each bus based on the travel time
            busTimings[i] = elapsedMinutes + (i - nextBusIndex) * interval + travelTimes[i];
        }

        return busTimings;
    }

    private static final double EARTH_RADIUS = 6371.0; // Radius of the Earth in kilometers
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between coordinates
        double latDiff = lat2Rad - lat1Rad;
        double lonDiff = lon2Rad - lon1Rad;

        // Calculate the distance using the Haversine formula
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance;
    }


}