package com.akei.vente.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Commande AKEI : Aggregate Root distinct du Panier — À COMPLÉTER.
 * <p>
 * Objectifs :
 *   - naître COHÉRENTE : invariant « au moins une ligne » vérifié à la création ;
 *   - copie défensive des lignes ;
 *   - ENREGISTRER un événement CommandePassee à la création (liste interne) ;
 *   - total = somme des sous-totaux ;
 *   - égalité PAR IDENTITÉ.
 * <p>
 * Le constructeur est volontairement package-private : une Commande se crée via
 * Panier.validerEnCommande(...), pas directement.
 */
public final class Commande {

    private final CommandeId id;
    private final List<LigneCommande> lignes;
    private final List<Object> evenements = new ArrayList<>();

    Commande(CommandeId id, List<LigneCommande> lignes) {
        // TODO : valider id non null, lignes non null et non vide
        // TODO : stocker une copie défensive (List.copyOf)
        // TODO : enregistrer un CommandePassee.depuis(this) dans 'evenements'
        throw new UnsupportedOperationException("À implémenter");
    }

    public Money total() {
        // TODO : somme des sousTotal() (partir de Money.zeroEuro())
        throw new UnsupportedOperationException("À implémenter");
    }

    public CommandeId id() {
        return id;
    }

    public List<LigneCommande> lignes() {
        return lignes;
    }

    /** Événements survenus, en lecture seule. */
    public List<Object> evenements() {
        return List.copyOf(evenements);
    }

    public void viderEvenements() {
        evenements.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commande other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public record CommandeId(UUID valeur) {
        public CommandeId {
            Objects.requireNonNull(valeur, "L'identifiant ne peut pas être null");
        }

        public static CommandeId nouveau() {
            return new CommandeId(UUID.randomUUID());
        }
    }

    /** Ligne de commande : VO figé au moment de la validation. FOURNIE complète. */
    public record LigneCommande(Sku sku, Quantity quantite, Money prixUnitaire) {
        public LigneCommande {
            Objects.requireNonNull(sku, "Le SKU est obligatoire");
            Objects.requireNonNull(quantite, "La quantité est obligatoire");
            Objects.requireNonNull(prixUnitaire, "Le prix unitaire est obligatoire");
        }

        public Money sousTotal() {
            return prixUnitaire.multiplie(quantite.valeur());
        }
    }
}
