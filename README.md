# tic-tac-toe

Web server hosted tic-tac-toe game

## Installation

```bash
docker-compose build
docker-compose up -d postgres
lein flyway migrate
```


## Usage

Deleting and recreating the postgres database
```bash
docker-compose down
docker volume rm ttt_database-data
docker-compose up -d postgres
lein flyway migrate
```

Connecting to the database
```
docker-compose exec postgres psql -h localhost -p 5432 -U postgres tic_tac_toe
```
 
## TODO

- [] Add a UI! - Maybe using react?
- [] Add push notifications 
- [] Replace println statements with a logging library


