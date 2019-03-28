
import java.util.ArrayList;

public class WaitZone {

    private String name;
    private volatile ArrayList<Ship> waitingShips = new ArrayList<Ship>();
    private final int MAX_SHIPS = 2;

    /**
     * Constructor method that sets the waitzone's name. It was assumed that the names would be used for outputting the
     * arrival and depart messages only when they arrive at arrival zone and depart from departure zone as this is how
     * it is constructed in the skeleton Main.java when it is instantiated
     *
     * @param name
     */
    public WaitZone(String name) { this.name = name; }

    /**
     *
     * @return the number of ships currently waiting in this waitzone
     */
    public int numShipsWaiting(){
        return waitingShips.size();
    }


    /**
     * Facilitates the arrival of a ship to a waitzone by adding the ship object to the zone's waiting zone.
     *
     * @param arrivedShip the ship that has just arrived after approaching
     *
     *
     */
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

    /**
     * Removes the departing ship from the waitzone and outputs the departure message.
     * Notifies pilots, producer and/or consumer that there is a free space in the zone in order for them to arrive
     */
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
        notify();
    }

    /**
     * Facilitates the pilot boarding the ship when it is at a specific zone, in this case the arrival zone
     *
     * @param pilot that is available to captain ship and guide it to the berth and then departure
     */
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
