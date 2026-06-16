package com.akei.vente.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Commande AKEI : Aggregate Root, distinct du Panier.
 * <p>
 * Points DDD illustrés ici :
 *   - Panier et Commande sont DEUX aggregates distincts. La Commande ne tient
 *     pas une référence vers le Panier ; elle est CONSTRUITE à partir de son
 *     contenu au moment de la validation, puis vit sa propre vie.
 *   - La Commande naît COHÉRENTE : ses invariants sont vérifiés à la création
 *     (au moins une ligne, lignes non nulles). Un état invalide est impossible.
 *   - Elle ENREGISTRE les Domain Events survenus (ici CommandePassee) dans une
 *     liste interne. La publication effective sera faite par la couche
 *     application après commit (vu au TP4) — l'aggregate ne publie pas lui-même.
 * <p>
 * La création se fait via Panier.validerEnCommande(...) : c'est le Panier qui
 * orchestre la vérification de disponibilité puis fabrique la Commande.
 */
public final class Commande {

    private final CommandeId id;
    private final List<LigneCommande> lignes;
    private final List<Object> evenements = new ArrayList<>();

    Commande(CommandeId id, List<LigneCommande> lignes) {
        Objects.requireNonNull(id, "L'identifiant de commande est obligatoire");
        Objects.requireNonNull(lignes, "Les lignes sont obligatoires");
        if (lignes.isEmpty()) {
            throw new IllegalArgumentException("Une commande a au moins une ligne");
        }
        this.id = id;
        this.lignes = List.copyOf(lignes);
        // La commande vient d'être passée : on enregistre le fait.
        this.evenements.add(CommandePassee.depuis(this));
    }

    /** Total de la commande = somme des sous-totaux. */
    public Money total() {
        Money total = Money.zeroEuro();
        for (LigneCommande ligne : lignes) {
            total = total.plus(ligne.sousTotal());
        }
        return total;
    }

    public CommandeId id() {
        return id;
    }

    public List<LigneCommande> lignes() {
        return lignes;
    }

    /** Événements survenus, en lecture seule. La couche application les publiera. */
    public List<Object> evenements() {
        return List.copyOf(evenements);
    }

    /** À appeler après publication, pour ne pas republier (cycle de vie de l'aggregate). */
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

    /**
     * Ligne de commande : Value Object figé au moment de la validation.
     * Contrairement à la LignePanier (qui peut évoluer tant qu'on est dans le
     * panier), la LigneCommande capture le prix et la quantité à l'instant T.
     */
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
