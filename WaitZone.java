
import java.util.ArrayList;

public class WaitZone {

    private String name;
    private volatile ArrayList<Ship> waitingShips = new ArrayList<Ship>();
    private final int MAX_SHIPS = 2;

    public WaitZone(String name) { this.name = name; }

    public int numShipsWaiting(){
        return waitingShips.size();
    }

    public synchronized void arrive(Ship arrivedShip){
        while(this.numShipsWaiting() >= MAX_SHIPS) {
            try{
                wait();
            } catch(InterruptedException e){}
        }
        waitingShips.add(arrivedShip);
        if (this.name.equals("arrival")) {
            String arrivalMsg = String.format("%s arrives at arrival zone", arrivedShip.toString());
            System.out.println(arrivalMsg);

        }
        notify();
    }

    public synchronized void depart(){
        while(this.numShipsWaiting() < 1){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        Ship departingShip = waitingShips.get(0);
        waitingShips.remove(0);
        if (this.name.equals("departure")) {
            String departureMsg = String.format("%s departs departure zone", departingShip.toString());
            System.out.println(departureMsg);
        }
//        notify();
    }

    // Facilitates the boarding of the ship, will make pilots wait if there are no ships available
    public synchronized void boardingProcedure(Pilot pilot){
        while(this.numShipsWaiting() < 1){
            try{
                wait();
            }catch(InterruptedException e){}
        }
        Ship acquiredShip = waitingShips.get(0);
        waitingShips.remove(0);
        pilot.setShip(acquiredShip);
        pilot.setStatus("arrival zone");
        String msg = String.format("%s has acquired %s", pilot.toString(), acquiredShip.toString());
        System.out.println(msg);
    }

}
