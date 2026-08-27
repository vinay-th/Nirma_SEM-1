// Write a program to determine balanced parentheses in a given
// algebraic expression, considering that only one type of
// parenthesis is used in an algebraic expression.
#include <stdio.h>
#include <string.h>

int main(){
    char exp[100];
    char stk[100];
    int top = -1;

    printf("Enter the expression to check: ");
    scanf("%s", exp);

    for(int i = 0; exp[i] != '\0'; i++){
        if(exp[i] == '('){
            stk[++top] = '(';
        }
        else if(exp[i] == ')'){
            stk[top] = '\0';
            if(top == -1){
                break;
            }
            top--;
        }
    }

    if(stk[0] == '\0' && top == -1)
        printf("The expresion is balanced.");
    else    
        printf("The expression is not balanced.");
    
    printf("\n");
    return 0;
}