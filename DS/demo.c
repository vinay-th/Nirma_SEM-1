#include <stdio.h>

char stack[100];
int top = -1;

void push(int data){
    if(top == 99){
        printf("Stack overflow");
        return;
    } 
    top++;
    stack[top] = data;
}

int pop(){
    if(top == -1){
        printf("Stack underflow");
        return 0;
    }
    int val = stack[top];
    top--;
    return val; 
}

void display(){
    int temp = top;
    while(temp >= 0){
        printf("%d -> ", stack[temp]);
        temp--;
    }
    printf("Null\n");
}

int main(){
    push(10);
    push(20);
    push(30);
    push(40);
    push(50);
    display();

    printf("%d", pop());

    return 0;
}