# tic-tac-toe

Web server hosted tic-tac-toe game

## Installation

```bash
docker-compose build
docker-compose up -d postgres
lein flyway migrate
```


## Usage

Deleting and creating the postgres database
```bash
docker-compose down
docker volume rm ttt_database-data
docker-compose up -d postgres
lein flyway migrate
```
 
## Examples

...

