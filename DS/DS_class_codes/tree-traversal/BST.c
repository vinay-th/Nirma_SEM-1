#include <stdio.h>
#include <stdlib.h>

struct Node{
    int data;
    struct Node* left;
    struct Node* right;
};

struct Node *root = NULL;

struct Node *createNode(struct Node* root, int data){
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    newNode->data = data;
    newNode->left = NULL;
    newNode->right = NULL;

    if(root == NULL){
        return newNode;
    }

    if(data < root->data){
        if(root->left == NULL){
            root->left = newNode;
        } else {
            createNode(root->left, data);
        }
    } else {
        if(root->right == NULL){
            root->right = newNode;
        } else {
            createNode(root->right, data);
        }
    }
    return root;
}

void preOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    printf("%d ", node->data);
    preOrder(node->left);
    preOrder(node->right);
}

void inOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    inOrder(node->left);
    printf("%d ", node->data);
    inOrder(node->right);
}

void postOrder(struct Node *node){
    if(node == NULL){
        return;
    }
    postOrder(node->left);
    postOrder(node->right);
    printf("%d ", node->data);
}

int main(){
    int array[] = {10, 3, 15, 22, 6, 45, 65, 23, 78, 34, 5};
    int n = sizeof(array)/sizeof(array[0]);

    root = createNode(root, array[0]);
    for(int i = 1; i < n; i++){
        createNode(root, array[i]);
    }

    printf("Pre-order Traversal: ");
    preOrder(root);
    printf("\nIn-order Traversal: ");
    inOrder(root);
    printf("\nPost-order Traversal: ");
    postOrder(root);

    printf("\n");

    return 0;
}