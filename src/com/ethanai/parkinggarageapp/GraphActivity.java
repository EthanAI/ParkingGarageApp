package com.ethanai.parkinggarageapp;

import java.io.File;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
//import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//TODO add reciever for orientation also add that graph, probably our important one
@SuppressLint("SimpleDateFormat")
public class GraphActivity extends Activity {
	
	public static UserSettings mySettings = new UserSettings(); //initialize the settings. Should only be one of this object ever. 
	
    private GraphicalView mChart;
    
    private TimeSeries rotationCountSeries;
    /*
    private TimeSeries mXSeries;
    private TimeSeries mYSeries;
    private TimeSeries mZSeries;
    private TimeSeries mAngleSeries;
    */

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    
    private XYSeriesRenderer rotationCountRenderer;
    /*
    private XYSeriesRenderer mXRenderer;
    private XYSeriesRenderer mYRenderer;
    private XYSeriesRenderer mZRenderer;
    private XYSeriesRenderer mAngleRenderer;
    */

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
       
    //int plotDataCount = 2000; //passed to sensor service then to recentsensordata. if we want to overrride defaults in sensorservice //move to user settings
    
    RecentSensorData recentData = new RecentSensorData();
        
	private final String ACCELEROMETER_TAG 	= "accelerometer";
	private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	private final String COMPASS_TAG 		= "compass";
	private final String PRESSURE_TAG 		= "pressure";
	
	private LocalBroadcastManager lbManager; //only handles messages sent from this app
    
       	
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
    //TODO move this to another class or clean up
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String sensorType = intent.getStringExtra("sensorType");
		    Log.i("GraphActivityReceiver", "Got message: " + sensorType);
		    recentData = (RecentSensorData) intent.getSerializableExtra("recentData");
		    
