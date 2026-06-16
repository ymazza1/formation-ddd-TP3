package com.akei.vente.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain Event : un fait métier qui s'est produit — À COMPLÉTER.
 * <p>
 * Rappels de nomenclature DDD :
 *   - nommé au PASSÉ (fait accompli), immuable (record) ;
 *   - porte les données utiles au moment de l'événement, pas l'aggregate entier.
 */
public record CommandePassee(
        Commande.CommandeId commandeId,
        Money total,
        int nombreDeLignes,
        Instant survenuLe) {

    public CommandePassee {
        // TODO : valider commandeId, total, survenuLe non null
        throw new UnsupportedOperationException("À implémenter");
    }

    /** Fabrique l'événement à partir d'une commande fraîchement créée. */
    public static CommandePassee depuis(Commande commande) {
        // TODO : construire l'événement à partir de l'id, du total, du nb de lignes
        //        et de Instant.now()
        throw new UnsupportedOperationException("À implémenter");
    }
}
