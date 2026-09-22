CREATE TABLE SALESPEOPLE(SNUM NUMBER(4) PRIMARY KEY,SNAME VARCHAR2(20),CITY VARCHAR2(15),COMM NUMBER(5,2));

-- salespeople data

INSERT INTO salespeople VALUES (101, 'Vinay Thakor','Ahmedabad',5.00);
INSERT INTO salespeople VALUES (102, 'Dhyey Chaudhry','Dubai',4.75);
INSERT INTO salespeople VALUES (103, 'Umang Panchal','Bangalore', 5.20);
INSERT INTO salespeople VALUES (104, 'Vinay Thakor', 'Hyderabad', 4.90);
INSERT INTO salespeople VALUES (105, 'Dhruv Jayswal', 'Ahmedabad', 5.10);
INSERT INTO salespeople VALUES (106, 'Riya Patel', 'Baroda', 5.05);
INSERT INTO salespeople VALUES (107, 'Darji Smit', 'Kolkata', 4.80);
INSERT INTO salespeople VALUES (108, 'Apoorva Chaudhry', 'Wasington DC', 5.15);
INSERT INTO salespeople VALUES (109, 'Nandani Goswami', 'Somnath', 5.00);
INSERT INTO salespeople VALUES (110, 'Krishna Panchal', 'Surat', 6.95);

-- CUSTOMER table create

CREATE TABLE CUSTOMER (CNUM NUMBER(4) PRIMARY KEY, CNAME VARCHAR2(20), CITY VARCHAR2(15), RATING NUMBER(4), SNUM NUMBER(4) REFERENCES SALESPEOPLE);

-- CUSTOMER data populate

INSERT INTO CUSTOMER VALUES (201, 'Rajanikant', 'Ahmedabad', 1, 101);
INSERT INTO CUSTOMER VALUES (202, 'Mohanlal', 'Bhopal', 2, 102);
INSERT INTO CUSTOMER VALUES (203, 'Sanjay', 'Ankleshwar', 3, 102);
INSERT INTO CUSTOMER VALUES (204, 'Allu', 'Surathkal', 4, 101);
INSERT INTO CUSTOMER VALUES (205, 'Priya', 'Delhi', 5, 103);
INSERT INTO CUSTOMER VALUES (206, 'Rohit', 'Mumbai', 3, 104);
INSERT INTO CUSTOMER VALUES (207, 'Neha', 'Chennai', 2, 105);
INSERT INTO CUSTOMER VALUES (208, 'Amit', 'Kolkata', 4, 106);
INSERT INTO CUSTOMER VALUES (209, 'Kavita', 'Hyderabad', 5, 107);
INSERT INTO CUSTOMER VALUES (210, 'Vikram', 'Jaipur', 1, 108);
INSERT INTO CUSTOMER VALUES (211, 'Sonia', 'Pune', 2, 109);
INSERT INTO CUSTOMER VALUES (212, 'Deepak', 'Ahmedabad', 3, 110);
INSERT INTO CUSTOMER VALUES (213, 'Anjali', 'Bengaluru', 4, 101);
INSERT INTO CUSTOMER VALUES (214, 'Manish', 'Lucknow', 5, 102);
INSERT INTO CUSTOMER VALUES (215, 'Ritu', 'Indore', 1, 103);
INSERT INTO CUSTOMER VALUES (216, 'Arjun', 'Patna', 2, 104);
INSERT INTO CUSTOMER VALUES (217, 'Shreya', 'Nagpur', 3, 105);
INSERT INTO CUSTOMER VALUES (218, 'Kiran', 'Bhubaneswar', 4, 106);
INSERT INTO CUSTOMER VALUES (219, 'Nisha', 'Coimbatore', 5, 107);
INSERT INTO CUSTOMER VALUES (220, 'Rahul', 'Guwahati', 1, 108);

-- ORDERS table

CREATE TABLE ORDERS(ONUM NUMBER(4) PRIMARY KEY,AMT NUMBER(8,2),ODATE DATE, CNUM NUMBER(4) REFERENCES CUSTOMER, SNUM NUMBER(4) REFERENCES SALESPEOPLE);

