package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Mechanims.GrabberArm;
import org.firstinspires.ftc.teamcode.Mechanims.LinearSlides;

/*
 * This file contains an example of a Linear "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode is executed.
 *
 * This particular OpMode illustrates driving a 4-motor Omni-Directional (or Holonomic) robot.
 * This code will work with either a Mecanum-Drive or an X-Drive train.
 * Both of these drives are illustrated at https://gm0.org/en/latest/docs/robot-design/drivetrains/holonomic.html
 * Note that a Mecanum drive must display an X roller-pattern when viewed from above.
 *
 * Also note that it is critical to set the correct rotation direction for each motor.  See details below.
 *
 * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
 * Each motion axis is controlled by one Joystick axis.
 *
 * 1) Axial:    Driving forward and backward               Left-joystick Forward/Backward
 * 2) Lateral:  Strafing right and left                     Left-joystick Right and Left
 * 3) Yaw:      Rotating Clockwise and counter clockwise    Right-joystick Right and Left
 *
 * This code is written assuming that the right-side motors need to be reversed for the robot to drive forward.
 * When you first test your robot, if it moves backward when you push the left stick forward, then you must flip
 * the direction of all 4 motors (see code below).
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Config
@TeleOp(name = "Basic: Jomni", group = "Linear OpMode")
public class JOmni extends LinearOpMode {

    //Remove before Comp
    FtcDashboard dashboard = FtcDashboard.getInstance();
    TelemetryPacket pk = new TelemetryPacket();
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor rightBackDrive = null;
    private LinearSlides linearSlides = null;
    private GrabberArm grabberArm = null;

    private void setDrive(){
        // Initialize the hardware variables. Note that the strings used here must
        // correspond
        // to the names assigned during the robot configuration step on the DS or RC
        // devices.
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);
    }

    private double[] drive(double axial, double lateral, double yaw){
        double max;

        double leftFrontPower = axial + lateral + yaw;
        double rightFrontPower = axial - lateral - yaw;
        double leftBackPower = axial - lateral + yaw;
        double rightBackPower = axial + lateral - yaw;

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower /= max;
            rightFrontPower /= max;
            leftBackPower /= max;
            rightBackPower /= max;
        }

        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);
        double[] drivePower = {leftFrontPower, rightFrontPower, leftBackPower, rightBackPower};
        return drivePower;
    }

    private void telemetry(){
        // Show the elapsed game time and wheel power.
        telemetry.addData("Status", "Run Time: " + runtime.toString());
//            telemetry.addData("Front left/Right", "%4.2f, %4.2f", drivePower[0], drivePower[1]);
//            telemetry.addData("Back left/Right", "%4.2f, %4.2f", drivePower[2], drivePower[3]);
//            telemetry.addData("Trigger Values", "%d, %d", gamepad1.right_trigger, gamepad1.left_trigger);
        telemetry.addData("Linear Slide Position left/right: ", "%d, %d",
                linearSlides.getPosition()[0], linearSlides.getPosition()[1]);
        telemetry.addData("Arm Position: ", "%d", grabberArm.getArm());
        telemetry.addData("Wrist Position: ", "%4.2f", grabberArm.getWrist());
        telemetry.addData("Claw Position: ", "%4.2f", grabberArm.getClaw());


        pk.put("Status", "Run Time: " + runtime.toString());


        dashboard.sendTelemetryPacket(pk);
        telemetry.update();
    }

    /**
     * LS MAX: 1500
     *
     * At top: arm 511, wrist 0
     *
     * at drop: arm 834, wrist 0.5
     *
     * at pickup: arm 0, wrist 0
     *
     * drive, arm 90
     */
    public void pickupPosition(){
        grabberArm.pickupPosition();
        linearSlides.bottomPosition();
    }
    @Override
    public void runOpMode() {

        setDrive();
        linearSlides = new LinearSlides(hardwareMap);
        grabberArm = new GrabberArm(hardwareMap);

        telemetry.addData("Status", "Initialized");
        pk.put("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            // POV Mode uses left joystick to go forward & strafe, and right joystick to
            // rotate.
            double axial = -gamepad1.left_stick_y; // Note: pushing stick forward gives negative value
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;

            if(gamepad1.right_trigger > 0){
                grabberArm.openClaw();
            }

            if(gamepad1.left_trigger > 0){
                grabberArm.closeClaw();
            }

            if(gamepad1.a){
                linearSlides.bottomPosition();
            }
            if(gamepad1.b){
                linearSlides.topPosition();
            }
            if(gamepad1.x){
                grabberArm.topPosition();
            }
            if(gamepad1.y){
                grabberArm.dropPosition();
            }

            drive(axial, lateral, yaw);
            telemetry();
        }
    }
}

