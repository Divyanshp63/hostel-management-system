-- =============================================================================
-- Web-Based PG and Hostel Management System
-- Sample Demo & Evaluation Data Seed Script
-- Default Logins:
--   Admin:      admin@hostel.com      / Admin@123
--   Accountant: accountant@hostel.com / Accountant@123
--   Student 1:  rahul@hostel.com      / Student@123
--   Student 2:  priya@hostel.com      / Student@123
--   Student 3:  aman@hostel.com       / Student@123
-- =============================================================================

USE hostel_db;

-- 1. Insert Initial Users (BCrypt hashes: Admin@123, Accountant@123, Student@123)
-- Hash for 'Admin@123': $2a$10$7Z8q2U/3DqJ6e4Y7Z01cOu4gNqgKkMhZfC8k5p6G0z0A5uXyF9yC2 (or generated on fly by DataInitializer)
-- Using ON DUPLICATE KEY UPDATE so script is safely idempotent
INSERT INTO users (id, name, email, password, phone, role, enabled) VALUES
(1, 'Hostel Administrator', 'admin@hostel.com', '$2a$10$n8rU4e9L7bQzW1oK.XyVreKqjL2xHj8zFqQ9xZlE9bO2cO5oR4u8y', '9876543210', 'ADMIN', true),
(2, 'Hostel Accountant', 'accountant@hostel.com', '$2a$10$n8rU4e9L7bQzW1oK.XyVreKqjL2xHj8zFqQ9xZlE9bO2cO5oR4u8y', '9876543211', 'ACCOUNTANT', true),
(3, 'Rahul Sharma', 'rahul@hostel.com', '$2a$10$n8rU4e9L7bQzW1oK.XyVreKqjL2xHj8zFqQ9xZlE9bO2cO5oR4u8y', '9876543212', 'STUDENT', true),
(4, 'Priya Patel', 'priya@hostel.com', '$2a$10$n8rU4e9L7bQzW1oK.XyVreKqjL2xHj8zFqQ9xZlE9bO2cO5oR4u8y', '9876543213', 'STUDENT', true),
(5, 'Aman Gupta', 'aman@hostel.com', '$2a$10$n8rU4e9L7bQzW1oK.XyVreKqjL2xHj8zFqQ9xZlE9bO2cO5oR4u8y', '9876543214', 'STUDENT', true)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Student Profiles
INSERT INTO students (id, user_id, admission_number, course, branch, year_of_study, date_of_birth, gender, address, emergency_contact, guardian_name, guardian_phone) VALUES
(1, 3, 'HOSTEL-2026-0001', 'B.Tech', 'Computer Science', 3, '2004-05-15', 'MALE', '124 Civil Lines, Jaipur, Rajasthan', '9876543212', 'Rajesh Sharma', '9876500001'),
(2, 4, 'HOSTEL-2026-0002', 'B.Tech', 'Information Technology', 2, '2005-08-20', 'FEMALE', '54 Navrangpura, Ahmedabad, Gujarat', '9876543213', 'Kirit Patel', '9876500002'),
(3, 5, 'HOSTEL-2026-0003', 'MCA', 'Computer Applications', 1, '2003-11-10', 'MALE', '89 Aliganj, Lucknow, UP', '9876543214', 'Suresh Gupta', '9876500003')
ON DUPLICATE KEY UPDATE admission_number=VALUES(admission_number);

-- 3. Insert Rooms
INSERT INTO rooms (id, room_number, block_name, floor, capacity, occupied, room_type, rent_per_month, status, amenities) VALUES
(1, 'A-101', 'Block A', 1, 2, 1, 'DOUBLE', 6500.00, 'AVAILABLE', 'Attached Washroom, Wi-Fi, Balcony'),
(2, 'A-102', 'Block A', 1, 1, 0, 'SINGLE', 9500.00, 'AVAILABLE', 'AC, Attached Washroom, Refrigerator'),
(3, 'B-201', 'Block B', 2, 2, 1, 'DOUBLE', 6000.00, 'AVAILABLE', 'Wi-Fi, Study Desk, Geyser'),
(4, 'B-202', 'Block B', 2, 3, 0, 'TRIPLE', 4500.00, 'AVAILABLE', 'Wi-Fi, Geyser, Spacious Cupboards'),
(5, 'C-301', 'Block C', 3, 4, 0, 'FOUR_SHARING', 3800.00, 'AVAILABLE', 'Budget Friendly, High-Speed Wi-Fi'),
(6, 'C-302', 'Block C', 3, 2, 2, 'DOUBLE', 6000.00, 'FULL', 'Attached Washroom, Garden View')
ON DUPLICATE KEY UPDATE room_number=VALUES(room_number);

