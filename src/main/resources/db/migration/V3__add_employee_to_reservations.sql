-- V3__add_employee_to_reservations.sql

-- Add employee_id column to reservations table (nullable, for the employee performing the treatment)
ALTER TABLE reservations
ADD COLUMN employee_id BIGINT NULL;

-- Add foreign key constraint for employee_id
ALTER TABLE reservations
ADD CONSTRAINT fk_reservation_employee
    FOREIGN KEY (employee_id) REFERENCES users(id);




