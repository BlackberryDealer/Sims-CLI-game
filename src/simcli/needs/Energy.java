package simcli.needs;
public class Energy extends Need {
    public Energy() {
        super("Energy");
    }
    
    @Override
    public void decay() {
        this.decrease(2);
    }
}