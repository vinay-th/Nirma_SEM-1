// Stack operation implementation using linked list
// Implement stack operations: 1) push, 2) pop, 3) peek using a linked list and demonstrate using an example.

#include <stdio.h>
#include <stdlib.h>

struct Node{
    int data;
    struct Node* next;
};

struct Node* top = NULL;

struct Node* createNode(int data){
    struct Node* newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->next = NULL;

    return newNode;
}

void push(int data){
    struct Node* newNode = createNode(data);
    if(top == NULL){
        top = newNode;
    } else {
        newNode->next = top;
        top = newNode;
    }
}

void pop(){
    if(top == NULL){
        printf("Stack is empty\n");
    } else {
        struct Node* temp = top;
        top = top->next;
        free(temp);
    }
}

void peek(){
    if(top == NULL){
        printf("Stack is empty\n");
    } else {
        printf("Top element is: %d\n", top->data);
    }
}

void display(){
    struct Node* temp = top;
    while(temp != NULL){
        printf("%d -> ", temp->data);
        temp = temp->next;
    }
    printf("NULL\n");
}

int main(){
    push(10);
    push(20);
    push(30);

    display(); // 30 -> 20 -> 10 -> NULL

    peek(); // Top element is: 30
    pop();

    display(); // 20 -> 10 -> NULL

    return 0;
}