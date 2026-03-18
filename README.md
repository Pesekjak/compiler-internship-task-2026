# MiniKotlin to Java CPS Compiler

This is an internship assignment for implementing a CPS-style (Continuation-Passing Style) compiler from MiniKotlin (a subset of Kotlin) to Java.

## Implementation Details
Instead of direct String generation from the `ProgramContext` parsed by ANTLR, I build my own AST first, containing
only the relevant nodes for code generation, see [AstBuilder](https://github.com/Pesekjak/compiler-internship-task-2026/blob/master/src/main/java/compiler/ast/AstBuilder.java). This makes traversing the program and generating the CPS Java code easier, see [CpsGenerator](https://github.com/Pesekjak/compiler-internship-task-2026/blob/master/src/main/java/compiler/CpsGenerator.java). 
Local variables are wrapped in single array elements to allow variable assignments in nested lambdas, as variables in Java lambdas need to be effectively final. `while` loops are lowered into self referencing recursive lambdas.

## Known limitations
* Because the `while` loop lambdas call themselves recursively, deep recursion in while loops will eventually cause stack overflow. This could be fixed using the trampoline pattern, but because of time constraints I was not able to implement it. 
* Right now any code following `if-else` block will be duplicated in both branches. This will cause the program size to grow exponentially in such cases. This could be optimized with both branches using shared continuation for the rest of the program, but I was not able to implement this in time.

## Overview

The goal is to implement a compiler that translates MiniKotlin source code into Java, where all functions are expressed using continuation-passing style.

## Project Structure

- `samples/` - Example MiniKotlin programs
- `src/main/antlr/MiniKotlin.g4` - Grammar definition for MiniKotlin
- `src/main/kotlin/compiler/` - Compiler implementation
- `src/test/` - Testing framework
- `stdlib/` - Standard library with `Prelude` class containing CPS function examples

## Task

Implement the `MiniKotlinCompiler` to translate MiniKotlin to Java such that:

1. All functions use continuation-passing style
2. The semantics of operators follows Kotlin 

See `Prelude` in the stdlib for examples of how CPS functions should look.

## Example

Suppose that the MiniKotlin code looks like this:
```kotlin
fun factorial(n: Int): Int {
    if (n <= 1) {
        return 1
    } else {
        return n * factorial(n - 1)
    }
}

// Main logic
fun main(): Unit {
    val result: Int = factorial(5)
    println(result)

    // Arithmetic and logical expressions
    val a: Int = 10 + 5
    val b: Boolean = a > 10
    println(a)
}
```

Then the supposed implementation can look like this: 
```java
public static void factorial(Integer n, Continuation<Integer> __continuation) { 
  if ((n <= 1)) {
    __continuation.accept(1);
    return;
  }
  else {
    factorial((n - 1), (arg0) -> {
      __continuation.accept((n * arg0));
      return;
      });
    }
}

public static void main(String[] args) { 
  factorial(5, (arg0) -> {
    Integer result = arg0;
    Prelude.println(result, (arg1) -> {
      Integer a = (10 + 5);
      Boolean b = (a > 10);
      Prelude.println(a, (arg2) -> {
      });
    });
  });
}
```


## Building and Running

```bash
# Build the project
./gradlew build

# Run with default example
./gradlew run

# Run with a specific file
./gradlew run --args="samples/example.mini"

# Run tests
./gradlew test
```

## Evaluation

The task will be tested on a hidden set of tests.
