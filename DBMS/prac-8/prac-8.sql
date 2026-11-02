-- (A). Write a PL SQL program that demonstrates the concept of table-based records.
DECLARE
    -- (A) Table-based record declaration
    emp_record employee%ROWTYPE;
BEGIN
    emp_record.EMP_ID := 101;
    emp_record.EMP_NAME := 'Vinay Thakor';
    emp_record.DEPT_ID := 5001;
    emp_record.SALARY := 50000;

    INSERT INTO EMPLOYEE VALUES emp_record;

    DBMS_OUTPUT.PUT_LINE('Record inserted successfully: ' || emp_record.EMP_ID || ', ' || emp_record.EMP_NAME || ', ' || emp_record.DEPT_ID || ', ' || emp_record.SALARY);
END;
/
-- (B). Write a PL SQL program that demonstrates the concept of cursor-based records.
DECLARE 
    CURSOR emp_cursor IS
        SELECT EMP_ID, EMP_NAME, DEPT_ID, SALARY FROM EMPLOYEE;
    emp_record emp_cursor%ROWTYPE;
BEGIN
    OPEN emp_cursor;
    LOOP
        FETCH emp_cursor INTO emp_record;
        EXIT WHEN emp_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Employee ID: ' || emp_record.EMP_ID || ', Name: ' || emp_record.EMP_NAME || ', Department ID: ' || emp_record.DEPT_ID || ', Salary: ' || emp_record.SALARY);
    END LOOP;
    CLOSE emp_cursor;
END;
/