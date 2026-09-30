// Write a program to implement a call management system for
// incoming calls at a call center. The first call received is the
// first to be connected. Use an appropriate data structure to
// implement the system.
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Calls {
    int call_id;
    char caller_name[50];
    struct Calls* next;
};

struct Calls* front = NULL;
struct Calls* rear = NULL;

void addCall(int id, char name[]){
    struct Calls* newCall = (struct Calls*)malloc(sizeof(struct Calls));
    newCall->call_id = id;
    strcpy(newCall->caller_name, name);
    newCall->next = NULL;

    if (rear == NULL) {
        front = rear = newCall;
    } else {
        rear->next = newCall;
        rear = newCall;
    }
}

void connectCall() {
    if (front == NULL) {
        printf("No calls to connect.\n");
        return;
    }

    struct Calls* temp = front;
    printf("Connecting call ID: %d, Caller Name: %s\n", temp->call_id, temp->caller_name);
    front = front->next;

    if (front == NULL) {
        rear = NULL;
    }

    free(temp);
}

void displayCalls() {
    if (front == NULL) {
        printf("No incoming calls.\n");
        return;
    }

    struct Calls* temp = front;
    printf("Incoming Calls:\n");
    while (temp != NULL) {
        printf("Call ID: %d, Caller Name: %s\n", temp->call_id, temp->caller_name);
        temp = temp->next;
    }
}

int main() {
    int choice, id;
    char name[50];

    while (1) {
        printf("\nCall Management System\n");
        printf("1. Add Call\n");
        printf("2. Connect Call\n");
        printf("3. Display Calls\n");
        printf("4. Exit\n");
        printf("Enter your choice: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                printf("Enter Call ID: ");
                scanf("%d", &id);
                printf("Enter Caller Name: ");
                scanf("%s", name);
                addCall(id, name);
                break;
            case 2:
                connectCall();
                break;
            case 3:
                displayCalls();
                break;
            case 4:
                exit(0);
            default:
                printf("Invalid choice. Please try again.\n");
        }
    }

    return 0;
}