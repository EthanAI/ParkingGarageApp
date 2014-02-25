accelerometerApp
================

App to automatically determine the floor you parked out. GPS is not helpful, so we use the accelerometers to judge how many times you've turned.

    Goals:
        xRead sensors
        xRecord sensors
        xGraphical View
          Needs prettier graphing. achart needs studying
        
        Run only when in parking garage
            Subgoals to achieve this:
                xRun in background
                XGPS/BT to trigger it on and off
                	Xreciever class to trigger this?
                	+check what type of BT we connected to (vs pebble watch)
                		store in options
            	Check GPS for start daemon
            		Check GPS for near home
            		Check gps travel speed?
            		Turn on when GPS accuracy goes down?
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
        /Record history of parking location/times
        	Organize somehow
        /Notification Icon
        	Make better art
        	stronger test that broadcastReceiver is actually running
        Widget to show most recent floor
        Confirm battery usage is minimal
        	xCurrently less than AndroidOS usage
        	Confirm low as I can go
        	Remove location recording for each data point. Its helpful for design but wastes a lot of battery
        	
    	//patern notes
    		xWork backwards from end
    		+Cut off last 90 degrees regardless of direction (turn into stall)
    		Check accelerometer for possible speed indications? Enter the gate?
    		-Ignore weird phone movements since working backwards. 
    		Subtract out gravity from accelerometer, build cumulative values. See if we can ID stopping
    		+use compass bearing to identify entry to garage (can we manage varying phone positions?)
    		+assume confusion means first floor
    		99 final signal strength means in garage?
    			Check for correlation
    		Record gps altitude? 
    		Record gps bearing?
    		+Increase GPS update rate
    		Sync all sensors to single line? 
    		+orientation only 5x per second. Fix it
    		Acceleration seems not useful. y is acceleration? (who knows, phone can move)
        	
        	+Use GPS speed to activate disactivate if no BT in car 
  
	Bugs:
		GraphActivity freezes after long 15min+ time Sensors seem to be unaffected.
        xSignal strength seems to be recording after the sensors should be turned off.
    	+Strange jumps sometimes
    		Maybe problems with low refresh rate?
    		Add safety override to:
    			Rate of degree change
    			Rate of azimuth change - 20 degree max per tick (0.2 s)
        
        
