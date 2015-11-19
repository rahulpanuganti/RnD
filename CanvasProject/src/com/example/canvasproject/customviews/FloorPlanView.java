package com.example.canvasproject.customviews;

import com.example.canvasproject.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloorPlanView extends ImageView{
	
	private double x,y;
	private boolean init;
	private Bitmap bitmap;
	private Paint paint;
	private ScaleGestureDetector detector;
	private static float MIN_ZOOM = 1f;
	private static float MAX_ZOOM = 5f;
	private float scaleFactor = 1.f;
	private Rect dst;
	private static int NONE = 0;
	private static int DRAG = 1;
	private static int ZOOM = 2;
	private int mode = 0;
	private float startX = 0f,startY = 0f, translateX = 0f, translateY = 0f, 
			displayWidth, displayHeight, lastGestureX, lastGestureY, 
			zoomTranslationX = 0f, zoomTranslationY = 0f, previousTranslateX = 0f, previousTranslateY = 0f,
			dx, dy;
	private Display display;
	private Point dimensions;
	
	public FloorPlanView(Context context) {
		super(context);
		init(context);
	}

	public FloorPlanView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context);
	}
	
	public FloorPlanView(Context context, AttributeSet attrs,int defStyleAttr) {
		super(context,attrs,defStyleAttr);
		init(context);
	}
	
	BroadcastReceiver pointReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			x = intent.getDoubleExtra("X", 0);
			y = intent.getDoubleExtra("Y", 0);
			postInvalidate();
		}
	};
	
	@Override
	public void onDraw(Canvas canvas){
		Log.e("Scale2", translateX - previousTranslateX + "");
		if(translateX + (getLimitLeft()) > 0) {
			translateX = -getLimitLeft();
		}
		else if(translateX - getLimitRight() < 0) {
			translateX = getLimitRight();
			}
		else {
			
		}
		if(translateY  + getLimitTop()> 0) {
			translateY = - getLimitTop();
		}
		else if(((translateY)) < getLimitBottom()) {
			translateY = getLimitBottom();
			}
		super.onDraw(canvas);
		canvas.save();
		canvas.translate(translateX, translateY);
		if(detector.isInProgress()) {
			canvas.scale(scaleFactor, scaleFactor,detector.getFocusX(),detector.getFocusY());
		}
		else {
			canvas.scale(scaleFactor, scaleFactor, lastGestureX, lastGestureY);
		}
		canvas.drawBitmap(bitmap, null, dst,null);
		canvas.drawCircle((float) x, (float) y, 5/scaleFactor, paint);
		canvas.restore();
		if(init) {
			displayWidth = getWidth();
			dst = new Rect(0, 0, (int)displayWidth, (int)displayWidth);
			init = false;
			invalidate();
		}
	}
	
	public void init(Context context) {
		init = true;
		context.registerReceiver(pointReceiver, new IntentFilter("Coordinates"));
		dimensions = new Point();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getSize(dimensions);
		//Log.e("Width", "" + getWidth());
		displayWidth = dimensions.x;
		dst = new Rect(0, 0, (int)displayWidth, (int)displayWidth);
		paint = new Paint();
		paint.setColor(Color.BLUE);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan);
		detector = new ScaleGestureDetector(getContext(), new ScaleListener());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			startX = event.getX() - previousTranslateX;
			startY = event.getY() - previousTranslateY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (detector.isInProgress()) {
					lastGestureX = detector.getFocusX();
					lastGestureY = detector.getFocusY();
			}
			dx = event.getX() - startX + previousTranslateX;
			dy = event.getY() - startY + previousTranslateY;
			
			if(dx == 0 && dy == 0) {
				mode = NONE;
			}
			if (mode == DRAG) {
				Log.e("Scale", event.getX() - startX + "");
				translateX = (event.getX() - startX);
				translateY = (event.getY() - startY);
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mode = ZOOM;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = DRAG;
			previousTranslateX = translateX;
			previousTranslateY = translateY;
			startX = event.getX() - previousTranslateX;
			startY = event.getY() - previousTranslateY;
			break;
		case MotionEvent.ACTION_UP:
			previousTranslateX = translateX;
			previousTranslateY = translateY;
			mode = NONE;
			break;
		}
		
		
		
		if(scaleFactor == 1f){
			translateX = 0f;
			translateY = 0f;
			invalidate();
		}
		if ((mode == DRAG && scaleFactor != 1f) || mode == ZOOM) {
			invalidate();
		}
		return true;
	}
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float oldScaleFactor = scaleFactor;
			scaleFactor *= detector.getScaleFactor();
			float scaleDifference = scaleFactor/oldScaleFactor;
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			zoomTranslationX = ((1 - scaleDifference) * detector.getFocusX());
			zoomTranslationY = ((1 - scaleDifference) * detector.getFocusY());
			//translateX -= zoomTranslationX;
			//translateY -= zoomTranslationY;
			previousTranslateX = translateX;
			previousTranslateY = translateY;
			return true;
		}
	}
	
	public float getLimitLeft() {
		return (1 - scaleFactor) * lastGestureX;
	}
	
	public float getLimitRight() {
		return (1 - scaleFactor) * (displayWidth - lastGestureX);
	}
	
	public float getLimitTop() {
		return (1 - scaleFactor) * lastGestureY;
	}
	
	public float getLimitBottom() {
		return (1 - scaleFactor) * (displayWidth - lastGestureY);
	}
}
