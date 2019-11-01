# Sample calculator REST API

Simple REST-like API implemented with Micronaut framework, providing
some basic arithmetic operation in full precision. 

# Building and running

JDK version at least 8 is required. 

To build from source, checkout the code and run:
```bash
./gradlew build
```

To run application locally: 
```bash
./gradlew run
```
Application is, by default, listening on port 8080 

# API Overview

There are two basic API endpoints:

- basic calculator: `http://localhost:8080/calculator/basic`
  - `GET` lists all available operators, each operator having two possible names
  - `POST` accepts operation request (operator + arguments) as JSON and returns the result
    - body must be `application/json` with two properties
      - `operator`: `string` with operator name or symbol (from the list above), case-insensitive
      - `arguments`: array of strings or numbers
        - minimum length depends on the operator, 1 for unary and 2 for binary ones
        - maximum length depends on the operator; usually it's the same as the minimum length, except of those that are commutative and associative (in that case, unlimited number of arguments is accepted)
        - single scalar (number or string) value is also accepted, in place of array with single value
    - `200 OK` response
      - `application/json` that contains everything from the request body, plus one more property:
        - `result`: result of the operation, number represented as a string (to avoid JavaScript/JSON limitations)
        - `error`: string with message explaining why the operation is not possible
          - note that this is NOT http error (it's still 200 OK); this error happens when the request is valid, but the result is not available for mathematical reasons 
          - typical example is division by zero or attempt to compute factorial of non-integer number
          - as a rule of thumb: this is the error that should be displayed to user
    - `400 Bad Request` response
      - `application/json` with `message` property
      - unlike success with `error` message, this response happens when the request does not make sense: operator is unknown, arguments are missing, etc
      - as a rule of thumb: this error means that the caller code contains some kind of error  
- scientific calculator: `http://localhost:8080/calculator/scientific`
  - full superset of the basic calculator (accepts all operators and adds some more)
  - `GET` is the same as above, except that there are more operators available
  - `POST` is the same as above, except that more operators are accepted
  - special sub-path (not available as operator):
    - `/prime`: compute prime factors of an integer
      - `POST` is just like normal operators, except the result is not one single number
        - body has the same format like all common operations, but the `operator` property is ignored
        - `arguments` property contain only single value (in array or not)
        - response is `application/json` with following properties:
          - `operator`: always `prime`
          - `arguments`: array with single value, the number from request
          - `result`: *array* of number, containing all prime factors; missing when `error` happens
          - `prime`: boolean value, `true` if the requested number is prime (in that case, `result` is of size 1)
          - `error`: string with message explaining why the operation is not possible (just like for normal operands)

## Examples

```bash
curl -s http://localhost:8080/calculator/basic -X POST -H 'Content-Type: application/json' --data-binary '{"operator":"+", "args":[10, 12, 15, 23]}'
```
```json
{
  "operator": "+",
  "arguments": [
    10,
    12,
    15,
    23
  ],
  "result": "60"
}
```
```bash
curl -s http://localhost:8080/calculator/basic -X POST -H 'Content-Type: application/json' --data-binary '{"operator":"div", "args":[10, 12]}'
```
```json
{
  "operator": "div",
  "arguments": [
    10,
    12
  ],
  "result": "0.8333333333333333"
}
```
```bash
curl -s http://localhost:8080/calculator/basic -X POST -H 'Content-Type: application/json' --data-binary '{"operator":"/", "args":["30000000000000000000000000000000000000000003", 3]}'
```
```json
{
  "operator": "/",
  "arguments": [
    3e+43,
    3
  ],
  "result": "10000000000000000000000000000000000000000001"
}
```
```bash
curl -s http://localhost:8080/calculator/scientific -X POST -H 'Content-Type: application/json' --data-binary '{"operator":"x!", "args":45}'
```
```json
{
  "operator": "x!",
  "arguments": [
    45
  ],
  "result": "119622220865480194561963161495657715064383733760000000000"
}
```
```bash
curl -s http://localhost:8080/calculator/scientific/prime -X POST -H 'Content-Type: application/json' --data-binary '{"arguments":307.1}'
```
```json
{
  "operator": "prime",
  "arguments": [
    307.1
  ],
  "error": "only integer number can be factorized to primes",
  "prime": false
}
```
