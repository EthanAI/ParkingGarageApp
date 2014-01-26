package com.example.accelerometerapp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GraphActivity extends Activity {
	
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    
    int plotDataCount = 100;
    File externalFile = null;
    ArrayList<SensorReading> recentEntries;
    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
    
		try {
		    Bundle bundle = getIntent().getExtras();
		    externalFile = new File(bundle.getString("fileName"));
			recentEntries = getRecentEntries(plotDataCount);
			
		    TextView tvTest = (TextView) findViewById(R.id.testField);
		    tvTest.setText(recentEntries.get(recentEntries.size() - 1).toString());
		} catch (NumberFormatException e) {
        	Log.e("Exception", e.toString());
		} catch (ParseException e) {
        	Log.e("Exception", e.toString());
		}	
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
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            addData();
            mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }
	
    /*
     * Reads data in from the file. Has to read the entire file.
     * Will throw away everything but the last n=readCount entries
     */
    public ArrayList<SensorReading> getRecentEntries(int readCount) throws NumberFormatException, ParseException {
    	ArrayList<SensorReading> readData = new ArrayList<SensorReading>();
    	try {
			FileReader fr = new FileReader(externalFile);
			BufferedReader br = new BufferedReader(fr);
			
			//read to end of file
			String line = "";
			br.readLine(); //skip the header line
			while((line = br.readLine()) != null) {
				SensorReading reading = new SensorReading(line);
				readData.add(reading);
				if(readData.size() > readCount)
					readData.remove(0);
			}
			br.close();
		} catch (FileNotFoundException e) {
        	Log.e("Exception", e.toString());
		} catch (IOException e) {
        	Log.e("Exception", e.toString());
		}
    	return readData;
    }
	
  /*
   * Function for the button
   */
	public void changeToMainActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, MainActivity.class);
	    startActivity(intent);
	}
  
}