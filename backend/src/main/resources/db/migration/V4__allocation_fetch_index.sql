-- Allocation fetch: available rows filtered by type, min condition, ordered by id (FOR UPDATE)
CREATE INDEX idx_equipment_available_type_condition_id
    ON equipment (type, condition_score, id)
    WHERE state = 'available';
