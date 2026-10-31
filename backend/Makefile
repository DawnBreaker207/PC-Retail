.PHONY: build build-svc up start down clean logs ps docker-build

# Build all services (Maven)
build:
	./mvnw clean package -DskipTests

# Build single service: make build-svc SVC=dawn-catalog
build-svc:
	./mvnw clean package -DskipTests -pl $(SVC) -am

# Build and start all containers
up:
	docker compose up --build -d

# Start without rebuild
start:
	docker compose up -d

# Stop all
down:
	docker compose down

# Stop and remove volumes
clean:
	docker compose down -v

# Logs for a service: make logs SVC=dawn-order
logs:
	docker compose logs -f $(SVC)

# Status
ps:
	docker compose ps

# Build single service docker image: make docker-build SVC=dawn-catalog
docker-build:
	cd $(SVC) && docker build -t $(SVC):local .
