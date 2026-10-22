#!/bin/bash
# a) Write a shell script to print the reverse of an input number (do not use REV command

read -p "Enter a number: " num

reverse=0
rem=0

while [ $num -gt 0 ]
do
    rem=$((num % 10))
    reverse=$((reverse * 10 + rem))
    num=$((num / 10))
done

echo "The reverse of the number is: $reverse"