import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import models.Group
import models.Student

object Marks {
    private var lisGroups = arrayListOf<Group>()
    private lateinit var mainSheet: Sheets;
    private lateinit var spreadsheetId: String;
    public fun fillMarks(mainSheet: Sheets, spreadsheetId: String){
        this.mainSheet=mainSheet
        this.spreadsheetId=spreadsheetId
        for (i in 1..10){
            lisGroups.add(readGroup(i.toString()))
        }
    }
    private fun readGroup(gropNum: String):Group{
        val range= "${gropNum}!A:U"
        val response: ValueRange = mainSheet.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
        val values = response.getValues()
        val listtudents = arrayListOf<Student>()
        for (i in 3..values.size){
            if (values.get(i).get(0).toString().equals("Середній бал групи:"))
                break;
            listtudents.add(readStudent(values.get(i),"КІУКІ-19-${gropNum}"))
        }
        val group = Group(gropNum)
        group.ListStudets=listtudents
        return group
    }
    private fun readStudent(student:List<Any>, group_name:String):Student{

        val student_name=student[0].toString();
        var numLabs=0;
        var can=false;
        var type=""
        if (student.size>1)
        for (i in 1..student.size-1){
            if(student[i].toString().contains("Д") || student[i].toString().contains("А")){
                numLabs++
            }
            if (student[i].toString().contains("З") || student[i].toString().contains("Б") ){
                can=true;
                type=student[i].toString();
                continue
            }

        }
        return Student(student_name,group_name,numLabs,can,type.trim())
    }

    fun getStudent(user:String, groupp: String): Student{
        val group_s=getGroupNum(groupp)
        val num = group_s.toInt()
        val group = lisGroups[num-1];
        for (Item in group.ListStudets){
            if (Item.name.trim().contains(user.trim())){
                return Item
            }
        }
        return Student(user,"${groupp}",-1,false,"",wantLabs = "")
    }
    private fun getGroupNum(group: String):String{
            return group.substring(group.lastIndexOf("-")+1,group.length)
    }






}