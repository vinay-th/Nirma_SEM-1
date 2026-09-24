#!/bin/bash

if [ ! -f employees.txt ]; then
cat > employees.txt <<EOF
Emp_ID|Emp_Name|Designation|Department|Salary|Age
EOF
fi

while true
do
echo "
1. Insert Data
2. View All Records
3. Search Data
4. Sort File
5. Modify Data
6. Delete Record
7. Exit
"

read -p "Enter your choice: " ch

case $ch in

1)
read -p "Enter Employee ID: " id
read -p "Enter Employee Name: " name
read -p "Enter Designation: " designation
read -p "Enter Department: " department
read -p "Enter Salary: " salary
read -p "Enter Age: " age
cat >> employees.txt <<EOF
$id|$name|$designation|$department|$salary|$age
EOF
echo "Record inserted successfully."
;;

2)
cat employees.txt
;;

3)
read -p "Enter Employee ID or Name to search: " search
grep -i "$search" employees.txt
;;

4)
(head -n 1 employees.txt && tail -n +2 employees.txt | sort -t'|' -k1,1n) > temp.txt
cat temp.txt > employees.txt
rm temp.txt
cat employees.txt
;;

5)
read -p "Enter Employee ID to modify: " id

record=$(grep "^$id|" employees.txt)

if [ -z "$record" ]; then
echo "Record not found."
else
read -p "Enter New Employee Name: " name
read -p "Enter New Designation: " designation
read -p "Enter New Department: " department
read -p "Enter New Salary: " salary
read -p "Enter New Age: " age

grep -v "^$id|" employees.txt > temp.txt

cat >> temp.txt <<EOF
$id|$name|$designation|$department|$salary|$age
EOF

cat temp.txt > employees.txt
rm temp.txt

echo "Record modified successfully."
fi
;;

6)
read -p "Enter Employee ID to delete: " id

if grep -q "^$id|" employees.txt
then
grep -v "^$id|" employees.txt > temp.txt
cat temp.txt > employees.txt
rm temp.txt
echo "Record deleted successfully."
else
echo "Record not found."
fi
;;

7)
exit
;;

*)
echo "Invalid choice."
;;

esac
done