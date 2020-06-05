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
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.ArrayList

object MySheets {
    private const val APPLICATION_NAME = "Google Sheets API Java Quickstart"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private const val TOKENS_DIRECTORY_PATH = "tokens"


     const val OUT_SPREADSHEET_ID = "1vc973GbKQDt3wmhAPyfjwpkx_kIwBIOnrCbm0IqoXhg"
     const val INPUT_SPREADSHEET_ID = "1sqxP_qCWby6zS5yDqM85ZFSoALGNZMvCAmS5pOaiV0I"
     const val MARKS_SPREADSHEET_ID = "1A2N0a5cyVqCE33-TtyuJsGKVVH-MLlLBhvCxCYnMgG8"

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


    @Throws(GeneralSecurityException::class, IOException::class)
    fun getSheeService(): Sheets? {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        return if (mService==null) Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build() else mService

    }




    @Throws(IOException::class)
    fun writeList(values: ArrayList<List<Any?>>?, sheetID:String) {
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
           getSheeService()!!.spreadsheets().values().batchUpdate(sheetID, body).execute()
        System.out.printf("%d cells updated.", result.totalUpdatedCells)
    }
}