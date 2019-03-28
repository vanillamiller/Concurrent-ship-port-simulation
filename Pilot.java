/**
 *  This class is the main actor and is responsible for transitioning the ship from arrival to departure.
 *  A pilot's responsibilites include
 *      - boarding and releasing ships
 *      - moving a ship between zones at a specific speed (represented by travel time)
 *      - docking and undocking the ship at a safe speed (represented by docking and undocking time)
 *      - avoiding colisions with other ships and the shield when docking and undocking (reserving and confriming
 *          docking with operator)
 *      - acquiring and releasing tugs
 *
 * @author anthonym1@student.unimelb.edu.au
 *
 */
public class Pilot extends Thread {

    // All instance variables that are described in the instantiation in Main.java
    private int id;
    private WaitZone arrivalZone;
    private WaitZone departureZone;
    private Tugs tugs;
    private Berth berth;

    // will initialize tugs currently in posession to 0
    private volatile int acquiredTugs = 0;


    // Will keep track of all possible statuses in the USS Emafor's system
    private enum Statuses {
        ARRIVAL, WAITING_FOR_SHIP, DEPARTURE, WAITING_TO_DOCK, WAITING_TO_DEPART, DOCKING,
        UNDOCKED, DOCKED
    }

    // Initialized to waiting for ship
    private volatile Statuses status = Statuses.WAITING_FOR_SHIP;

    // The ship the pilot is currently captaining
    private volatile Ship ship;

    /**
     *
     * @param id the pilot's unique identifier
     * @param arrivalZone the pilots designated arrival zone
     * @param departureZone the pilots designated departure zone
     * @param tugs the tug cache the pilot will be accessing
     * @param berth the USS Emafor's berth
     */
    public Pilot(int id, WaitZone arrivalZone, WaitZone departureZone, Tugs tugs, Berth berth) {
        this.id = id;
        this.arrivalZone = arrivalZone;
        this.departureZone = departureZone;
        this.tugs = tugs;
        this.berth = berth;
    }

    @Override
    public String toString() {
        return String.format("pilot %d", this.id);
    }

    /**
     * Commences docking procedure in the following steps
     *      - reserve berth spot
     *      - check if the shield is down with operator and confirm docking approach
     *      - execute docking (sleep for docking time)
     *      - complete docking procedure and change state
     */
    private void engageDockingProcedure() {

        // reserve berth spot
        berth.reserve();

        // check if the shield is down with operator and confirm docking approach
        this.berth.confirmDockingWithOperator();

        // execute docking
        try {
            sleep(Params.DOCKING_TIME);
        } catch (InterruptedException e) {
        }
        // complete docking
        this.setStatus("docking");
        this.berth.completeDocking(this);
    }

    /**
     * Commences docking procedure in the following steps
     *      - check if the shield is down with operator and confirm undocking
     *      - execute docking (sleep for undocking time)
     *      - complete undocking procedure and change state
     */
    private void engageUndockingProceedure() {

        this.berth.confirmDockingWithOperator();
        try {
            sleep(Params.UNDOCKING_TIME);
        } catch (InterruptedException e) {
        }
        berth.completeUndocking(this);

    }

    /**
     * Once the ship is docked, the drones that are no longer needed are released and cargo is unloaded
     */
    private void unloadCargo() {

        // derive how many tugs are to be jettisoned
        int numTugsToRelease = Math.abs(Params.DOCKING_TUGS - Params.UNDOCKING_TUGS);

        // jettison tugs
        tugs.returnTugs(this, numTugsToRelease);
        String msg = String.format("%s being unloaded", this.getShip());
        System.out.println(msg);

        // unload ship (remove load)
        try {
            sleep(Params.UNLOADING_TIME);
        } catch (InterruptedException e) {
        }
        this.ship.loaded = false;
    }

