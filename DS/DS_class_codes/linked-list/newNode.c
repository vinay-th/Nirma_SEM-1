#include <stdio.h>
#include <stdlib.h>
struct Node{
    int data;
    struct Node* next;
};

int main(){
    struct Node* head = NULL;
    
    struct Node* newNode1 = (struct Node*)malloc(sizeof(struct Node));
    newNode1->data = 10;
    struct Node* newNode2 = (struct Node*)malloc(sizeof(struct Node));
    newNode2->data = 20;
    struct Node* newNode3 = (struct Node*)malloc(sizeof(struct Node));
    newNode3->data = 30;
    head = newNode1;
    newNode1->next = newNode2;
    newNode2->next = newNode3;
    newNode3->next = NULL;

    printf("\nData in the new node: %d", head->data);
    printf("\nData in the new node: %d", head->next->data);
    printf("\nData in the new node: %d", head->next->next->data);
    
    printf("\n");
    return 0;
}