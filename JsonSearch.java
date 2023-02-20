/*  1 . Дана строка sql-запроса "select * from students where ".
        Сформируйте часть WHERE этого запроса, используя StringBuilder. Данные для фильтрации приведены ниже в виде json строки.
        Если значение null, то параметр не должен попадать в запрос.
        Параметры для фильтрации: {"name":"Ivanov", "country":"Russia", "city":"Moscow", "age":"null"}  //*/
import java.io.File;
import java.util.Scanner;
public class JsonSearch {
    static String[] keys = new String[] {"name", "country", "city", "age"};
    static String[] select_keys = new String[keys.length];
    static String[] files_names;

    static int getIndex(String[] fields, String value){
        int index = -1;
        for(int i = 0; i < fields.length; i++){
            if(fields[i] == value){
                index = i;
                i = fields.length;
            }
        }
        return index;
    }
    static boolean fillSelect(String[] fields){
        boolean seek = false;
        try {
            Scanner iScanner = new Scanner(System.in);
            for (String key : keys) {
                System.out.printf("Введите %s: ", key);
                select_keys[getIndex(keys, key)] = iScanner.nextLine();
                if (select_keys[getIndex(keys, key)].length() == 0) {
                    select_keys[getIndex(keys, key)] = "null";
                }
                else{
                    seek = true;
                }
            }
            if(!seek) iScanner.close();
        }
        catch(Exception e){
            System.out.printf("Что-то не пошло: %s, это можно залогжить в файл", e.getMessage());
            seek = false;
        }
        return seek;
    }
    static String seekStudent(String[] fields){
        int i = 0;
        for(String value : fields){
            if (value != "null") i++;
        }
        String[] mergeKeyFields = new String[i];
        i = 0;
        for(String key : keys){
            if(fields[getIndex(keys, key)] != "null"){
                mergeKeyFields[i++] = key + ":" + fields[getIndex(keys, key)];
            }
        }
        return String.join(", ",mergeKeyFields);
    }

    public static void main(String[] args){
        String pathProject = System.getProperty ("user.dir" );
        String pathDir = pathProject .concat("/JnSh");
        File dir = new File(pathDir);
        System.out.println(dir.getAbsolutePath ());
        if (dir.mkdir()) {
            try {
                if (dir.list().length == 0) {
                    String pathFile = pathDir.concat("/students.base");
                    File file = new File(pathFile);
                    if(file.createNewFile()){
                        System.out.println("Файл базы данных студентов создан, он пуст, можно это заложить в файл");
                        File[] files = new File[keys.length];
                        byte i = 0;
                        for (String key : keys){
                            pathFile = pathDir.concat("/" + key + ".keys");
                            files[i] = new File(pathFile);
                            if(files[i].createNewFile()){
                                System.out.printf("Поисковый Файл %s.keys  базы данных студенитов создан, он пуст, можно это заложить в файл\n", key);
                            }
                        }
                    }
                    else{
                        System.out.println( "Этого не может быть, " +
                                            "файла не может существовать, " +
                                            "мы дирректорию-то только что создали," +
                                            "это тоже можно заложить в файл"            );
                    }
                }
            }
            catch(Exception e){
                System.out.printf("Что-то не пошло: %s, это можно залогжить в файл", e.getMessage());
            }
        } else {
            System.out.println("-");
        }
        for (String fname : dir.list()) {
            System.out.printf("%s \t%d\n",fname, fname.length());
        }

        while(fillSelect(select_keys)){
            System.out.println(seekStudent(select_keys));
        }

    }
}