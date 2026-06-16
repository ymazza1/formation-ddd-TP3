package com.akei.vente.domain;

/**
 * Value Object : référence article du catalogue AKEI.
 * <p>
 * Format métier : 3 lettres majuscules + '-' + 4 chiffres (ex. "CAN-0427").
 * Invariant porté par le constructeur : un Sku ne peut exister que s'il
 * respecte ce format. Impossible de construire un Sku invalide.
 */
public record Sku(String valeur) {

    private static final java.util.regex.Pattern FORMAT =
            java.util.regex.Pattern.compile("^[A-Z]{3}-\\d{4}$");

    public Sku {
        if (valeur == null) {
            throw new IllegalArgumentException("Un SKU ne peut pas être null");
        }
        if (!FORMAT.matcher(valeur).matches()) {
            throw new IllegalArgumentException(
                    "Format de SKU invalide : '" + valeur + "' (attendu : AAA-9999)");
        }
    }

    public static Sku of(String valeur) {
        return new Sku(valeur);
    }

    @Override
    public String toString() {
        return valeur;
    }
}
