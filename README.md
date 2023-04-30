# Phunctions

## Introduction

Phunctions is a simple functional programming language that is inspired by Lisp.

This project is the first and only implementation for the interpreter of this language.

## Grammar

### Examples

#### Basic

```Phunctions
(do
    (def 
        :a 
        (* 2 2 :b)
    )
    (def 
        :b 
        10
    )
    (* :a :b)
)
```
This example returns 400.

### Expression

Everything in Phunctions are expression.

An expression is consist of one operator at least one operand expression, and is wrapped with '('s and ')'s.
For example:
```Phunctions
(+ 1 1)
```

There are many kinds of expression.

#### Do Expression

Do expression start with keyword(or operator) 'do'. When being evaluated, do expression equals the result of the 
    evaluation of the last expression.

This example is evaluated as `14`(the result of `(+ 13 1)`).
```Phunctions
(do
    (+ 1 1)
    (+ 1 2)
    (+ 13 1)
)
```

#### Basic Operation Expression

You can do plus, minus, multiply and divide with two or more integers.

The grammar is as example:
```Phunctions
(+ 1 1)
```
This example adds 1 and 1. Replace '+' with '-', '*', '/' to perform 
    different operations.

#### Variable Define Expression

You can bind an constant integer expression with a name:
```Phunctions
(def :a 10)
```
Or a symbol expression:
```Phunctions
(def :a (+ 1 1))
```
The value of variables will not be evaluated unless they are accessed.

TODO: force phunctions to evaluate as soon as declared.

Variables can be rebind. This following expression is evaluated to 3.
```Phunctions
(do
    (def :a (+ 1 1))
    (def :a (+ 1 2))
    (+ 0 :a)
)
```

TODO: restricted bind - a variable that can not be rebind

#### Function Define Expression

TODO: to be done