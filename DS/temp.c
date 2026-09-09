#include <stdio.h>
void push(char stack[], int *top, int val){
    if(*top == 99){
        printf("Stack is full.\n");
        return;
    }
    stack[++(*top)] = val;
}

int pop(char stack[], int *top){
    if(*top == -1){
        printf("Stack is empty.\n");
        return -1;
    }
    return stack[(*top)--];
}

void printStack(char stack[], int top){
    while(top != -1){
        printf("%d ", stack[top]);
        top--;
    }
}

int main(){
    char stack[100];
    int top = -1;
    
    push(stack, &top, 10);
    push(stack, &top, 20);
    push(stack, &top, 30);

    printStack(stack, top);
    
    printf("Popped: %d\n", pop(stack, &top));
    printStack(stack, top);    
    
    printf("\n");
    return 0;
}