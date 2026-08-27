// check for a grammar L = {WCW(rev)}
#include <iostream>
int main() {
  char stack[100];
  int top = -1;
  bool isValid = true;

  std::string str;
  std::cout << "Enter the string: ";
  std::cin >> str;

  std::string w = str.substr(0, str.find('C'));
  std::string w_rev = str.substr(str.find('C') + 1);

  for (char ch : w) {
    stack[++top] = ch;
  }

  for (int i = 0; i <= w.length() - 1; i++) {
    if (stack[top] == w_rev[i]) {
      top--;
    } else {
      isValid = false;
      break;
    }
  }

  if (isValid && top == -1) {
    std::cout << "The string is in the language L." << std::endl;
  } else {
    std::cout << "The string is not in the language L." << std::endl;
  }

  return 0;
}