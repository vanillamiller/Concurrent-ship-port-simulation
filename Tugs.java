/**
 *  Represents the USS Emafor's tugs cache where tugs can be requested from for docking and released after docking and
 *  departure
 *
 *  @author anthonym1@student.unimelb.edu.au
 */

public class Tugs{

    private volatile int availableTugs;

    public Tugs(int numTugs){
        this.availableTugs = numTugs;
    }

    /**
     * Removes tugs from the pilot's ship,  adds them to available tugs and notifies ships that have requested tugs
     * that there are now tugs available
     *
     * @param pilot pilot that is jettisoning tugs when arrived at berth or departure
     * @param tugsNotNeeded amount of tugs that are to be jettisoned and returned to the tug stockpile (available tugs)
     */
    public synchronized void returnTugs(Pilot pilot, int tugsNotNeeded){

        pilot.alterTugs(0 - tugsNotNeeded);
        this.availableTugs += tugsNotNeeded;
        String msg = String.format("%s releases %d tugs %s", pilot.toString(), tugsNotNeeded, this.toString());
        System.out.println(msg);
        notify();
    }

    /**
     * Once the pilot has requested tugs it will take the amount it needs if there are enough tugs available, or wait
     * until there are enough tugs available
     *
     * @param pilot pilot requesting tugs
     * @param tugsNeeded the amount of tugs the pilot is requesting
     */
    public synchronized void acquireTugs(Pilot pilot, int tugsNeeded){

        while(this.availableTugs < tugsNeeded) {
            try{
                wait();
            }catch(InterruptedException e){}
        }
        this.availableTugs -= tugsNeeded;
        pilot.alterTugs(tugsNeeded);
        String msg = String.format("%s acquires %d tugs %s", pilot.toString(), tugsNeeded, this.toString());
        System.out.println(msg);
    }

    @Override
    public String toString(){
        return String.format("(%d available)", this.availableTugs);
    }
}
