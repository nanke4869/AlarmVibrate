package b16112112.myapplication;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Button baojing;
    private SoundPool soundPool;
    private Vibrator vibrator;
    int hit;
    String key="temandhum";
    public static double[] Temperature = new double[50];
    public static double[] Humidity = new double[50];
    public static int k=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        hit = soundPool.load(this, R.raw.beep, 0);

        baojing=(Button)findViewById(R.id.baojing);
        baojing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baojing.setText("111111" + "222222");
            }

        });

        init();

    }
    private void init(){
        Timer timer = new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                SendByHttpClient(key);
                double tem = Temperature[k];
                double hum = Humidity[k];
                if ((tem <= -6 || tem >= 0) || (hum >= 50 || hum <= 40)) {
                    soundPool.play(hit, 5, 5, 0, 1, (float) 1);
                    vibrator.vibrate(new long[]{100, 2000, 500, 2500}, -1);
                }
            }
        };
        timer.schedule(task,0,1000);
    }


    public void SendByHttpClient(final String key){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    HttpClient httpclient=new DefaultHttpClient();
                    HttpPost httpPost=new HttpPost("http://192.168.31.223:8080/HttpSearch/Search");
                    List<NameValuePair> params=new ArrayList<NameValuePair>();//用来存放post请求的参数，前面一个键，后面一个值
                    params.add(new BasicNameValuePair("search",key));
                    //UrlEncodedFormEntity这个类是用来把输入数据编码成合适的内容
                    //两个键值对，被UrlEncodedFormEntity实例编码后变为如下内容：param1=value1&param2=value2
                    final UrlEncodedFormEntity entity=new UrlEncodedFormEntity(params,"utf-8");
                    httpPost.setEntity(entity);//带上参数
                    HttpResponse httpResponse= httpclient.execute(httpPost);//响应结果
                    if(httpResponse.getStatusLine().getStatusCode()==200)
                    {
                        HttpEntity entity1=httpResponse.getEntity();
                        String response= EntityUtils.toString(entity1, "utf-8");
                        Message message=new Message();
                        message.what=0;

                        message.obj=response;
                        handler.sendMessage(message);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    String response = (String) msg.obj;
                    try {
                        getTemandHum(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "异常111111", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void getTemandHum(String jsonString) throws JSONException {
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            //JSONObject mapsObject = jsonObject.getJSONObject("maps");
            JSONArray mapsArray = jsonObject.getJSONArray("maps");
            for(int i=0; i<mapsArray.length();i++){
                JSONObject  temp = mapsArray.getJSONObject(i);
                // 查看Map中的键值对的key值
//                Iterator<String> iterator = temp.keys();
//                while(iterator.hasNext()){
//                    String json_key = iterator.next();
//                    String object =temp.get(json_key).toString();
//                    coords[k++]=Double.parseDouble(object);
//                }
                double tem = temp.getDouble("Temperature");
                Temperature[i]=tem;
                double hum = temp.getDouble("Humidity");
                Humidity[i]=hum;
                k=i;
            }
        }catch (Exception e) {
            // TODO: handle exception
            //Toast.makeText(PointActivity.this, "异常22222", Toast.LENGTH_SHORT).show();
        }
    }
}
