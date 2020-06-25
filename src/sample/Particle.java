package sample;


public class Particle {
   public Particle(int x, int y, double dir  ){
       this.x=x;
       this.y=y;
       this.direction=dir;
   }
    public Particle(int x, int y ){
        this.x=x;
        this.y=y;
        this.direction=0;
    }

    int x;
    int y;
    public   double direction;
boolean isValid = true;

}
