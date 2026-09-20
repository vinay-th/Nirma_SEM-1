// Queue operation implementation using linked list
// Implement queue operations: 1) enqueue, 2) dequeue, 3) traverse the queue using a linked list.

#include <stdio.h>
#include <stdlib.h>

struct Node{
    int data;
    struct Node* next;
};

struct Node* head = NULL;

struct Node* createNode(int data){
    struct Node* newNode = (struct Node*) malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->next = NULL;

    return newNode;
}

void enqueue(int data){
    struct Node* newNode = createNode(data);
    if(head == NULL){
        head = newNode;
    } else {
        struct Node* ptr = head;
        while(ptr->next != NULL){
            ptr = ptr->next;
        }
        ptr->next = newNode;
    }
}

void dequeue(){
    if(head == NULL){
        printf("The queue is empty.\n");
    } else {
        printf("The node with value: %d is dequeued.\n", head->data);
        struct Node* temp = head;
        head = head->next;
        free(temp);
    }
}

void display(){
    if(head == NULL){
        printf("The queue is empty.\n");
    } else {
        struct Node* ptr = head;
        while(ptr != NULL){
            printf("%d -> ", ptr->data);
            ptr = ptr->next;
        }
        printf("NULL\n");
    }
}

int main(){
    enqueue(10);
    enqueue(20);
    enqueue(30);

    display(); // 10 -> 20 -> 30 -> NULL

    dequeue();

    display(); // 20 -> 30 -> NULL

    return 0;
}