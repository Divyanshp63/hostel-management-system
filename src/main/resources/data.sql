-- =============================================================================
-- Smart PG & Hostel Management System
-- Test Accounts and Initial Setup Seed Script
-- Role-based test credentials:
--   Warden:             warden@smarthostel.com     / Warden@123
--   Accountant:         accountant@smarthostel.com / Accountant@123
--   Student:            student@smarthostel.com    / Student@123
--   Complaint Staff:    complaint@smarthostel.com  / Complaint@123
-- =============================================================================

USE hostel_db;

-- 1. Insert Initial Rooms
INSERT INTO rooms (id, room_number, block_name, floor, capacity, occupied, room_type, rent_per_month, status, description) VALUES
(1, 'A-101', 'Block A', 1, 2, 1, 'DOUBLE', 6000.00, 'AVAILABLE', 'Double sharing room with attached balcony and study desks'),
(2, 'A-102', 'Block A', 1, 1, 0, 'SINGLE', 8500.00, 'AVAILABLE', 'Single room with AC and attached washroom'),
(3, 'B-201', 'Block B', 2, 2, 0, 'DOUBLE', 5500.00, 'AVAILABLE', 'Double room on second floor with high-speed Wi-Fi'),
(4, 'B-202', 'Block B', 2, 3, 0, 'TRIPLE', 4500.00, 'AVAILABLE', 'Triple sharing room with spacious storage lockers')
ON DUPLICATE KEY UPDATE room_number=VALUES(room_number);

-- 2. Insert Users (BCrypt Hashes)
-- Hash for Warden@123: $2a$10$wKxN0sL.8nK9zZ9gL2v8aeXfXgXj3xKzP8sZ7bL9aK0zZ9gL2v8ae (DataInitializer ensures live BCrypt hashes)
INSERT INTO users (id, name, email, password, phone, role, enabled) VALUES
(1, 'Chief Warden', 'warden@smarthostel.com', '$2a$10$kP7vX7U5uU97cK0o5t9mje0mI2bH8X2gKzJ6kF/7aO2yC8uI5jF4K', '9876543210', 'WARDEN', true),
(2, 'Hostel Accountant', 'accountant@smarthostel.com', '$2a$10$kP7vX7U5uU97cK0o5t9mje0mI2bH8X2gKzJ6kF/7aO2yC8uI5jF4K', '9876543211', 'ACCOUNTANT', true),
(3, 'Student Resident', 'student@smarthostel.com', '$2a$10$kP7vX7U5uU97cK0o5t9mje0mI2bH8X2gKzJ6kF/7aO2yC8uI5jF4K', '9876543212', 'STUDENT', true),
(4, 'Maintenance Staff', 'complaint@smarthostel.com', '$2a$10$kP7vX7U5uU97cK0o5t9mje0mI2bH8X2gKzJ6kF/7aO2yC8uI5jF4K', '9876543215', 'COMPLAINT_STAFF', true)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Insert Staff Profiles
INSERT INTO staff (id, user_id, employee_id, department, designation, hostel_assignment) VALUES
(1, 1, 'WRD-001', NULL, 'Chief Warden', 'Main Hostel Campus'),
(2, 2, 'ACC-001', NULL, 'Chief Accountant', NULL),
(3, 4, 'CMP-001', 'MAINTENANCE', 'Senior Maintenance Staff', NULL)
ON DUPLICATE KEY UPDATE employee_id=VALUES(employee_id);

-- 4. Insert Student Profile
INSERT INTO students (id, user_id, admission_number, college, course, branch, year_of_study, date_of_birth, gender, blood_group, hostel_name, address, emergency_contact, guardian_name, guardian_phone) VALUES
(1, 3, 'STU-2026-001', 'Apex Engineering Institute', 'B.Tech Computer Science', 'CSE', '3', '2004-05-15', 'MALE', 'O+', 'Block A', '104 Park Avenue, City Center', '9876500001', 'Ramesh Kumar', '9876500001')
ON DUPLICATE KEY UPDATE admission_number=VALUES(admission_number);

-- 5. Insert Initial Allocation
INSERT INTO room_allocations (id, student_id, room_id, bed_number, status, request_date, start_date, remarks) VALUES
(1, 1, 1, 'Bed 1', 'ACTIVE', '2026-08-01', '2026-08-01', 'Initial resident allocation')
ON DUPLICATE KEY UPDATE status=VALUES(status);
