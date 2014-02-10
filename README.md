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
        	Button export via internet/wifi
        Pattern analyze
            Is acc or jerk the best descriptor? Record both for now
            XTry with compass data instead?
            	Do math with 3D compass & angle change
        Hardcode the pattern
        XRecord history of parking location/times
        Notification Icon
        Widget to show most recent floor
        
        
