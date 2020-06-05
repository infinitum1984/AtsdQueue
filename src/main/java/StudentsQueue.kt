import com.google.api.services.sheets.v4.Sheets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

val studQueue = StudQueue(MySheets.getSheeService()!!)
fun main(args: Array<String>){

    val frame = JFrame("Вибір блока")
    frame.isVisible = true
    frame.setSize(900, 300)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    frame.isResizable=false

    val panel = JPanel()
    panel.border=BorderFactory.createEmptyBorder(50,0,0,0)
    frame.add(panel)
    val text = JLabel("Виберіть блок: ")
    panel.add(text)
    val btn1 = JButton("БЛОК 1")
    btn1.addActionListener(MyListener(1))
    panel.add(btn1)

    val btn2 = JButton("БЛОК 2")
    btn2.addActionListener(MyListener(2))
    panel.add(btn2)

    val btn3= JButton("БЛОК 3")
    btn3.addActionListener(MyListener(3))
    panel.add(btn3)

    val btn4 = JButton("БЛОК 4")
    btn4.addActionListener(MyListener(4))
    panel.add(btn4)

    val btn5 = JButton("БЛОК 5")
    btn5.addActionListener(MyListener(5))
    panel.add(btn5)

    val btn6= JButton("БЛОК 6")
    btn6.addActionListener(MyListener(6))
    panel.add(btn6)

    val btn7 = JButton("БЛОК 7")
    btn7.addActionListener(MyListener(7))
    panel.add(btn7)

    val btn8 = JButton("БЛОК 8")
    btn8.addActionListener(MyListener(8))
    panel.add(btn8)

    val btn9= JButton("БЛОК 9")
    btn9.addActionListener(MyListener(9))
    panel.add(btn9)

    val btn10= JButton("БЛОК 10")
    btn10.addActionListener(MyListener(10))
    panel.add(btn10)

}
data class QStud(
    var name:String="",
    var group: String=""
)
class MyListener(val numBlock:Int ) :ActionListener{
    override fun actionPerformed(event: ActionEvent) {
        studQueue.showRandomStudInBlock(numBlock)
    }

}
class StudQueue(val service: Sheets){
    private val RANGE = "queue_!D:E"
    private val SHEET_ID="1kbUTvax6SzYxSML5kTAkK_tuXnFYQVUioM-XHAS8fIM"
    private var listBlocksIndexes= arrayListOf<Pair<Int,Int>>()
    var studQueue:ArrayList<QStud>
    init {
        studQueue= ArrayList()
        fillQueue()

    }
    private fun fillQueue(){
        var response = service.spreadsheets().values()[SHEET_ID, RANGE]
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
        response = service.spreadsheets().values()[SHEET_ID, "queue_!B:C"].execute()
        val blocks = response.getValues()
        var prev=0
        for (index in 2 until blocks.size){
            if (blocks[index].isNotEmpty()) {
                if (blocks[index][1].toString().contains("ЛБ")) {
                    listBlocksIndexes.add(prev to index-3)
                    if (index-3==0) {
                        prev = index - 3
                    }else{
                        prev=index-2
                    }
                }
            }
        }
        listBlocksIndexes.add(prev to studQueue.size-1)

    }
    fun showRandomStud(){
        val winnerID=(0..studQueue.size-1).random()
        val winner=studQueue[winnerID]


        val msg = "<html><h1 style='font-family: Calibri; font-size: 48pt;'> Пять баллов получает!! \n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;' >${winner.name}\n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;'> ${winner.group}"
        JOptionPane.showMessageDialog(
            JFrame(),msg , "Winner!!",
            JOptionPane.INFORMATION_MESSAGE)
    }
    fun showRandomStudInBlock(block_num:Int){
        val first = listBlocksIndexes[block_num].first
        val last = listBlocksIndexes[block_num].second
        val winnerID=(first..last).random()
        val winner=studQueue[winnerID]
        println("BLOCK ${block_num} -------------------")
        for (i in first .. last){
            println(studQueue[i])
        }
        println(" -------------------")


        val msg = "<html><h1 style='font-family: Calibri; font-size: 48pt;'> П'ять білв отримує!! \n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;' >${winner.name}\n" +
                "<html><h1 style='font-family: Calibri; font-size: 48pt;'> ${winner.group}"
        JOptionPane.showMessageDialog(
            JFrame(),msg , "Winner!!",
            JOptionPane.INFORMATION_MESSAGE)
    }
}