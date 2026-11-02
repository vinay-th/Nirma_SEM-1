-- Create table
CREATE TABLE Department (
    DeptID NUMBER PRIMARY KEY,
    DeptName VARCHAR2(50)
);
-- Create table
CREATE TABLE Employee (
    EmpID NUMBER PRIMARY KEY,
    EmpName VARCHAR2(50),
    DeptID NUMBER,
    Salary NUMBER,
    FOREIGN KEY (DeptID) REFERENCES Department(DeptID)
);

-- Insert sample data into Department table
INSERT INTO Department (DeptID, DeptName) VALUES (1, 'HR');
INSERT INTO Department (DeptID, DeptName) VALUES (2, 'Finance');
INSERT INTO Department (DeptID, DeptName) VALUES (3, 'IT');

-- Insert sample data into Employee table
INSERT INTO Employee (EmpID, EmpName, DeptID, Salary) VALUES (101, 'Moksh', 1, 50000);
INSERT INTO Employee (EmpID, EmpName, DeptID, Salary) VALUES (102, 'Naitali', 2, 60000);
INSERT INTO Employee (EmpID, EmpName, DeptID, Salary) VALUES (103, 'Rajvi', 3, 70000);

-- Joins on Tables: (considering the Practical-1 Tables peform the below joins)
-- 1. Equi Join (List customer names along with their salesperson names
-- (matching snum).)

SELECT e.EmpName AS EmployeeName, d.DeptName AS DepartmentName
FROM Employee e JOIN Department d ON e.DeptID = d.DeptID;

-- 2. Self Join (List pairs of salespeople who are in the same city but are not the same person.)

SELECT e1.EmpName AS Employee1, e2.EmpName AS Employee2
FROM Employee e1, Employee e2
WHERE e1.DeptID = e2.DeptID AND e1.EmpID <> e2.EmpID;

-- 3. Natural Join (List order details along with customer information (joins
-- automatically on same column names like cnum or snum).)

SELECT EmpName, DeptName
FROM Employee NATURAL JOIN Department;

-- 4. Theta Join (List orders where the order amount is greater than 20,000,
-- along with salesperson name.)

SELECT e.EmpName, d.DeptName
FROM Employee e JOIN Department d ON e.DeptID = d.DeptID
WHERE e.Salary > 20000;

-- Use of Set operators: (considering the Practical-1 Tables peform the below set
-- operators)
-- 1. Union (Get a list of all cities where either a salesperson or a customer is located (duplicates removed).)

SELECT DISTINCT DeptName AS City FROM Department
UNION 
SELECT DISTINCT EmpName AS City FROM Employee;

-- 2. Intersect (Get a list of cities that are common to both salespeople and
-- customers.)

SELECT DISTINCT DeptName AS City FROM Department
INTERSECT
SELECT DISTINCT EmpName AS City FROM Employee;

-- 3. Minus (Except) (Get a list of cities where salespeople are located but no
-- customer is located.)

SELECT DISTINCT DeptName AS City FROM Department
MINUS
SELECT DISTINCT EmpName AS City FROM Employee;