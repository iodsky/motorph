INSERT INTO department (id, title, created_at, updated_at, version)
VALUES ('IT', 'INFORMATION TECHNOLOGY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HR', 'HUMAN RESOURCES', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CORP', 'CORPORATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACC', 'ACCOUNTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PAY', 'PAYROLL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('SAL', 'SALES', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('LOG', 'LOGISTICS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CS', 'CUSTOMER SERVICE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO position (id, title, department_id, created_at, updated_at, version)
VALUES ('CEO', 'Chief Executive Officer', 'CORP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('COO', 'Chief Operating Officer', 'CORP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CFO', 'Chief Finance Officer', 'CORP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CMO', 'Chief Marketing Officer', 'CORP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CSR', 'Customer Service and Relations', 'CS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ITOPSYS', 'IT Operations and Systems', 'IT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HRM', 'HR Manager', 'HR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HRTL', 'HR Team Leader', 'HR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HRRL', 'HR Rank and File', 'HR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACCHEAD', 'Accounting Head', 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACCMNGR', 'Account Manager', 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACCTL', 'Account Team Leader', 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('ACCRL', 'Account Rank and File', 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PAYRMNGR', 'Payroll Manager', 'PAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PAYRL', 'Payroll Rank and File', 'PAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PAYTL', 'Payroll Team Leader', 'PAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('SCL', 'Supply Chain and Logistics', 'LOG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('SLMKT', 'Sales and Marketing', 'SAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO deduction_type (code, type, created_at, updated_at, version)
VALUES ('SSS', 'Social Security System', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PHIC', 'PhilHealth', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HDMF', 'Pag-IBIG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('TAX', 'Withholding Tax', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO benefit_type (id, type, created_at, updated_at, version)
VALUES ('MEAL', 'MEAL ALLOWANCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PHONE', 'PHONE ALLOWANCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('CLOTHING', 'CLOTHING ALLOWANCE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee (id, first_name, last_name, birthday, address, phone_number, created_at, updated_at, version)
VALUES (10000, 'Super', 'User', '1990-01-15', '123 Test Street, Manila, Philippines', '+639171234567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO government_id (id, employee_id, sss_no, tin_no, philhealth_no, pagibig_no, created_at, updated_at, version)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 10000, '34-1234567-8', '123-456-789-000', '12-345678901-2', '1234-5678-9012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employment_details (id, employee_id, supervisor_id, position_id, department_id, status, created_at, updated_at, version)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 10000, NULL, 'ITOPSYS', 'IT', 'REGULAR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO compensation (id, employee_id, basic_salary, hourly_rate, semi_monthly_rate, created_at, updated_at, version)
VALUES ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 10000, 50000.00, 297.62, 25000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (role, created_at, updated_at, version)
VALUES ('IT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('HR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('PAYROLL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('EMPLOYEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (role) DO NOTHING;

INSERT INTO users (id, employee_id, email, password, role_id, created_at, updated_at, version)
VALUES ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 10000, 'superuser@motorph.com', '$2a$12$lMIUx49rQdGhsrfLbQB3Hetueio4UgmdWV/Vcw3KweucDgZ6fDs/a', 'IT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;
