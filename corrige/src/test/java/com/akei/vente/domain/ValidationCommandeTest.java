package com.akei.vente.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TP3 — Validation Panier → Commande")
class ValidationCommandeTest {

    private static final Sku CANAPE = Sku.of("CAN-0427");
    private static final Sku CHAISE = Sku.of("CHA-0001");

    /** Test double : tout est disponible. */
    private static final Disponibilite TOUT_DISPONIBLE = (sku, q) -> true;
    /** Test double : rien n'est disponible. */
    private static final Disponibilite RIEN_DISPONIBLE = (sku, q) -> false;

    /** Test double paramétrable : seuls les SKU listés sont disponibles. */
    private static Disponibilite seulementDisponibles(Set<Sku> dispos) {
        return (sku, q) -> dispos.contains(sku);
    }

    @Nested
    @DisplayName("Cas nominal")
    class Nominal {

        @Test
        @DisplayName("un panier disponible produit une commande au bon total")
        void produitUneCommande() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(2), Money.euros("199.99"));
            panier.ajouterArticle(CHAISE, Quantity.of(1), Money.euros("49.50"));

            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);

            assertThat(commande.lignes()).hasSize(2);
            assertThat(commande.total()).isEqualTo(Money.euros("449.48"));
            assertThat(commande.id()).isNotNull();
        }

        @Test
        @DisplayName("la commande capture le prix au moment de la validation")
        void captureLePrix() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);
            assertThat(commande.total()).isEqualTo(Money.euros("199.99"));
        }
    }

    @Nested
    @DisplayName("Invariants à la validation")
    class Invariants {

        @Test
        @DisplayName("un panier vide ne peut pas être validé")
        void panierVideRefuse() {
            Panier panier = Panier.nouveau();
            assertThatThrownBy(() -> panier.validerEnCommande(TOUT_DISPONIBLE))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("un article indisponible bloque la validation")
        void articleIndisponibleBloque() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            assertThatThrownBy(() -> panier.validerEnCommande(RIEN_DISPONIBLE))
                    .isInstanceOf(ArticleIndisponible.class);
        }

        @Test
        @DisplayName("l'exception porte le SKU fautif")
        void exceptionPorteLeSku() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            panier.ajouterArticle(CHAISE, Quantity.of(1), Money.euros("49.50"));
            // seul le canapé est dispo : la chaise doit faire échouer
            assertThatThrownBy(() ->
                    panier.validerEnCommande(seulementDisponibles(Set.of(CANAPE))))
                    .isInstanceOf(ArticleIndisponible.class)
                    .extracting(e -> ((ArticleIndisponible) e).sku())
                    .isEqualTo(CHAISE);
        }

        @Test
        @DisplayName("la disponibilité est vérifiée à l'instant de la validation, pas à l'ajout")
        void disponibiliteVerifieeALaValidation() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            // À l'ajout, on ne consulte aucune disponibilité. C'est seulement
            // ici, à la validation, que le stock (devenu indisponible) bloque.
            assertThatThrownBy(() -> panier.validerEnCommande(RIEN_DISPONIBLE))
                    .isInstanceOf(ArticleIndisponible.class);
        }
    }

    @Nested
    @DisplayName("Domain Event")
    class Evenement {

        @Test
        @DisplayName("valider enregistre un événement CommandePassee")
        void enregistreCommandePassee() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(2), Money.euros("199.99"));
            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);

            assertThat(commande.evenements()).hasSize(1);
            assertThat(commande.evenements().get(0)).isInstanceOf(CommandePassee.class);
        }

        @Test
        @DisplayName("l'événement porte l'id, le total et le nombre de lignes de la commande")
        void evenementPorteLesDonnees() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(2), Money.euros("199.99"));
            panier.ajouterArticle(CHAISE, Quantity.of(1), Money.euros("49.50"));
            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);

            CommandePassee event = (CommandePassee) commande.evenements().get(0);
            assertThat(event.commandeId()).isEqualTo(commande.id());
            assertThat(event.total()).isEqualTo(Money.euros("449.48"));
            assertThat(event.nombreDeLignes()).isEqualTo(2);
            assertThat(event.survenuLe()).isNotNull();
        }

        @Test
        @DisplayName("vider les événements après publication")
        void viderEvenements() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);
            commande.viderEvenements();
            assertThat(commande.evenements()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Deux aggregates distincts")
    class AggregatesDistincts {

        @Test
        @DisplayName("le panier reste utilisable après validation (objets distincts)")
        void panierResteIntact() {
            Panier panier = Panier.nouveau();
            panier.ajouterArticle(CANAPE, Quantity.of(1), Money.euros("199.99"));
            Commande commande = panier.validerEnCommande(TOUT_DISPONIBLE);

            // La commande ne partage pas l'état du panier : modifier le panier
            // n'affecte pas la commande déjà créée.
            panier.ajouterArticle(CHAISE, Quantity.of(1), Money.euros("49.50"));
            assertThat(commande.lignes()).hasSize(1);
            assertThat(panier.nombreDeLignes()).isEqualTo(2);
        }
    }
}
