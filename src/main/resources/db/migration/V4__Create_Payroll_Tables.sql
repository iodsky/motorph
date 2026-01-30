CREATE TABLE IF NOT EXISTS pagibig_contribution (
    id UUID PRIMARY KEY,
    employee_rate DECIMAL(5,4) NOT NULL, -- 2%
    employer_rate DECIMAL(5,4) NOT NULL, -- 2%
    low_income_threshold NUMERIC DEFAULT 1500.00,
    low_income_employee_rate DECIMAL(5,4) DEFAULT 0.01,
    max_salary_cap NUMERIC NOT NULL DEFAULT 10000.00,
    effective_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    last_modified_by UUID,
    version BIGINT
);

CREATE TABLE IF NOT EXISTS philhealth_contribution (
    id UUID PRIMARY KEY,
    premium_rate DECIMAL(5, 4) NOT NULL, -- 0.05 (5%)
    max_salary_cap NUMERIC NOT NULL DEFAULT 100000.00,
    min_salary_floor NUMERIC NOT NULL DEFAULT 10000.00,
    fixed_contribution NUMERIC NOT NULL DEFAULT 500.00,
    effective_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    last_modified_by UUID,
    version BIGINT
);

CREATE TABLE sss_contribution (
    id UUID PRIMARY KEY,
    total_sss NUMERIC NOT NULL,
    employee_sss DECIMAL(5,4) NOT NULL,
    employer_sss DECIMAL(5,4) NOT NULL,
    salary_brackets JSONB NOT NULL,
    effective_date DATE NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    last_modified_by UUID,
    version BIGINT
);


CREATE TABLE IF NOT EXISTS income_tax_bracket (
    id UUID PRIMARY KEY,
    min_income NUMERIC NOT NULL,
    max_income NUMERIC,
    base_tax NUMERIC NOT NULL DEFAULT 0.00,
    marginal_rate DECIMAL(5,4) NOT NULL, -- e.g., 0.15
    threshold NUMERIC NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    last_modified_by UUID,
    version BIGINT,
    UNIQUE (effective_date, min_income)
);
