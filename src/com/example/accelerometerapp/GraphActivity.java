package com.example.accelerometerapp;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import android.os.Environment;
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
    ArrayList<SensorReading> recentEntries;
    
    private final String DIRECTORY_NAME = "Documents";
    String fileNameFullPath = null;
    File externalFile = null;
	private String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm";
	
	
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String message = intent.getStringExtra("message");
		    Log.d("GraphActivityReceiver", "Got message: " + message);
		    recentEntries = (ArrayList<SensorReading>) intent.getSerializableExtra("recentEntries");
	        updateChart();
		}
	};
    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
           
		Date date = new Date();        
	    String dateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(date);             
	    String fileName = dateString + " sensorReadings.csv";
        externalFile = createExternalFile(DIRECTORY_NAME, fileName); //maybe can move this to service
		
		//make this poll sensor service status and verify if it is running. May need some kind of trigger to repaint
	    TextView tvTest = (TextView) findViewById(R.id.testField);
	    tvTest.setText("0.0"); //recentEntries.get(recentEntries.size() - 1).toString());
			
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
      	      new IntentFilter("sensorData"));
	}
	
    public <E> void addUpToLimit(ArrayList<E> list, E newEntry, int maxEntries) {
        if(list.size() > maxEntries)
            list.remove(0);
        list.add(newEntry);
    }


	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
    public File createExternalFile(String directory, String fileName) {
        File myFile = null;
        try {
            String sdCard = Environment.getExternalStorageDirectory().toString(); //get root of external storage
            File dir = new File(sdCard, directory);
            if (!dir.exists()) { //make directory if it doesnt exist
                dir.mkdirs();  //make all parent directories even.
            }

            myFile = new File(dir.getAbsolutePath(), fileName); //add on the filename to the total path

            // if file doesn't exists, then create it
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return myFile;
    }
	

    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    private void addData() {
    	for(int i = 0; i < plotDataCount && i < recentEntries.size(); i++) {
    		mCurrentSeries.add(i, recentEntries.get(i).x);
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
        
        if(recentEntries != null && recentEntries.size() > 0) {
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
		intent.putExtra("externalFile", externalFile);
		intent.putExtra("DATE_FORMAT_STRING", DATE_FORMAT_STRING);
	    startActivity(intent);
		//this.finish();
	}
	
	public void startAccelerometerService(View view) {
		Intent intent = new Intent(getBaseContext(), AccelerometerService.class);
		intent.putExtra("externalFile", externalFile);
		intent.putExtra("DATE_FORMAT_STRING", DATE_FORMAT_STRING);
		intent.putExtra("maxReadingHistoryCount", plotDataCount);
		startService(intent); //start Accelerometer Service. Pass it info
	}
	
	public void stopAccelerometerService(View view) {
		stopService(new Intent(getBaseContext(), AccelerometerService.class)); //start Accelerometer Service. Pass it info
	}
  
}