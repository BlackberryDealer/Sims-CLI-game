package simcli.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.LifecycleManager;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.managers.CareerManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regression — Needs, Infants, Careers")
public class NeedsAndCareerRegressionTest {

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
    }

    @Test
    @DisplayName("An unfed infant loses health and can starve")
    void testInfantCanStarve() {
        Sim baby = new Sim("Bub", 0, Gender.MALE);
        baby.setChildSim(true);
        baby.getHunger().setValue(0);

        baby.tick();
        assertTrue(baby.getHealth() < 100, "Starving infant should lose health");

        for (int i = 0; i < 50 && baby.getState() != SimState.DEAD; i++) {
            baby.tick();
        }
        assertEquals(SimState.DEAD, baby.getState());
    }

    @Test
    @DisplayName("Newborns don't receive the adult starting money")
    void testNewbornHasNoMoney() {
        Sim dad = new Sim("Dad", 30, Gender.MALE);
        Sim mum = new Sim("Mum", 30, Gender.FEMALE);
        dad.getRelationshipManager().setSpouse(mum);
        mum.getRelationshipManager().setSpouse(dad);

        Sim baby = dad.getRelationshipManager().finalizeBaby("Bub", Gender.FEMALE);

        assertEquals(0, baby.getMoney());
        assertEquals(0, baby.getTotalMoneyEarned());
    }

    @Test
    @DisplayName("Need.decrease() with a negative amount never exceeds the maximum")
    void testNeedNeverExceedsMax() {
        Sim sim = new Sim("S", 30, Gender.MALE);
        sim.getHunger().decrease(-5);
        assertEquals(100, sim.getHunger().getValue());
    }

    @Test
    @DisplayName("A worked day resets the truancy counter instead of incrementing it")
    void testWorkedDayIsNotTruancy() {
        CareerManager career = new CareerManager(Job.SOFTWARE_ENGINEER);
        career.setConsecutiveDaysMissed(2);
        career.incrementShiftsWorkedToday();

        career.checkTruancy("Bob", true);

        assertEquals(0, career.getConsecutiveDaysMissed());
    }

    @Test
    @DisplayName("A weekend day never counts as missed work")
    void testWeekendIsNotTruancy() {
        CareerManager career = new CareerManager(Job.SOFTWARE_ENGINEER);
        for (int i = 0; i < 10; i++) {
            career.checkTruancy("Bob", false);
        }
        assertEquals(0, career.getConsecutiveDaysMissed());
        assertEquals(Job.SOFTWARE_ENGINEER, career.getCareer());
    }

    @Test
    @DisplayName("A Sim who dies of old age doesn't collect a pension")
    void testNoPensionAfterDeath() {
        SimulationLogger logger = new SimulationLogger();
        LifecycleManager lifecycle = new LifecycleManager(1, logger);
        Sim elder = new Sim("Elder", GameConstants.DEATH_AGE - 1, Gender.FEMALE, Job.UNEMPLOYED);
        int moneyBefore = elder.getMoney();

        lifecycle.processDay(elder);

        assertEquals(SimState.DEAD, elder.getState());
        assertEquals(moneyBefore, elder.getMoney());
    }
}
