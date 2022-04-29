.PHONY: create-db
create-db:
	psql -h localhost -U postgres postgres -c "create database getclojure";
