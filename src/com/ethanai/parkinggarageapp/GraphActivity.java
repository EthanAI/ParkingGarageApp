package com.ethanai.parkinggarageapp;

import java.io.File;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
//import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.ethanai.parkinggarageapp.UserSettings.GarageLocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class GraphActivity extends Activity {
	
	public static UserSettings mySettings = new UserSettings(); //initialize the settings. Should only be one of this object ever. 
	
    private GraphicalView mChart;
    
    private TimeSeries rotationCountSeries;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    
    private XYSeriesRenderer rotationCountRenderer;

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    
    RecentSensorData recentData = new RecentSensorData();
        
	private final String ACCELEROMETER_TAG 	= "accelerometer";
	private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	private final String COMPASS_TAG 		= "compass";
	private final String PRESSURE_TAG 		= "pressure";
	
	private final String GPS_UPDATE_TAG		= "gpsUpdate";
	private final String NETWORK_UPDATE_TAG	= "networkUpdate";

	
	private LocalBroadcastManager lbManager; //only handles messages sent from this app
	
	private int floorNumber = 1; //for counting which floor we are recording next
    
       	
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String updateType = intent.getStringExtra("updateType");
		    Log.i("GraphActivityReceiver", "Got message: " + updateType);
		    recentData = (RecentSensorData) intent.getSerializableExtra("recentData");
		    
		    if(updateType.equals(ACCELEROMETER_TAG)) {
 
		    } else if (updateType.equals(MAGNETIC_TAG)) {
		    		    	
		    } else if (updateType.equals(ORIENTATION_TAG)) {
		    	updateTextViews();
		    	updateChart();	
		    } else if (updateType.equals(COMPASS_TAG)) {
		    	
		    } else if (updateType.equals(PRESSURE_TAG)) {	
		    	
		    } else if (updateType.equals(GPS_UPDATE_TAG) || updateType.equals(NETWORK_UPDATE_TAG)) {
		    	updateTextViews();
		    }
		    	
		    
		}
	};
	    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        
        TextView tvNextFloor = (TextView) findViewById(R.id.nextFloorField);
    	tvNextFloor.setText(Integer.toString(floorNumber));
        
		
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
		lbManager.registerReceiver(mMessageReceiver, new IntentFilter(GPS_UPDATE_TAG));	
		lbManager.registerReceiver(mMessageReceiver, new IntentFilter(NETWORK_UPDATE_TAG));	
		
	}
	

	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
	public void updateTextViews()	{
		TextView tvTurn = (TextView) findViewById(R.id.turnField);
    	tvTurn.setText("Raw Turns: " + recentData.turnConsecutiveCount);
    	
    	TextView tvFloor = (TextView) findViewById(R.id.floorField);
    	tvFloor.setText("Floor: " + recentData.parkedFloor);
    	
    	TextView tvNextFloor = (TextView) findViewById(R.id.nextFloorField);
    	tvNextFloor.setText(Integer.toString(floorNumber));

    	int newLocationUpdateMinTime = 0;
    	if(recentData.distanceNearestGarage < 2000) {
    		newLocationUpdateMinTime = 0;
    	} else if(recentData.distanceNearestGarage < 5000) {
    		newLocationUpdateMinTime = 30 * 1000;
    	} else if(recentData.distanceNearestGarage < 10000) {
    		newLocationUpdateMinTime = 2 * 60 * 1000;
    	} else {
    		newLocationUpdateMinTime = 6 * 60 * 1000;
    	}
    	
    	TextView tvGarage = (TextView) findViewById(R.id.garageField);
    	tvGarage.setText("D: " + Float.toString(recentData.distanceNearestGarage) + "\n" 
    			+ recentData.newestPhoneLocation.getProvider() + " " + recentData.newestPhoneLocation.getNearestGarage().name
    			+ "\n" + "updates: " + newLocationUpdateMinTime);
	}
	
    private void initChart() {
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	float MEDIUM_TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);

    	rotationCountSeries = new TimeSeries("Turns"); //add better time element?
    	mDataset.addSeries(rotationCountSeries);
    	rotationCountRenderer = new XYSeriesRenderer();
    	rotationCountRenderer.setColor(Color.BLACK);
    	mRenderer.addSeriesRenderer(rotationCountRenderer);
    	
		mRenderer.setYTitle("Quarter Turns");
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
		mRenderer.setShowGridX(true);
		//TODO add button to restore view to following
		
		mRenderer.setLabelsTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.WHITE);
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
    	int i = 0;
    	if(recentData.orientRecent.size() > UserSettings.graphHistoryCount)
    		i = recentData.orientRecent.size() - UserSettings.graphHistoryCount;
    	for(; i < recentData.orientRecent.size(); i++) {
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
    public void deleteCSVFiles(View view) {
    	//TODO make this softcoded once user settings is in a final implementation
    	String folderName = "Documents";
    	String sdCard = Environment.getExternalStorageDirectory().toString(); //get root of external storage
        File dir = new File(sdCard, folderName);
    	for(File file: dir.listFiles()) {
    		if(file.getName().endsWith(".csv"))
    			file.delete();
    	}
    	Toast.makeText(getBaseContext(), "CSVs deleted.", Toast.LENGTH_SHORT).show();
    }
    
    public void storeFloorData(View view) {
    	EditText floorTextField = (EditText) findViewById(R.id.floorEntryField);
    	String floorText = floorTextField.getText().toString();
    	
    	DataAnalyzer dataAnalyzer = new DataAnalyzer(recentData);
    	float turnCount = dataAnalyzer.getConsecutiveTurns();
    	Log.i("GraphActivity", "Got Floor: " + floorText);
    	floorTextField.setText("");
    	
    	//get current garage location
    	//get current entry location
    	//get current borderfile
    	
    	//get current turn history since ... garage entrance? (Just do consecutive turns for now)
    	//find middle between this border and previous/most similar floor
    	//insert a border with averaged value OR refactor code to take floor data and find nearest match
    	mySettings.addFloorRecord("TestGarage", floorText, turnCount);
    	
    	Toast.makeText(getBaseContext(), "Stored Floor: " + floorText + "\n" + "TurnCount: " + turnCount, Toast.LENGTH_SHORT).show();
    }
    
    public void clearFloor(View view) {
    	EditText floorTextField = (EditText) findViewById(R.id.floorEntryField);
    	floorTextField.setText("");
    }
    
   // public void forceStartSensors(View view) {
   // 	Toast.makeText(getBaseContext(), "Not Impelemented Yet", Toast.LENGTH_SHORT).show();
    //}
    
    public void createNewGarageLocation(View view) {
    	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		
    	if(null != location) {
	    	String name = "Temp";
	    	//PhoneLocation phoneLocation = recentData.newestPhoneLocation;
	    	GarageLocation garageLocation = mySettings.new GarageLocation(name, location, null);
	    	UserSettings.allGarageLocations.add(garageLocation);
	    	mySettings.saveSettings();
	    	
	    	Toast.makeText(getBaseContext(), "Created Garage: " + name, Toast.LENGTH_SHORT).show();
    	} else {
    		Toast.makeText(getBaseContext(), "No location data yet\nTry later", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void resetGarageLocations(View view) {
    	mySettings.resetGarageLocations();
    	Toast.makeText(getBaseContext(), "GarageLocations Reset", Toast.LENGTH_SHORT).show();
    }
    
    public void addFloor(View view) {
		DataAnalyzer dataAnalyzer = new DataAnalyzer(recentData);
		
    	if(null == recentData.newestPhoneLocation) {
    		Toast.makeText(getBaseContext(), "No location data yet\nTurn on sensors", Toast.LENGTH_SHORT).show();
    	} else if (dataAnalyzer.turnDegreesArray.size() == 0) {
    		Toast.makeText(getBaseContext(), "Sensors not running\nRestart 1 km away", Toast.LENGTH_SHORT).show();
    	} else {
	    	//PhoneLocation phoneLocation = recentData.newestPhoneLocation;
	    	String locationName = recentData.newestPhoneLocation.getLocationName();
	    	
	    	float turnCount = dataAnalyzer.getConsecutiveTurns();
	    	mySettings.addFloorRecord(locationName, Integer.toString(floorNumber), turnCount);
	    	floorNumber++;
	    	Toast.makeText(getBaseContext(), "addfloor Located:" + locationName + "\n" + turnCount, Toast.LENGTH_SHORT).show();
    	} 
    	//mySettings.addFloorRecord("TestGarage", floorText, turnCount);
    	
    	//Toast.makeText(getBaseContext(), "Stored Floor: " + floorText + "\n" + "TurnCount: " + turnCount, Toast.LENGTH_SHORT).show();
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
		startService(intent); //start Accelerometer Service. 
	}
	
	public void stopSensorService(View view) {
		//Context context = getBaseContext();
		Intent intent = new Intent(getApplicationContext(), SensorService.class);
		stopService(intent); 
	}
  
}