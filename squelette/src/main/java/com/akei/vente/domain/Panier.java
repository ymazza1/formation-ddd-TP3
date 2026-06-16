package com.akei.vente.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Panier AKEI : Aggregate Root.
 * <p>
 * C'est le GARDIEN des invariants qui portent sur l'ensemble du panier :
 * <ul>
 *   <li>chaque ligne a une quantité strictement positive (porté par Quantity) ;</li>
 *   <li>pas de doublon de SKU : ajouter un SKU déjà présent FUSIONNE les
 *       quantités sur la même ligne (règle métier issue de l'Example Mapping) ;</li>
 *   <li>le total est toujours cohérent (recalculé à partir des lignes) ;</li>
 *   <li>la collection interne ne fuit jamais en mutable.</li>
 * </ul>
 * <p>
 * Règle d'or de l'aggregate : tout accès et toute modification passent par la
 * racine. On n'expose pas les LignePanier en écriture, et les mutations se font
 * via des méthodes métier (ajouterArticle, retirerArticle, modifierQuantite),
 * jamais via des setters.
 * <p>
 * Choix d'implémentation : on indexe les lignes par SKU dans une LinkedHashMap
 * pour garantir l'unicité du SKU (invariant de fusion) tout en conservant
 * l'ordre d'insertion (confort d'affichage).
 */
public final class Panier {

    private final PanierId id;
    private final Map<Sku, LignePanier> lignesParSku = new LinkedHashMap<>();

    public Panier(PanierId id) {
        this.id = Objects.requireNonNull(id, "L'identifiant de panier est obligatoire");
    }

    public static Panier nouveau() {
        return new Panier(PanierId.nouveau());
    }

    /**
     * Ajoute un article. Si le SKU est déjà présent, fusionne les quantités
     * sur la ligne existante (pas de doublon). Sinon, crée une nouvelle ligne.
     */
    public void ajouterArticle(Sku sku, Quantity quantite, Money prixUnitaire) {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        Objects.requireNonNull(quantite, "La quantité est obligatoire");
        Objects.requireNonNull(prixUnitaire, "Le prix unitaire est obligatoire");

        LignePanier existante = lignesParSku.get(sku);
        if (existante == null) {
            lignesParSku.put(sku, new LignePanier(sku, quantite, prixUnitaire));
        } else {
            lignesParSku.put(sku, existante.avecQuantiteAugmentee(quantite));
        }
    }

    /** Retire entièrement la ligne correspondant au SKU. Sans effet si absent. */
    public void retirerArticle(Sku sku) {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        lignesParSku.remove(sku);
    }

    /**
     * Remplace la quantité de la ligne du SKU. La quantité étant un Quantity,
     * elle est forcément strictement positive : pour « descendre à zéro »,
     * on retire l'article (retirerArticle), on ne met pas une quantité 0.
     *
     * @throws IllegalArgumentException si le SKU n'est pas dans le panier
     */
    public void modifierQuantite(Sku sku, Quantity nouvelleQuantite) {
        Objects.requireNonNull(sku, "Le SKU est obligatoire");
        Objects.requireNonNull(nouvelleQuantite, "La quantité est obligatoire");
        LignePanier ligne = lignesParSku.get(sku);
        if (ligne == null) {
            throw new IllegalArgumentException("Aucune ligne pour le SKU " + sku);
        }
        lignesParSku.put(sku, ligne.avecQuantite(nouvelleQuantite));
    }

    /**
     * Valide le panier et le transforme en Commande (deux aggregates distincts).
     * <p>
     * Invariants vérifiés À L'INSTANT DE LA VALIDATION (pas à l'ajout) :
     *   - le panier n'est pas vide ;
     *   - chaque ligne est disponible dans sa quantité, selon le port Disponibilite.
     * <p>
     * La disponibilité est consultée maintenant : c'est l'invariant temporel de
     * l'Example Mapping (le stock a pu changer depuis l'ajout au panier).
     *
     * @throws IllegalStateException   si le panier est vide
     * @throws ArticleIndisponible     si une ligne n'est plus disponible
     */
    public Commande validerEnCommande(Disponibilite disponibilite) {
        // TODO : refuser un panier vide (IllegalStateException)
        // TODO : pour chaque ligne, vérifier disponibilite.estDisponible(sku, quantite)
        //        sinon lever ArticleIndisponible(sku)
        // TODO : construire les Commande.LigneCommande à partir des lignes du panier
        // TODO : créer et renvoyer une Commande (CommandeId.nouveau())
        throw new UnsupportedOperationException("À implémenter");
    }

    /** Total du panier = somme des sous-totaux. Toujours recalculé, jamais stocké. */
    public Money total() {
        Money total = Money.zeroEuro();
        for (LignePanier ligne : lignesParSku.values()) {
            total = total.plus(ligne.sousTotal());
        }
        return total;
    }

    public boolean estVide() {
        return lignesParSku.isEmpty();
    }

    public int nombreDeLignes() {
        return lignesParSku.size();
    }

    public Optional<LignePanier> ligneDe(Sku sku) {
        return Optional.ofNullable(lignesParSku.get(sku));
    }

    /** Lecture seule : copie défensive non modifiable, l'interne ne fuit jamais. */
    public List<LignePanier> lignes() {
        return List.copyOf(new ArrayList<>(lignesParSku.values()));
    }

    public PanierId id() {
        return id;
    }

    /** Égalité d'Entity : par identité. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Panier other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public record PanierId(UUID valeur) {
        public PanierId {
            Objects.requireNonNull(valeur, "L'identifiant ne peut pas être null");
        }

        public static PanierId nouveau() {
            return new PanierId(UUID.randomUUID());
        }
    }
}
