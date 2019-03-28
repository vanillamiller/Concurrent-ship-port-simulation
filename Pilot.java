public class Pilot extends Thread {

    private int id;
    private WaitZone arrivalZone;
    private WaitZone departureZone;
    private Tugs tugs;

    private volatile int acquiredTugs = 0;
    private Berth berth;

    private enum Statuses {
        ARRIVAL, WAITING_FOR_SHIP, DEPARTURE, WAITING_TO_DOCK, WAITING_TO_DEPART, DOCKING,
        UNDOCKED, DOCKED
    }

    private volatile Statuses status = Statuses.WAITING_FOR_SHIP;
    private volatile Ship ship;

    public Pilot(int id, WaitZone arrivalZone, WaitZone departureZone, Tugs tugs, Berth berth) {
        this.id = id;
        this.arrivalZone = arrivalZone;
        this.departureZone = departureZone;
        this.tugs = tugs;
        this.berth = berth;
    }

    public String toString() {
        return String.format("pilot %d", this.id);
    }

    private void acquireShip() {
        this.arrivalZone.boardingProcedure(this);
    }

    private void engageDockingProcedure() {


        berth.reserve();

        // if shield is deployed wait
        this.berth.isShieldDeployed();
        try {
            sleep(Params.DOCKING_TIME);
        } catch (InterruptedException e) {
        }
        this.setStatus("docking");
        this.berth.dockingProcedure(this);
    }

    private void engageUndockingProceedure() {

            // if shield is deployed wait
            this.berth.isShieldDeployed();
            try {
                sleep(Params.UNDOCKING_TIME);
            } catch (InterruptedException e) {
            }
            berth.undockingProcedure(this);

    }

    private void unloadCargo() {
        int numTugsToRelease = Math.abs(Params.DOCKING_TUGS - Params.UNDOCKING_TUGS);
        tugs.returnTugs(this, numTugsToRelease);
        String msg = String.format("%s is being unloaded", this.getShip());
        System.out.println(msg);
        try {
            sleep(Params.UNLOADING_TIME);
        } catch (InterruptedException e) {
        }
        this.ship.loaded = false;
    }

    private void approachZone() {
        try {
            sleep(Params.TRAVEL_TIME);
        } catch (InterruptedException e) {
        }
        if (this.status == Statuses.ARRIVAL) {
            this.setStatus("waiting to dock");
        } else if (this.status == Statuses.UNDOCKED) {
            this.setStatus("waiting to depart");
        }
    }

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
        } else if (location.equals("waiting to dock")){
            this.status = Statuses.WAITING_TO_DOCK;
        } else if (location.equals("waiting to depart")){
            this.status = Statuses.WAITING_TO_DEPART;
        }
    }

    public void requestTugs(int tugsNeeded) {
        tugs.acquireTugs(this, tugsNeeded);
    }

    public void alterTugs(int tugs) {
        this.acquiredTugs += tugs;
    }

    public Ship getShip() {
        return this.ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void releaseShip() {
        String msg = String.format("%s has released %s", this.toString(), this.ship.toString());
        this.ship = null;
        this.setStatus("waiting for ship");
        System.out.println(msg);
    }


    public void run() {
        /* if there is a ship at arrival */

        while (true) {
            if (this.status == Statuses.WAITING_FOR_SHIP) {
                this.acquireShip();
                this.arrivalZone.depart();
                this.approachZone();
            }
            if (this.status == Statuses.WAITING_TO_DOCK) {
                this.requestTugs(Params.DOCKING_TUGS);
                this.engageDockingProcedure();
            }
            if (this.status == Statuses.DOCKED && this.ship.loaded) {
                this.unloadCargo();
            }
            if (this.status == Statuses.DOCKED && !this.ship.loaded) {
                this.engageUndockingProceedure();
                this.approachZone();
            }
            if (this.status == Statuses.WAITING_TO_DEPART) {
                this.departureZone.arrive(this.ship);
                this.setStatus("departure zone");
                this.releaseShip();
                tugs.returnTugs(this, this.acquiredTugs);




            }
        }
    }


}
