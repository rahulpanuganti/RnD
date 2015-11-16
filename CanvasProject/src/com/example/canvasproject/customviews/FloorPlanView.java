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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class FloorPlanView extends ImageView{
	
	private double x,y;
	private Bitmap bitmap,mutableBitmap;
	private Paint paint;
	private ScaleGestureDetector detector;
	private static float MIN_ZOOM = 1f;
	private static float MAX_ZOOM = 5f;
	private float scaleFactor = 1.f;
	private Canvas tempCanvas;
	private Rect dst;
	
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
		canvas.scale(scaleFactor, scaleFactor);
		canvas.drawBitmap(bitmap, null, dst,null);
		canvas.drawCircle((float) x, (float) y, 5, paint);
		canvas.restore();
	}
	
	public void init(Context context) {
		context.registerReceiver(pointReceiver, new IntentFilter("Coordinates"));
		dst = new Rect(0, 0, 400, 400);
		paint = new Paint();
		paint.setColor(Color.BLUE);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan);
		updateBitmap();
		detector = new ScaleGestureDetector(getContext(), new ScaleListener());
	}
	
	public void updateBitmap(){
		mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		tempCanvas = new Canvas(mutableBitmap);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return detector.onTouchEvent(event);
	}
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			updateBitmap();
			invalidate();
			return true;
		}
	}
}
