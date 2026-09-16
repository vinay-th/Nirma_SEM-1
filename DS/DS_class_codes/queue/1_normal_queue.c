#include <stdio.h>

int queue[5];

int front = -1;
int rear = -1;

void enqueue(int value){
    if(rear == 4){
        printf("Queue is overflow\n");
        return;
    }
    if(front == -1){
        front = 0;
    }
    rear++;
    queue[rear] = value;
}

void dequeue(){
    if(front == -1 || rear == -1){
        printf("Queue is underflow\n");
        return;
    }
    front++;
}

void display(){
    if(front == -1 || rear == -1){
        printf("Queue is empty\n");
        return;
    }
    for(int i = front; i <= rear; i++){
        printf("%d ", queue[i]);
    }
    printf("\n");
}

int main(){
    enqueue(10);
    enqueue(20);
    enqueue(30);

    display(); // Output: 10 20 30

    dequeue();
    dequeue();

    display(); // Output: 30
    
    enqueue(40);
    enqueue(50);
    enqueue(60); // This will cause overflow since the queue size is 5
    display(); // Output: 30 40 50

    printf("\n");
    return 0;
}