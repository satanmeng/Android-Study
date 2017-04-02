package com.example.wuzipqi;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.R.integer;
import android.R.string;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


//自定义VIEW
public class WuziqiPanel extends View{
	
	private int mPanelWidth;
	private float mLineHeight;
	private int MAX_LINE=10;
	private int MAX_COUNT_IN_LINE=5;
	
	private Paint mPaint=new Paint();
	
	private Bitmap mWhitePiece;
	private Bitmap mBlackPiece;
	
	private float ratioPieceofLineHeight=3*1.0f/4;

	private boolean mIsWhite=true;
	private ArrayList<Point> mWhiteArray=new ArrayList();
	private ArrayList<Point> mBlackArray=new ArrayList();
	
	private boolean mIsGameOver;
	private boolean mIsWhiteWinner;
	
	public WuziqiPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//setBackgroundColor(0x44ff0000);
		init();//初始化
	}
	
	private void init() {
	   mPaint.setColor(0x88000000);
	   mPaint.setAntiAlias(true);//防止边缘的锯齿
	   mPaint.setDither(true);//设置防抖动
	   mPaint.setStyle(Paint.Style.STROKE);	//设置画笔风格,让画出的图形是空心的 
	   
	   //从资源文件中读取一张图片并生成一个Bitmap,
	   //第一个参数是包含你要加载的位图资源文件的对象（一般写成 getResources（）就ok了）；第二个时你需要加载的位图资源的Id。
	   mWhitePiece=BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
	   mBlackPiece=BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}

	
	
	@Override
	/*View本身大小多少，这由onMeasure()决定*/
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		/*提供了三个对外公开的static变量UNSPECIFIED,
		 * EXACTLY,AT_MOST,
		 * ,这三个变量统称为specMode，
		 * 对于一个View来说它的宽和高各有属于自己的specMode
		 */
		int widthSize=MeasureSpec.getSize(widthMeasureSpec);
		int widthMode=MeasureSpec.getMode(widthMeasureSpec);
		
		int heighSize=MeasureSpec.getSize(heightMeasureSpec);
		int heighMode=MeasureSpec.getMode(heightMeasureSpec);
		
		int width=Math.min(widthSize, heighSize);
		
		//UNSPECIFIED(未指定)
		if(widthMode==MeasureSpec.UNSPECIFIED)
		{
		   width=heighSize;
		}else if(heighMode==MeasureSpec.UNSPECIFIED)
		{
			width=widthSize;
		}
		
		//setMeasuredDimension这个方法，这个方法决定了当前View的大小
		setMeasuredDimension(width, width);
	}
	
	@Override
	/*这个方法会在这个view的大小发生改变是被系统调用*/
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		mPanelWidth=w;
		mLineHeight=mPanelWidth*1.0f/MAX_LINE;
		
		int pieceWidth=(int)(mLineHeight*ratioPieceofLineHeight);
		
		//createScaledBitmap()从当前存在的位图，按一定的比例创建一个新的位图。
		mWhitePiece=Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
		mBlackPiece=Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
		
	}
	
	@Override
	/*处理手机屏幕的触摸事件*/
	public boolean onTouchEvent(MotionEvent event) {
		if(mIsGameOver)
			return false;
		int action=event.getAction();
		
		/*ACTION_DOWN: 表示用户开始触摸.
          ACTION_MOVE: 表示用户在移动(手指或者其他)
          ACTION_UP:表示用户抬起了手指 
          ACTION_CANCEL:表示手势被取消了
          */
		if(action==MotionEvent.ACTION_UP)
		{
			
			int x=(int)event.getX();
			int y=(int)event.getY();
			
			Point p=getValidPoint(x,y);
			
			//contains包含
			if(mWhiteArray.contains(p)||mBlackArray.contains(p))
				return false;
			
			if(mIsWhite)
			{
				mWhiteArray.add(p);
			}else {
				mBlackArray.add(p);
			}
			
			/*invalidate()是用来刷新View的，必须是在UI线程中进行工作。
			 * 比如在修改某个view的显示时，调用invalidate()才能看到重新绘制的界面。
			 * invalidate()的调用是把之前的旧的view从主UI线程队列中pop掉。 
			 */
			invalidate();
			mIsWhite=!mIsWhite;			
		}
		return true;
	}
	
	private Point getValidPoint(int x, int y) {
		
		return new Point((int)(x/mLineHeight),(int)(y/mLineHeight));
	}

	@Override
	/*onDraw()定义了如何绘制这个View*/
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		
		//绘画棋盘
		drawBoard(canvas);
		
		//绘画棋子
		drawPieces(canvas);
		
		//检查游戏是否结束
		checkGameOver();
	}

	private void checkGameOver()
	{
		boolean whiteWin=checkFiveInLine(mWhiteArray);
		boolean blackWin=checkFiveInLine(mBlackArray);
		
		if(whiteWin||blackWin)
		{
			mIsGameOver=true;
			mIsWhiteWinner=whiteWin;
			
			String text=mIsWhiteWinner?"白棋胜利":"黑棋胜利";
			
			/*Toast 是一个 View 视图，快速的为用户显示少量的信息。
			 *  Toast 在应用程序上浮动显示信息给用户，它永远不会获得焦点，
			 *  不影响用户的输入等操作，主要用于 一些帮助 / 提示。
			 *  Toast 最常见的创建方式是使用静态方法 Toast.makeText
			 */
			Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
		}
		
	}

	private boolean checkFiveInLine(List<Point> points) 
	{
		for(Point p:points)
		{
			int x=p.x;
			int y=p.y;
			
			boolean win=checkHorizontal(x,y,points);
			if(win)return true;
			win=checkVertical(x, y, points);
			if(win)return true;
			win=checkLeftDiagonal(x, y, points);
			if(win)return true;
			win=checkRightDiagonal(x, y, points);
			if(win)return true;
		}
		return false;
	}
	
	//判断横向是否有五个一致的棋子
	private boolean checkHorizontal(int x, int y, List<Point> points) {
		int count=1;
		//左
		for(int i=1;i<MAX_COUNT_IN_LINE;i++)
		{
			if(points.contains(new Point(x-i,y)))
			{
				count++;
			}else
			{
				break;
			}
		}		
		if(count==MAX_COUNT_IN_LINE)return true;
		
		//右
		for(int i=1;i<MAX_COUNT_IN_LINE;i++)
		{
			if(points.contains(new Point(x+i,y)))
			{
				count++;
			}else
			{
				break;
			}
		}		
		if(count==MAX_COUNT_IN_LINE)return true;
		
		return false;
	}
			
		//判断左斜是否有五个一致的棋子
	 private boolean checkLeftDiagonal(int x, int y, List<Point> points) 
		{
				    int count=1;

				    for(int i=1;i<MAX_COUNT_IN_LINE;i++)
					{
						if(points.contains(new Point(x-i,y+i)))
						{
							count++;
						}else
						{
							break;
						}
					}		
					if(count==MAX_COUNT_IN_LINE)return true;
					
					for(int i=1;i<MAX_COUNT_IN_LINE;i++)
					{
						if(points.contains(new Point(x+i,y-i)))
						{
							count++;
						}else
						{
							break;
						}
					}		
					if(count==MAX_COUNT_IN_LINE)return true;
					
					return false;
			}
	
	//判断上下是否有五个一致的棋子
	private boolean checkVertical(int x, int y, List<Point> points)
	{
			int count=1;
			//上
			for(int i=1;i<MAX_COUNT_IN_LINE;i++)
			{
				if(points.contains(new Point(x,y-i)))
				{
					count++;
				}else
				{
					break;
				}
			}		
			if(count==MAX_COUNT_IN_LINE)return true;
			
			//右
			for(int i=1;i<MAX_COUNT_IN_LINE;i++)
			{
				if(points.contains(new Point(x,y+i)))
				{
					count++;
				}else
				{
					break;
				}
			}		
			if(count==MAX_COUNT_IN_LINE)return true;
			
			return false;
	}
	
	//判断左斜是否有五个一致的棋子
			private boolean checkRightDiagonal(int x, int y, List<Point> points) 
			{
					    int count=1;

					    for(int i=1;i<MAX_COUNT_IN_LINE;i++)
						{
							if(points.contains(new Point(x-i,y-i)))
							{
								count++;
							}else
							{
								break;
							}
						}		
						if(count==MAX_COUNT_IN_LINE)return true;
						
						for(int i=1;i<MAX_COUNT_IN_LINE;i++)
						{
							if(points.contains(new Point(x+i,y+i)))
							{
								count++;
							}else
							{
								break;
							}
						}		
						if(count==MAX_COUNT_IN_LINE)return true;
						
						return false;
				}

	private void drawPieces(Canvas canvas) {
		for(int i=0,n=mWhiteArray.size();i<n;i++)
		{
			Point whitePoint=mWhiteArray.get(i);
			canvas.drawBitmap(mWhitePiece, 
					(whitePoint.x+(1-ratioPieceofLineHeight)/2)*mLineHeight, 
					(whitePoint.y+(1-ratioPieceofLineHeight)/2)*mLineHeight,null);
		}
		
		for(int i=0,n=mBlackArray.size();i<n;i++)
		{
			Point blackPoint=mBlackArray.get(i);
			canvas.drawBitmap(mBlackPiece, 
					(blackPoint.x+(1-ratioPieceofLineHeight)/2)*mLineHeight, 
					(blackPoint.y+(1-ratioPieceofLineHeight)/2)*mLineHeight,null);
		}
		
	}

	private void drawBoard(Canvas canvas) {
		int w=mPanelWidth;
		float lineHeight=mLineHeight;
		for (int i = 0; i < MAX_LINE; i++) {
			int startX=(int)(lineHeight/2);
			int endX=(int)(w-lineHeight/2);
			int y=(int)((0.5+i)*lineHeight);
			canvas.drawLine(startX, y, endX, y, mPaint);
			canvas.drawLine(y, startX, y, endX, mPaint);
			
		}
		
	}
	
	public void start()
	{
		mWhiteArray.clear();
		mBlackArray.clear();
		mIsGameOver=false;
		mIsWhiteWinner=false;
		invalidate();
	}
	
	private static final String INSTANCE="instance";
	private static final String INSTANCE_GAME_OVER="instance_game_over";
	private static final String INSTANCE_WHITE_ARRAY="instance_white_array";
	private static final String INSTANCE_BLACK_ARRAY="instanc_black_array";
	
	@Override
	/*系统会自动调用它来保存Activity的一些数据
	 * Parcelable就是为了进行序列化
	 * 永久性保存对象，保存对象的字节序列到本地文件中
	 * @see android.view.View#onSaveInstanceState()
	 */
	protected Parcelable onSaveInstanceState() 
	{
		
		/*Bundle类是一个key-value对,两个activity之间的通讯可以通过bundle类来实现*/
		Bundle bundle=new Bundle();
		bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
		bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
		bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
		bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
		return bundle;
	}
	
	@Override
	/*恢复Activity*/
	protected void onRestoreInstanceState(Parcelable state) 
	{
		/*instanceof 运算符是用来在运行时指出对象是否是特定类的一个实例*/
		if(state instanceof Bundle)
		{
			Bundle bundle=(Bundle)state;
			mIsGameOver=bundle.getBoolean(INSTANCE_GAME_OVER);
			mWhiteArray=bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
			mBlackArray=bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
			
			super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
			
			return;
		}
		super.onRestoreInstanceState(state);
	}
}


