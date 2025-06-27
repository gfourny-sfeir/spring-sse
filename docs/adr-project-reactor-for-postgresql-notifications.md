# ADR: Utilisation de Project Reactor pour la diffusion des notifications PostgreSQL

## Statut
Accepté

## Contexte
Notre application nécessite un mécanisme pour écouter et réagir aux notifications PostgreSQL. Ces notifications sont émises lorsque des changements se produisent dans la base de données et doivent être transmises à plusieurs composants de l'application.

Les approches traditionnelles présentent plusieurs inconvénients :
- Créer une connexion PostgreSQL distincte pour chaque composant qui souhaite écouter les notifications
- Utiliser un modèle de polling qui interroge régulièrement la base de données pour détecter les changements
- Implémenter un mécanisme de distribution personnalisé qui pourrait être complexe à maintenir

## Décision
Nous avons décidé d'utiliser Project Reactor (version 3.7.6), une bibliothèque de programmation réactive, pour diffuser les notifications PostgreSQL à plusieurs abonnés (Subscribers) tout en n'utilisant qu'une seule connexion à la base de données.

### Implémentation
1. Un seul thread dédié (`listenerThread`) est créé pour écouter les notifications PostgreSQL
2. Ce thread établit une unique connexion à la base de données et s'abonne aux canaux de notification spécifiés
3. Les notifications reçues sont publiées sur des `Sinks.Many` de Project Reactor (un par canal)
4. Les composants de l'application (comme `ProductListener`) peuvent s'abonner à ces flux (Flux) pour recevoir les notifications sans créer de nouvelles connexions
5. Les notifications sont transformées en objets métier (comme `Product`) pour être utilisées par l'application
6. Ces flux d'objets métier sont exposés aux clients via Server-Sent Events (SSE) en utilisant le `ProductEmitter`

Le flux de données complet est le suivant :
1. PostgreSQL émet une notification sur un canal spécifique
2. `PostgreSQLNotificationService` reçoit cette notification via une unique connexion
3. La notification est publiée sur le `Sink` correspondant au canal
4. `ProductListener` transforme la notification en objet métier `Product`
5. `ProductController` expose un endpoint SSE qui utilise l'interface `EmitProduct` pour convertir le flux de `Product` en événements SSE
6. Les clients web reçoivent les mises à jour en temps réel via la connexion SSE

### Avantages techniques
- **Efficacité des ressources** : Une seule connexion à la base de données est utilisée, peu importe le nombre d'abonnés
- **Réactivité** : Les notifications sont diffusées de manière asynchrone et non-bloquante
- **Backpressure** : Project Reactor gère naturellement la backpressure, protégeant l'application contre les surcharges
- **Découplage** : Les producteurs et consommateurs de notifications sont découplés

## Conséquences
### Positives
- Réduction significative de l'utilisation des ressources de la base de données
- Simplification de la gestion des connexions
- Meilleure réactivité de l'application
- Facilité d'ajout de nouveaux consommateurs sans impact sur les performances
- Code plus maintenable et testable grâce au paradigme réactif

### Négatives
- Introduction d'une dépendance à Project Reactor
- Courbe d'apprentissage pour les développeurs non familiers avec la programmation réactive
- Nécessité de gérer correctement le cycle de vie du thread d'écoute

## Alternatives considérées
1. **Connexions multiples** : Chaque composant établit sa propre connexion pour écouter les notifications
   - Rejeté en raison de la consommation excessive de ressources et des limitations potentielles du nombre de connexions

2. **Polling périodique** : Interroger régulièrement la base de données pour détecter les changements
   - Rejeté en raison de la latence introduite et de la charge inutile sur la base de données

3. **Message broker externe** (comme RabbitMQ ou Kafka)
   - Rejeté pour cette fonctionnalité spécifique car cela introduirait une complexité et une dépendance supplémentaires alors que PostgreSQL offre déjà un mécanisme de notification natif
