accelerometerApp
================

App to automatically determine the floor you parked out. GPS is not helpful, so we use the sensors to judge how many times you've turned.

Primary mechanics:
1. Detect when driving starts
2. Detect when nearing parking garage
3. Detect when driving stops
4. Use sensor data from 2 to 3 to analyze parked floor 

	Floor Finder Goals:
		I. Load up correct garage description (each entrance should have different floor borders signature)
			1. Identify entry vector
				Bearing/turn TRAIL (cannot guarantee where GPS will get cut off)
				Match against historical signatures
			2. Identify lat and long
		II. Identify location within that garage
			1. turn history (what if loops? Just guarantee no loops?)
		III. Create data to match against
			1. Allow user to register a location
			2. Record entries to that location
			3. Allow user to drive through garage mapping its floors
		

    General Goals:
        xRead sensors
        xRecord sensors
        xGraphical View
        
        Run only when in parking garage
            Subgoals to achieve this:
                xRun in background
                XGPS/BT to trigger it on and off
                	Xreceiver class to trigger this?
                	+check what type of BT we connected to (vs pebble watch)
                		store in options
            	xCheck GPS for start daemon
            		xCheck GPS for near home
            		Check gps travel speed?
            		xTurn on when GPS accuracy goes down?
        xExport data
        	xStore locally in a way accessible by pc via usb
        	-Button export via internet/wifi
        Pattern analyze
            xIs acc or jerk the best descriptor? Record both for now
            XTry with compass data instead?
            	xDo math with 3D compass & angle change (this worked great. Compensates for any position you phone is in)
        	Advanced pattern recognition
        		-Compensate for someone moving the phone? (low priority, high challenge)
        		Detect turn movements, not just angles
        			Time break between angle changes
        			Pick up on which direction you turned into the stall
        				Angled stalls
    				+Identify direction of turn, size of turn and sequence of turn (High priority, this makes it useful for more than just my garage)
    				+Maybe identify entry/exit of garage is most critical. How to do this?
    					-Learn / initial map a users garage. No. What if we find parking on the first floor?
    					While near target GPS location: *2 rings. 1 for proximity-start sensors 2 for match-check/analyze if this is garage.
    						Coming to a stop for gate? Only I have a gate?
    						Loosing gps connection? Just less accurate?
    						Loosing cell connection?
    					
    					Maybe just report 'hey, what floor are we on?' 'Was this guess correct?' and learn
					Identify start of garage behavior. (GPS, speed etc. Signals to detect this)
	        xHardcode the pattern
	        	xStart simple
	        	x1. Get told when entering garage, just calculate total rotations 
	        	x2. azimuth from ortientation calculation seems pretty good. Needs compensation for crossing 180 to -180 and back
        xRecord history of parking location/times
        	xOrganize somehow
        /Notification Icon
        	xMake better art
        	stronger test that broadcastReceiver is actually running
        xWidget to show most recent floor
        	improve its appearance
        Confirm battery usage is minimal
        	xCurrently less than AndroidOS usage
        	Confirm low as I can go
        	Remove location recording for each data point. Its helpful for design but wastes a lot of battery
        	
    	//patern notes
    		xWork backwards from end
    		xCut off last 90 degrees regardless of direction (turn into stall)
    		-Check accelerometer for possible speed indications? Enter the gate?
    		-Subtract out gravity from accelerometer, build cumulative values. See if we can ID stopping
    			-this looks unproductive and is already done by the getOrientation() api
    		+use compass bearing to identify entry to garage (can we manage varying phone positions?)
    			GPS bearing is immune to phone orientation changes
    		+assume confusion means first floor
    		99 final signal strength means in garage?
    			Check for correlation
    		xRecord gps altitude? 
    		xRecord gps bearing?
    		+Increase GPS update rate
    		+orientation only 5x per second. Fix it
    		Acceleration seems not useful. y is acceleration? (who knows, phone can move)
        	
        	+Use GPS speed to activate disactivate if no BT in car 
        		Reduce distance from garage where it activates
        		Practice plotting things on maps
        			+Activate if within 500m of target. Disactivate if outside 1000m of target (only passed by)
        	xMake dataAnalyzer count all turns for my use
        	
        	Does seem in garage correlates with gps (Accuracy degrades, then all stops updating)
        	Slowing down correlates with turns, and parking. Speed could be very useful
        	
        	++Better on/off control. Have GPS ping more frequently when near than far. 
        		Only run sensors when close to a garage. 
    		+Threads to run file read operations even if gps data reduction helps
    			Thread for datarecord building, just run the sensor, fire it off to the thread to store it
    			Thread for data analysis. Could be time intensive
    		
    		some garages may have loops on the same floor (UHM)
    		Name locations based on last available GPS location. Network is just too inaccurate	
    			
			oOption for only frequent garages or all garages on database to save battery
			
			Add ads
			
	Next Steps:
		+get age of location, see if old gps matches up with entry point
			jump in accuracy and lack of distance changes seems to work.
		UI interaction for getting difficulty level of garage and save in UserSettings
			Button - remember garage
			'how many car entrances does this garage have?' 1-2-3-4+
		Recognize different entry vectors
			Recognize entry time
				GPS not Changing / Accuracy (error) rises and stays fixed
					This seems most reliable measure (at home, how about for school?)
					Sometimes flucutation at the end (park near window) need to cut off
				Age?
					Network seems to be working,
						..sometimes
					Not so great with gps
				Slow speed, then speed not changing
					Humans:
						Walk - 1.5 m/s
						Run - 7 m/s
				
			Different directions of approach
			Different locations of parking start
				Gps loss
				Recognizable back pattern? Can I do this for UH looping garage?
			Expand Garage Location to have multiple entry directions map to the same data
			Expand Garage location to contain multiple border data structures for various entry points
				Merge entry direction into the pattern? Different entry points share any common patterns?
		UI for mapping floor borders (umm, get middles and build borders as splitting to the difference)
		
		
		+test at home, all directions, package release
		+better matching structure, instead of series of comparisons, directly find closest match
		xBetter final turn removal
		+switch to floor centers not floor borders and match closest
		xmove garageLocation records to stored/loaded data
		xadd dialog/activity for making new floor records / add garage
			xSaves to storage (serialing is slow. Try SQLite or put in separate task)
			xAdd floor data, need to allow add garage.
		+make left turn count
		+switch from raw count to turn history from entry point (need id entry point time)
		
