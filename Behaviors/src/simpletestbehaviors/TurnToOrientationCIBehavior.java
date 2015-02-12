package simpletestbehaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.LedState;
import commoninterface.utils.CIArguments;

public class TurnToOrientationCIBehavior extends CIBehavior {

	private double targetOrientation = 0;
	private double tolerance         = 10;
	
	public TurnToOrientationCIBehavior(CIArguments args, AquaticDroneCI drone) {
		super(args, drone);
		
		targetOrientation = args.getArgumentAsDoubleOrSetDefault("target", targetOrientation);
		tolerance = args.getArgumentAsDoubleOrSetDefault("tolerance", tolerance);
	}

	@Override
	public void step(double timestep) {
		double currentOrientation = drone.getCompassOrientationInDegrees();		
		double difference = currentOrientation - targetOrientation;
		
		difference%=360;
		
		if(difference > 180){
			difference = -((180 -difference) + 180);
		}
			
		if (Math.abs(difference) <= tolerance) {
			drone.setLed(0, LedState.ON);
			drone.setMotorSpeeds(0, 0);
		}
		else {
			drone.setLed(0, LedState.BLINKING);
			if (difference > 0) {
				drone.setMotorSpeeds(-0.1, 0.1);
			} else {
				drone.setMotorSpeeds(0.1, -0.1);
			}
		}
	}
	
	@Override
	public void cleanUp() {
		drone.setLed(0, LedState.OFF);
		drone.setMotorSpeeds(0, 0);
	}
}
