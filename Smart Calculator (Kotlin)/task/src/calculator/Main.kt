package calculator

import java.util.Stack
import kotlin.math.*
import java.math.BigInteger
import java.math.BigDecimal

class Calculator {
    val pairs = mutableMapOf<String, String>()
    val finalList = mutableListOf<String>()
    val azRegex = "[a-z]+".toRegex()
    val digitRegex = "-?\\d+".toRegex()

    fun makePairs(input: String) {

        if (digitRegex.matches(input)) {
            println(input)
        } else if ("=" in input) {
            val list = input.split("\\s*=\\s*".toRegex()).toMutableList()
            list.set(1, list[1].replace(" ", ""))
            if (!azRegex.matches(list[0])) {
                println("Invalid identifier")
            } else if ((input.toList().count {it == '='} > 1) || !azRegex.matches(list[1]) && !digitRegex.matches(list[1])) {
                println("Invalid assignment")
            } else if (digitRegex.matches(list[1])) {
                pairs += list[0] to list[1]
            } else {
                if (pairs.containsKey(list[1])) {
                    pairs.put(list[0], pairs[list[1]].toString())
                } else {
                    println("Unknown variable")
                }
            }
        } else if ("[a-z]+|[a-z]+[A-Z]+|[A-Z]+[a-z]+|[A-Z]+".toRegex().matches(input)) {
            when {
                azRegex.matches(input) -> println(if (pairs.containsKey(input)) pairs[input] else "Unknown variable")
                else -> println("Unknown variable")
            }
        } else {
            makeList(input)
            val postfixExpression = convertToPostfix(finalList)
            val result = evaluatePostfix(postfixExpression)
            println(result)
            finalList.clear()
        }
    }

    fun inputClean(input: String): String {
        var input = input
            .replace("-{2}".toRegex(), "+")
            .replace("/+".toRegex(), " / ")
            .replace("[+]+".toRegex(), " + ")
            .replace("[-]".toRegex(), " - ")
            .replace("[(]".toRegex(), "( ")
            .replace("[)]".toRegex(), " )")
            .replace("[*]+".toRegex(), " * ")
            .replace("\\^+".toRegex(), " ^ ")
            .replace("\\s+".toRegex(), " ")
        input = input.replace("[+] -|- [+]".toRegex(), "-")
        return input
    }
    fun makeList(input: String): MutableList<String> {
        finalList.addAll(input.split(" "))
        for (i in finalList.indices) {
            if ("[a-zA-Z]+".toRegex().matches(finalList[i])) {
                if (finalList[i] in pairs.keys) {
                    finalList.set(i, pairs[finalList[i]].toString())
                } else {
                    println("Unknown variable")
                }
            }
        }
        return finalList
    }
    fun convertToPostfix(expression: MutableList<String>): List<String> {
        val operatorStack = Stack<String>()
        val postfixExpression = mutableListOf<String>()

        for (element in expression) {
            if (element.matches(digitRegex)) {
                postfixExpression.add(element)
            } else if (element == "(") {
                operatorStack.push(element)
            } else if (element == ")") {
                while (!operatorStack.empty() && operatorStack.peek() != "(") {
                    postfixExpression.add(operatorStack.pop())
                }
                operatorStack.pop() // Pop the opening parenthesis
            } else {
                while (!operatorStack.empty() && hasHigherPrecedence(operatorStack.peek(), element)) {
                    postfixExpression.add(operatorStack.pop())
                }
                operatorStack.push(element)
            }
        }
        while (!operatorStack.empty()) {
            postfixExpression.add(operatorStack.pop())
        }
        return postfixExpression
    }
    fun hasHigherPrecedence(operator1: String, operator2: String): Boolean {
        val precedence1 = getPrecedence(operator1)
        val precedence2 = getPrecedence(operator2)
        return precedence1 >= precedence2
    }
    fun getPrecedence(operator: String): Int {
        return when (operator) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            else -> 0
        }
    }
    fun evaluatePostfix(expression: List<String>): BigInteger {
        val operandStack = Stack<BigInteger>()
        for (element in expression) {
            if (element.matches(Regex("-?\\d+(\\.\\d+)?"))) {
                operandStack.push(element.toBigInteger())
            } else {
                val operand2 = operandStack.pop()
                val operand1 = operandStack.pop()
                val result: Any = when (element) {
                    "+" -> operand1 + operand2
                    "-" -> operand1 - operand2
                    "*" -> operand1 * operand2
                    "/" -> {
                        try {
                            operand1 / operand2
                        } catch (e: ArithmeticException) {
                            println("Division by zero")
                        }
                    }
                    "^" -> operand1.toDouble().pow(operand2.toInt())
                    else -> throw IllegalArgumentException("Invalid operator: $element")
                }
                operandStack.push(result as BigInteger)
            }
        }
        return operandStack.pop()
    }
}

fun main() {
    val calculator = Calculator()
    val commandRegex = "/+.*".toRegex()
    val invExpRegex = "[/]{2}+|[*]{2}+|[/][*]|[*][/]".toRegex()
    while(true){
        var input = readln().trim().replace("\\s+".toRegex(), "")
        when {
            input == "/exit" -> {
                println("Bye!")
                break
            }
            input == "/help" -> println("""The program calculates the sum and difference of numbers, 
                |can divide and multiply numbers as well as rise a number to a power.""".trimMargin())
            commandRegex.matches(input) -> println("Unknown command")
            input.toList().count {it == '('} != input.toList().count {it == ')'} || invExpRegex in input -> println("Invalid expression")
            input.isNotBlank() -> {
                input = calculator.inputClean(input)
                calculator.makePairs(input)
                }
            else -> continue
        }
    }
}