CREATE TABLE user_departments (
    user_id UUID NOT NULL,
    department_id UUID NOT NULL,
    PRIMARY KEY (user_id, department_id),
    CONSTRAINT fk_user_departments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_departments_department FOREIGN KEY (department_id) REFERENCES departments(id)
);