    /**
     * encapsulates the arrival time between the zones
     */
    private void approachZone() {
        try {
            sleep(Params.TRAVEL_TIME);
        } catch (InterruptedException e) {
        }
        // changes status after trip time: arrival -> waiting to dock
        if (this.status == Statuses.ARRIVAL) {
            this.setStatus("waiting to dock");
            // changes status after trip time: undocked -> waiting to depart
        } else if (this.status == Statuses.UNDOCKED) {
            this.setStatus("waiting to depart");
        }
    }

    /**
     * This will set the enumerated state of the pilot in the USS Emafor in a plain English manner
     * @param location that the pilot is currently in
     */
    public void setStatus(String location) {

        if (location.equals("waiting for ship")) {
            this.status = Statuses.WAITING_FOR_SHIP;
        } else if (location.equals("arrival zone")) {
            this.status = Statuses.ARRIVAL;
        } else if (location.equals("departure zone")) {
            this.status = Statuses.DEPARTURE;
        } else if (location.equals("docking")) {
            this.status = Statuses.DOCKING;
        } else if (location.equals("undocked")) {
            this.status = Statuses.UNDOCKED;
        } else if (location.equals("docked")) {
            this.status = Statuses.DOCKED;
        } else if (location.equals("waiting to dock")) {
            this.status = Statuses.WAITING_TO_DOCK;
        } else if (location.equals("waiting to depart")) {
            this.status = Statuses.WAITING_TO_DEPART;
        }
    }

    /**
     * Requests tugs and waits until there are enough tugs to fulfil its needs, then takes said tugs
     * @param tugsNeeded
     */
    public void requestTugs(int tugsNeeded) {
        tugs.acquireTugs(this, tugsNeeded);
    }

    /**
     * Will add or remove tugs as needed
     * @param tugs
     */
    public void alterTugs(int tugs) {
        this.acquiredTugs += tugs;
    }

    /**
     * Getter method for ship
     * @return ship pilot is currently piloting
     */
    public Ship getShip() {
        return this.ship;
    }

    /**
     * Setter method for ship
     * @param ship  the pilot has just boarded
     */
    public void setShip(Ship ship) {
        this.ship = ship;
    }

    /**
     * Triggers the pilot releasing the ship and outputs message
     */
    private void releaseShip() {
        String msg = String.format("%s releases %s", this.toString(), this.ship.toString());
        this.ship = null;
        this.setStatus("waiting for ship");
        System.out.println(msg);
    }

    /**
     * Will commence all departure duties in this order:
     *      - releasing ship
     *      - releasing tugs
     *      - arrive at departure zone
     *      - return to USS Emafor and wait for another ship to arrive
     */
    public void engageDepartureProcedure() {
        Ship departingShip = this.ship;
        this.releaseShip();
        tugs.returnTugs(this, this.acquiredTugs);
        this.departureZone.arrive(departingShip);
        this.setStatus("departure zone");
        this.setStatus("waiting for ship");
    }


    public void run() {
        

        while (true) {
            // if at USS Emafor awaiting ship, board ship and approach berth to await docking
            if (this.status == Statuses.WAITING_FOR_SHIP) {
                this.arrivalZone.boardingProcedure(this);
                this.arrivalZone.depart();
                this.approachZone();
            }

            // if waiting to dock, request tugs needed to dock and then engage docking proceedure
            if (this.status == Statuses.WAITING_TO_DOCK) {
                this.requestTugs(Params.DOCKING_TUGS);
                this.engageDockingProcedure();
            }

            // if docked and loaded, unload
            if (this.status == Statuses.DOCKED && this.ship.loaded) {
                this.unloadCargo();
            }

            // if docked and unloaded, undock and approach departure zone
            if (this.status == Statuses.DOCKED && !this.ship.loaded) {
                this.engageUndockingProceedure();
                this.approachZone();
            }

            // if waiting to depart, release ship -> release ship -> return to USS Emaphor and await new ship arrival
            if (this.status == Statuses.WAITING_TO_DEPART) {
                this.engageDepartureProcedure();
            }
        }
    }


}
