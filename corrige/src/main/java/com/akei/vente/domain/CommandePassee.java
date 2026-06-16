package com.akei.vente.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain Event : un fait métier qui s'est produit.
 * <p>
 * Nomenclature DDD :
 *   - nommé au PASSÉ (« CommandePassee »), car il décrit quelque chose d'arrivé,
 *     pas une intention (ce serait « PasserCommande », une Command, pas un Event) ;
 *   - immuable (un record) : un fait passé ne change pas ;
 *   - porte les données utiles au moment de l'événement (ici l'id de la commande,
 *     son total, le nombre de lignes, l'horodatage), pas l'aggregate entier.
 * <p>
 * Cet événement permettra plus tard à d'autres contextes (Stock, Fidélité) de
 * réagir sans que la Vente les connaisse (découplage — voir TP4 et J3).
 */
public record CommandePassee(
        Commande.CommandeId commandeId,
        Money total,
        int nombreDeLignes,
        Instant survenuLe) {

    public CommandePassee {
        Objects.requireNonNull(commandeId, "commandeId obligatoire");
        Objects.requireNonNull(total, "total obligatoire");
        Objects.requireNonNull(survenuLe, "survenuLe obligatoire");
    }

    /** Fabrique l'événement à partir d'une commande fraîchement créée. */
    public static CommandePassee depuis(Commande commande) {
        return new CommandePassee(
                commande.id(),
                commande.total(),
                commande.lignes().size(),
                Instant.now());
    }
}
