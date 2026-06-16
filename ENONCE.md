# TP3 — Validation Panier → Commande (itération 2)

**Durée indicative :** 1h – 1h15
**Contexte :** AKEI, Bounded Context de la **Vente**. On franchit l'étape clé du parcours : transformer un panier en commande.
**Stack :** Java 21, Maven, JUnit 5 + AssertJ. Tests fournis.

---

## Objectif pédagogique

Trois notions DDD réunies dans une seule opération métier :

1. **Deux aggregates distincts** : le `Panier` et la `Commande` ne sont pas le même objet. La validation *construit* une commande à partir du contenu du panier, à un instant T.
2. **Invariant temporel** : la disponibilité est vérifiée **à la validation**, pas à l'ajout (le stock a pu changer entre-temps — règle issue de l'Example Mapping).
3. **Domain Event** : la commande, en naissant, **enregistre** le fait `CommandePassee` (nommé au passé, immuable).

---

## Ce qui vous est fourni

- Tous les acquis TP1/TP2 : `Money`, `Quantity`, `Sku`, `LignePanier`, et le `Panier` (méthodes d'ajout/retrait/total déjà complètes).
- `Disponibilite` — **interface fournie** (le *port* : le besoin métier « est-ce disponible maintenant ? »). Vous n'avez pas à l'implémenter : les tests fournissent des doubles.
- `ArticleIndisponible` — **exception métier fournie**.
- `CommandePassee` et `Commande` — **à compléter** (cherchez les `// TODO`).
- `Panier.validerEnCommande(...)` — **à compléter**.
- Tests `ValidationCommandeTest` — **fournis**. `mvn test`.

---

## Travail demandé

### 1. `CommandePassee` (Domain Event)
- Valider les champs ; `depuis(commande)` fabrique l'événement (id, total, nb de lignes, `Instant.now()`).

### 2. `Commande` (aggregate cible)
- Naît cohérente : invariant **au moins une ligne**.
- Copie défensive des lignes.
- **Enregistre** un `CommandePassee` à la création.
- `total()` = somme des sous-totaux.
- Égalité **par identité**.

### 3. `Panier.validerEnCommande(Disponibilite)`
- Refuse un panier **vide** (`IllegalStateException`).
- Pour chaque ligne : si **non disponible** → `ArticleIndisponible(sku)`.
- Sinon, construit les `LigneCommande` et renvoie une `Commande`.

---

## Points de discussion (débrief)

- Pourquoi la disponibilité est-elle une **interface** (`Disponibilite`) placée dans le domaine, et non un appel direct à une base ou une API ? (Indice : *port* — on y revient au TP4 et au J3.)
- Pourquoi l'aggregate **enregistre** l'événement au lieu de le **publier** lui-même ? Qui devrait publier, et quand ?
- En quoi « le panier reste intact après validation » illustre-t-il que Panier et Commande sont deux aggregates **distincts** ?
- L'invariant « disponible à la validation » : pourquoi ne pas l'avoir vérifié dès l'ajout au panier ? Qu'est-ce que ça change pour le client ?

---

## Extension optionnelle (pour les plus rapides)

Ajoutez un second Domain Event ou enrichissez `CommandePassee` avec la liste des SKU commandés. Réfléchissez : quelles données un consommateur de l'événement (le contexte Stock, la Fidélité) aurait-il besoin de connaître **sans** avoir à recharger la commande entière ?

> Le **Repository** (persistance de la commande) et le **Domain Service** (disponibilité transverse, vraie implémentation du port) sont l'objet du **TP4**.
