#include <stdio.h>
#include <stdlib.h>
struct Node{
    int data;
    struct Node* next;
};

struct Node *head = NULL;

void printList(struct Node* node) {
    struct Node *temp = node;
    while (temp != NULL) {
        printf("%d -> ", temp->data);
        temp = temp->next;
    }
    printf("NULL\n");
}

int main(){
    int n;
    printf("Enter the number of nodes: ");
    scanf("%d", &n);

    for(int i = 0; i < n; i++){
        struct Node* newNode = (struct Node*)malloc(sizeof(struct Node));
        printf("Enter data for node %d: ", i + 1);
        scanf("%d", &newNode->data);
        newNode->next = NULL;

        if(head == NULL){
            head = newNode;
        } else {
            struct Node* temp = head;
            while(temp->next != NULL){
                temp = temp->next;
            }
            temp->next = newNode;
        }

    }
    
    printList(head);
    
    return 0;
}