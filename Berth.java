public class Berth extends WaitZone{

    private volatile boolean shieldDeployed;
    private volatile boolean reserved = false;
    private final int MAX_SHIPS = 1;

    public Berth(String name) {
        super(name);
    }

    public synchronized void deployShield(){
        this.shieldDeployed = true;
        System.out.println("Shield is activated");
    }

    public synchronized void dockingProcedure(Pilot pilot) {
        while (shieldDeployed || this.numShipsWaiting() >= MAX_SHIPS){
            try{
                wait();
                System.out.println("waiting at docking procedure");
            }catch(InterruptedException e){}
        }
        this.arrive(pilot.getShip());
        String msg = String.format("%s docks at berth", pilot.getShip());
        System.out.println(msg);
        pilot.setStatus("docked");
    }

    public synchronized void undockingProcedure(Pilot pilot){
        while (shieldDeployed) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.depart();
        this.reserved = false;
        String msg = String.format("%s undocks from berth", pilot.getShip());
        pilot.setStatus("undocked");
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

    public boolean shieldDeployed(){
        return shieldDeployed;
    }









}
