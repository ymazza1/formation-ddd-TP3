package com.akei.vente.domain;

/**
 * Port (interface du domaine) exprimant le besoin métier de la Vente :
 * « cet article est-il disponible dans cette quantité, MAINTENANT ? »
 * <p>
 * Le domaine définit CE dont il a besoin (le contrat), pas COMMENT on le sait.
 * L'implémentation concrète (stock magasin, entrepôt, appel à un autre service)
 * appartiendra à l'infrastructure — c'est l'amorce du couple port/adapter (TP4)
 * et de l'Anticorruption Layer Vente ↔ Stock (J3).
 * <p>
 * Point pédagogique clé : l'invariant métier « la ligne doit être disponible à
 * l'instant de la validation » vit ici, dans le domaine. La FAÇON d'obtenir le
 * stock est volontairement hors du domaine.
 */
public interface Disponibilite {

    /** Vrai si le SKU est disponible dans (au moins) la quantité demandée, à l'instant de l'appel. */
    boolean estDisponible(Sku sku, Quantity quantiteDemandee);
}
