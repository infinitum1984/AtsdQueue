import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetsMain{
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String bliz = "Б";
    private static final String zach = "З";

    private static final String outSpreadSheetID="1dN3s9kB2oePMgutAp-0hvGNtD6ygYGObMScsguAgzHk";
    private static final String inputSpreadSheetID="1zUuw9cZfpV-gPSh-NOXyVCKTuO3CjURj_dzyBHXvEZw";
    private static final String marksSpreadSheetID="1982C_txw46NqiEXE3aczP5iu_5sleGVNFgogzJANVZI";

    private static Sheets mService;
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsMain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")

                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
    public static void main(String... args) throws IOException, GeneralSecurityException {
        mService=getSheeService();

        readStudents();
    }
    static Sheets getSheeService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)

                .build();
        return service;
    }
    static void readStudents() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = inputSpreadSheetID;
        final String range = "students!C:E";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        MarksReader reader = new MarksReader(mService,marksSpreadSheetID);
        ArrayList<MarksReader.Student> queueStudents= new ArrayList<>();
        ArrayList<MarksReader.Student> tmpStudents=new ArrayList<>();
        ArrayList<MarksReader.Student> cantStudents=new ArrayList<>();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (int i=2; i <values.size();i++){
                try{
                    MarksReader.Student stud =reader.getStudent(values.get(i).get(0).toString(), values.get(i).get(1).toString());
                    stud.setWantLabs(values.get(i).get(2).toString());
                    int numLabs=stud.getNumLabs();

                    if (numLabs==-1){
                        tmpStudents.add(stud);
                    }else {
                        if (stud.getCan()){
                            queueStudents.add(stud);
                        }else {
                            cantStudents.add(stud);
                        }
                    }

                }catch (Exception e){

                }
            }
            sortQueue(queueStudents);
            ArrayList<MarksReader.Student> group9=new ArrayList<>();
            for (int i=0;i<queueStudents.size();i++){
                if (queueStudents.get(i).getGroup().contains("19-9")){
                    group9.add(queueStudents.get(i));
                    queueStudents.remove(i);
                }
            }
            System.out.println("\n\tStudents in nine "+group9.size());

            for (int i=0;i<group9.size();i++){
                System.out.println(group9.get(i).toString());
            }
            System.out.println("\n\tStudents in line "+queueStudents.size());
            for (int i=0;i<queueStudents.size();i++){
                System.out.println(queueStudents.get(i).toString());
            }



            System.out.println("\n\tNumber of untreated students "+tmpStudents.size());
            for (int i=0;i<tmpStudents.size();i++){
                System.out.println(tmpStudents.get(i).toString());
            }

            System.out.println("\n\tNumber of students not allowed "+cantStudents.size());
            for (int i=0;i<cantStudents.size();i++){
                System.out.println(cantStudents.get(i).toString());
            }

            ArrayList<List<Object>> outList = new ArrayList<>();

            group9.addAll(queueStudents);
            outList.add(Arrays.asList());

            addTurnToOutList(outList,group9);


            outList.add(Arrays.asList("Unsampled students","","",""));
            addTurnToOutList(outList,tmpStudents);
            outList.add(Arrays.asList("Unacceptable students","","",""));
            addTurnToOutList(outList,cantStudents);
            writeList(outList);

        }
    }
    static void sortQueue(ArrayList<MarksReader.Student> queue){
        for (int i=0;i<queue.size();i++){
            for (int j=0;j<queue.size()-1;j++){


                if(queue.get(j).getNumLabs()>queue.get(j+1).getNumLabs()){
                    MarksReader.Student tmp = queue.get(j);
                    queue.set(j,queue.get(j+1));
                    queue.set(j+1,tmp);

                }
            }
        }
        for (int i=0;i<queue.size();i++){
            for (int j=0;j<queue.size()-1;j++){
                if(queue.get(j).getNumLabs()==queue.get(j+1).getNumLabs()){

//                    if (CompareKt.isZach(queue.get(j).getType()) && CompareKt.isBliz(queue.get(j+1).getType())) {
//                        MarksReader.Student tmp = queue.get(j);
//                        queue.set(j, queue.get(j + 1));
//                        queue.set(j + 1, tmp);
//                    }

                }

            }
        }

    }
    static void writeList(ArrayList<List<Object>> values) throws IOException {
      List<ValueRange> data = new ArrayList<>();
        data.add(new ValueRange()
                .setRange("queue!A:D")
                .setValues(values));
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(data);

        BatchUpdateValuesResponse result =
                mService.spreadsheets().values().batchUpdate(outSpreadSheetID, body).execute();
        System.out.printf("%d cells updated.", result.getTotalUpdatedCells());
    }
    static void addTurnToOutList(List<List<Object>> values,ArrayList<MarksReader.Student> queue){
        for (int i=0;i<queue.size();i++){
            MarksReader.Student stud = queue.get(i);
            ArrayList<Object> stud_data= new ArrayList();
            stud_data.add(stud.getName());
            stud_data.add(stud.getGroup());
            stud_data.add(String.valueOf(stud.getNumLabs()));
            stud_data.add(stud.getWantLabs());
            values.add(stud_data);
        }
    }


}
