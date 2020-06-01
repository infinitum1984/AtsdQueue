import java.io.*
import java.security.GeneralSecurityException



@Throws(IOException::class, GeneralSecurityException::class)
fun main(args: Array<String>) {
    var mService = MySheets.getSheeService()

    Marks.fillMarks(mService!!, MySheets.MARKS_SPREADSHEET_ID)

    var orders = Orders(mService!!,MySheets.INPUT_SPREADSHEET_ID)

    MySheets.writeList(orders.queueAny,MySheets.OUT_SPREADSHEET_ID)
}

