/*  1 . Дана строка sql-запроса "select * from students where ".
        Сформируйте часть WHERE этого запроса, используя StringBuilder. Данные для фильтрации приведены ниже в виде json строки.
        Если значение null, то параметр не должен попадать в запрос.
        Параметры для фильтрации: {"name":"Ivanov", "country":"Russia", "city":"Moscow", "age":"null"}  //*/
import java.io.*;
import java.util.Scanner;
public class JsonSearch {
    static String[] keys = new String[] {"name", "country", "city", "age"};
    static String[] selectKeys = new String[keys.length];
    static String[] filesNames = new String[keys.length];
    static boolean[] filesChanged = new boolean[keys.length];

    static int deltaFreeSpase = 10;
    static StringBuilder[][] findIndexes = new StringBuilder[keys.length][];

    static int[] getInt(String user_task, int index){
        int[] takeInt = new int[] {-1, index};
        StringBuilder arg = new StringBuilder();
        for(;index < user_task.length(); index++){
            switch (user_task.charAt(index)) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                    arg.append(user_task.charAt(index));
                }break;
                default:{
                    if(arg.length() != 0){
                        takeInt[1] = index - 1;
                        index = user_task.length();
                    }
                }
            }
        }
        if(arg.length() == 0){
            takeInt[1] = user_task.length();
        }
        else{
            takeInt[0] = Integer.parseInt(arg.toString());
        }
        return takeInt;
    }
    static StringBuilder[] readFile(String path){
        StringBuilder[]  findIndex;
        StringBuilder    element = new StringBuilder();
        int lastIndex;
        int[] lastInt = new int[] {0, -1};
        try {
            BufferedReader file = new BufferedReader(new FileReader(path));
            String str = file.readLine();
            lastIndex = str.lastIndexOf("length:");
            if(lastIndex < 0){
                System.out.printf("Отсутствует параметр %s в файле %s, можно это заложить в файл", "length:", path);
                findIndex = new StringBuilder[1 + deltaFreeSpase];
                element.append("length:0,students:0" + System.lineSeparator());
                findIndex[0] = element;
            }
            else{
                lastInt = getInt(str, lastIndex);
                if(lastInt[0] <= 0){
                    findIndex = new StringBuilder[1 + deltaFreeSpase];
                    element.append("length:0,students:0" + System.lineSeparator());
                    findIndex[0] = element;
                }
                else{
                    int students = 0;       // это не прочитанное значение из файла, а заготовленный счетчик для последующего контроля "целостности" файла
                    int lengthFile = lastInt[0];
                    findIndex = new StringBuilder[lengthFile + deltaFreeSpase];
                    findIndex[0] = new StringBuilder();
                    findIndex[0].append(str);
                    for(int i = 1; i <= lengthFile; i++){
                        str = file.readLine();
                        if(str == null){
                            str = findIndex[0].toString();
                            lastIndex = str.lastIndexOf("students:");
                            lastInt = getInt(str, lastIndex);
                            findIndex[0].delete(0, findIndex[0].length());
                            if(lastInt[0] != students){
                                System.out.printf(  "В файле %s отличаются приведённое " +
                                                    "количество студентов:%s от фактического:%s, " +
                                                    "можно это заложить в файл", path, lastInt[0], students);
                            }
                            findIndex[0].append("length:" + String.valueOf(lengthFile) + ",students:" + String.valueOf(students));
                        }
                        else{
                            findIndex[i] = new StringBuilder();
                            findIndex[i].append(str);
                            lastInt[1] = str.lastIndexOf(":");
                            while ((lastInt = getInt(str, lastInt[1]))[0] > 0){
                                students += 1;
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){
            System.out.printf("Что-то не пошло в файле %s: %s, это можно залогжить в файл", path, e.getMessage());
            findIndex = new StringBuilder[1  + deltaFreeSpase];
            element.append("length:0,students:0" + System.lineSeparator());
            findIndex[0] = element;
        }
        return findIndex;
    }
    static void startInitialFiles(){
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
                        FileWriter fileWriter = new FileWriter(file, false);
                        fileWriter.write("length:0");
                        fileWriter.append(System.lineSeparator());
                        fileWriter.flush();
                        fileWriter.close();
                        System.out.println("Файл базы данных студентов создан, он пуст, можно это заложить в файл");
                        File[] files = new File[keys.length];
                        byte i = 0;
                        for (String key : keys){
                            pathFile = pathDir.concat("/" + key + ".keys");
                            files[i] = new File(pathFile);
                            if(files[i].createNewFile()){
                                FileWriter filesWriter = new FileWriter(files[i], false);
                                filesWriter.write("length:0");
                                filesWriter.append(System.lineSeparator());
                                filesWriter.flush();
                                filesWriter.close();
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
    }

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
                selectKeys[getIndex(keys, key)] = iScanner.nextLine();
                if (selectKeys[getIndex(keys, key)].length() == 0) {
                    selectKeys[getIndex(keys, key)] = "null";
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
        startInitialFiles();
        while(fillSelect(selectKeys)){
            System.out.println(seekStudent(selectKeys));
        }

    }
}