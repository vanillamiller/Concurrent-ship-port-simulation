public class Operator extends Thread {

    private Berth berth;

    public Operator(Berth berth){
        this.berth = berth;
    }

    public void deployShield(){
        if(! this.berth.isDockingInProgress()) {
            this.berth.deployShield();
        }
        try{
            sleep(Params.DEBRIS_TIME);
        }catch(InterruptedException e){}
        this.berth.retractShield();
    }

    public void run(){
        while(true){
            try{
                sleep(Params.debrisLapse());
            } catch(InterruptedException e){}
            this.deployShield();
        }

    }
}
