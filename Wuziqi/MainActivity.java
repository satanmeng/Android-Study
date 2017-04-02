package com.example.wuzipqi;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	
	private WuziqiPanel wuziqiPanel;

    @Override
    /*OnCreate��Android�е�һ���ر�ĺ�������������ʾһ�������������ɡ���
     * �䲻�������ڣ�ֻ���ڴ�����ʾǰ���ô��ڵ���������λ�õȡ�
     * ���� Javadoc��
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        wuziqiPanel=(WuziqiPanel)findViewById(R.id.id_wuziqi);
    }


    @Override
    /*androidһ����������ʽ�Ĳ˵�: 
            1.ѡ��˵���optinosMenu�� 
            2.�����Ĳ˵���ContextMenu�� 
            3.�Ӳ˵�(subMenu) */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	/*����Activity��getMenuInflater()�õ�һ��MenuInflater��  
                         ʹ��inflate�������Ѳ����ļ��еĶ���Ĳ˵� ���ظ� �ڶ�����������Ӧ��menu���� */
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    /*ֻҪ�˵��еĲ˵�������
     * ���ᴥ��onOptionsItemSelected(MenuItem item) 
     * item������Ϊ������Ĳ˵��
     * ��ô��Ҫ�ڴ˷������ж��ĸ�Item������ˣ��Ӷ�ʵ�ֲ�ͬ�Ĳ�����
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
     
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	int id=item.getItemId();
    	
    	if(id==R.id.action_settings)
    	{
    		wuziqiPanel.start();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
}
