#include <stdio.h>
#include <stdlib.h>

struct Node{
    int data;
    struct Node* next;
};

struct Node * head = NULL;

struct Node *insertSorted(struct Node *head, int data){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->next = NULL;

    if(head == NULL || head->data >= data){
        newNode->next = head;
        head = newNode;
    }
    else{
        struct Node *temp = head;
        while(temp->next != NULL && temp->next->data < data){
            temp = temp->next;
        }
        newNode->next = temp->next;
        temp->next = newNode;
    }
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
    head = insertSorted(head, 5); // 5 -> NULL
    head = insertSorted(head, 2); // 2 -> 5 -> NULL
    head = insertSorted(head, 8); // 2 -> 5 -> 8 -> NULL
    head = insertSorted(head, 1); // 1 -> 2 -> 5 -> 8 -> NULL
    head = insertSorted(head, 4); // 1 -> 2 -> 4 -> 5 -> 8 -> NULL

    printList(head);

    return 0;
}