#include <stdio.h>
#include <stdlib.h>
int main(){
    int n = 4;
    int *ptr = (int *)malloc(n * sizeof(int));
    if(ptr == NULL){
        printf("No memory left");
        return 0;
    }
    
    for(int i = 0; i < n; i++){
        printf("%d",*(ptr + i));
    }
    return 0;
}