/**
 *  This class opens and closes the shields according to the debris_lapse and whether the pilot has confirmed docking
 *  procedures
 *
 *  @author anthonym1@student.unimelb.edu.au
 */
public class Operator extends Thread {

    private Berth berth;

    public Operator(Berth berth){
        this.berth = berth;
    }

    /**
     * The operator activates the berth's shield while docking is not in progress, and deactivates it after the debris
     * time is up
     */
    public void deployShield(){
        if(! this.berth.isDockingInProgress()) {
            this.berth.deployShield();
        }
        try{
            sleep(Params.DEBRIS_TIME);
        }catch(InterruptedException e){}
        this.berth.retractShield();
    }

    /**
     * Control loop where the operator will activate the shield if the USS Emaphor is coming under attack from debris
     */
    public void run(){
        while(true){
            try{
                sleep(Params.debrisLapse());
            } catch(InterruptedException e){}
            this.deployShield();
        }

    }
}
