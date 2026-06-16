package com.akei.vente.domain;

import java.util.Objects;

/**
 * Ligne d'un panier : objet INTERNE à l'aggregate Panier.
 * <p>
 * Important : on ne manipule jamais une LignePanier directement depuis
 * l'extérieur de l'aggregate. Toutes les opérations passent par le Panier
 * (l'Aggregate Root). C'est volontairement un package-private en intention
 * (ici public pour la lisibilité du TP, mais conceptuellement interne).
 * <p>
 * Modélisée comme un Value Object : entièrement définie par (sku, quantité,
 * prix unitaire). Immuable : changer la quantité produit une nouvelle ligne.
 */
public record LignePanier(Sku sku, Quantity quantite, Money prixUnitaire) {

    public LignePanier {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        Objects.requireNonNull(quantite, "La quantité est obligatoire");
        Objects.requireNonNull(prixUnitaire, "Le prix unitaire est obligatoire");
    }

    /** Sous-total de la ligne = prix unitaire x quantité. */
    public Money sousTotal() {
        return prixUnitaire.multiplie(quantite.valeur());
    }

    /** Renvoie une NOUVELLE ligne avec la quantité augmentée (fusion de SKU). */
    public LignePanier avecQuantiteAugmentee(Quantity ajout) {
        return new LignePanier(sku, quantite.plus(ajout), prixUnitaire);
    }

    /** Renvoie une NOUVELLE ligne avec la quantité remplacée. */
    public LignePanier avecQuantite(Quantity nouvelleQuantite) {
        return new LignePanier(sku, nouvelleQuantite, prixUnitaire);
    }
}
