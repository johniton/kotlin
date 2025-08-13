

import kotlin.math.sqrt

import android.app.Activity
fun main() {
    // val x:Float = 5f
    // val y: Boolean = true
    //     val x: Int = 5
    //     val y: Int = 4
    //     val s:String = "Hello world"
    //     // val x: 9 for short and var x:9 -> can be changed
    //     println(s)
    //     println(x+y)
    //     println("The vakue of x is ${x}")
    //     println("Enter a number")
    //     val input = readln()
    //     val inputInt = input.toIntOrNull()?:0
    //     println("You have enetred $input")

    //     val output = if(inputInt%2==0){
    //         "NUmber is even"
    //     }else{
    //         "Number is nit even"
    //     }
    //     println(output)

    //     // When expression
    //     val value = when{
    //         inputInt%2==0 -> "The number is even"
    //         inputInt<10 -> "Inout integer less than 10"
    //         inputInt  in 10..20 -> "The nmber i sbetween 10 and 20"
    //         else -> "Bumber odd and greater than 10"
    // }
    //

//     val favourite_numbers = intArrayOf(1, 2, 3, 4, 5, 6)
//     // printing second
//     println(favourite_numbers[1])
//     val input = readln().toIntOrNull()
//     if (input != null && input < favourite_numbers.size && input >= 0) 
//         println(favourite_numbers.getOrNull(input))
//     val more = intArrayOf(1, 2, 3, 4, 5, 6)+7

//  println(more[6])

// While loop
//    println("How many numbers will you enter?")
//     val amountofNumber = readln().toIntOrNull()?:0
//     var numbers = mutableListOf<Int>()
//     var i =0
//     var sum=0
//     while(i< amountofNumber){
//         var number = readln().toIntOrNull()?:continue
//         sum+=number
//         i++
//         numbers.add(number)
    // }
    // println("Sum = $sum")
    // println("List = $numbers")

//     // For loop
// //     for(i in 0 until amountofNumber){
    //     println(numbers[i])
    // }
    
//     for(number in numbers){
//         println(number)
//     }
        // for(i in numbers.lastIndex downTo 0){
        //     println(numbers[i])
        // }

        // println(reversed(inputString = "Hello"))
        // println("lmao".extendedReverse())

        // println("Enter a String :")

        // val input = readln()
        // val lettersOnly = input.filter { 
        //     it.isLetter()
        //  }
        //  println(lettersOnly)
         
        // val input2 = readln()
        // val lettersOnly2 = input2.myFilter { 
        //     it.isLetter()
        //  }
        //  println(lettersOnly2)
        
         
        // val input3 = readln()
        // val lettersOnly3 = input3.myExtendedFilter { 
        //     isLetter()
        //  }
        //  println(lettersOnly3)
        val rec1 = Rectangle(2F,3F)
        val rec2 = Rectangle(2F,3F)
        println(rec1)
        print(rec1==rec2) // keyword "data" not present in fronto fthe xlass so different instances with with same values turn out unequal
        val rec3=rec1
        print(rec1==rec3) // Reference to rec1 thats why equal
        // With data keyword specified
        print(rec1==rec2) // keyword "data"  present in fronto fthe xlass so different instances with with same values are equal
        println(rec1) // more readble due to data keyword
}

fun reversed(inputString:String):String{
    val finalString = buildString{
        for(i in inputString.lastIndex downTo 0){
            append(inputString[i])
        }
    }
    return finalString
}

fun String.extendedReverse():String{
       val finalString = buildString{
        for(i in this@extendedReverse.lastIndex downTo 0){
            append(this@extendedReverse[i])
        }
    }
    return finalString
 
}

fun Int.extendedReverse():Int{
    return this@extendedReverse.extendedReverse().toInt()
}
 
// Lambda function under the hood

fun String.myFilter(predicate:(Char)->Boolean):String{
    return buildString{
        for(char in this@myFilter){
            if(predicate(char)){
                append(char)
            }
        }
    }
}
fun String.myExtendedFilter(predicate:Char.()->Boolean):String{
    return buildString{
        for(char in this@myExtendedFilter){
            if(char.predicate()){
                append(char)
            }
        }
    }
}

data class Rectangle(val width:Float,val height:Float){
    val diagonal = sqrt(width*width + height*height)
}