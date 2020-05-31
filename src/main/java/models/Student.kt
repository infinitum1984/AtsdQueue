package models

data class Student(val name: String, val group: String,val numLabs:Int,
                   val can:Boolean,val type: String, var wantLabs: String="",
                   var wantMark: String=""){

}