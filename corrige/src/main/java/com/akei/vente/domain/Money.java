package com.akei.vente.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object : un montant monétaire (montant + devise).
 * <p>
 * Invariants :
 * - le montant ne peut pas être null ni négatif (AKEI ne manipule pas de
 *   montant négatif dans la Vente : une remise ne descend jamais sous zéro) ;
 * - la devise est obligatoire ;
 * - on n'additionne / compare que des montants de même devise.
 * <p>
 * Le montant est normalisé à 2 décimales (centimes). L'égalité par valeur
 * tient compte du montant normalisé ET de la devise.
 * <p>
 * Note : on n'utilise PAS le record ici car on veut normaliser le BigDecimal
 * (scale = 2) dans le constructeur et garder le contrôle sur equals/hashCode.
 * C'est un bon exemple de VO « à la main » à côté des records.
 */
public final class Money {

    public static final Currency EURO = Currency.getInstance("EUR");

    private final BigDecimal montant;
    private final Currency devise;

    private Money(BigDecimal montant, Currency devise) {
        Objects.requireNonNull(montant, "Le montant ne peut pas être null");
        Objects.requireNonNull(devise, "La devise ne peut pas être null");
        if (montant.signum() < 0) {
            throw new IllegalArgumentException(
                    "Un montant ne peut pas être négatif : " + montant);
        }
        this.montant = montant.setScale(2, RoundingMode.HALF_UP);
        this.devise = devise;
    }

    public static Money euros(String montant) {
        return new Money(new BigDecimal(montant), EURO);
    }

    public static Money euros(BigDecimal montant) {
        return new Money(montant, EURO);
    }

    public static Money zeroEuro() {
        return new Money(BigDecimal.ZERO, EURO);
    }

    public Money plus(Money autre) {
        exigerMemeDevise(autre);
        return new Money(this.montant.add(autre.montant), this.devise);
    }

    public Money multiplie(int facteur) {
        if (facteur < 0) {
            throw new IllegalArgumentException("Facteur négatif interdit : " + facteur);
        }
        return new Money(this.montant.multiply(BigDecimal.valueOf(facteur)), this.devise);
    }

    /**
     * Applique une remise (montant absolu). Invariant clé : une remise ne rend
     * jamais le montant négatif — on plafonne à zéro.
     */
    public Money appliqueRemise(Money remise) {
        exigerMemeDevise(remise);
        BigDecimal resultat = this.montant.subtract(remise.montant);
        if (resultat.signum() < 0) {
            resultat = BigDecimal.ZERO;
        }
        return new Money(resultat, this.devise);
    }

    private void exigerMemeDevise(Money autre) {
        if (!this.devise.equals(autre.devise)) {
            throw new IllegalArgumentException(
                    "Devises incompatibles : " + this.devise + " vs " + autre.devise);
        }
    }

    public BigDecimal montant() {
        return montant;
    }

    public Currency devise() {
        return devise;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return montant.compareTo(other.montant) == 0 && devise.equals(other.devise);
    }

    @Override
    public int hashCode() {
        return Objects.hash(montant.stripTrailingZeros(), devise);
    }

    @Override
    public String toString() {
        return montant.toPlainString() + " " + devise.getCurrencyCode();
    }
}
