# Règles de code — JobTrack

## Général
- Langue du code : anglais (classes, méthodes, variables, packages).
- Langue de l'interface : français par défaut, i18n fr/en.
- Pas de secret ni de credential dans le code.
- Pas de `System.out.println` : utiliser SLF4J.
- Pas de dépendance circulaire entre packages.

## Backend Java
- Java 21, records pour les DTO et value objects.
- Injection par constructeur uniquement.
- `@Transactional` sur les cas d'usage en écriture, `readOnly = true` en lecture.
- Les contrôleurs ne contiennent pas de logique métier.
- Les entités JPA ne sortent jamais de la couche infrastructure.
- Validation Bean Validation sur les DTO d'entrée.
- Erreurs métier via exceptions dédiées converties en `ProblemDetail`.
- Nommage : `*Service`, `*Repository` (port), `*JpaRepository` (Spring Data), `*Adapter`, `*Controller`, `*Request`, `*Response`.
- Tests : un test par règle métier importante, tests d'intégration pour les endpoints critiques.

## Frontend React
- Composants fonctionnels, hooks, pas de classe.
- TypeScript strict, pas de `any` non justifié.
- TanStack Query pour tout appel réseau, clés de query centralisées.
- React Hook Form + Zod pour tous les formulaires.
- Tailwind uniquement, pas de CSS inline hors variables de thème.
- Accessibilité : labels, `aria-*`, focus visible, navigation clavier.
- Composants < 200 lignes si possible, découper par feature.
- Aucun appel `fetch` direct dans un composant : passer par `lib/api.ts` et les hooks de feature.

## Tests
- Backend : `mvn verify` doit passer.
- Frontend : `npm run lint`, `npm run test -- --run`, `npm run build` doivent passer.
- Les tests ne dépendent pas de données de production.

## Git / livraison
- README à jour avec lancement en une commande.
- Migrations Flyway immuables une fois livrées : ajouter une nouvelle migration.
- CI verte avant merge.
