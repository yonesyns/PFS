CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number VARCHAR(20) NOT NULL UNIQUE,
    vin VARCHAR(17) NOT NULL UNIQUE,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    fuel_type VARCHAR(20) NOT NULL DEFAULT 'DIESEL',
    mileage BIGINT,
    registration_date DATE,
    insurance_expiry_date DATE,
    technical_inspection_date DATE,
    customer_id UUID,
    gps_device_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vehicles_customer ON vehicles(customer_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_insurance ON vehicles(insurance_expiry_date);
CREATE INDEX idx_vehicles_inspection ON vehicles(technical_inspection_date);

CREATE OR REPLACE FUNCTION update_vehicles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_vehicles_updated_at 
    BEFORE UPDATE ON vehicles 
    FOR EACH ROW 
    EXECUTE FUNCTION update_vehicles_updated_at();
