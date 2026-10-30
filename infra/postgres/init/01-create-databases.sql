-- Per-service database isolation: 1 PostgreSQL process, multiple databases
-- Each service connects only to its own database (enforced via DB_URL env var per service)
CREATE DATABASE dawn_order;
CREATE DATABASE dawn_payment;
CREATE DATABASE dawn_inventory;
CREATE DATABASE dawn_catalog;
CREATE DATABASE dawn_user;
CREATE DATABASE dawn_auth;
CREATE DATABASE dawn_notification;
CREATE DATABASE dawn_media;
CREATE DATABASE dawn_shipping;
CREATE DATABASE dawn_rating;
CREATE DATABASE dawn_tax;
CREATE DATABASE dawn_wishlist;
CREATE DATABASE dawn_ai;

GRANT ALL PRIVILEGES ON DATABASE dawn_order TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_payment TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_inventory TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_catalog TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_user TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_auth TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_notification TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_media TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_shipping TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_rating TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_tax TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_wishlist TO admin;
GRANT ALL PRIVILEGES ON DATABASE dawn_ai TO admin;
