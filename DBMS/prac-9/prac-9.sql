-- Perform the DCL commands (grant, revoke):
-- Create a student table with the following fields: StudentCode, Name, Dateofbirth, Course,
-- RollNo, Batch, Result.

Create table student_grant(
    StudentCode number(4),
    Name varchar2(8),
    Dateofbirth date,
    Course varchar2(8),
    RollNo varchar2(8),
    Batch varchar2(8),
    Result varchar2(8)
);

-- 1. User Moksh can retrieve the entire table
CREATE USER Moksh IDENTIFIED BY password;
GRANT SELECT ON student_grant TO Moksh;
-- 2. User Naitali can insert and delete the entire table.
CREATE USER Naitali IDENTIFIED BY password;
GRANT INSERT, DELETE ON student_grant TO Naitali;
-- 3. Every user can retrieve his/her record only.
GRANT SELECT ON student_grant TO PUBLIC;
-- 4. User Vishwas can retrieve the entire table and update on Course and RollNo only
CREATE USER Vishwas IDENTIFIED BY password;
GRANT SELECT, UPDATE (Course, RollNo) ON student_grant TO Vishwas;
-- 5. User Rajvi can retrieve over Name, StudentCode, and Result only.
CREATE USER Rajvi IDENTIFIED BY password;
CREATE VIEW abc AS SELECT Name, StudentCode, Result FROM student_grant;
GRANT SELECT ON abc TO Rajvi;
-- 6. User Meet can retrieve as T and update as N
CREATE USER Meet IDENTIFIED BY password;
GRANT CREATE SESSION TO Meet;

CREATE VIEW student_select AS SELECT * FROM student_grant;
CREATE VIEW student_update AS SELECT * FROM student_grant;
GRANT SELECT ON student_select TO Meet;
GRANT UPDATE ON student_update TO Meet;
-- 7. User Naushid can all the privileges for BE-IT student records.
CREATE USER Naushid IDENTIFIED BY password;
CREATE VIEW be_it_students AS SELECT * FROM student_grant WHERE Course = 'BE-IT';
GRANT SELECT, INSERT, UPDATE, DELETE ON be_it_students TO Naushid;
-- 8. User Sagar can delete records for students of Batch B2
CREATE USER Sagar IDENTIFIED BY password;
CREATE VIEW batch_b2_students AS SELECT * FROM student_grant WHERE Batch = 'B2';
GRANT DELETE ON batch_b2_students TO Sagar;
-- 9. GRANT ALL ON Student TO Sagar WHERE Course = 'BE-IT';
CREATE OR REPLACE FORCE VIEW grant_all_be_it AS SELECT * FROM student_grant WHERE Course = 'BE-IT';
GRANT UPDATE, DELETE ON grant_all_be_it TO Sagar;
-- 10. User Shani can update and delete students record of courses where there are no more than 5 student
CREATE USER Shani IDENTIFIED BY password;
CREATE OR REPLACE FORCE VIEW course_students AS SELECT * FROM student_grant WHERE Course IN (SELECT Course FROM student_grant GROUP BY Course HAVING COUNT(*) > 5);
GRANT UPDATE, DELETE ON course_students TO Shani;
-- 11. User Sujal can retrieve the Eldest and youngest student
CREATE USER Sujal IDENTIFIED BY password;
CREATE OR REPLACE FORCE VIEW eldest_student AS SELECT * FROM student_grant WHERE Dateofbirth = (SELECT MIN(Dateofbirth) FROM student_grant);
CREATE OR REPLACE FORCE VIEW youngest_student AS SELECT * FROM student_grant WHERE Dateofbirth = (SELECT MAX(Dateofbirth) FROM student_grant);
GRANT SELECT ON eldest_student TO Sujal;
GRANT SELECT ON youngest_student TO Sujal;