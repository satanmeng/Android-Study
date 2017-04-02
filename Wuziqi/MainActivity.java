package com.example.wuzipqi;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	
	private WuziqiPanel wuziqiPanel;

    @Override
    /*OnCreate是Android中的一个特别的函数，用来“表示一个窗口正在生成”。
     * 其不产生窗口，只是在窗口显示前设置窗口的属性如风格、位置等。
     * （非 Javadoc）
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        wuziqiPanel=(WuziqiPanel)findViewById(R.id.id_wuziqi);
    }


    @Override
    /*android一共有三种形式的菜单: 
            1.选项菜单（optinosMenu） 
            2.上下文菜单（ContextMenu） 
            3.子菜单(subMenu) */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	/*调用Activity的getMenuInflater()得到一个MenuInflater，  
                         使用inflate方法来把布局文件中的定义的菜单 加载给 第二个参数所对应的menu对象 */
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    /*只要菜单中的菜单项被点击，
     * 都会触发onOptionsItemSelected(MenuItem item) 
     * item参数即为被点击的菜单项，
     * 那么需要在此方法内判断哪个Item被点击了，从而实现不同的操作。
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
