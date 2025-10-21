-- V1__create_table_salons.sql
CREATE TABLE salons (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        public_id VARCHAR(36) NOT NULL UNIQUE,
                        name VARCHAR(120) NOT NULL,

    -- champs de l'@Embedded Address, avec préfixe "address_"
                        address_street        VARCHAR(255),
                        address_house_number  VARCHAR(10),
                        address_postal_box    VARCHAR(10),
                        address_postcode      VARCHAR(10) NOT NULL,
                        address_city          VARCHAR(120),

                        created_at TIMESTAMP NOT NULL
);
