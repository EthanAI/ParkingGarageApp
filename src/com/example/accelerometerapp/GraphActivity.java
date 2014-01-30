package com.example.accelerometerapp;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class GraphActivity extends Activity {
	
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
       
    int plotDataCount = 100;
    RecentSensorData recentData = new RecentSensorData();
       	
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String sensorType = intent.getStringExtra("sensorType");
		    Log.d("GraphActivityReceiver", "Got message: " + sensorType);
		    recentData = (RecentSensorData) intent.getSerializableExtra("recentData");
		    if(sensorType.equals("accelerometer")) {
		    	updateChart();
		    } else if (sensorType.equals("compass")) {
		    	
		    } else if (sensorType.equals("humidity")) {
		    	
		    } else if (sensorType.equals("pressure")) {
		    	
		    }
		}
	};
    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
		
		//make this poll sensor service status and verify if it is running. May need some kind of trigger to repaint
	    TextView tvTest = (TextView) findViewById(R.id.testField);
	    tvTest.setText("0.0"); //recentEntries.get(recentEntries.size() - 1).toString());
			
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("accelerometer"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("compass"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("humidity"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("pressure"));
	}
	

	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addData() {
    	for(int i = 0; i < plotDataCount && i < recentData.accRecent.size(); i++) {
    		mCurrentSeries.add(i, recentData.accRecent.get(i).x);
    	}
    }

    protected void onResume() {
        super.onResume();
        updateChart();
    }
    
    private void updateChart() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        
        if(mChart != null)
        	layout.removeView(mChart);
        
        if(recentData.accRecent != null && recentData.accRecent.size() > 0) {
        	initChart();
        	addData();
        	mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
        	layout.addView(mChart);
        }
    }  

	
  /*
   * Function for the buttons
   */
	public void changeToTextActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, TextActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void startAccelerometerService(View view) { 
		Intent intent = new Intent(getBaseContext(), AccelerometerService.class);
		intent.putExtra("maxReadingHistoryCount", plotDataCount);
		startService(intent); //start Accelerometer Service. Pass it info
	}
	
	public void stopAccelerometerService(View view) {
		stopService(new Intent(getBaseContext(), AccelerometerService.class)); //start Accelerometer Service. Pass it info
	}
  
}