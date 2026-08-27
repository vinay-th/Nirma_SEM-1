// infix to postfix conversion
#include <iostream>
#include <string>
using namespace std;

int main() {
  string infix;
  cout << "Enter the infix expression: ";
  cin >> infix;

  char stack[100];
  int top = -1;
  string postfix;

  auto precedence = [](char op) {
    if (op == '*' || op == '/')
      return 2;
    if (op == '+' || op == '-')
      return 1;
    return 0;
  };

  for (char ch : infix) {
    if (isalnum(ch)) {
      postfix += ch;
    } else if (ch == '(') {
      stack[++top] = ch;
    } else if (ch == ')') {
      while (top != -1 && stack[top] != '(') {
        postfix += stack[top--];
      }
      if (top != -1 && stack[top] == '(') {
        top--;
      }
    } else {
      while (top != -1 && stack[top] != '(' &&
             precedence(stack[top]) >= precedence(ch)) {
        postfix += stack[top--];
      }
      stack[++top] = ch;
    }
  }

  while (top != -1) {
    postfix += stack[top--];
  }

  cout << "The postfix expression is: " << postfix << std::endl;

  return 0;
}