package simcli.needs;

/**
 * Abstract representation of a Sim's biological or emotional need.
 */
public abstract class Need {
    protected String name;
    protected int value;
    public static final int MAX_VALUE = 100;
    
    public Need(String name) {
        this.name = name;
        this.value = MAX_VALUE;
    }
    
    public abstract void decay(); 
    
    public void increase(int amount) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
    }
    
    public void decrease(int amount) {
        this.value = Math.max(this.value - amount, 0);
    }
    
    public int getValue() { return this.value; }

    public void setValue(int value) { 
        this.value = Math.max(0, Math.min(value, MAX_VALUE));
    }
    public String getName() { return this.name; }
}