package sample;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    public Controller controller;
    int simStepDefDuration = 20;//50
    Space2D board;
    int simTimeFactor = 1;
    Timeline timeline;
    int simTime = 0;
    boolean isRunning = false;
boolean isPaused = false;
    private final int SIM_TIME_LIMIT=2001;
public List<PublicPartOfAgent> publicPartsOfAgents = new ArrayList<>();


//public List<PublicPartOfAgent> agents=new ArrayList<>(10);

    Simulation(Space2D board){
        this.board=board;
        timeline = new Timeline(new KeyFrame(Duration.millis(simStepDefDuration), ae -> simulationStep()));
        timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    }

    synchronized void simulationStep() {
        simTime++;
        board.draw();
        for (PublicPartOfAgent a : publicPartsOfAgents) {
            a.movementStep();
        a.draw();
        }
        if(simTime==1000)publicPartsOfAgents.get(2).moveForwardBy(100);
        if(simTime==1100)publicPartsOfAgents.get(1).moveForwardBy(100);

    }



    /**
     * use to create new agents and get their public parts to acces their data
     * @return  public part of agent
     */
    PublicPartOfAgent createAgent(){
        PublicPartOfAgent ppa = new PublicPartOfAgent(board,this);

        if(createAgent(ppa))
            return ppa;
        else
            return null;
    }
   private boolean createAgent(PublicPartOfAgent publicPartOfAgent) {

publicPartsOfAgents.add(publicPartOfAgent);
   return true;
    }









    void restart(){
        simTime=0;
    }




}
