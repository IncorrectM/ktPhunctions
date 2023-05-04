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

Another version that doesn't need `:` to declare identifier can be seen in the branch `feature-var-no-prefix`.
    Be ware that `feature-var-no-prefix` is currently under estimation.

##### Lazy Evaluation
By default, variables' value is evaluated only when accessed. 

Which means the example below is valid.
```Phunctions
(do
    (def :a (+ 1 :b))
    (def :b 1)
    (+ 0 :a)
)
```
This example returns `2`.

Under this condition, `(def :a (+ 1 :b))` and `(def :b 1)` simply returns `0`.

#### Instant Evaluation

You can explicitly ask Phunctions to evaluate the value of variables as soon as it is defined using operator `!`.

Here's an example.

```Phunctions
(def :a (+ 1 1) !)
```

This example returns `2`(the result of (+ 1 1)).

To show the different between instant evaluation and lazy evaluation, here is one more example.

```Phunctions
(def :a (+ 1 :b) !)
```

This example will throw an EvaluationErrorException which says `b is no defined` since variable `b` is not defined 
    before `a`.

```Phunctions
(do
    (def :b 0)
    (def :a (+ 1 :b) !)
    (+ 0 :a)
    (def :b 99)
    (+ 0 :a)
)
```

In this example, two `(+ 0 :a)` returns exactly the same `1` since the value of `a` is evaluated as `a` as soon as it 
    is defined.

#### Re-binding
Variables can be re-bond. This following expression is evaluated to 3.
```Phunctions
(do
    (def :a (+ 1 1))
    (def :a (+ 1 2))
    (+ 0 :a)
)
```

TODO: restricted bind - a variable that can not be rebind

#### Function Define Expression

```Phunctions
(def
    :addTwo
    (args :a :b)
    (+ :a :b)
)
```

This example defines a function that adds two integers.

TODO: to be done