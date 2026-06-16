package com.akei.vente.domain;

/**
 * Value Object : quantité d'un article.
 * <p>
 * Invariant : une quantité est strictement positive. Le zéro et le négatif
 * n'ont pas de sens métier dans une ligne de commande (retirer un article
 * = supprimer la ligne, pas mettre quantité 0).
 * <p>
 * Immuable : toute opération (plus, fois) renvoie une nouvelle Quantity.
 */
public record Quantity(int valeur) {

    public Quantity {
        if (valeur <= 0) {
            throw new IllegalArgumentException(
                    "Une quantité doit être strictement positive : " + valeur);
        }
    }

    public static Quantity of(int valeur) {
        return new Quantity(valeur);
    }

    /** Renvoie une nouvelle Quantity augmentée (immutabilité). */
    public Quantity plus(Quantity autre) {
        return new Quantity(this.valeur + autre.valeur);
    }

    @Override
    public String toString() {
        return Integer.toString(valeur);
    }
}
