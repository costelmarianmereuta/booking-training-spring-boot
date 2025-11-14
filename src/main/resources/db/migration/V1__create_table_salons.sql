-- V1__create_table_salons.sql
-- V1__create_table_salons.sql
CREATE TABLE salons (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        public_id VARCHAR(36) NOT NULL UNIQUE,
                        name VARCHAR(120) NOT NULL,

    -- embedded Address fields
                        street        VARCHAR(255),
                        house_number  VARCHAR(10),
                        postal_box    VARCHAR(10),
                        postcode      VARCHAR(10) NOT NULL,
                        city          VARCHAR(120),

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP
);


CREATE TABLE categories (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL UNIQUE,
                                      description TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP,
                                      salon_id BIGINT NOT NULL,
                                      CONSTRAINT fk_category_salon
                                          FOREIGN KEY (salon_id) REFERENCES salons(id)


);

CREATE TABLE treatments (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(120) NOT NULL UNIQUE,
                            price DECIMAL(10,2) NOT NULL,
                            duration INT NOT NULL,
                            time_between_treatments INT DEFAULT 0,
                            description TEXT,
                            category_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP,
                            CONSTRAINT fk_category
                                FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE users (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         public_id VARCHAR(36) NOT NULL UNIQUE,

                         first_name VARCHAR(80) NOT NULL,
                         last_name VARCHAR(80) NOT NULL,

                         email VARCHAR(120) NOT NULL UNIQUE,
                         phone VARCHAR(20) UNIQUE,

                         birth_date DATE,
                         gender VARCHAR(10),

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);




