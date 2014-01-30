accelerometerApp
================

App to automatically determine the floor you parked out. GPS is not helpful, so we use the accelerometers to judge how many times you've turned.

    Goals:
        xRead sensors
        xRecord sensors
        /Graphical View
          Needs prettier graphing. achart needs studying
        Run only when in parking garage
            Subgoals to achieve this:
                xRun in background
                GPS/BT to trigger it on and off
                	reciever class to trigger this?
        xExport data
        Pattern analyze
            Is acc or jerk the best descriptor? Record both for now
            XTry with compass data instead?
        Hardcode the pattern
        Record history of parking location/times
        Widget to show most recent floor
        
        
