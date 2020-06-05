import java.util.*

fun main(){
    val scanner = Scanner(System.`in`)
    val arrayStuds= arrayListOf<String>()
    var line=""
    while (line!="end"){
        line=scanner.nextLine()
        arrayStuds.add(line)
    }
    val winnerID =(0..arrayStuds.size-2).random()
    println("П'ять балів отримує ${arrayStuds[winnerID]}")

}