Steps to Beta release: 
	-First run set up
		-Convert to dialogs instead of activity
		+complete the meat behind it to save user settings
		-ask questions to set up for auto/manual operation
			-auto fork:
				-Set bluetooth name to empty
				-Everytime bluetooth connects, ask 'is this your car stereo?' if bluetoothName still empty
		-ask questions to set up for preset garages
			-choose from list of preset garages (include database later)
	-Design UI for post-setup app
		-Buttons
			-[Where's My Car?] see where parked
			-[Parking Garages] see/change which garages active
				-create new profile from here
			-[Send Report] - Get user explanation of what went wrong
				-include recent sensor data
				-include recent app log
				-create email
	-Add Adsense
	-Test left and right turns at home
	-Test manual on and off
	-test automatic on and off
	-test new garage mapping
	-add logging/bug reporting system (copy to email, complete control over transmitting data)
		
		
add location if exit car without sensors activated?
Share parking locations?
Master the most crowded garage
			
			 
			
  
	Bugs:
		xGraphActivity freezes after long 15min+ time Sensors seem to be unaffected.
			XWas a problem to appending final info to the front of the file
			xBug returned 2/28. Long time analyzing the completed csv? Threading should be applied
				Down to 12 minutes to lag out
				Not much of a problem now that we record less data. Fix for future stability
			xadded multithreading for the final analysis, should permenantly fix the bug. 
			
		XGPS info not updating on the output. Pretty sure we're listening, not getting saved?
			XWe're just feeding itself its own value -_-. 
			XExpand headers to have network and gps versions
			XStore each location type in each reading
			XOutput both location types in toFormattedString
			XAdjust dataAnalyzer to use the new column
				Eventually pick a single location as winner and discard the other one
        xSignal strength seems to be recording after the sensors should be turned off.
    	/Strange jumps sometimes
    		Maybe problems with low refresh rate?
    		Add safety override to:
    			Rate of degree change
    			-Rate of azimuth change - 20 degree max per tick (0.2 s) -this causes problems, do other way
			Jumps seem to happen around the river bridge of 99/H1 around 21.33497179	-157.8951345
				(mag field seems crazy!) probably a bit west of river since not much GPS under the river
				Limit the mag values? Recorded amounts are hundreds of times normal values
				Accelerometer seems fine
			Depreciated. Does not seem to occur near any of my garages
		Fix startup bug
			First connect of the day from home crashes
		Not saving settings to file
			UserSettings class has no fields when debugged. So odd.
			
			
        
        
