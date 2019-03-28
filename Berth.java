/**
 * This class represents the USS Emaphor's berth and applies monitoring conditions to elements that do not exist
 * in the other wait zones such:
 *      - shield deployment
 *      - only one ship being able to dock at once (reservation handling)
 *
 * @author anthonym1@student.unimelb.edu.au
 */
public class Berth extends WaitZone{

    private volatile boolean shieldDeployed;
    private volatile boolean dockingInProgress = false;
    private volatile boolean reserved = false;

    public Berth(String name) {
        super(name);
    }

    /**
     * Allows an operator to deploy a shield
     */
    public synchronized void deployShield(){
        this.shieldDeployed = true;
        System.out.println("Shield is activated");
    }

    /**
     * Will dock ship as long as shield is not deployed, which entails adding it to the berth waitingShips that it
     * inherits from the WaitZone class. Its synchronized nature will mean it will print the output message as soon as
     * the event occurs so that the output sequencing will match the proper state transitions.
     *
     * The shield checking is overkill because the pilot won't engage docking proceedure unless the shield is down and this
     * method is synchronized so the operator will not be able to run. If somehow the shield is deployed during docking
     * and the pilot has to wait, it will instantly dock as soon as the shield is lifted instead of taking regular
     * docking time.
     *
     * @param pilot
     */
    public synchronized void dockingProcedure(Pilot pilot) {

        this.arrive(pilot.getShip());
        String msg = String.format("%s docks at berth", pilot.getShip());
        System.out.println(msg);
        pilot.setStatus("docked");
        this.dockingInProgress = false;
    }

    /**
     * Removes the ship from the berth and the sheildDeployed is also a bit overkill as the
     *
     * @param pilot
     */
    public synchronized void undockingProcedure(Pilot pilot){

        this.depart();
        this.reserved = false;
        String msg = String.format("%s undocks from berth", pilot.getShip());
        pilot.setStatus("undocked");
        this.dockingInProgress = false;
        System.out.println(msg);
        notify();

    }

    public synchronized void retractShield() {
        this.shieldDeployed = false;
        System.out.println("Shield is deactivated");
        notify();
    }

    public synchronized void reserve(){
        while(reserved){
            try{
                wait();
            } catch(InterruptedException e){}
        }
        this.reserved = true;
    }

    public synchronized void isShieldDeployed(){
        while(shieldDeployed){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        this.dockingInProgress = true;
    }

    public boolean isDockingInProgress(){
        return dockingInProgress;
    }









}
