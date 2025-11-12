-- V1__create_table_salons.sql
CREATE TABLE salons (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        public_id VARCHAR(36) NOT NULL UNIQUE,
                        name VARCHAR(120) NOT NULL,

    -- champs de l'@Embedded Address, avec préfixe "address_"
                        street        VARCHAR(255),
                        house_number  VARCHAR(10),
                        postal_box    VARCHAR(10),
                        postcode      VARCHAR(10) NOT NULL,
                        city          VARCHAR(120),

                        created_at TIMESTAMP NOT NULL
);
