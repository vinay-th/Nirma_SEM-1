// Write a program to determine balanced parentheses in a given
// algebraic expression, considering that only one type of
// parenthesis is used in an algebraic expression.
#include <stdio.h>
int main(){
    char exp[100];
    char stack[100];
    int top = -1;
    int balanced = 1;

    printf("Enter an algebraic expression: ");
    scanf("%s", exp);

    for(int i = 0; exp[i] != '\0'; i++){
        if(exp[i] == '('){
            stack[++top] = '(';
            printf("Top: %d\n", top);
        } else if(exp[i] == ')'){
            if(top == -1){
                balanced = 0;
                break;
            }
            top--;
        }
    }
    
    if(balanced && top == -1){
        printf("The expression has balanced parentheses.\n");
    } else {
        printf("The expression has unbalanced parentheses.\n");
    }
    return 0;
}