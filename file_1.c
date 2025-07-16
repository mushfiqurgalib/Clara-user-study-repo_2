#include <stdio.h>
#include <string.h>

#define MAX_LEN 100

void reverseString(char str[]) {
    int len = strlen(str);
    int i = 0, j = len - 1;
    char temp;

    // Swap characters from both ends moving toward the center
    while (i < j) {
        temp = str[i];
        str[i] = str[j];
        str[j] = temp;
        i++;
        j--;
    }
}

int main() {
    char input[MAX_LEN];

    printf("Enter a string: ");
    fgets(input, MAX_LEN, stdin);

    // Remove newline character if present
    size_t len = strlen(input);
    if (len > 0 && input[len - 1] == '\n')
        input[len - 1] = '\0';

    reverseString(input);

    printf("Reversed string: %s\n", input);

    return 0;
}
