# Moderation Service 🛡️

## Rôle
Ce microservice gère la modération des contenus sur la plateforme SafeTag. Il centralise les signalements des utilisateurs et l'application des décisions de modération (ex: validation ou rejet d'un avis).

## Responsabilités
- **Signalements** : Réception et stockage des rapports émis par les utilisateurs.
- **Décisions** : Traitement des actions de modération.
- **Synchronisation** : Mise à jour de l'état des contenus dans les autres services.

## Architecture & Communication

- **API Publique** : Accessible via la Gateway (`/api/v1/moderation/**`).
- **Communication inter-services** : 
  - Actuellement : Appels HTTP synchrones (via `RestTemplate`) vers les APIs internes des autres services (ex: `/api/internal/reviews/{id}/reject` sur le `review-service`).
  - *Évolution prévue* : Transition vers un Message Broker pour assurer un couplage lâche et asynchrone.
- **Sécurité** : Les actions de modération nécessitent une validation des droits (via la Gateway/JWT).

## Endpoints Principaux (Exemples)
- `POST /api/v1/moderation/reports` : Soumettre un signalement (Utilisateur).
- `POST /api/v1/moderation/reviews/{id}/reject` : Appliquer un rejet sur un avis (Modérateur).

## Configuration
- **Port local** : `8083`
- **Variables d'environnement requises** :
  - `REVIEW_SERVICE_URL` : URL de base pour joindre le Review Service (ex: `http://localhost:8083`).
