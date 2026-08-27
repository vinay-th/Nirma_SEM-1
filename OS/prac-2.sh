#!/bin/sh
read -p "Enter first number: " a
read -p "Enter second number: " b

echo "\nTo perform following operations->\n"
echo "1.Addition"
echo "2.Subtraction"
echo "3.Multiplication"
echo "4.Division \n"
read -p "Enter the number of operation: " opr

case $opr in
1)  
    echo "Addition is : $(($a + $b))"
    ;;
2)
    echo "Subtraction is : $(($a - $b))"
    ;;
3)
    echo "Multiplication is : $(($a * $b))"
    ;;
4)
    if [ $b -eq 0 ]; then
        echo "Division by zero is not allowed"
    else
        echo "Division is : $(($a / $b))"
    fi
    ;;
*) 
    echo "The operation is invalid."
    ;;
esac