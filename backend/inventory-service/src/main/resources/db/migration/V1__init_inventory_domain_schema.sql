-- SmartQueue AI - Inventory Service Schema Migration V1

CREATE TABLE IF NOT EXISTS resources (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_resources_name ON resources(name);
CREATE INDEX idx_resources_type ON resources(type);
CREATE INDEX idx_resources_status ON resources(status);

CREATE TABLE IF NOT EXISTS resource_locks (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    lock_key VARCHAR(255) NOT NULL UNIQUE,
    locked_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_resource_locks_resource_id ON resource_locks(resource_id);
CREATE INDEX idx_resource_locks_user_id ON resource_locks(user_id);
CREATE INDEX idx_resource_locks_lock_key ON resource_locks(lock_key);
CREATE INDEX idx_resource_locks_status ON resource_locks(status);

CREATE TABLE IF NOT EXISTS inventory_audits (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    details TEXT,
    performed_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_inventory_audits_resource_id ON inventory_audits(resource_id);
CREATE INDEX idx_inventory_audits_action ON inventory_audits(action);
CREATE INDEX idx_inventory_audits_created_at ON inventory_audits(created_at);
