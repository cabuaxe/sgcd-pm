-- Composite index for common task queries filtered by sprint and status
CREATE INDEX idx_task_sprint_status ON tasks (sprint_id, status);
