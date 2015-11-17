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
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloorPlanView extends ImageView{
	
	private double x,y;
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
			zoomTranslationX = 0f, zoomTranslationY = 0f;
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
		super.onDraw(canvas);
		canvas.save();
		if(translateX - zoomTranslationX > 0) {
			translateX = zoomTranslationX;
		}
		else if(((translateX - zoomTranslationX) * -1) > (scaleFactor - 1) * displayWidth) {
			translateX = (1 - scaleFactor) * displayWidth +zoomTranslationX;
			}
		else {
			
		}
		if(translateY - zoomTranslationY > 0) {
			translateY = zoomTranslationY;
		}
		else if(((translateY - zoomTranslationY) * -1) > (scaleFactor - 1) * displayWidth) {
			translateY = (1 - scaleFactor) * displayWidth + zoomTranslationY;
			}
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
	}
	
	public void init(Context context) {
		context.registerReceiver(pointReceiver, new IntentFilter("Coordinates"));
		dimensions = new Point();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getSize(dimensions);
		displayWidth = dimensions.x - 50;
		dst = new Rect(0, 0, (int)displayWidth, (int)displayWidth);
		paint = new Paint();
		paint.setColor(Color.BLUE);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan);
		detector = new ScaleGestureDetector(getContext(), new ScaleListener());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			startX = event.getX() - translateX;
			startY = event.getY() - translateY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (detector.isInProgress()) {
					lastGestureX = detector.getFocusX();
					lastGestureY = detector.getFocusY();
			}
			if (mode == DRAG) {
				translateX = event.getX() - startX;
				translateY = event.getY() - startY;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mode = ZOOM;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
			mode = NONE;
			break;
		}
		boolean result = detector.onTouchEvent(event);
		if ((mode == DRAG && scaleFactor != 1f) || mode == ZOOM) {
			invalidate();
		}
		return result;
	}
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float oldScaleFactor = scaleFactor;
			scaleFactor *= detector.getScaleFactor();
			float scaleDifference = scaleFactor/oldScaleFactor;
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			zoomTranslationX = ((1 - scaleDifference) * detector.getFocusX())/scaleFactor;
			zoomTranslationY = ((1 - scaleDifference) * detector.getFocusY())/scaleFactor;
			translateX -= zoomTranslationX;
			translateY -= zoomTranslationY;
			return true;
		}
	}
}
