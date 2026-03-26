-- P0 业务表：检测记录、处理记录、生产线、摄像头、工位
CREATE TABLE detection_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    detection_no VARCHAR(64) NOT NULL UNIQUE,
    serial_no VARCHAR(64) NOT NULL,
    defect VARCHAR(255),
    defect_type VARCHAR(64),
    severity VARCHAR(32),
    confidence DOUBLE,
    position_x VARCHAR(32),
    position_y VARCHAR(32),
    area VARCHAR(32),
    impact_level VARCHAR(16),
    production_line VARCHAR(64),
    shift VARCHAR(64),
    model_name VARCHAR(64),
    status VARCHAR(16),
    process_status VARCHAR(16),
    status_note VARCHAR(255),
    image_url VARCHAR(255),
    timestamp DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE process_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    detection_id BIGINT NOT NULL,
    type VARCHAR(32),
    action VARCHAR(255),
    operator VARCHAR(64),
    note VARCHAR(255),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_process_records_detection FOREIGN KEY (detection_id) REFERENCES detection_records(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE production_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    location VARCHAR(128),
    shift VARCHAR(64),
    running_time VARCHAR(32),
    today_output INT,
    target_output INT,
    qualified_count INT,
    defect_count INT,
    yield_rate DOUBLE,
    utilization_rate DOUBLE,
    cycle_time DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cameras (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    production_line_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    position VARCHAR(64),
    online BOOLEAN NOT NULL,
    ip VARCHAR(64),
    CONSTRAINT fk_cameras_line FOREIGN KEY (production_line_id) REFERENCES production_lines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    production_line_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT fk_stations_line FOREIGN KEY (production_line_id) REFERENCES production_lines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
