package com.example.youdao;

import java.lang.reflect.Type;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {  
	  
    private EditText edit = null;  
    private TextView search = null;  
    private TextView text = null;  
    private String YouDaoBaseUrl = "http://fanyi.youdao.com/openapi.do";  
    private String YouDaoKeyFrom = "imoocdic123";  
    private String YouDaoKey = "1895564141";  
    private String YouDaoType = "data";  
    private String YouDaoDoctype = "json";  
    private String YouDaoVersion = "1.1";  
    private TranslateHandler handler;  
  
    private static final int SUCCEE_RESULT = 10;  
    private static final int ERROR_TEXT_TOO_LONG = 20;  
    private static final int ERROR_CANNOT_TRANSLATE = 30;  
    private static final int ERROR_UNSUPPORT_LANGUAGE = 40;  
    private static final int ERROR_WRONG_KEY = 50;  
    private static final int ERROR_WRONG_RESULT = 60;  
    
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        edit = (EditText) findViewById(R.id.edit);  
        search = (Button) findViewById(R.id.search);  
        search.setOnClickListener(new searchListener());  
        text = (TextView) findViewById(R.id.text);  
        handler = new TranslateHandler(this, text);  
    }  
  
    private class searchListener implements OnClickListener {  
        @Override  
        public void onClick(View v) {  
        	
        	//trim()移除字符串两侧的空白字符
            String content = edit.getText().toString().trim();  
            if (content == null || "".equals(content)) {  
                Toast.makeText(getApplicationContext(), "请输入要翻译的内容", Toast.LENGTH_SHORT).show();  
                return;  
            }  
            final String YouDaoUrl = YouDaoBaseUrl + "?keyfrom=" + YouDaoKeyFrom + "&key=" + YouDaoKey + "&type="  
                    + YouDaoType + "&doctype=" + YouDaoDoctype + "&type=" + YouDaoType + "&version=" + YouDaoVersion  
                    + "&q=" + content;  
            new Thread() {  
                public void run() {  
                    try {  
                        AnalyzingOfJson(YouDaoUrl);  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                };  
            }.start();  
        }  
    }  
  
    private void AnalyzingOfJson(String url) throws Exception {  
        // 第一步，创建HttpGet对象  
    	/*Get：是以实体的方式得到由请求URI所指定资源的信息，
    	 * 如果请求URI只是一个数据产生过程，
    	 * 那么最终要在响应实体中返回的是处理过程的结果所指向的资源，
    	 * 而不是处理过程的描述。
    	 */
        HttpGet httpGet = new HttpGet(url);  
        // 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象  
        HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);  
        if (httpResponse.getStatusLine().getStatusCode() == 200) {  
            // 第三步，使用getEntity方法活得返回结果  
            String result = EntityUtils.toString(httpResponse.getEntity());  
            System.out.println("result:" + result);  
            JSONArray jsonArray = new JSONArray("[" + result + "]");  
            String message = null;  
            for (int i = 0; i < jsonArray.length(); i++) {  
                JSONObject jsonObject = jsonArray.getJSONObject(i);  
                if (jsonObject != null) {  
                    String errorCode = jsonObject.getString("errorCode");  
                    if (errorCode.equals("20")) {  
                        handler.sendEmptyMessage(ERROR_TEXT_TOO_LONG);  
                    } else if (errorCode.equals("30 ")) {  
                        handler.sendEmptyMessage(ERROR_CANNOT_TRANSLATE);  
                    } else if (errorCode.equals("40")) {  
                        handler.sendEmptyMessage(ERROR_UNSUPPORT_LANGUAGE);  
                    } else if (errorCode.equals("50")) {  
                        handler.sendEmptyMessage(ERROR_WRONG_KEY);  
                    } else {  
                        Message msg = new Message();  
                        msg.what = SUCCEE_RESULT;  
                        // 要翻译的内容  
                        String query = jsonObject.getString("query");  
                        message = "翻译结果：";  
                        // 翻译内容  
                        Gson gson = new Gson(); 
                        
                        //TypeToken，它是gson提供的数据类型转换器，可以支持各种数据集合类型转换。
                        Type lt = new TypeToken<String[]>() {  
                        }.getType();  
                        
                        //从结果获取需要的json数据，将其转化为String类型
                        String[] translations = gson.fromJson(jsonObject.getString("translation"), lt);  
                        for (String translation : translations) {  
                            message += "\t" + translation;  
                        }  
                        // 有道词典-基本词典  
                        if (jsonObject.has("basic")) {  
                            JSONObject basic = jsonObject.getJSONObject("basic");  
                            if (basic.has("phonetic")) {  
                                String phonetic = basic.getString("phonetic");  
                               // message += "\n音标："; 
                               // message += "\n\t" + phonetic;  
                            }  
                            if (basic.has("explains")) {  
                                String explains = basic.getString("explains");
                                message += "\n词典释义："; 
                                message += "\n\t" + explains;  
                            }  
                        }  
                       // 有道词典-网络释义  
                        if (jsonObject.has("web")) {  
                            String web = jsonObject.getString("web");  
                            JSONArray webString = new JSONArray("[" + web + "]");  
                            message += "\n网络释义：";  
                            JSONArray webArray = webString.getJSONArray(0);  
                            int count = 0;  
                            while (!webArray.isNull(count)) {  
  
                                if (webArray.getJSONObject(count).has("key")) {  
                                    String key = webArray.getJSONObject(count).getString("key");  
                                    message += "\n（" + (count + 1) + "）" + key + "\n";  
                                }  
                                if (webArray.getJSONObject(count).has("value")) {  
                                    String[] values = gson.fromJson(webArray.getJSONObject(count).getString("value"),  
                                            lt);  
                                    for (int j = 0; j < values.length; j++) {  
                                        String value = values[j];  
                                        message += value;  
                                        if (j < values.length - 1) {  
                                            message += "，";  
                                        }  
                                    }  
                                }  
                                count++;  
                            }  
                        }  
                        msg.obj = message;  
                        
                        //异步发送消息
                        handler.sendMessage(msg);  
                    }  
                }  
            }  
            text.setText(message);  
        } else {  
            handler.sendEmptyMessage(ERROR_WRONG_RESULT);  
        }  
    }  

    
    /*Handler主要用于异步消息的处理：
     * 当发出一个消息之后，首先进入一个消息队列，
     * 发送消息的函数即刻返回，
     * 而另外一个部分在消息队列中逐一将消息取出，
     * 然后对消息进行处理，
     * 也就是发送消息和接收消息不是同步的处理。 
     * 这种机制通常用来处理相对耗时比较长的操作。
     */
    private class TranslateHandler extends Handler {  
        private Context mContext;  
        private TextView mTextView;  
  
        public TranslateHandler(Context context, TextView textView) {  
            this.mContext = context;  
            this.mTextView = textView;  
        }  
  
        /*可以根据参数选择对此消息是否需要做出处理*/
        public void handleMessage(Message msg) {  
        	/*
              msg.obj使用来放对象的，这个对象可以放任何类型
              msg.what只能放数字（作用可以使用来做if判断）
            */
            switch (msg.what) {  
            case SUCCEE_RESULT:  
                mTextView.setText((String) msg.obj);  
                closeInput();  
                break;  
            case ERROR_TEXT_TOO_LONG:  
                Toast.makeText(mContext, "要翻译的文本过长", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_CANNOT_TRANSLATE:  
                Toast.makeText(mContext, "无法进行有效的翻译", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_UNSUPPORT_LANGUAGE:  
                Toast.makeText(mContext, "不支持的语言类型", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_WRONG_KEY:  
                Toast.makeText(mContext, "无效的key", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_WRONG_RESULT:  
                Toast.makeText(mContext, "提取异常", Toast.LENGTH_SHORT).show();  
                break;  
            default:  
                break;  
            }  
            super.handleMessage(msg);  
        }  
    }  
  
    //隐藏软键盘
    public void closeInput() { 
    	
    	//Android中软键盘的管理主要是通过InputMethodManager类来完成的。
    	//获取到InputMethodManager对象后就可以通过调用其成员方法来对软键盘进行操作。
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
        
        //不过在使用InputMethodManager对象前通常都需要判断其是否为null，避免运行时异常。
        //getCurrentFocus()获取当前activity中获得焦点的view
        if ((inputMethodManager != null) && (this.getCurrentFocus() != null)) { 
        	
        	//getWindowToken()获取调用的view依附在哪个window的令牌
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),  
                    InputMethodManager.HIDE_NOT_ALWAYS);  
        }  
    }  
}  


