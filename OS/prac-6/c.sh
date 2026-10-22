#!/bin/bash
# c) Write a shell script to concatenate all the given files into a single 7file

read -p "Enter the names of files (space-separated): " files

for file in $files; do
    if [[ -f $file ]]; then
        cat "$file" >> output.txt
    else
        echo "File '$file' does not exist."
    fi
done

echo "All files have been concatenated into output.txt"