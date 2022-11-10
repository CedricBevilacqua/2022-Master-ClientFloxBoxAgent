# Agent Flopbox

Altinkaynak Sema, Bevilacqua Cedric

## Ce qui a été implémenté

L'Agent Flopbox synchronise des serveurs FTP déclarés sur l'API Flopbox en local.

L'entiereté des fonctionalités demandées dans le sujet ont été implémentées : 
    - Les fichiers modifiés localement sont envoyés sur leur serveur FTP associé.
    - Les fichiers modifiés à distance sont rapatriés en local dans leur emplacement associé.
    - Les nouveaux fichiers créés localement sont ajoutés au serveur FTP associé.
    - Les fichiers supprimés en local sont déplacés dans le dossier "deleted" du serveur FTP associé.
    - Il est possible de lister à la manière de la commande Tree le contenu du dossier "deleted" des serveurs FTP.
    - Il est possible de restaurer un fichir contenu dans le répertoire "deleted" d'un serveur FTP.

Le code a également été testé et documenté.

## Utiliser le programme

#### Démarrer et configurer l'API Flopbox

Il est important d'exécuter en premier l'API Flopbox.

Il est nécessaire ensuite de créer un compte afin d'y ajouter un serveur, vous devez taper les commandes cURL suivantes : 

```
curl --location --request POST 'http://localhost:8080/v1/auths' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'username=toto' --data-urlencode 'password=toto'
```

Puis autant de fois cette commande pour chaque seveur à déclarer : 

```
curl --location --request POST 'http://localhost:8080/v1/servers' --header 'username: toto' --header 'password: toto' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'name=NOM_DU_SERVEUR' --data-urlencode 'LIEN_DU_SERVEUR' --data-urlencode 'port=21' --data-urlencode 'mode=passive'
```

Remplacez à chaque fois *'NOM_DU_SERVEUR'* et *'LIEN_DU_SERVEUR'* par les informations du serveur vous déclarez dans l'API.

Vous pouvez ensuite voir la liste des serveurs que vous avez déclaré avec la commande suivante : 

```
curl --location --request GET 'http://localhost:8080/v1/servers' --header 'username: toto' --header 'password: toto'
```

Et également lister la racine du serveur que vous souhaitez avec la commande suivante afin de vous assurer de la bonne configuration de l'API Flopbox : 

```
curl --location --request GET 'http://localhost:8080/v1/servers/NOM_DU_SERVEUR' --header 'username: toto' --header 'password: toto' --header 'FTPUsername: IDENTIFIANT_SERVEUR' --header 'FTPPassword: MOT_DE_PASSE_SERVEUR'
```

En remplaçant *'NOM_DU_SERVEUR'* ainsi que les identifiants *'IDENTIFIANT_SERVEUR'* et *'MOT_DE_PASSE_SERVEUR'*.

Plus d'informations sont disponibles sur l'API Flopbox dans la documentation de celle-ci fourni dans le dossier de l'API sur ce dépôt.

### Démarrer Agent Flopbox

Avant de démarrer l'Agent Flopbox, il faut en premier lieu configurer un fichier texte qui contiendra les identifians de chaque serveur. L'emplacement par défaut de ce fichier est à la racine du projet, son nom doit être *serverlist.txt*. Si vous souhaitez indiquer un emplacement différent, cela peut se préciser dans les arguments, voir plus loin.

Voici un exemple du squelette du fichier de configuration des serveurs, il faut indiquer pour chaque serveur son nom, son identifiant et son mot de passe : 

```
Name1
Username
Password
Name2
Username
Password
```

Une fois l'API Flopbox en cours d'exécution et les serveurs correctement déclarés, vous pouvez démarrer Agent Flopbox.

Pour cela, plusieurs arguments sont supportés : 

- Mode de démarrage : 
    - *run* : Pour importer les données des serveurs en local puis les synchroniser régulièrement.
    - *trash* : Pour afficher sous la forme d'un arbre le contenu de la corbeille de chaque serveur.
    - *restore* : Pour restaurer un fichier dans la corbeille d'un serveur. Dans ce cas, il faudra écrire dans la console le nom du serveur, l'adresse du fichier à restaurer dans la corbeille et l'adresse à laquelle enregistrer le fichier.
