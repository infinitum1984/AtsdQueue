import com.google.api.services.sheets.v4.Sheets
import models.Student
import java.io.PrintStream
import java.util.*


class Orders(val service:Sheets, val sheetId:String) {

    private val RANGE = "students!A:C"

    lateinit var queueAny:ArrayList<List<Any?>>
    lateinit var queueStudents: ArrayList<Student>;
    lateinit var cantStudents: ArrayList<Student>;
    lateinit var unSortedStudents: ArrayList<Student>;


    init {
        fillQueue()
    }

    private fun fillQueue() {
        val response = service.spreadsheets().values()[sheetId, RANGE]
            .execute()
        val values = response.getValues()
        System.setOut(PrintStream(System.out, true, "UTF-8"))
        val reader = Marks
        val queueStudents = ArrayList<Student>()
        val tmpStudents = ArrayList<Student>()
        val cantStudents = ArrayList<Student>()
        if (values == null || values.isEmpty()) {
            println("No data found.")
        } else {
            for (i in 1 until values.size) {
                try {
                    val stud = reader.getStudent(values[i][0].toString(), values[i][1].toString())
                    stud.wantLabs = values[i][2].toString()
                   // stud.wantMark = values[i][3].toString()
                    val numLabs = stud.numLabs
                    if (numLabs == -1) {
                        tmpStudents.add(stud)
                    } else {
                        if (stud.can) {
                            queueStudents.add(stud)
                        } else {
                            cantStudents.add(stud)
                        }
                    }
                } catch (e: Exception) {
                }
            }

            sortQueue(queueStudents)

            this.queueStudents=queueStudents
            this.cantStudents=cantStudents
            unSortedStudents=tmpStudents

            println("\n\tStudents in line " + queueStudents.size)
            for (i in queueStudents.indices) {
                println(queueStudents[i].toString())
            }
            println("\n\tNumber of untreated students " + tmpStudents.size)
            for (i in tmpStudents.indices) {
                println(tmpStudents[i].toString())
            }
            println("\n\tNumber of students not allowed " + cantStudents.size)
            for (i in cantStudents.indices) {
                println(cantStudents[i].toString())
            }
            val outList =
                ArrayList<List<Any?>>()


            outList.add(Arrays.asList())
            addTurnToOutList(outList, queueStudents)
            outList.add(Arrays.asList<Any?>("Не відсортовіні студенти", "", "", ""))
            addTurnToOutList(outList, tmpStudents)
            outList.add(Arrays.asList<Any?>("Недопущені студенти", "", "", ""))
            addTurnToOutList(outList, cantStudents)
            queueAny=outList
        }
    }
    private fun addTurnToOutList(
        values: MutableList<List<Any?>>,
        queue: ArrayList<Student>
    ) {
        for (i in queue.indices) {
            val stud = queue[i]
            val stud_data = ArrayList<Any?>()
            stud_data.add(stud.name)
            stud_data.add(stud.group)
            stud_data.add(stud.numLabs.toString())
            stud_data.add(stud.wantLabs)
            stud_data.add(stud.wantMark)
            values.add(stud_data)
        }
    }
    private fun sortQueue(queue: ArrayList<Student>) {
        for (i in queue.indices) {
            for (j in 0 until queue.size - 1) {
                if (queue[j].numLabs > queue[j + 1].numLabs) {
                    val tmp = queue[j]
                    queue[j] = queue[j + 1]
                    queue[j + 1] = tmp
                }
            }
        }
        for (i in queue.indices) {
            for (j in 0 until queue.size - 1) {
                if (queue[j].numLabs == queue[j + 1].numLabs) {

                    if (queue[j].type.contains("З") && queue[j + 1].type.contains("Б")) {
                        val tmp = queue[j]
                        queue[j] = queue[j + 1]
                        queue[j + 1] = tmp
                    }
                }
            }
        }
//        for (i in queue.indices) {
//            for (j in 0 until queue.size - 1) {
//                if (queue[j].numLabs == queue[j + 1].numLabs) {
//                    if (queue[j].group!="КІУКІ-19-5" && queue[j + 1].group=="КІУКІ-19-5") {
//                        val tmp = queue[j]
//                        queue[j] = queue[j + 1]
//                        queue[j + 1] = tmp
//                    }
//                }
//            }
//        }
    }




}