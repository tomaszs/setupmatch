CREATE TABLE equipment (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    brand VARCHAR(128) NOT NULL,
    model VARCHAR(128) NOT NULL,
    state VARCHAR(32) NOT NULL,
    condition_score NUMERIC(4, 3) NOT NULL,
    purchase_date DATE NOT NULL,
    retire_reason TEXT,
    retired_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_equipment_condition CHECK (condition_score >= 0 AND condition_score <= 1)
);

CREATE INDEX idx_equipment_state_type ON equipment(state, type);

CREATE TABLE allocation_request (
    id UUID PRIMARY KEY,
    employee_id VARCHAR(128) NOT NULL,
    policy JSONB NOT NULL,
    state VARCHAR(32) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_allocation_request_state ON allocation_request(state);
CREATE INDEX idx_allocation_request_employee ON allocation_request(employee_id);

CREATE TABLE allocation_line (
    id UUID PRIMARY KEY,
    allocation_request_id UUID NOT NULL REFERENCES allocation_request(id) ON DELETE CASCADE,
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    policy_slot_index INT NOT NULL,
    CONSTRAINT uq_allocation_line_equipment UNIQUE (equipment_id)
);

CREATE INDEX idx_allocation_line_request ON allocation_line(allocation_request_id);
