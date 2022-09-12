# SWEN-755
    Heartbeat tactic implemetation for autonomous cars
    
    Features:
    - Sensors will sense the obstacles(Pothole, water) on the road and update the detetors accordingly
    
    - Detector (Heart beat sender) will continously send heart beat to the central controller. In case of any 
      failure central controller(Heart beat reciever) will activate the Backup Detector

    - Fault Monitor will monitor the heart beat and will reboot the detector in case of any failure sensed by the
      central controller.
    
![img.png](img.png)