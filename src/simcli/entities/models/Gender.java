package simcli.entities.models;

/**
 * Represents the biological gender of a Sim, affecting marriage and
 * reproduction mechanics.
 *
 * <p>Same-gender couples cannot reproduce biologically, per the rules
 * enforced by {@link simcli.entities.managers.RelationshipManager#attemptPregnancy()}.</p>
 */
public enum Gender {

    /** Male gender. */
    MALE,

    /** Female gender. */
    FEMALE
}
