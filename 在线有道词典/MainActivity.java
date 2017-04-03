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
        	
        	//trim()�Ƴ��ַ�������Ŀհ��ַ�
            String content = edit.getText().toString().trim();  
            if (content == null || "".equals(content)) {  
                Toast.makeText(getApplicationContext(), "������Ҫ���������", Toast.LENGTH_SHORT).show();  
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
        // ��һ��������HttpGet����  
    	/*Get������ʵ��ķ�ʽ�õ�������URI��ָ����Դ����Ϣ��
    	 * �������URIֻ��һ�����ݲ������̣�
    	 * ��ô����Ҫ����Ӧʵ���з��ص��Ǵ�����̵Ľ����ָ�����Դ��
    	 * �����Ǵ�����̵�������
    	 */
        HttpGet httpGet = new HttpGet(url);  
        // �ڶ�����ʹ��execute��������HTTP GET���󣬲�����HttpResponse����  
        HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);  
        if (httpResponse.getStatusLine().getStatusCode() == 200) {  
            // ��������ʹ��getEntity������÷��ؽ��  
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
                        // Ҫ���������  
                        String query = jsonObject.getString("query");  
                        message = "��������";  
                        // ��������  
                        Gson gson = new Gson(); 
                        
                        //TypeToken������gson�ṩ����������ת����������֧�ָ������ݼ�������ת����
                        Type lt = new TypeToken<String[]>() {  
                        }.getType();  
                        
                        //�ӽ����ȡ��Ҫ��json���ݣ�����ת��ΪString����
                        String[] translations = gson.fromJson(jsonObject.getString("translation"), lt);  
                        for (String translation : translations) {  
                            message += "\t" + translation;  
                        }  
                        // �е��ʵ�-�����ʵ�  
                        if (jsonObject.has("basic")) {  
                            JSONObject basic = jsonObject.getJSONObject("basic");  
                            if (basic.has("phonetic")) {  
                                String phonetic = basic.getString("phonetic");  
                               // message += "\n���꣺"; 
                               // message += "\n\t" + phonetic;  
                            }  
                            if (basic.has("explains")) {  
                                String explains = basic.getString("explains");
                                message += "\n�ʵ����壺"; 
                                message += "\n\t" + explains;  
                            }  
                        }  
                       // �е��ʵ�-��������  
                        if (jsonObject.has("web")) {  
                            String web = jsonObject.getString("web");  
                            JSONArray webString = new JSONArray("[" + web + "]");  
                            message += "\n�������壺";  
                            JSONArray webArray = webString.getJSONArray(0);  
                            int count = 0;  
                            while (!webArray.isNull(count)) {  
  
                                if (webArray.getJSONObject(count).has("key")) {  
                                    String key = webArray.getJSONObject(count).getString("key");  
                                    message += "\n��" + (count + 1) + "��" + key + "\n";  
                                }  
                                if (webArray.getJSONObject(count).has("value")) {  
                                    String[] values = gson.fromJson(webArray.getJSONObject(count).getString("value"),  
                                            lt);  
                                    for (int j = 0; j < values.length; j++) {  
                                        String value = values[j];  
                                        message += value;  
                                        if (j < values.length - 1) {  
                                            message += "��";  
                                        }  
                                    }  
                                }  
                                count++;  
                            }  
                        }  
                        msg.obj = message;  
                        
                        //�첽������Ϣ
                        handler.sendMessage(msg);  
                    }  
                }  
            }  
            text.setText(message);  
        } else {  
            handler.sendEmptyMessage(ERROR_WRONG_RESULT);  
        }  
    }  

    
    /*Handler��Ҫ�����첽��Ϣ�Ĵ���
     * ������һ����Ϣ֮�����Ƚ���һ����Ϣ���У�
     * ������Ϣ�ĺ������̷��أ�
     * ������һ����������Ϣ��������һ����Ϣȡ����
     * Ȼ�����Ϣ���д���
     * Ҳ���Ƿ�����Ϣ�ͽ�����Ϣ����ͬ���Ĵ��� 
     * ���ֻ���ͨ������������Ժ�ʱ�Ƚϳ��Ĳ�����
     */
    private class TranslateHandler extends Handler {  
        private Context mContext;  
        private TextView mTextView;  
  
        public TranslateHandler(Context context, TextView textView) {  
            this.mContext = context;  
            this.mTextView = textView;  
        }  
  
        /*���Ը��ݲ���ѡ��Դ���Ϣ�Ƿ���Ҫ��������*/
        public void handleMessage(Message msg) {  
        	/*
              msg.objʹ�����Ŷ���ģ����������Է��κ�����
              msg.whatֻ�ܷ����֣����ÿ���ʹ������if�жϣ�
            */
            switch (msg.what) {  
            case SUCCEE_RESULT:  
                mTextView.setText((String) msg.obj);  
                closeInput();  
                break;  
            case ERROR_TEXT_TOO_LONG:  
                Toast.makeText(mContext, "Ҫ������ı�����", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_CANNOT_TRANSLATE:  
                Toast.makeText(mContext, "�޷�������Ч�ķ���", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_UNSUPPORT_LANGUAGE:  
                Toast.makeText(mContext, "��֧�ֵ���������", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_WRONG_KEY:  
                Toast.makeText(mContext, "��Ч��key", Toast.LENGTH_SHORT).show();  
                break;  
            case ERROR_WRONG_RESULT:  
                Toast.makeText(mContext, "��ȡ�쳣", Toast.LENGTH_SHORT).show();  
                break;  
            default:  
                break;  
            }  
            super.handleMessage(msg);  
        }  
    }  
  
    //���������
    public void closeInput() { 
    	
    	//Android������̵Ĺ�����Ҫ��ͨ��InputMethodManager������ɵġ�
    	//��ȡ��InputMethodManager�����Ϳ���ͨ���������Ա������������̽��в�����
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
        
        //������ʹ��InputMethodManager����ǰͨ������Ҫ�ж����Ƿ�Ϊnull����������ʱ�쳣��
        //getCurrentFocus()��ȡ��ǰactivity�л�ý����view
        if ((inputMethodManager != null) && (this.getCurrentFocus() != null)) { 
        	
        	//getWindowToken()��ȡ���õ�view�������ĸ�window������
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),  
                    InputMethodManager.HIDE_NOT_ALWAYS);  
        }  
    }  
}  


