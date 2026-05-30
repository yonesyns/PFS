-- Table maintenance (interventions)
CREATE TABLE maintenances (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              vehicle_id UUID NOT NULL,
                              type VARCHAR(30) NOT NULL,
                              description VARCHAR(500) NOT NULL,
                              scheduled_date DATE NOT NULL,
                              completed_date DATE,
                              status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                              estimated_cost BIGINT,
                              actual_cost BIGINT,
                              mileage_at_maintenance BIGINT,
                              garage_name VARCHAR(100),
                              garage_contact VARCHAR(200),
                              parts_used VARCHAR(1000),
                              technician_name VARCHAR(100),
                              notes VARCHAR(2000),
                              cancellation_reason VARCHAR(500),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_maintenances_vehicle ON maintenances(vehicle_id);
CREATE INDEX idx_maintenances_status ON maintenances(status);
CREATE INDEX idx_maintenances_scheduled_date ON maintenances(scheduled_date);
CREATE INDEX idx_maintenances_type ON maintenances(type);

-- Table maintenance_plans (plans préventifs)
CREATE TABLE maintenance_plans (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   vehicle_id UUID NOT NULL,
                                   name VARCHAR(100) NOT NULL,
                                   interval_type VARCHAR(20) NOT NULL,
                                   interval_value INTEGER NOT NULL,
                                   last_done_date DATE,
                                   last_done_mileage BIGINT,
                                   next_due_date DATE,
                                   next_due_mileage BIGINT,
                                   alert_days_before INTEGER DEFAULT 30,
                                   is_active BOOLEAN DEFAULT true,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mplans_vehicle ON maintenance_plans(vehicle_id);
CREATE INDEX idx_mplans_active ON maintenance_plans(is_active);

-- Trigger pour updated_at
CREATE OR REPLACE FUNCTION update_maintenance_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_maintenances_updated_at
    BEFORE UPDATE ON maintenances
    FOR EACH ROW
    EXECUTE FUNCTION update_maintenance_updated_at();

CREATE TRIGGER update_maintenance_plans_updated_at
    BEFORE UPDATE ON maintenance_plans
    FOR EACH ROW
    EXECUTE FUNCTION update_maintenance_updated_at();