-- ORDER data populate

INSERT INTO ORDERS VALUES (301,  9050.0, TO_DATE('02-JAN-2026','DD-MON-YYYY'), 201, 101);
INSERT INTO ORDERS VALUES (302, 15000.0, TO_DATE('15-FEB-2025','DD-MON-YYYY'), 202, 104);
INSERT INTO ORDERS VALUES (303, 14000.0, TO_DATE('14-FEB-2024','DD-MON-YYYY'), 203, 103);
INSERT INTO ORDERS VALUES (304, 1800.0, TO_DATE('15-FEB-2023','DD-MON-YYYY'), 204, 102);
INSERT INTO ORDERS VALUES (305,  80000.0, TO_DATE('10-MAR-2026','DD-MON-YYYY'), 205, 105);
INSERT INTO ORDERS VALUES (306, 1100.0, TO_DATE('22-MAR-2025','DD-MON-YYYY'), 206, 106);
INSERT INTO ORDERS VALUES (307, 1300.0, TO_DATE('05-APR-2026','DD-MON-YYYY'), 207, 107);
INSERT INTO ORDERS VALUES (308,  9500.0, TO_DATE('18-APR-2024','DD-MON-YYYY'), 208, 108);
INSERT INTO ORDERS VALUES (309, 1400.0, TO_DATE('30-APR-2026','DD-MON-YYYY'), 209, 109);
INSERT INTO ORDERS VALUES (310, 1600.0, TO_DATE('12-MAY-2026','DD-MON-YYYY'), 210, 110);
INSERT INTO ORDERS VALUES (311,  7500.0, TO_DATE('25-MAY-2026','DD-MON-YYYY'), 211, 101);
INSERT INTO ORDERS VALUES (312,  950.0, TO_DATE('07-JUN-2025','DD-MON-YYYY'), 212, 102);
INSERT INTO ORDERS VALUES (313, 1150.0, TO_DATE('20-JUN-2024','DD-MON-YYYY'), 213, 103);
INSERT INTO ORDERS VALUES (314, 1350.0, TO_DATE('02-JUL-2023','DD-MON-YYYY'), 214, 104);
INSERT INTO ORDERS VALUES (315, 1550.0, TO_DATE('15-JUL-2021','DD-MON-YYYY'), 215, 105);

-- Write a query that produces the salesperson table with the columns in the following order: city, sname, snum, comm.

SELECT CITY, SNAME, SNUM, COMM FROM SALESPEOPLE;

-- Write a SELECT command that produces the order number, amount, and date for all rows  in the order table

SELECT ONUM, AMT, ODATE FROM ORDERS;

-- Write a query that produces all rows from the customer table for which the salesperson’s number is 101.

SELECT * FROM CUSTOMER WHERE SNUM = 101;

-- Write a query to display the only salesman no from the orders table.

SELECT DISTINCT SNUM FROM ORDERS;

-- Write a query that will give you all orders for more than Rs. 100/-.

SELECT * FROM ORDERS WHERE AMT > 100;

-- Write a query that produces all customers serviced by salespeople who is located in Belgaum with a commission above 10%. Output the salespeople's name and city.

SELECT C.CNAME, C.CITY
FROM CUSTOMER C
where C.SNUM IN (SELECT SNUM FROM SALESPEOPLE WHERE CITY = 'Belgaum' AND COMM > 10);

-- Write a query on the Customers table whose output will exclude all customers with a rating <>1; they are not located in Ahmedabad.

SELECT * FROM CUSTOMER WHERE RATING <> 1 AND CITY <> 'Ahmedabad';

-- Write a query that will produce all orders taken on October 14th Feb 2004 or 15th Feb 2005.

SELECT * FROM ORDERS WHERE ODATE IN (TO_DATE('14-FEB-2004','DD-MON-YYYY'), TO_DATE('15-FEB-2005','DD-MON-YYYY'));
