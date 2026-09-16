#include <stdio.h>
#include <stdlib.h>

struct Node{
    int data;
    struct Node* next;
};

struct Node *head = NULL;
struct Node *tail = NULL;

struct Node *insertBack(struct Node *head, int data){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->next = NULL;
    if(head == NULL){
        head = newNode;
        tail = newNode;
    }
    else{
        tail->next = newNode;
        tail = newNode;
    }
    return head;
}

struct Node *insertFront(struct Node *head, int data){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->next = head;
    head = newNode;
    return head;
}

struct Node *insertAtPosition(struct Node *head, int data, int position){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    if(position == 0){
        newNode->next = head;
        head = newNode;
    }
    else{
        struct Node *temp = head;
        for(int i=0; i<position-1 && temp!=NULL; i++){
            temp = temp->next;
        }
        if(temp == NULL){
            printf("Position out of bounds\n");
            free(newNode);
            return head;
        }
        newNode->next = temp->next;
        temp->next = newNode;
    }
    return head;
}

struct Node *deleteNode(struct Node *head, int data){
    struct Node *ptr = head;
    struct Node *preptr = NULL;
    while(ptr != NULL && ptr->data != data){
        preptr = ptr;
        ptr = ptr->next;
    }
    preptr->next = ptr->next;
    free(ptr);
    return head;
}

void printList(struct Node *head){
    struct Node *ptr = head;
    while(ptr != NULL){
        printf("%d -> ", ptr->data);
        ptr = ptr->next;
    }
    printf("NULL\n");
}

int main(){
    head = insertBack(head, 1); // 1 -> NULL
    head = insertBack(head, 2); // 1 -> 2 -> NULL
    head = insertBack(head, 3); // 1 -> 2 -> 3 -> NULL
    head = insertFront(head, 0); // 0 -> 1 -> 2 -> 3 -> NULL
    head = insertAtPosition(head, 5, 2); // 0 -> 1 -> 5 -> 2 -> 3 -> NULL
    printList(head);  // 0 -> 1 -> 5 -> 2 -> 3 -> NULL
    
    printf("\n");
    return 0;
}