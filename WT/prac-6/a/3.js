// 3.	Write a script to find factorial of given number.
var number = 5;

var factorial = 1;
for (var i = 1; i <= number; i++) {
  factorial *= i;
}

console.log('Factorial of ' + number + ' is: ' + factorial);
