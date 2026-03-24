package simcli.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.items.Furniture;
import simcli.entities.models.Gender;
import simcli.entities.actors.Sim;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room Bounds & Geometry Tests")
public class RoomTest {

    @Test
    @DisplayName("Room.canFit() safely rejects objects larger than current capacity")
    void testCapacityLimits() {
        Room room = new Room("Tiny Closet", 5);
        Furniture desk = new Furniture("Desk", 50, 4); // uses 4 capacity
        
        assertTrue(room.canFit(desk), "Desk (4) should fit in room (5)");
        room.placeFurniture(desk, 4);
        
        assertEquals(4, room.getUsedCapacity());
        
        Furniture bigBed = new Furniture("Big Bed", 50, 3); // uses 3 capacity
        assertFalse(room.canFit(bigBed), "Bed (3) + Desk (4) = 7, cannot exceed room cap (5)");
    }

    @Test
    @DisplayName("Upgrading room capacity correctly drains money")
    void testRoomUpgradeDeductsMoney() {
        Room room = new Room("Basic Room", 10);
        Sim sim = new Sim("Bob", 25, Gender.MALE);
        sim.setMoney(1500);
        
        // Sim buys 5 extra capacity for $1000
        room.upgradeCapacity(sim, 5, 1000); 
        
        assertEquals(500, sim.getMoney(), "Sim should have $500 remaining");
        assertEquals(15, room.getMaxCapacity(), "Room capacity should now be 15");
    }
    
    @Test
    @DisplayName("Upgrading room capacity rejects if Sim doesn't have enough money")
    void testRoomUpgradeRejectsInsufficientFunds() {
        Room room = new Room("Basic Room", 10);
        Sim sim = new Sim("Bob", 25, Gender.MALE);
        sim.setMoney(0);
        
        // Sim attempts to buy 5 capacity for $1000
        room.upgradeCapacity(sim, 5, 1000); 
        
        assertEquals(0, sim.getMoney(), "Sim money must not dive into negatives");
        assertEquals(10, room.getMaxCapacity(), "Room capacity should not expand on failed purchase");
    }
}
