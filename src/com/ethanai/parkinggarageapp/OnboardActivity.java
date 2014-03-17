package com.ethanai.parkinggarageapp;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

//naive implementation to show a few pages of info and get user input
public class OnboardActivity extends Activity {
	
	public ArrayList<String> textArray = new ArrayList<String>();
	public Iterator<String> textIterator;
	
	public TextView tv; 

	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_onboard);	    	    
	    
	    textArray.add(
	    		"Welcome to Parking Garage App. This app will help you find your car inside parking garages, where "
	    		+ "GPS cannot reach. The app runs automatically when you drive, and uses the sensors do detect "
	    		+ "what floor you parked your car on. After the initial set up, the app will do everything automatically."
	    		+ "\n\nThis app in in beta. Currently it works successfuly on garages with one "
	    		+ "entrance, and no intersections within the garage. This version can run automatically if "
	    		+ "you have a bluetooth stereo in your car. This will no longer be required by future versions of the "
	    		+ "app. If you do not have a bluetooth stereo, you give it a try by running it manually. Tell us what "
	    		+ "you think of our app, and look forward to future improvments!"
	    		);
		textArray.add(
				"Details:\n\tChoose or manually set up one or more garage profiles. When you get near one of these "
				+ "parking garages, the app will turn on your phone's sensors and run until you stop your car. "
				+ "Then it will record what floor you parked on in the app or the widget for a quick reminder. "
				+ "It's best for people who have a parking garage at home or work. The app records GPS and sensor "
				+ "information, but all that data stays on your phone. If you ever want to share some of your data "
				+ "with us to help improve the app, the app will create an email with the data attached so you can "
				+ "review what is being sent, when, and always have control over whether or not to send the email. \n\n"
				+ "Thank you for participating in the beta."
				);
	    textArray.add(
	    		"Coming Improvements:\n"
	    		+ "\tAuto start/stop without bluetooth stereo\n"
	    		+ "\tSupport for multi-entrance garages\n"
	    		+ "\tSupport for garages with internal intersections\n"
	    		+ "\tDatabase of pre-mapped garages\n"
	    		+ "\tImproved battery usage\n"
	    		);
	    textArray.add(
	    		"Initial setup:\nDo you have a bluetooth device in your car?"
	    		); //TODO set yes or no. Add options to select bluetooth name if user says yes.
	    textArray.add(
	    		"Are you connected now?"
	    		); //TODO
	    textArray.add(
	    		"Select any of the following preset garage profiles:"
	    		); //TODO also have a none of the above option
	    textArray.add(
	    		"Would you like to map a new garage?"
	    		); //TODO 
	    textArray.add(
	    		"All done! We'll take it from here."
	    		);
	    textIterator = textArray.iterator();

	    tv = (TextView) findViewById(R.id.onboardText);
	    tv.setText(textIterator.next());
	    
	}
	
	public void onOK(View view) {
		if(textIterator.hasNext())
			tv.setText(textIterator.next());
		else {
			UserSettings.isFirstRun = false;
			toGraphActivity();
		}
		
	}
	
	public void toGraphActivity() {
		this.finish();
		Intent intent = new Intent(OnboardActivity.this, GraphActivity.class);
		startActivity(intent);
	}
	
	
}
