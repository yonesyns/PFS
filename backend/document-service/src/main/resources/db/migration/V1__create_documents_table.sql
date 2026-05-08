CREATE TABLE documents (
                           id BIGSERIAL PRIMARY KEY,
                           owner_type VARCHAR(50) NOT NULL,
                           owner_id BIGINT NOT NULL,
                           document_type VARCHAR(50) NOT NULL,
                           file_name VARCHAR(255) NOT NULL,
                           file_url VARCHAR(500),
                           expiration_date DATE,
                           status VARCHAR(50) NOT NULL,
                           created_at TIMESTAMP,
                           updated_at TIMESTAMP
);

CREATE INDEX idx_documents_owner ON documents(owner_type, owner_id);
CREATE INDEX idx_documents_expiration_date ON documents(expiration_date);
CREATE INDEX idx_documents_status ON documents(status);