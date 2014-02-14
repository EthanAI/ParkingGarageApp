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
            	Check GPS for start daemon
            		Check GPS for near home
        xExport data
        	xStore locally in a way accessible by pc via usb
        	Button export via internet/wifi
        Pattern analyze
            xIs acc or jerk the best descriptor? Record both for now
            XTry with compass data instead?
            	xDo math with 3D compass & angle change (this worked great. Compensates for any position you phone is in)
        	Advanced pattern recognition
        		Compensate for someone moving the phone? (low priority, high challenge)
        		Detect turn movements, not just angles
        			Time break between angle changes
        			Pick up on which direction you turned into the stall
        				Angled stalls
    				Identify direction of turn, size of turn and sequence of turn (High priority, this makes it useful for more than just my garage)
    					Learn / initial map a users garage.
    					Maybe just report 'hey, what floor are we on?' 'Was this guess correct?' and learn
					Identify start of garage behavior. (GPS, speed etc. Signals to detect this)
	        xHardcode the pattern
	        	xStart simple
	        	x1. Get told when entering garage, just calculate total rotations 
	        	x2. azimuth from ortientation calculation seems pretty good. Needs compensation for crossing 180 to -180 and back
        /Record history of parking location/times
        	Organize somehow
        Notification Icon
        Widget to show most recent floor
        Confirm battery usage is minimal
        	xCurrently less than AndroidOS usage
        	Confirm low as I can go
        
        
