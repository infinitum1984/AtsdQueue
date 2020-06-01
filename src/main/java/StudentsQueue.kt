import com.google.api.services.sheets.v4.Sheets
import java.awt.Font
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane


fun main(){
    val studQueue = StudQueue(MySheets.getSheeService()!!)
    studQueue.getRandomStud()
}
data class QStud(
    var name:String="",
    var group: String=""
)
class StudQueue(val service: Sheets){
    private val RANGE = "queue_!D:E"
    private val SHEET_ID="1kbUTvax6SzYxSML5kTAkK_tuXnFYQVUioM-XHAS8fIM"

    var studQueue:ArrayList<QStud>
    init {
        studQueue= ArrayList()
        fillQueue()

    }
    private fun fillQueue(){
        val response = service.spreadsheets().values()[SHEET_ID, RANGE]
            .execute()
        val values = response.getValues()
        if (values!=null)
            for (i in 2 until values.size){
                try {
                    val name = values[i][0].toString()
                    if (name==null || name.isEmpty()) break

                    val group= values[i][1].toString()
                    if (group==null || group.isEmpty()) break

                    val stud = QStud(name,group)
                    studQueue.add(stud)
                }catch (e:Exception){
                    break
                }
            }
        for (stud in studQueue){
            println(stud)
        }

    }
    fun getRandomStud(){
        val winnerID=(0..studQueue.size-1).random()
        val winner=studQueue[winnerID]


        val msg = "<html><h1 style='font-family: Calibri; font-size: 48pt;'> Пять баллов получает!! \n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;' >${winner.name}\n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;'> ${winner.group}"
        JOptionPane.showMessageDialog(
            JFrame(),msg , "Winner!!",
            JOptionPane.INFORMATION_MESSAGE)
    }
}