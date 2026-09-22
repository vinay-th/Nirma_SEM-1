#!/bin/bash
# write a sh file for printing series of numbers multiplied by 5
read -p "Enter a number: " num;
res=$((num * 5))

for i in $(seq 1 $num); do
    echo "$res * 5 = $res"
    res=$((res * 5))
done
