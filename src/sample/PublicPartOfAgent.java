package sample;

import com.sun.org.apache.bcel.internal.generic.SWITCH;
import com.sun.xml.internal.bind.v2.model.core.ID;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class PublicPartOfAgent {
    static final int DIST_ERR = 10;//cm?
    public double x = 92;
    public double y = 120;
    public int battLevel = 97;//
    float targetX;
    float targetY;
    float targetDirection;
    public double odometryTotal = 0;
    Particle ownRelativePosition;
    public static final int radius = 10;
    public Canvas canvas;
    public double direction;
    double moveDistanceRemaining = 0; // for seperate moves e.g. moveForward
    double angleRemaining = 0; // for seperate moves e.g. turnRight

    public static double scale = 1;
    Space2D space2D;
    public Simulation simulation;
    public MovementState movementState = MovementState.STILL;
    public volatile MovementMode movementMode = MovementMode.MANUAL;//default

    public String agentName;
    public int agentNumber;
    static int agentNumberCounter = -1;
    static double nominalSpeed = 1; //px per iteration
    static double nominalAngularSpeed = 1 * Math.PI / 180; //rad per iteration

    public double angleToOtherRobot = 0;
    public Double angleToSecond = null;
    double otherRobotLineDistance = 100;//for drawing
    ParticleSet ps = new ParticleSet();
    boolean isStatic = false;
    double odoBeforeTravel = 0;
    RobotState state = RobotState.IDLE;
    RobotState nextStateAfterPause = RobotState.IDLE;
    static final int targetAgentCount = 4;
    int previousMeasureTargetNr = -1;
    double dirBeforeTurning = 0;
    int measurementCountInCycle = 0;
ArrayList<Integer> dists = new ArrayList<>(1000);
    ArrayList<Integer> areas = new ArrayList<>(1000);

    PublicPartOfAgent(Space2D space2D, Simulation simulation) {
        this.space2D = space2D;
        this.simulation = simulation;
        canvas = Main.canvas;
        agentNumber = generateAgentNumber();
        agentName = "UWBAgent " + agentNumber;
        ownRelativePosition = new Particle(0, 0, 0);
        isStatic = true;
        if (agentNumber == 1) {
            x = 200;
            y = 100;
        } else if (agentNumber == 2) {
            x = 200;
            y = 220;
        }
        if (agentNumber > 2) {

            x = 250;
            y = 250;
            isStatic = false;
        }
        ps.initializeParticles(this, (int) x, (int) y);

    }

    //kārtas skaits ievietošanai jaunā aģenta vārdā
    int generateAgentNumber() {
        return ++agentNumberCounter;
    }

    int getNextTargetNr() {
        previousMeasureTargetNr++;
        if (previousMeasureTargetNr == agentNumber)
            previousMeasureTargetNr++; //dont measure own distance
        if (previousMeasureTargetNr >= targetAgentCount)
            previousMeasureTargetNr = 0;

        measurementCountInCycle++;

        return previousMeasureTargetNr;
    }

    void turn(double rad) {
        direction += rad;
    }

    boolean moveForward(double dist) {
        double xNew = x + dist * Math.cos(direction);
        double yNew = y + dist * Math.sin(direction);

        if ((xNew - radius <= 0 || xNew + radius >= space2D.width || yNew - radius <= 0 || yNew + radius >= space2D.height) || hasHitObstacle(xNew, yNew)) {//hitted boundry of space, bounce back
            //direction = direction + Math.PI;
            return false;
        }
        x = xNew;
        y = yNew;
        odometryTotal += dist;
        return true;
    }

    //    void moveForwardOneTick(double speed){
//        x+=dist*Math.cos(direction);
//        y+=dist*Math.sin(direction);
//
//    }
    int getAgentNumberByName(String agentName) {
        String intValue = agentName.replaceAll("[^0-9]", "");
        return Integer.parseInt(intValue);
    }

    /**
     * changes movement mode to to_target so that robot keeps moving at its speed every iteration until
     *
     * @param distance is reached
     */
    public void moveForwardBy(double distance) {
        movementMode = MovementMode.TO_TARGET;
        state = RobotState.MOVEMENT;
        moveDistanceRemaining = distance;
        odoBeforeTravel = odometryTotal;
    }

    public void turnRightBy(double angleRadians) {
        movementMode = MovementMode.TO_ANGLE;
        angleRemaining = Math.abs(angleRadians);
        dirBeforeTurning = direction;
    }

    void manualMovementStep() {

        if (movementState.equals(MovementState.FORWARD)) {
            moveForward(nominalSpeed);
        } else if (movementState.equals(MovementState.TURNING_LEFT)) {
            turn(-nominalAngularSpeed);
        } else if (movementState.equals(MovementState.TURNING_RIGHT)) {
            turn(nominalAngularSpeed);
        }

    }

    void movementStep() {

        switch (state) {
            case IDLE:
                if (!simulation.isPaused) {
                    state = nextStateAfterPause;

                }

                break;
            case MOVEMENT:
                if (movementMode.equals(MovementMode.TO_TARGET)) {
                    boolean noObst = moveForward(nominalSpeed);
                    moveDistanceRemaining -= nominalSpeed;
                    if (moveDistanceRemaining <= 0 || !noObst) {
                        movementMode = MovementMode.MANUAL;// target distance reached -> switch off to target mode
                        double distTraveled = odometryTotal - odoBeforeTravel;

                        ps.moveParticles((int) distTraveled);
                        setNextStateAfterPause(RobotState.SUSPENDING_AFTER_MEASUREMENT);
                        //state = RobotState.SUSPENDING_AFTER_MEASUREMENT;

                    }
                } else if (movementMode.equals(MovementMode.TO_ANGLE)) {
                    turn(+nominalAngularSpeed);
                    angleRemaining -= nominalAngularSpeed;
                    if (angleRemaining < nominalAngularSpeed * 2) {
                        movementMode = MovementMode.MANUAL;// target distance reached -> switch off to target mode
                        double angle = direction - dirBeforeTurning;
                        System.out.println(agentNumber + " turned angle = " + Math.toDegrees(angle));
                        ps.turnParticles(angle);
                        setNextStateAfterPause(RobotState.PREPARE_MOVE);

                    }
                } else if (movementMode.equals(MovementMode.MANUAL)) {//
                    manualMovementStep();

                }
                break;
            case MOVING_PARTICLES:
                break;
            case PREPARE_MOVE:
                moveForwardBy(200);
                break;

            case SUSPENDING_AFTER_MEASUREMENT:
                //int[] meas = measureDistance(simulation.publicPartsOfAgents.get(getNextTargetNr()));
                int[] meas = measureDistanceMinMax(simulation.publicPartsOfAgents.get(getNextTargetNr()));

                System.out.println("meas = " + meas[0] + " " + meas[1] + " " + meas[2] + " " + meas[3] + " " + meas[4]);

                //  ps.suspendAfterMeasurement(meas);
                ps.suspendAfterMeasurementMinMax(meas);
                setNextStateAfterPause(RobotState.REPOPULATE_PARTICLES);
                break;

            case REPOPULATE_PARTICLES:
                // ps.regenerateParticles();
                ps.regenerateParticles2();
                if (measurementCountInCycle % (targetAgentCount - 1) == 0) {//measurement cycle is over
                    setNextStateAfterPause(RobotState.TURNING);
                } else
                    setNextStateAfterPause(RobotState.SUSPENDING_AFTER_MEASUREMENT);

                break;

            case TURNING:
                dists.add(calcDistanceBetweenActualAndParticles());
                areas.add(calcParticlesAreaSidesSum());

                if(agentNumber==3){
                    if(dists.size()%50==0)
                    printStatistics();
                }

                turnRightBy(Math.toRadians(ps.rnd.nextFloat() * 120 + 60));
                state = RobotState.MOVEMENT;
                break;

        }

    }

    int calcDistanceBetweenActualAndParticles() {
        Point c = ps.getAverageParticle();
        double dx = c.x - x;
        double dy = c.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return (int) dist;
    }

    int calcParticlesAreaSidesSum() {
        int[] r = ps.getParticlesDxDy();

        return Math.abs(r[0] - r[1]) + Math.abs(r[2] - r[3]);
    }

    void setNextStateAfterPause(RobotState stateAfterPause) {
        state = RobotState.IDLE;
        nextStateAfterPause = stateAfterPause;
        //  simulation.isPaused = true;
        System.out.println("Paused, stateAfterPause = " + stateAfterPause.name());
    }

//    public double measureDistance(String otherAgentName){// throws Exception {
//        for (PublicPartOfAgent ppa : simulation.publicPartsOfAgents) {
//            if (otherAgentName.contains(ppa.agentName)) {
//                return measureDistance(ppa);
//            }
//        }
//        //throw new Exception("No agent with given name found");
//    return 0;
//    }

    public int[] measureDistance(PublicPartOfAgent otherAgent) {
        System.out.println("ag " + agentNumber + " measuring dist to otherAgent = " + otherAgent.agentNumber);

        double dx = x - otherAgent.x;
        double dy = y - otherAgent.y;

        double dis = Math.sqrt(dx * dx + dy * dy);//optionally add measurement error
        dis = dis + ps.rnd.nextGaussian() * DIST_ERR;

        if (otherAgent.movementMode == MovementMode.TO_TARGET) {
            //update particle distribution
            double dist = otherAgent.odometryTotal - otherAgent.odoBeforeTravel;
            otherAgent.ps.moveParticles((int) dist);
            otherAgent.odoBeforeTravel = otherAgent.odometryTotal;//restart dist counter
        }

        int[] dxdy = otherAgent.ps.getParticlesDxDy();
        dxdy[4] = (int) dis;
        return dxdy;

    }

    public int[] measureDistanceMinMax(PublicPartOfAgent otherAgent) {
        System.out.println("ag " + agentNumber + " measuring distMinMax to otherAgent = " + otherAgent.agentNumber);

        double dx = x - otherAgent.x;
        double dy = y - otherAgent.y;

        double dis = Math.sqrt(dx * dx + dy * dy);//optionally add measurement error
        dis = dis + ps.rnd.nextGaussian() * DIST_ERR;

        if (otherAgent.movementMode == MovementMode.TO_TARGET) {
            //update particle distribution
            double dist = otherAgent.odometryTotal - otherAgent.odoBeforeTravel;
            otherAgent.ps.moveParticles((int) dist);
            otherAgent.odoBeforeTravel = otherAgent.odometryTotal;//restart dist counter
        }
        Point c = ps.getAverageParticle();
        int[] res = getFarestAndNearestPoints(otherAgent.ps, c);
        res[4] = (int) dis;
        return res;
    }

    int[] getFarestAndNearestPoints(ParticleSet ps, Point p) {
        double farestDistSaoFar = 0;
        int farestIndex = 0;
        double nearestDistSaoFar = Double.MAX_VALUE;
        int nearestIndex = 0;

        for (int i = 0; i < ps.particles.size(); i++) {
            int dx = ps.particles.get(i).x - p.x;
            int dy = ps.particles.get(i).y - p.y;
            double dis = Math.sqrt(dx * dx + dy * dy);
            if (dis > farestDistSaoFar) {
                farestDistSaoFar = dis;
                farestIndex = i;
            }
            if (dis < nearestDistSaoFar) {
                nearestDistSaoFar = dis;
                nearestIndex = i;
            }
        }
        int[] res = new int[]{ps.particles.get(nearestIndex).x, ps.particles.get(nearestIndex).y, ps.particles.get(farestIndex).x, ps.particles.get(farestIndex).y, 0};
        return res;

    }

    /**
     * @param mes1    first distance measurement between robots
     * @param mes2    second distance measurement between robots after this robot moved in stright direction
     * @param odoDist estimated distance of this robot movement between interrobot distance measurements
     * @return aprox angle in radians of the other robot
     */
    public double calculateRelativeAngle(double mes1, double mes2, double odoDist) {
        if ((mes2 + odoDist) < mes1) return 0;
        if ((mes1 + odoDist) < mes2) return Math.PI;

        double angle = Math.acos((odoDist * odoDist + mes2 * mes2 - mes1 * mes1) / (2 * odoDist * mes2));
        return Math.PI - angle;
    }

    public static double calcThirdSide(double a, double b, double angleRad) {
        return Math.sqrt(a * a + b * b - 2 * a * b * Math.cos(angleRad));
    }

    //returns angle oposite to c
    public static double calcAngle(double a, double b, double c) {//cos theorem
        return Math.acos((a * a + b * b - c * c) / (2 * a * b));
    }

    void draw() {

        ps.draw(canvas);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.beginPath();
        switch (agentNumber) {
            case 3:
                g.setStroke(Color.DARKBLUE);
                break;
            default:
                g.setStroke(Color.BLACK);

        }

        g.strokeOval(Space2D.cellsOffset + (x - radius) * scale, Space2D.cellsOffset + (y - radius) * scale, radius * 2 * scale, radius * 2 * scale);
        g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + radius * Math.cos(direction)), Space2D.cellsOffset + scale * (y + radius * Math.sin(direction)));
        g.strokeText(String.valueOf(odometryTotal), Space2D.cellsOffset + x * scale, Space2D.cellsOffset + (y - radius) * scale);

        if (angleToOtherRobot != 0) {//needs beter test- 0 can be valid angle
            g.setStroke(Color.GREEN);
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(angleToOtherRobot + direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(angleToOtherRobot + direction)));
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(-angleToOtherRobot + direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(-angleToOtherRobot + direction)));

        }
        if (angleToSecond != null) {
            g.strokeLine(Space2D.cellsOffset + x * scale, Space2D.cellsOffset + y * scale, Space2D.cellsOffset + scale * (x + otherRobotLineDistance * Math.cos(angleToSecond + direction)), Space2D.cellsOffset + scale * (y + otherRobotLineDistance * Math.sin(angleToSecond + direction)));

        }
        int rowHeight = 20;

        g.strokeText("r " + agentNumber, Space2D.cellsOffset + x * scale + rowHeight, Space2D.cellsOffset + (y - radius + rowHeight) * scale);

    }

    boolean hasHitObstacle(double x, double y) {
        for (Rectangle r : space2D.obstacles) {
            if (x + radius > r.getX() && x - radius < (r.getX() + r.getWidth()) && y + radius > r.getY() && y - radius < (r.getY() + r.getHeight())) {//hitted boundry of space, bounce back
                return true;
            }
        }
        return false;
    }

void printStatistics(){
    System.out.println("dists areas");
        for (int i = 0; i < dists.size(); i++) {
        System.out.println( dists.get(i)+" "+areas.get(i));
    }

}
}