-- 4. Insert Room Allocations
INSERT INTO room_allocations (id, student_id, room_id, start_date, status, remarks) VALUES
(1, 1, 1, '2026-08-01', 'ACTIVE', 'Allocated by Warden'),
(2, 2, 3, '2026-08-01', 'ACTIVE', 'Allocated by Warden'),
(3, 3, 2, '2026-10-01', 'PENDING', 'Awaiting single room approval')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 5. Insert Fees
INSERT INTO fees (id, student_id, month, room_rent, mess_fee, electricity_fee, maintenance_fee, total_amount, paid_amount, remaining_amount, due_date, status) VALUES
(1, 1, '2026-09', 6500.00, 2500.00, 500.00, 500.00, 10000.00, 10000.00, 0.00, '2026-09-15', 'PAID'),
(2, 1, '2026-10', 6500.00, 2500.00, 500.00, 500.00, 10000.00, 5000.00, 5000.00, '2026-10-15', 'PARTIALLY_PAID'),
(3, 2, '2026-10', 6000.00, 2500.00, 500.00, 500.00, 9500.00, 9500.00, 0.00, '2026-10-15', 'PAID')
ON DUPLICATE KEY UPDATE month=VALUES(month);

-- 6. Insert Payments
INSERT INTO payments (id, fee_id, student_id, amount, payment_method, payment_status, transaction_id, payment_date) VALUES
(1, 1, 1, 10000.00, 'UPI', 'SUCCESS', 'TXN-20260905102030-9812', '2026-09-05 10:20:30'),
(2, 2, 1, 5000.00, 'DEBIT_CARD', 'SUCCESS', 'TXN-20261001143015-4421', '2026-10-01 14:30:15'),
(3, 3, 2, 9500.00, 'UPI', 'SUCCESS', 'TXN-20261002164500-1129', '2026-10-02 16:45:00')
ON DUPLICATE KEY UPDATE transaction_id=VALUES(transaction_id);

-- 7. Insert Complaints
INSERT INTO complaints (id, student_id, room_id, title, description, category, status, resolution_notes) VALUES
(1, 1, 1, 'Water leakage in bathroom tap', 'The main sink tap in washroom is leaking continuously.', 'PLUMBING', 'RESOLVED', 'Plumber replaced washer on 02-Oct.'),
(2, 1, 1, 'Wi-Fi router signal weak in corner', 'Unable to connect to 5GHz Wi-Fi network near study table.', 'INTERNET_WIFI', 'IN_PROGRESS', 'Network engineer assigned to add extender.'),
(3, 2, 3, 'Study chair leg cracked', 'The wooden leg of chair has developed a crack.', 'FURNITURE', 'PENDING', NULL)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 8. Insert Visitor Requests
INSERT INTO visitor_requests (id, student_id, visitor_name, visitor_phone, relation_to_student, visit_date, purpose, status) VALUES
(1, 1, 'Rajesh Sharma', '9876500001', 'Father', '2026-10-04', 'Dropping off winter clothes and books', 'APPROVED'),
(2, 2, 'Sneha Patel', '9876500004', 'Sister', '2026-10-06', 'Family meetup on weekend', 'PENDING')
ON DUPLICATE KEY UPDATE visitor_name=VALUES(visitor_name);

-- 9. Insert Leave Requests
INSERT INTO leave_requests (id, student_id, leave_type, from_date, to_date, total_days, reason, contact_number, status, admin_remarks) VALUES
(1, 1, 'HOME_VISIT', '2026-10-20', '2026-10-25', 6, 'Diwali festival family gathering', '9876543212', 'APPROVED', 'Granted for Diwali vacation.'),
(2, 2, 'MEDICAL', '2026-10-08', '2026-10-10', 3, 'Dental appointment and treatment', '9876543213', 'PENDING', NULL)
ON DUPLICATE KEY UPDATE reason=VALUES(reason);

-- 10. Insert Notices
INSERT INTO notices (id, title, content, priority, target_audience, active) VALUES
(1, 'Diwali Grand Feast & Mess Timings', 'Special festive dinner will be served on Diwali evening including Paneer Tikka, Biryani, and Gulab Jamun. Mess timings: 07:30 PM to 10:30 PM.', 'HIGH', 'ALL', true),
(2, 'Wi-Fi Maintenance & Core Switch Upgrade', 'Campus internet backbone will undergo scheduled maintenance this Saturday between 02:00 AM and 05:00 AM.', 'MEDIUM', 'ALL', true),
(3, 'Hostel Gate Curfew Reminder', 'All residents are strictly reminded that the hostel main gate closes at 10:00 PM. Late entries require prior warden approval.', 'HIGH', 'STUDENTS_ONLY', true)
ON DUPLICATE KEY UPDATE title=VALUES(title);
