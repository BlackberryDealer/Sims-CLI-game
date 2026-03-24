package simcli.entities.models;

/**
 * Enumerated thresholds detailing the severity and nature of an association between two Sims.
 */
public enum RelationshipStatus {
    /** Interactions have just begun */
    STRANGER,
    /** Cordial but uninvested association */
    ACQUAINTANCE,
    /** Platonically high bounds of interaction */
    FRIEND,
    /** Active partnered/romantic involvement */
    ROMANTIC,
    /** Legally bound marriage */
    MARRIED
}
