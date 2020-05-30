import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import models.Student
import java.io.*
import java.security.GeneralSecurityException
import java.util.*

private const val APPLICATION_NAME = "Google Sheets API Java Quickstart"
private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
private const val TOKENS_DIRECTORY_PATH = "tokens"


private const val outSpreadSheetID = "1CEvp1lgf72zKhkjo0RiWd0ZJLIvuUMxtl66yGLbXSn0"
private const val inputSpreadSheetID = "1wx6fidB0RhF8B4J6XWPFnA1fxv5wKUq05bbwYaxiHwY"
private const val marksSpreadSheetID = "1A6kB9EBR0vUhli9mYFv9pamWe6pa0VBIdU5aLTn6LVg"

private var mService: Sheets? = null

private val SCOPES = listOf(
    SheetsScopes.SPREADSHEETS
)
private const val CREDENTIALS_FILE_PATH = "/credentials.json"

@Throws(IOException::class)
private  fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential? { // Load client secrets.
    val inputData = Student::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
        ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputData))
    // Build flow and trigger user authorization request.
    val flow = GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
    )
        .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build()
    val receiver = LocalServerReceiver.Builder().setPort(8888).build()
    return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
}

/**
 * Prints the names and majors of students in a sample spreadsheet:
 * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
 */
@Throws(IOException::class, GeneralSecurityException::class)
fun main(args: Array<String>) {
    mService = getSheeService()
    readStudents()

    val name = "Danya"



}

@Throws(GeneralSecurityException::class, IOException::class)
fun getSheeService(): Sheets? {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    return Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build()
}

@Throws(GeneralSecurityException::class, IOException::class)
fun readStudents() { // Build a new authorized API client service.
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    val spreadsheetId = inputSpreadSheetID
    val range = "students!C:F"

    val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build()

    val response = service.spreadsheets().values()[spreadsheetId, range]
        .execute()
    val values = response.getValues()
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    val reader = Marks
    reader.fillMarks(mService!!, marksSpreadSheetID)
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
                stud.wantMark = values[i][3].toString()
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
        writeList(outList)
    }
}

fun sortQueue(queue: ArrayList<Student>) {
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
    for (i in queue.indices) {
        for (j in 0 until queue.size - 1) {
            if (queue[j].numLabs == queue[j + 1].numLabs) {
                if (queue[j].group!="КІУКІ-19-5" && queue[j + 1].group=="КІУКІ-19-5") {
                    val tmp = queue[j]
                    queue[j] = queue[j + 1]
                    queue[j + 1] = tmp
                }
            }
        }
    }
}

@Throws(IOException::class)
fun writeList(values: ArrayList<List<Any?>>?) {
    val data: MutableList<ValueRange> =
        ArrayList()
    data.add(
        ValueRange()
            .setRange("queue!A:E")
            .setValues(values)
    )
    val body = BatchUpdateValuesRequest()
        .setValueInputOption("RAW")
        .setData(data)
    val result =
        mService!!.spreadsheets().values().batchUpdate(outSpreadSheetID, body).execute()
    System.out.printf("%d cells updated.", result.totalUpdatedCells)
}

fun addTurnToOutList(
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