# 📖 Configurer PostgreSQL

### 💾 Paramétrer votre BDD

2 scripts sont à initialiser :

- `src/main/resources/db/migration/V1__ddl.sql :` script d'initialisation de la BDD utilisé à la fois par **Flyway** pour initialiser la 1ère version de votre schéma au démarrage de l'API, et également pour initialiser votre **BDD locale conteneurisée**.

- `src/test/resources/IT_datas.sql :` script pour construire vos jeux de données, ce script est utilisé pour votre **BDD local conteneurisée** et pour les **TIs**.

Le port exposé par votre BDD locale est le **port 5432**.
Vous aurez besoin d'ajouter dans votre fichier C:\Windows\System32\drivers\etc\hosts :

```
127.0.0.1	postgres
```
#### 🐳 Démarrage de la BDD locale avec Docker

- Démarrage
```sh
docker-compose up -d
```

- Arrêt
```sh
docker-compose down
```

- Suppression des volumes pour repartir avec une BDD vierge
```sh
docker-compose down -v
```
### ⚙️ Administrer votre BDD avec pgAdmin

* Si vous ne possédez pas la version Ultimate de Intellij ou si vous préférez tout simplement, vous pouvez utiliser **pgAdmin** à  la place du **Database** de Intellij.

* Le port par défaut est le **port 8888**. Une fois votre **docker** lancé,

vous avez juste à aller sur votre navigateur à l'adresse suivante : http://localhost:8888.

* Cliquez sur Add New Server :

![pgAdmin_new_server.png](docs/pgadmin/pgAdmin_new_server.png)

* Renseignez un nom de server.

* Voici les informations de connexion à saisir dans l'onglet connexion :

![pgAdmin_connection.png](docs/pgadmin/pgAdmin_connection.png)
