package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ParticleSet {
    static final int PARTICLE_COUNT = 100;
    static final int PARTICLE_SIZE = 3;
    static final double TRAVEL_ERR = 0.05;
    static final double TURN_ERR = Math.toRadians(5);

    static final int dirLine = 10;

    ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    Random rnd = new Random();
    int distRadius = 30;
    PublicPartOfAgent owner;
    Point previousPoint = new Point(0, 0);

    void initializeParticles(PublicPartOfAgent owner, int x, int y) {
        this.owner = owner;
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double r = (distRadius * rnd.nextGaussian());
            int alfa = rnd.nextInt(360);
            double alfaRad = Math.toRadians(alfa);
            int dx = x + (int) (r * Math.cos(alfaRad));
            int dy = y + (int) (r * Math.sin(alfaRad));
            particles.add(new Particle(dx, dy, rnd.nextInt(360)));

        }

    }

    void initSingleParticle(int x, int y, int radi, double dir, Particle p) {
        double r = (radi * rnd.nextGaussian());
        int alfa = rnd.nextInt(360);
        double alfaRad = Math.toRadians(alfa);
        int dx = x + (int) (r * Math.cos(alfaRad));
        int dy = y + (int) (r * Math.sin(alfaRad));
        p.x = dx;
        p.y = dy;
        p.direction = dir;
        p.isValid = true;
    }

    void draw(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        switch (owner.agentNumber) {
            case 3:
                gc.setStroke(Color.LIGHTBLUE);
                break;
            default:
                gc.setStroke(Color.DARKGRAY);
        }
        int ofs = Space2D.cellsOffset;
        double sc = PublicPartOfAgent.scale;
        for (Particle p :
                particles) {
            if (!p.isValid) continue;
            gc.strokeOval((p.x - PARTICLE_SIZE) * sc + ofs, (p.y - PARTICLE_SIZE) * sc + ofs, 2 * PARTICLE_SIZE * sc, 2 * PARTICLE_SIZE * sc);
            gc.strokeLine(ofs + p.x * sc, ofs + p.y * sc, ofs + sc * (p.x + dirLine * Math.cos(p.direction)), ofs + sc * (p.y + dirLine * Math.sin(p.direction)));
        }
    }

    void moveParticles(int dist) {
        for (Particle p : particles) {
            double dr = rnd.nextGaussian();
            double distD = dr * dist * TRAVEL_ERR + dist;
            int dy = (int) (Math.sin(p.direction) * distD);
            int dx = (int) (Math.cos(p.direction) * distD);
            p.x += dx;
            p.y += dy;
        }
    }

    void turnParticles(double radians){
        for (Particle p : particles) {
        p.direction += radians+rnd.nextGaussian()*TURN_ERR;

        }
        }

    int[] getParticlesDxDy() {
        int minxSoFar = Integer.MAX_VALUE;
        int minySoFar = Integer.MAX_VALUE;
        int maxxSoFar = Integer.MIN_VALUE;
        int maxySoFar = Integer.MIN_VALUE;
        for (Particle p : particles) {
            if (p.x > maxxSoFar) maxxSoFar = p.x;
            if (p.y > maxySoFar) maxySoFar = p.y;
            if (p.x < minxSoFar) minxSoFar = p.x;
            if (p.y < minySoFar) minySoFar = p.y;

        }
        return new int[]{minxSoFar, maxxSoFar, minySoFar, maxySoFar, 0};

    }

    Point getAverageParticle() {//wo direction

        double xSum = 0;
        double ySum = 0;
        int validParticles = 0;
        for (Particle p : particles) {
            if (!p.isValid) continue;
            validParticles++;
            xSum += p.x;
            ySum += p.y;
        }
        if (validParticles <= 0)
            return null;

        Point r = new Point((int) xSum / validParticles, (int) ySum / validParticles);
        return r;
    }

    void regenerateParticles() {
        Point c = getAverageParticle();
        if (c == null) {
            c = previousPoint;
        }
        previousPoint = c;


        for (Particle p : particles) {
            if (p.isValid) continue;

            initSingleParticle(c.x, c.y, distRadius, 0, p);

        }
    }

    void regenerateParticles2() {
        ArrayList<Integer> valid = new ArrayList<>(PARTICLE_COUNT);
        //mark valid particles for regeneration;
        for (int i = 0; i < particles.size(); i++) {
            if (particles.get(i).isValid) valid.add(i);
        }
//
        int i = 0;
        for (Particle p : particles) {
            if (p.isValid) continue;

            initSingleParticle(particles.get(valid.get(i)).x, particles.get(valid.get(i)).y, distRadius, particles.get(valid.get(i)).direction, p);
            i++;
            if (!(i < valid.size())) i = 0;
        }

    }

    void suspendAfterMeasurement(int[] m) {
        int cx = m[0] + (m[1] - m[0]) / 2;
        int cy = m[2] + (m[3] - m[2]) / 2;

        int maxDist = m[4] + PublicPartOfAgent.DIST_ERR;
        int minDist = m[4] - PublicPartOfAgent.DIST_ERR;//negative dist warning
        int susp = 0;
        for (Particle p : particles) {
            int dist = (int) (Math.sqrt((p.x - cx) * (p.x - cx) + (p.y - cy) * (p.y - cy)));
            if (dist > maxDist || dist < minDist) {
                p.isValid = false;
                susp++;
            }
        }
        System.out.println("susp = " + susp);

    }
}
