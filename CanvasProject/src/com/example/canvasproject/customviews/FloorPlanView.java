package com.example.canvasproject.customviews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FloorPlanView extends ImageView{
	
	private double x,y;
	private Paint paint;

	public FloorPlanView(Context context, AttributeSet attr) {
		super(context,attr);
		context.registerReceiver(pointReceiver, new IntentFilter("Coordinates"));
		paint = new Paint();
		paint.setColor(Color.BLUE);
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
		try {
			canvas.drawCircle((float) x, (float) y, 5, paint);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
