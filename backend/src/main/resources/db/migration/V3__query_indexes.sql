-- Equipment: support inventory filters (state, type) with created_at sort
DROP INDEX IF EXISTS idx_equipment_state_type;
CREATE INDEX idx_equipment_state_type_created_at ON equipment (state, type, created_at);

-- Equipment: default inventory list excludes retired rows
CREATE INDEX idx_equipment_active_created_at ON equipment (created_at)
    WHERE state IN ('available', 'reserved', 'assigned');

-- Equipment: full inventory list including retired
CREATE INDEX idx_equipment_created_at ON equipment (created_at);

-- Allocation requests: support list filters with created_at DESC sort
DROP INDEX IF EXISTS idx_allocation_request_state;
DROP INDEX IF EXISTS idx_allocation_request_employee;
CREATE INDEX idx_allocation_request_created_at ON allocation_request (created_at DESC);
CREATE INDEX idx_allocation_request_state_created_at ON allocation_request (state, created_at DESC);
CREATE INDEX idx_allocation_request_employee_state_created_at
    ON allocation_request (employee_id, state, created_at DESC);
