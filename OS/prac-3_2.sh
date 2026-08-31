#!/bin/sh

for i in {1..9}; do
    for j in {0..9}; do
        for k in {0..9}; do
            if [ $k -eq $i ] || [ $k -eq $j ]; then
                continue
            fi
            echo "$i$j$k"
        done
    done
done