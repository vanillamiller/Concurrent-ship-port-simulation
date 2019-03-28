/**
 * This class represents the USS Emaphor's berth and applies monitoring conditions to elements that do not exist
 * in the other wait zones such:
 *      - shield deployment
 *      - only one ship being able to dock at once (reservation handling)
 *
 * @author anthonym1@student.unimelb.edu.au (Anthony Miller 636550)
 */
public class Berth extends WaitZone{

    private volatile boolean shieldDeployed;
    private volatile boolean dockingInProgress = false;
    private volatile int reserved = 0;
    public final int MAX_SHIPS = 1;

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
     * There is no monitor on this method as if the shield were indeed down when entering this method then the docking
     * time would take place in the critical section and nothing else would be able to happen in the system during that
     * time. This is controlled by confirmDockingWithOperator() which will tell the operator that a ship is docking and
     * @param pilot
     */
    public synchronized void completeDocking(Pilot pilot) {

        this.arrive(pilot.getShip());
        String msg = String.format("%s docks at berth", pilot.getShip());
        System.out.println(msg);
        pilot.setStatus("docked");
        this.dockingInProgress = false;
    }

    /**
     * Removes the ship from the berth and removes the reservation completing the undocking procedure after the undocking
     * time has elapsed. It will notify pilots waiting to dock that they can recommence docking procedure after reserving
     * the dock. Keeping the output statements in the synchronized block will keep output in proper sequence
     *
     * @param pilot wishing to complete undocking after undocking time has elapsed.
     */
    public synchronized void completeUndocking(Pilot pilot){
        this.depart();
        this.reserved -= 1;
        String msg = String.format("%s undocks from berth", pilot.getShip());
        pilot.setStatus("undocked");
        this.dockingInProgress = false;
        System.out.println(msg);
        notify();

    }

    /**
     * Enables operator to deactivate shield, will notify pilots that shield has become deactivated
     */
    public synchronized void retractShield() {
        this.shieldDeployed = false;
        System.out.println("Shield is deactivated");
        notify();
    }

    /**
     * This will enable ships to reserve a spot so that no two ships will commence docking at the same time and cause a
     * collision in the berth
     */
    public synchronized void reserve(){
        while(this.reserved >= this.MAX_SHIPS){
            try{
                wait();
            } catch(InterruptedException e){}
        }
        this.reserved += 1;
    }

    /**
     * As the shield will not be deployed once docking has commenced, this will set dockingInProgress to true and allow
     * the ship to dock without having to abort due to the shield being activated
     */
    public synchronized void confirmDockingWithOperator(){
        while(shieldDeployed){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        this.dockingInProgress = true;
    }

    /**
     * Will alert the operator to whether or not a ship is currently docking and if so will not allow him to activate
     * the shield
     *
     * @return whether or not a pilot in currently docking
     */
    public boolean isDockingInProgress(){
        return dockingInProgress;
    }









}
