## Introduction 

Ce projet contient le code fait pour le tp noté scalix.


## Dependencies
Dans ce projet, nous utilisons : 

- **scala** *3.3.4* (scala *3.3.1* crée une erreur lors de l'utilisation de json4s)
- **org.json4s** *4.0.7*

## Structure globale
Ce projet contient 4 répertoires :

- le répertoire **data** qui contient toutes les classes représentant les données (comme les acteurs, les films, le cache). Ce répertoire est principalement utile pour la question 5

- le répertoire **cache** contient les json correspondant aux caches secondaires utilisés dans la question 4.

- le répertoire **cache2** : idem mais pour la question 5 cette fois

- le répertoire **request** contient les fichiers qui vont intéragir avec TMDB. API_json4 parle avec l'api et les fichiers Request héritent de API_json.

- le répertoire **main** contient les fichiers à lancer pour tester l'application. Il héritent de App.

## Questions

### Questions 3
Les fichiers utiles qui ont été utilisés pour la question 3 sont : request.API_json.scala, request.Request.scala, data.FullName et main.TMDBTestNoCache.

Le fait que Request héritent API_json permet de factoriser le code puisque nous allons définir plusieurs points d'entrées.
De plus Request est une classe puisqu'on veut pouvoir l'instancier avec différentes apiKey et différents cache_folder_path. Chaque utilisateur doit avoir sa key et un cache privé.

### Question 4 
Les fichiers utiles qui ont été utilisés pour la question 3 sont : request.API_json.scala, request.RequestJSONCache.scala, data.FullName, les json dans cache, et main.TMDBCache.

Le fait d'hériter API_json permet de ne pas réécrire le code des méthodes et uniquement d'ajouter la gestion des caches.

### Question 5
Les fichiers utiles qui ont été utilisés pour la question 3 sont : request.API_json.scala, request.RequestObjectInCache.scala, les json dans cache2, toutes les classes dans data et main.TMDBCache.

De la même manière que précédemment, nous avons juste besoin d'implémenter le cache.
Les caches sont gérés par une instance de Cache (chaque utilisateur a son cache). Nous avons décidé d'implémenter un cache héritant de l'interface CacheInterface. En effet, nous pouvons imaginer plusieurs types de cache qui gèrent différents types de données (par exemple, on pourrait refaire la question 4 en implémentant un cache qui étant CacheInterface).
Ainsi, on peut ajouter différents type de cache dans RequestObjectInCache tant que ce cache implémente CacheInterface.

Enfin, le fait d'utiliser des objets comme Acteur ou Movie permet de manipuler les données de manière plus complexe. Par exemple, on pourrait ajouter des filtres dans la classe Acteur (pour changer le format des noms dès l'obtention par exemple). On aurait aussi pu lier les classes Acteurs et movie en ajoutant des instances d'acteurs dans la classe Movie.