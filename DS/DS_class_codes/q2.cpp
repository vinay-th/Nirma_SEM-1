// to check a grammar L = {a^ib^i} using stack array
#include <iostream>
int main() {
  std::string str;
  std::cout << "Enter the string: ";
  std::cin >> str;

  char stack[100];
  int top = -1;

  for (char ch : str) {
    if (ch == 'a') {
      stack[++top] = ch;
    } else if (ch == 'b') {
      if (top == -1) {
        std::cout << "The string is not in the language L." << std::endl;
        return 0;
      }
      top--;
    } else {
      std::cout << "Invalid character in the string." << std::endl;
      return 0;
    }
  }

  if (top == -1) {
    std::cout << "The string is in the language L." << std::endl;
  } else {
    std::cout << "The string is not in the language L." << std::endl;
  }

  return 0;
}