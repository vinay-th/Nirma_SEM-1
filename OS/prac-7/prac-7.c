// Write a program to implement the following Unix command in C language
// using command line arguments
// a) cp
// b) cat
#include <stdio.h>

int main(int argc, char *argv[]) {
    if (argc < 3) {
        printf("Usage: %s <source_file> <destination_file>\n", argv[0]);
        return 1;
    }

    FILE *source, *destination;
    char ch;

    source = fopen(argv[1], "r");
    if (source == NULL) {
        perror("Error opening source file");
        return 1;
    }

    destination = fopen(argv[2], "w");
    if (destination == NULL) {
        perror("Error opening destination file");
        fclose(source);
        return 1;
    }

    while ((ch = fgetc(source)) != EOF) {
        fputc(ch, destination);
    }

    printf("File copied successfully from %s to %s\n", argv[1], argv[2]);

    fclose(source);
    fclose(destination);

    return 0;
}