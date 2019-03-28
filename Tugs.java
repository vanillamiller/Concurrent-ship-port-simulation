public class Tugs{

    private volatile int availableTugs;

    public Tugs(int numTugs){
        this.availableTugs = numTugs;
    }


    public synchronized void returnTugs(Pilot pilot, int tugsNotNeeded){

        pilot.alterTugs(0 - tugsNotNeeded);
        this.availableTugs += tugsNotNeeded;
        String msg = String.format("%s releases %d tugs %s", pilot.toString(), tugsNotNeeded, this.toString());
        System.out.println(msg);
        notify();
    }

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
