-- Write a code in PL/SQL TO create a trigger that automatically updates a &#39;last_modified; timestamp whenever a row in a specific table is updated.

CREATE TABLE employee (
employee_id NUMBER PRIMARY KEY,
first_name VARCHAR2(50),
last_name VARCHAR2(50),
last_modified TIMESTAMP
);

CREATE OR REPLACE TRIGGER update_last_modified
BEFORE UPDATE ON employee
FOR EACH ROW
BEGIN
    :NEW.last_modified := SYSTIMESTAMP;
END;
/


-- TEST 1
INSERT INTO employee (employee_id, first_name, last_name) VALUES (1, 'Vinay', 'Thakor', SYSTIMESTAMP);
UPDATE employee SET first_name = 'Niomi' WHERE employee_id = 1;