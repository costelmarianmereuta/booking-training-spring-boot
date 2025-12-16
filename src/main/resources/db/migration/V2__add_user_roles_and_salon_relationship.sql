-- V2__add_user_roles_and_salon_relationship.sql

-- Add salon_id column to users table (nullable, for employees/managers)
ALTER TABLE users
ADD COLUMN salon_id BIGINT NULL;

-- Add foreign key constraint for salon_id
ALTER TABLE users
ADD CONSTRAINT fk_user_salon
    FOREIGN KEY (salon_id) REFERENCES salons(id);

-- Create user_roles table for many-to-many relationship between users and roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create employee_working_hours table for employee working hours
CREATE TABLE employee_working_hours (
    user_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    PRIMARY KEY (user_id, day_of_week),
    CONSTRAINT fk_employee_working_hours_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);




