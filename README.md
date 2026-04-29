# Moderation Service - SafeTag

## Rôle du service
Ce microservice est responsable de la modération des avis sur la plateforme SafeTag. 
Il gère le workflow de vérification (signalement, approbation, rejet) et maintient une trace d'audit complète des décisions de modération.

## Faits & Fonctionnalités
- **Modération automatique :** Vérification à la volée du contenu textuel (détection de mots bannis/insultes) avant même la création de l'avis.
- **Cycle de vie des avis :** Pilote la transition des statuts (`PENDING` -> `APPROVED` ou `REJECTED`).
- **Historisation :** Stocke chaque action dans une table `ModerationHistory` (Action, Raison, Opérateur, Timestamp).
- **Communication sortante :** Ordonne au `review-service` de modifier le statut des avis via des appels internes.

## Architecture & Communication
- **Gateway :** L'authentification est déléguée à la Gateway, qui injecte le rôle de l'utilisateur dans le header HTTP `X-User-Role`.
- **Review Service :** La communication interne se fait actuellement via `RestTemplate` (appels HTTP synchrones REST). 

*Hypothèse d'évolution :* Le passage de `RestTemplate` vers un Message Broker (ex: RabbitMQ) est documenté mais différé pour éviter l'over-engineering initial.

## Endpoints Principaux (API REST)

| Méthode | Endpoint | Rôles requis (Header) | Description |
|---|---|---|---|
| `POST` | `/api/v1/moderation/reviews/{reviewId}/approve` | `MODERATOR`, `ADMIN` | Valide un avis (déclenche l'appel au review-service) |
| `POST` | `/api/v1/moderation/reviews/{reviewId}/reject` | `MODERATOR`, `ADMIN` | Rejette un avis avec un motif |
| `POST` | `/api/v1/moderation/reviews/{reviewId}/report` | `USER` | Signale un avis (passe en `PENDING`) |
| `POST` | `/api/v1/moderation/text/check` | `USER` (ou public) | Vérifie un texte à la volée (détection de mots bannis) |


## Décisions Techniques (Decision Log)
1. **Sécurité pragmatique :** Pas de validation JWT dans ce service. Il fait confiance au header `X-User-Role` fourni par l'API Gateway de SafeTag.
2. **Séparation des données :** La donnée froide/historique reste ici (`ModerationHistory`), la donnée chaude (`Review`) reste dans le `review-service`.