		    if(sensorType.equals(ACCELEROMETER_TAG)) {
 
		    } else if (sensorType.equals(MAGNETIC_TAG)) {
		    		    	
		    } else if (sensorType.equals(ORIENTATION_TAG)) {
		    	TextView tvTurn = (TextView) findViewById(R.id.turnField);
		    	tvTurn.setText("Raw Turns: " + recentData.turnConsecutiveCount);
		    	
		    	TextView tvFloor = (TextView) findViewById(R.id.floorField);
		    	tvFloor.setText("Floor: " + recentData.parkedFloor);
		    	
		    	updateChart();	
		    } else if (sensorType.equals(COMPASS_TAG)) {
		    	
		    } else if (sensorType.equals(PRESSURE_TAG)) {	
		    }
		    
		}
	};
	    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
		
		//make this poll sensor service status and verify if it is running. May need some kind of trigger to repaint
	    //TextView tvTest = (TextView) findViewById(R.id.floorField);
	    //tvTest.setText("0.0"); //recentEntries.get(recentEntries.size() - 1).toString()); //don't show anything until we're getting data. 
			
	    //listeners are so we can hear when the sensor service updates so we can update our graph view. 
	    //need to list each term we are listening for here
	    lbManager = LocalBroadcastManager.getInstance(getApplicationContext());
	    lbManager.registerReceiver(mMessageReceiver, new IntentFilter(ACCELEROMETER_TAG));
	    lbManager.registerReceiver(mMessageReceiver, new IntentFilter(MAGNETIC_TAG));
	    lbManager.registerReceiver(mMessageReceiver, new IntentFilter(ORIENTATION_TAG));
	    lbManager.registerReceiver(mMessageReceiver, new IntentFilter(COMPASS_TAG));
		lbManager.registerReceiver(mMessageReceiver, new IntentFilter(PRESSURE_TAG));	
		
	}
	

	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
    private void initChart() {
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	float MEDIUM_TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);

    	rotationCountSeries = new TimeSeries("Turns"); //add better time element?
    	mDataset.addSeries(rotationCountSeries);
    	rotationCountRenderer = new XYSeriesRenderer();
    	rotationCountRenderer.setColor(Color.RED);
    	mRenderer.addSeriesRenderer(rotationCountRenderer);
    	
		mRenderer.setYTitle("Quarter Turns");
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
		mRenderer.setShowGridX(true);
		//TODO add button to restore view to following
		
		mRenderer.setLabelsTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.BLACK);
		
    	/*
        //mCurrentSeries = new TimeSeries("Accelerometer");
        mXSeries = new TimeSeries("X");
        mYSeries = new TimeSeries("Y");
        mZSeries = new TimeSeries("Z");
        mAngleSeries = new TimeSeries("Angle");

        mDataset.addSeries(mXSeries);
        mDataset.addSeries(mYSeries);
        mDataset.addSeries(mZSeries);
        mDataset.addSeries(mAngleSeries);

        mXRenderer = new XYSeriesRenderer();
        mXRenderer.setColor(Color.RED);
        mYRenderer = new XYSeriesRenderer();
        mYRenderer.setColor(Color.YELLOW);
        mZRenderer = new XYSeriesRenderer();
        mZRenderer.setColor(Color.GREEN);
        mAngleRenderer = new XYSeriesRenderer();
        mAngleRenderer.setColor(Color.WHITE);

        mRenderer.addSeriesRenderer(mXRenderer);
		mRenderer.addSeriesRenderer(mYRenderer);
		mRenderer.addSeriesRenderer(mZRenderer);
		mRenderer.addSeriesRenderer(mAngleRenderer);
        
		//mRenderer.setYTitle("gs of force");
		mRenderer.setYTitle("Angle");
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
		mRenderer.setShowGridX(true);
		//TODO add button to restore view to following
		
		mRenderer.setLabelsTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.BLACK);
		//mRenderer.setMarginsColor(Color.RED);
		
    	//mCurrentRenderer.setChartValuesTextSize(val);
		*/

    }

    private void loadData() {
    	rotationCountSeries.clear();

    	/*
    	mXSeries.clear();
    	mYSeries.clear();
    	mZSeries.clear();
    	mAngleSeries.clear();
    	*/
    	//for(int i = 0; i < plotDataCount && i < recentData.accRecent.size(); i++) {
    	//	mCurrentSeries.add(i, recentData.accRecent.get(i).x);
    	//}
    	
    	for(int i = 0; i < recentData.orientRecent.size(); i++) {
    		//if(rotationCountSeries.getItemCount() > plotDataCount) { //limit size of what we want to plot
    		//	rotationCountSeries.remove(0); //remove oldest item
    		//}
    		rotationCountSeries.add(i, recentData.orientRecent.get(i).totalTurnDegrees / 90);
    		/*
    		mXSeries.add(i, recentData.orientRecent.get(i).azimuthInDegrees);
    		mYSeries.add(i, recentData.orientRecent.get(i).pitchInDegrees);
    		mZSeries.add(i, recentData.orientRecent.get(i).rollInDegrees);
    		//mAngleSeries.add(i, recentData.orientRecent.get(i).inclinationInDegrees);
    		mAngleSeries.add(i, recentData.orientRecent.get(i).totalTurnDegrees);
    		*/
    	}
    }

    protected void onResume() {
        super.onResume();
        updateChart();
    }
    
    /*
     * Deletes and reloads all the data. Runs really fast anyway, but could be optimized if needed. 
     * (maybe if we plot 7 or 8 sensors at once it might be important?)
     * Good demo here https://www.youtube.com/watch?v=E9fozQ5NlSo
     */
    private void updateChart() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        
        if(mChart == null) {
        	initChart();
        	loadData();
        	mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        	layout.addView(mChart);
        
        //} else if(recentData.accRecent != null && recentData.accRecent.size() > 0) {

        } else if(recentData.magnRecent != null && recentData.magnRecent.size() > 0) {
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
    public void deleteFiles(View view) {
    	//TODO make this softcoded once user settings is in a final implementation
    	String folderName = "Documents";
    	String sdCard = Environment.getExternalStorageDirectory().toString(); //get root of external storage
        File dir = new File(sdCard, folderName);
    	for(File file: dir.listFiles()) 
    		file.delete();
    	Toast.makeText(getBaseContext(), "CSVs deleted.", Toast.LENGTH_SHORT).show();
    }
    
	public void changeToTextActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, TextActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void changeToHistoryActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, HistoryActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void changeToSettingsActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, SettingsActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void startSensorService(View view) { 
		Intent intent = new Intent(getBaseContext(), SensorService.class);
		//intent.putExtra("maxReadingHistoryCount", plotDataCount); //later restore this so we can control the graph view easily
		startService(intent); //start Accelerometer Service. Pass it info
	}
	
	public void stopSensorService(View view) {
		//Context context = getBaseContext();
		Intent intent = new Intent(getApplicationContext(), SensorService.class);
		stopService(intent); 
	}
  
}