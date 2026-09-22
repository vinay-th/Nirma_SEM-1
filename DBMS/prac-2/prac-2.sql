-- SQL Where
-- =========
-- 1. Select all records where the Ciry column has the value "Ahmedabad".
SELECT * FROM CUSTOMER WHERE CITY = 'Ahmedabad';
-- 2. Use the NOT keyword to select all records where Ciry is NOT "Ahmedabad".
SELECT * FROM CUSTOMER WHERE CITY != 'Ahmedabad';
-- 3. Select all records where the comm column has the value 11.11.
 SELECT * FROM SALESPEOPLE WHERE COMM = 11.11;
-- 4. Select all records where the Ciry column has the value 'Ahmedabad' and the comm column has
-- the value 25.52.
 SELECT * FROM SALESPEOPLE WHERE CITY = 'Ahmedabad' AND COMM = 11.11;
-- 5. Select all records where the Ciry column has the value 'Ahmedabad' or 'Kosamba'
 SELECT * FROM SALESPEOPLE WHERE CITY = 'Ahmedabad' OR  CITY = '';


-- SQL Order By
-- ==========
-- 1. Select all records from the salespeople table, sort the result alphabetically by the column Ciry.
 SELECT * FROM SALESPEOPLE ORDER BY CITY;
-- 2. Select all records from the salespeople table, sort the result reversed alphabetically by the column
-- Ciry.
 SELECT * FROM SALESPEOPLE ORDER BY CITY DESC;
-- 3. Select all records from the salespeople table, sort the result alphabetically, first by the column CIRY,
-- then by the column comm.
 SELECT * FROM SALESPEOPLE ORDER BY CITY, SNAME;



--  SQL Null
-- =======
-- 1. Select all records from the salespeople where the CIRY column is empty.
SELECT * FROM SALESPEOPLE WHERE CITY IS NULL;
-- 2. Select all records from the salespeople where the salespeople where the CIRY column is NOT
-- empty.
SELECT * FROM SALESPEOPLE WHERE CITY IS NOT NULL;

-- SQL Update
-- =========
-- 1. Update the Ciry column of all records in the salespeople table.
UPDATE SALESPEOPLE SET CITY = 'Helsinki';
-- 2. Set the value of the Ciry columns to 'Surat', but only the ones where the Comm column has the
-- value 88.99.
UPDATE SALESPEOPLE SET CITY = 'Surat' WHERE COMM=89.99;
-- 3. Update the salesperson name value and the Ciry value for the perticualr salesperson
UPDATE SALESPEOPLE SET CITY = 'Anand', SNAME = 'Vivek' WHERE SNUM = 103; 

-- SQL Delete
-- =========
-- 1. Delete all the records from the orders table where the onum value is 305.
DELETE FROM ORDERS WHERE ONUM = 305;
-- 2. Delete all the records from the orders table.
DELETE FROM ORDERS;