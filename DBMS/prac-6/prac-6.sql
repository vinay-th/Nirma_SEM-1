-- (A). Write a PL SQL program that demonstrates Declaring, Defining, and Invoking a Simple PL/SQL Function that computes and returns the maximum of two values.
DECLARE
    num1 NUMBER := 10;
    num2 NUMBER := 20;
    res NUMBER;

    FUNCTION get_max(a NUMBER, b NUMBER)
    RETURN NUMBER IS
    BEGIN
        IF a > b THEN
            RETURN a;
        ELSE
            RETURN b;
        END IF;
    END get_max;

BEGIN
    res := get_max(num1, num2);
    DBMS_OUTPUT.PUT_LINE('The maximum of ' || num1 || ' and ' || num2 || ' is: ' || res);
END;
/

-- (B). Write a PL SQL program which calculates the factorial of a given number by calling itself recursively
DECLARE
    n NUMBER := 5;
    fact NUMBER;

    FUNCTION factorial(num NUMBER)
    RETURN NUMBER IS
    BEGIN
        IF num = 0 THEN
            RETURN 1;
        ELSE
            RETURN num * factorial(num - 1);
        END IF;
    END factorial;

BEGIN
    fact := factorial(n);
    DBMS_OUTPUT.PUT_LINE('The factorial of ' || n || ' is: ' || fact);
END;
/