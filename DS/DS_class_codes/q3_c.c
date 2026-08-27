// infix to postfix conversion
#include <stdio.h>
#include <string.h>
#include <ctype.h>

int precedence(char op) {
    if (op == '*' || op == '/')
        return 2;
    if (op == '+' || op == '-')
        return 1;
    return 0;
}

int main() {
    char infix[100];
    printf("Enter the infix expression: ");
    scanf("%s", infix);

    char stack[100];
    int top = -1;
    char postfix[100];
    int j = 0;
    int len = strlen(infix);

    for (int i = 0; i < len; i++) {
        char ch = infix[i];

        if (isalnum(ch)) {
            postfix[j++] = ch;
        }
        else if (ch == '(') {
            stack[++top] = ch;
        }
        else if (ch == ')') {
            while (top != -1 && stack[top] != '(') {
                postfix[j++] = stack[top--];
            }

            if (top != -1 && stack[top] == '(') {
                top--;
            }
        }
        else {
            while (top != -1 && stack[top] != '(' && precedence(stack[top]) >= precedence(ch)) {
                postfix[j++] = stack[top--];
            }

            stack[++top] = ch;
        }
    }

    while (top != -1) {
        postfix[j++] = stack[top--];
    }

    postfix[j] = '\0';

    printf("The postfix expression is: %s\n", postfix);

    return 0;
}
