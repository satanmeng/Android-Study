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


//�Զ���VIEW
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
		init();//��ʼ��
	}
	
	private void init() {
	   mPaint.setColor(0x88000000);
	   mPaint.setAntiAlias(true);//��ֹ��Ե�ľ��
	   mPaint.setDither(true);//���÷�����
	   mPaint.setStyle(Paint.Style.STROKE);	//���û��ʷ��,�û�����ͼ���ǿ��ĵ� 
	   
	   //����Դ�ļ��ж�ȡһ��ͼƬ������һ��Bitmap,
	   //��һ�������ǰ�����Ҫ���ص�λͼ��Դ�ļ��Ķ���һ��д�� getResources������ok�ˣ����ڶ���ʱ����Ҫ���ص�λͼ��Դ��Id��
	   mWhitePiece=BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
	   mBlackPiece=BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
	}

	
	
	@Override
	/*View�����С���٣�����onMeasure()����*/
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		/*�ṩ���������⹫����static����UNSPECIFIED,
		 * EXACTLY,AT_MOST,
		 * ,����������ͳ��ΪspecMode��
		 * ����һ��View��˵���Ŀ�͸߸��������Լ���specMode
		 */
		int widthSize=MeasureSpec.getSize(widthMeasureSpec);
		int widthMode=MeasureSpec.getMode(widthMeasureSpec);
		
		int heighSize=MeasureSpec.getSize(heightMeasureSpec);
		int heighMode=MeasureSpec.getMode(heightMeasureSpec);
		
		int width=Math.min(widthSize, heighSize);
		
		//UNSPECIFIED(δָ��)
		if(widthMode==MeasureSpec.UNSPECIFIED)
		{
		   width=heighSize;
		}else if(heighMode==MeasureSpec.UNSPECIFIED)
		{
			width=widthSize;
		}
		
		//setMeasuredDimension���������������������˵�ǰView�Ĵ�С
		setMeasuredDimension(width, width);
	}
	
	@Override
	/*��������������view�Ĵ�С�����ı��Ǳ�ϵͳ����*/
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		mPanelWidth=w;
		mLineHeight=mPanelWidth*1.0f/MAX_LINE;
		
		int pieceWidth=(int)(mLineHeight*ratioPieceofLineHeight);
		
		//createScaledBitmap()�ӵ�ǰ���ڵ�λͼ����һ���ı�������һ���µ�λͼ��
		mWhitePiece=Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
		mBlackPiece=Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
		
	}
	
	@Override
	/*�����ֻ���Ļ�Ĵ����¼�*/
	public boolean onTouchEvent(MotionEvent event) {
		if(mIsGameOver)
			return false;
		int action=event.getAction();
		
		/*ACTION_DOWN: ��ʾ�û���ʼ����.
          ACTION_MOVE: ��ʾ�û����ƶ�(��ָ��������)
          ACTION_UP:��ʾ�û�̧������ָ 
          ACTION_CANCEL:��ʾ���Ʊ�ȡ����
          */
		if(action==MotionEvent.ACTION_UP)
		{
			
			int x=(int)event.getX();
			int y=(int)event.getY();
			
			Point p=getValidPoint(x,y);
			
			//contains����
			if(mWhiteArray.contains(p)||mBlackArray.contains(p))
				return false;
			
			if(mIsWhite)
			{
				mWhiteArray.add(p);
			}else {
				mBlackArray.add(p);
			}
			
			/*invalidate()������ˢ��View�ģ���������UI�߳��н��й�����
			 * �������޸�ĳ��view����ʾʱ������invalidate()���ܿ������»��ƵĽ��档
			 * invalidate()�ĵ����ǰ�֮ǰ�ľɵ�view����UI�̶߳�����pop���� 
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
	/*onDraw()��������λ������View*/
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		
		//�滭����
		drawBoard(canvas);
		
		//�滭����
		drawPieces(canvas);
		
		//�����Ϸ�Ƿ����
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
			
			String text=mIsWhiteWinner?"����ʤ��":"����ʤ��";
			
			/*Toast ��һ�� View ��ͼ�����ٵ�Ϊ�û���ʾ��������Ϣ��
			 *  Toast ��Ӧ�ó����ϸ�����ʾ��Ϣ���û�������Զ�����ý��㣬
			 *  ��Ӱ���û�������Ȳ�������Ҫ���� һЩ���� / ��ʾ��
			 *  Toast ����Ĵ�����ʽ��ʹ�þ�̬���� Toast.makeText
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
	
	//�жϺ����Ƿ������һ�µ�����
	private boolean checkHorizontal(int x, int y, List<Point> points) {
		int count=1;
		//��
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
		
		//��
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
			
		//�ж���б�Ƿ������һ�µ�����
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
	
	//�ж������Ƿ������һ�µ�����
	private boolean checkVertical(int x, int y, List<Point> points)
	{
			int count=1;
			//��
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
			
			//��
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
	
	//�ж���б�Ƿ������һ�µ�����
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
	/*ϵͳ���Զ�������������Activity��һЩ����
	 * Parcelable����Ϊ�˽������л�
	 * �����Ա�����󣬱��������ֽ����е������ļ���
	 * @see android.view.View#onSaveInstanceState()
	 */
	protected Parcelable onSaveInstanceState() 
	{
		
		/*Bundle����һ��key-value��,����activity֮���ͨѶ����ͨ��bundle����ʵ��*/
		Bundle bundle=new Bundle();
		bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
		bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
		bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
		bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
		return bundle;
	}
	
	@Override
	/*�ָ�Activity*/
	protected void onRestoreInstanceState(Parcelable state) 
	{
		/*instanceof �����������������ʱָ�������Ƿ����ض����һ��ʵ��*/
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