- Adresse du répertoire local dans lequel on enregistre les données locales.
- Identifiant du compte à utiliser sur l'API (facultatif, une valeur par défaut sera utilisée pour créer le compte).
- Mot de passe du compte à utiliser sur l'API (facultatif, une valeur par défaut sera utilisée pour créer le compte).
- Emplacement du fichier des identifiants serveur (facultatif, par défaut le fichier *serverlist* situé à la racine du projet est utilisé).

### Serveurs FTP

Pour configurer des serveurs FTP afin de tester l'application, il est possible d'utiliser le serveur WEBTP de l'université.

Voici le lien pour procéder à l'activation de votre serveur FTP personalisé : https://webtp.fil.univ-lille.fr

Sinon, vous pouvez utiliser le script Python ci-dessous. Attention, en ce qui nous concerne, ce script ne fonctionne que sur les ordinateurs de l'université, vous devez au préalable avoir installé *pyftpdlib*.

```python
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import FTPHandler
from pyftpdlib.servers import FTPServer

authorizer = DummyAuthorizer()
authorizer.add_user("byvoid", "pass", "path", perm="elradfmw")

handler = FTPHandler
handler.authorizer = authorizer

server = FTPServer(("localhost", 21), handler)
server.serve_forever()
```

## Fonctionnement

### Système de synchronisation

Au démarrage du programme, plusieurs actions sont effectuées : 
- Le compte est créé sur l'API.
- Le fichier des identifiants de chaque serveur est lu est interprété.
- Les instances d'objets sont créés pour chaque serveur.
- Les répertoires *delete* sont créés sur chaque serveur si ils n'existaient pas déjà.
- Un dossier est créé pour chaque serveur à l'emplacement local.
- Les données en ligne sont entièrement rapatriées en local.

Le système de synchronisation est exécuté au démarrage puis toutes les 30 secondes après le démarrage du programme en mode *run*.

A chaque synchronisation, les actions suivantes sont entreprises pour chaque serveur : 

- L'arborescence local et distant est entièrement parcouru en profondeur pour générer des listes contenant des paires pour chaque fichier (couple nom / timestamp de la date de modification) ainsi que des sous listes pour chaque sous dossier (chaque liste contient le nom du dossier en première entrée). Ces éléments sont stockés et seront utilisés pour toute la suite de la synchronisation afin de ne pas aller à chaque fois parcourir l'arborescence sur le serveur ou le système de fichier qui est plus lent.
- L'arborescence local est parcouru en profondeur par une fonctionn récursive qui va pour chaque élément vérifier si il est présent au même niveau de l'arborescencne distante. Pour chaque fichier ou dossier non présent, celui-ci est rajouté à une liste, ce sont les fichiers créés en local qui doivent être envoyés sur le serveur. Cette liste est ensuite parcourue et chaque fichier est envoyé sur le serveur.
- L'arborescence distant est cette fois parcouru afin d'identifier les fichiers présents en ligne qui ne sont pas présents en local. Une liste est alors créée puis parcourue. Chaque fichier est alors considéré comme supprimé puis déplacé dans le dossier *deleted*.
- L'arborescence local est parcouru et chaque élément est comparé avec son homologue dans l'arborescence distante. Le timestamp de ces deux éléments est alors comparé afin d'établir l'élément le plus récent, si ils ne sont pas identiques, alors le fichier a été modifié en local ou sur le serveur et sa dernière version est alors téléchargé ou envoyée.

### Conception objet

Aucun héritage n'a été utilisé dans ce projet. Deux package ont été créés : 

- Agent : Contient la classe *main* ainsi que les classes se chargeant de gérer les étapes de la synchronisation et de comparer les arborescences. Une classe statique sert également à stocker les identifiants du compte permettant de communiquer avec l'API Flopbox afin que cette donnée soit accessible partout à tout moment pour effectuer des requêtes.
- Commander : Contient deux classes permettant d'effectuer des actions sur les fichiers distants où locaux. On y retrouve des actions comme récupérer l'arborescence en le parcourant en profondeur, créer des dossiers, récupérer ou envoyer un fichier...

### Tests unitaires

Des tests unitaires ont été réalisés sur les fonctions internes de comparaison des arborescences et de la génération du tree sur le dossier *delete*.

Pour cela, des arborescences factices ont été instanciéés puis envoyées sur les méthodes de comparaison afin de tester le résultat de leur analyse.

Pour accéder à ces méthodes privées, certaines ont été passées en protected, des mock ont été crées afin de les rendre accessibles.