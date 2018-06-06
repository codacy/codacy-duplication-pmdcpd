# Define our 3 functions
def my_function():
    print("Hello From My Function!")

def my_function_with_args(username, greeting):
    print("Yoooooooo")
    y = 1 + 1
    y += y
    y += y
    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))
    print("Yoooooooo")
    y = 1 + 1
    y += y
    y += y
    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))

def my_function_with_args1(username, greeting):
    print("Yoooooooo")
    y = 1 + 1
    y += y
    y += y
    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))
    print("Yoooooooo")
    y = 1 + 1
    y += y
    y += y
    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))

def sum_two_numbers(a, b):
    return a + b

# print(a simple greeting)
my_function()

#prints - "Hello, John Doe, From My Function!, I wish you a great year!"
my_function_with_args("John Doe", "a great year!")
my_function_with_args1("John Doe", "a great year!")

# after this line x will hold the value 3!
x = sum_two_numbers(1,2)