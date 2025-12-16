-- V5__make_employee_id_not_null.sql

-- Make employee_id NOT NULL since employee is required for all reservations
ALTER TABLE reservations
ALTER COLUMN employee_id SET NOT NULL;




