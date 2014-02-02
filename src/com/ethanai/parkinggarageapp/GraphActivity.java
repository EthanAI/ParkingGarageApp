package com.ethanai.parkinggarageapp;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class GraphActivity extends Activity {
	
    private GraphicalView mChart;
    
    private TimeSeries mCurrentSeries;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    
    private XYSeriesRenderer mCurrentRenderer;
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
       
    int plotDataCount = 100000; //plot limited by size of recent data object. Not limited here
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
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("pressure"));
	}
	

	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
    private void initChart() {
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	float MEDIUM_TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);

    	
        mCurrentSeries = new TimeSeries("Accelerometer");
        mDataset.addSeries(mCurrentSeries);
        
        mCurrentRenderer = new XYSeriesRenderer();
        
		mRenderer.setYTitle("gs of force");
		mRenderer.addSeriesRenderer(mCurrentRenderer);
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
    	//mCurrentRenderer.setChartValuesTextSize(val);


    }

    private void loadData() {
    	mCurrentSeries.clear();
    	for(int i = 0; i < plotDataCount && i < recentData.accRecent.size(); i++) {
    		mCurrentSeries.add(i, recentData.accRecent.get(i).x);
    	}
    }

    protected void onResume() {
        super.onResume();
        updateChart();
    }
    
    /*
     * Deletes and reloads all the data. Runs really fast anyway, but could be optimized if needed. 
     * (maybe if we plot 7 or 8 sensors at once it might be important?)
     */
    private void updateChart() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        
        if(mChart == null) {
        	initChart();
        	loadData();
        	mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        	layout.addView(mChart);
        
        } else if(recentData.accRecent != null && recentData.accRecent.size() > 0) {
        	//initChart();
        	loadData();
        	//mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        	//layout.addView(mChart);
        	mChart.repaint();
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