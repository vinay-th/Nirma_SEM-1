#!/bin/bash
echo "1. Navigate Directory"
echo "2. List Contents"
echo "3. Create Directory"
echo "4. Modify"
echo "5. Exit"
read -p "Select an option (1-5): " choice


case $choice in
   1)
       read -p "Enter Path: " path
       cd "$path"
       echo "Current path: $(pwd)"
       ;;
   2)
       echo "Directory Contents"
       ls
       ;;
   3)
       read -p "Enter new directory name: " dname
       mkdir "$dname"
       echo "Directory created."
       ;;
   4)
       read -p "Enter your old Directory name:" d1
       read -p "Enter your new Directory name:" d2
       mv "$d1" "$d2"
       echo "Changed Directory Name"
       ;;
   5)
       echo "Exit"
       exit 0
       ;;
   *)
       echo "Invalid"
       ;;
esac
