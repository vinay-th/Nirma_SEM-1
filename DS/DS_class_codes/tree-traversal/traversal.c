#include <stdio.h>
#include <stdlib.h>

struct Node{
    char data;
    struct Node* left;
    struct Node* right;
};

struct Node *root = NULL;

struct Node *createNode(char data){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->left = NULL;
    newNode->right = NULL;
    return newNode;
}

void preOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    printf("%c ", node->data);
    preOrder(node->left);
    preOrder(node->right);
}

void inOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    preOrder(node->left);
    printf("%c ", node->data);
    preOrder(node->right);
}

void postOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    preOrder(node->left);
    preOrder(node->right);
    printf("%c ", node->data);
}

int main(){
    root = createNode('A');
    root->left = createNode('B');
    root->right = createNode('D');

    root->left->left = createNode('C');
    root->right->left = createNode('E');
    root->right->right = createNode('G');

    root->right->left->right = createNode('F');

    printf("Pre-order Traversal: ");
    preOrder(root);
    printf("\nIn-order Traversal: ");
    inOrder(root);
    printf("\nPost-order Traversal: ");
    postOrder(root);

    return 0;
}