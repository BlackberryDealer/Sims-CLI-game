package simcli.utils;

import java.util.Random;

/**
 * Utility class providing a single, global instance of {@link java.util.Random} 
 * to be used across the entire game for consistency and performance.
 */
public final class GameRandom {
    /** The global random number generator instance. */
    public static final Random RANDOM = new Random();
    
    /** Private constructor to prevent instantiation of this utility class. */
    private GameRandom() {}
}
