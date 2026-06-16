package com.akei.vente.domain;

/**
 * Exception métier levée quand la validation du panier échoue parce qu'un
 * article n'est plus disponible dans la quantité demandée à l'instant T.
 * <p>
 * On lève une exception métier explicite (et non un IllegalArgumentException
 * générique) : l'échec fait partie du langage du domaine. Le SKU fautif est
 * porté pour que l'appelant (couche application / UI) puisse informer le client.
 */
public class ArticleIndisponible extends RuntimeException {

    private final Sku sku;

    public ArticleIndisponible(Sku sku) {
        super("Article indisponible pour le SKU " + sku);
        this.sku = sku;
    }

    public Sku sku() {
        return sku;
    }
}
