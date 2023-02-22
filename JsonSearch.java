/*  1 . Дана строка sql-запроса "select * from students where ".
        Сформируйте часть WHERE этого запроса, используя StringBuilder. Данные для фильтрации приведены ниже в виде json строки.
        Если значение null, то параметр не должен попадать в запрос.
        Параметры для фильтрации: {"name":"Ivanov", "country":"Russia", "city":"Moscow", "age":"null"}  //*/
import java.io.*;
import java.util.Scanner;
public class JsonSearch {

    static Scanner iScanner = new Scanner(System.in);
    static String[] keys = new String[] {"name", "country", "city", "age"};
    static String[] selectKeys = new String[keys.length]; // Сам Json поисковый запрос
    static String[] filesNames = new String[keys.length];
    static boolean[] filesChanged = new boolean[] {false, false, false, false};

    static int deltaFreeSpase = 10;
    static StringBuilder[][] indexesByWord = new StringBuilder[keys.length][];
    static StringBuilder[] studentsBase = null;

    static int[][] findIndexesByWord = new int[keys.length][];
    static StringBuilder[] matchedStudents = null;


    static int[] getInt(String user_task, int index){
        int[] takeInt = new int[] {-1, index};
        StringBuilder arg = new StringBuilder();
        for(;index < user_task.length(); index++){
            switch (user_task.charAt(index)) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9': {
                    arg.append(user_task.charAt(index));
                    takeInt[1] = index + 1;
                }break;
                default:{
                    if(arg.length() != 0){
                        takeInt[1] = index;
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
        }
        else{
            String pathFile = pathDir.concat("/students.base");
            studentsBase = readFile(pathFile);
            for(String key : keys){
                pathFile = pathDir.concat("/" + key + ".keys");
                indexesByWord[getIndex(keys, key)] = readFile(pathFile);
            }
            filesChanged = new boolean[] {false, false, false, false};
        }
/*        for (String fname : dir.list()) {
            System.out.printf("%s \t%d\n",fname, fname.length());
        } //*/
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
            for (String key : keys) {
                System.out.printf("Введите %s: ", key);
                selectKeys[getIndex(keys, key)] = iScanner.nextLine();
                if (selectKeys[getIndex(keys, key)].length() == 0) {
                    selectKeys[getIndex(keys, key)] = null;
                    findIndexesByWord[getIndex(keys, key)] = null;
                }
                else{
                    findIndexesByWord[getIndex(keys, key)] = fillFindIndexesByWord(selectKeys[getIndex(keys, key)], indexesByWord[getIndex(keys, key)]);
                    seek = true;
                }
            }
        }
        catch(Exception e){
            System.out.printf("Что-то не пошло: %s, это можно залогжить в файл", e.getMessage());
            seek = false;
        }
        return seek;
    }
    static String getSelectString(String[] fields){
        int i = 0;
        for(String value : fields){
            if (value != null) i++;
        }
        String[] mergeKeyFields = new String[i];
        i = 0;
        for(String key : keys){
            if(fields[getIndex(keys, key)] != null){
                mergeKeyFields[i++] = key + ":" + fields[getIndex(keys, key)];
            }
            else{}
        }
        return String.join(", ",mergeKeyFields);
    }
    static void seekStudent(String[] fields){
        int[] findStudents = null;
        for (String key : keys) {
            int i = getIndex(keys, key);
            if(fields[i]!= null) {
                if (findIndexesByWord[i] != null) {
                    if (findStudents == null) {
                        findStudents = getSliceInt(findIndexesByWord[i], 0, findIndexesByWord[i].length - 1);
                    } else {
                        if ((findStudents = getMatchedOfCoupleIndexes(findStudents, findIndexesByWord[i])) == null)
                            break;
                    }
                }
                else{
                    findStudents = null;
                    break;
                }
            }
        }
        if(ifSearchQueryFull(fields)){
            if (findStudents == null){
                System.out.println("Студента: {"+getSelectString(fields)+"} не было в базе и мы его добавили");
                int newStudent = addNewStudentToStudentsBase(studentsBase, getSelectString(fields));
                if(newStudent > 0){
                    int indexKey;
                    for(String key : keys){
                        indexKey = getIndex(keys, key);
                        filesChanged[indexKey] = addIndexOfNewStudentToDataBase(
                                                                                indexesByWord[indexKey],
                                                                                fields[indexKey],
                                                                                newStudent);
                    }
                    System.out.println("У нас получилось");
                }


            }
            else{
                System.out.println("Поиск по запросу: {"+getSelectString(fields)+"} дал следующий результат:");
                for(int student: findStudents){
                    System.out.println(studentsBase[student].toString());
                }
            }
        }
        else{
            if (findStudents == null){
                System.out.println("Поиск по запросу: {"+getSelectString(fields)+"} не дал следующий результата");
            }
            else{
                System.out.println("Поиск по запросу: {"+getSelectString(fields)+"} дал следующий результат:");
                for(int student: findStudents){
                    System.out.println(studentsBase[student].toString());
                }
            }
        }
    }

    static int addNewStudentToStudentsBase(StringBuilder[] base, String sNewStudent){
        int iNewStudent = -1;

        int[] lastInt = new int[2];
        lastInt[1] = base[0].indexOf(":");
        int length = (lastInt = getInt(base[0].toString(), lastInt[1]))[0];
        lastInt[1] = base[0].indexOf(":", lastInt[1]);
        int students = (lastInt = getInt(base[0].toString(), lastInt[1]))[0];
        if(students < 0) students = 0;

        for(int i = base.length - deltaFreeSpase; i < base.length; i++){
            if(base[i] == null){
                base[i] = new StringBuilder().append(sNewStudent);
                iNewStudent = i;
                length++;
                students++;
                break;
            }
        }
        if(iNewStudent > 0){
            base[0].delete(0, base[0].length()).append(String.format("length:%d, students:%d",length, students));
        }
        return iNewStudent;
    }
    static boolean addIndexOfNewStudentToDataBase(StringBuilder[] base, String sWordNewStudent, int iNewStudent){
        boolean success = false;

        int[] lastInt = new int[2];
        lastInt[1] = base[0].indexOf(":");
        int length = (lastInt = getInt(base[0].toString(), lastInt[1]))[0];
        lastInt[1] = base[0].indexOf(":", lastInt[1]);
        int students = (lastInt = getInt(base[0].toString(), lastInt[1]))[0];
        if(students < 0) students = 0;

        for(int i = 0; i < base.length; i++){
            if(base[i] == null){
                base[i] = new StringBuilder().append(String.format("%s:%d",sWordNewStudent,iNewStudent));
                success = true;
                length++;
                students++;
                break;
            }
            else{
                if(base[i].lastIndexOf(sWordNewStudent+":") >= 0){
                    base[i].append(String.format(",%d",iNewStudent));
                    success = true;
                    students++;
                    break;
                }
            }
        }

        if(success) base[0].delete(0, base[0].length()).append(String.format("length:%d, students:%d",length, students));

        return success;
    }
    static boolean ifSearchQueryFull(String[] fields){
        boolean bSearchQueryFull = true;
        for(String word: fields){
            if(word == null) bSearchQueryFull = false; break;
        }
        return bSearchQueryFull;
    }
    static int[] getMatchedOfCoupleIndexes(int[] first, int[] second){
        int[] matched = new int[first.length];
        int matcedValue = 0;
        for(int i = 0; i < first.length; i++){
            for(int j = 0; j < second.length; j++){
                if(first[i] == second[j]){
                    matched[matcedValue++] = first[i];
                    j = second.length;
                }
            }
        }
        matched = getSliceInt(matched, 0, matcedValue-1);
        return matched;
    }

    static int[] fillFindIndexesByWord(String word, StringBuilder[] indByWord){
        int[] fillIndexses = null;
        StringBuilder keyElement;
        int[] lastInt = new int[2];
        for(int i = 1; i <= indByWord.length; i++){
            if((keyElement =  indByWord[i]) != null) {
                lastInt[1] = keyElement.lastIndexOf(word);
                if ((lastInt[1]) >= 0) {
                    lastInt[1] = keyElement.lastIndexOf(":");
                    fillIndexses = new int[keyElement.toString().length()]; // это грубый подсчёт
                    int j = 0;
                    for (; (lastInt = getInt(keyElement.toString(), lastInt[1]))[0] > 0; j++) {
                        fillIndexses[j] = lastInt[0];
                    }
                /*fillIndexses[j] = -1;                       // обозначили границу полезного массива
                fillIndexses[fillIndexses.length-1] = j;    // зафиксировали полезную длину массива*/
                    fillIndexses = getSliceInt(fillIndexses, 0, j - 1);
                    i = indByWord.length;              // прерываем цикл поиска ключа, тк он был найден и обработан
                }
            }
            else{
                i = indByWord.length;              // прерываем цикл поиска ключа, дошли до пустого ключа
            }
        }
        return fillIndexses;
    }
    static int[] getSliceInt(int[] mass, int first, int last){
        int[] slice = null;
        if(last >= mass.length)  last = mass.length - 1;
        if(last >= first){
            slice = new int[last - first + 1];
            for(int i = 0; i < slice.length; i++, first++){
                slice[i] = mass[first];
            }
        }
        return slice;
    }
    static void stopAndQuit(){
        boolean saveStudentBase = false;
        String pathProject = System.getProperty ("user.dir" );
        String pathDir = pathProject .concat("/JnSh");

        for(String key: keys){
            int indexKey = getIndex(keys, key);
            if(filesChanged[indexKey]){
                saveStudentBase = true;
                String pathFile = pathDir.concat("/" + key + ".keys");
                File file = new File(pathFile);
                if(file.isFile()) {
                    try {
                        FileWriter fileWriter = new FileWriter(file, false);
                        for(int i = 0; i < indexesByWord[indexKey].length; i++){
                            if(indexesByWord[indexKey][i] != null) {
                                fileWriter.write(indexesByWord[indexKey][i].toString());
                                fileWriter.append(System.lineSeparator());
                            }
                            else{
                                i = indexesByWord[indexKey].length;
                            }
                        }
                        fileWriter.flush();
                        fileWriter.close();
                    }
                    catch(Exception e){
                        System.out.printf("Что-то не пошло c файлом %s во время сохранения: %s, это можно залогжить в файл",pathFile, e.getMessage());
                    }
                }
            }
        }
        if(saveStudentBase){
            String pathFile = pathDir.concat("/students.base");
            File file = new File(pathFile);
            if(file.isFile()) {
                try {
                    FileWriter fileWriter = new FileWriter(file, false);
                    for(int i = 0; i < studentsBase.length; i++){
                        if(studentsBase[i] != null) {
                            fileWriter.write(studentsBase[i].toString());
                            fileWriter.append(System.lineSeparator());
                        }
                        else{
                            i = studentsBase.length;
                        }
                    }
                    fileWriter.flush();
                    fileWriter.close();
                }
                catch(Exception e){
                    System.out.printf("Что-то не пошло c файлом %s во время сохранения: %s, это можно залогжить в файл",pathFile, e.getMessage());
                }
            }
        }
        iScanner.close();
    }

    public static void main(String[] args){
        startInitialFiles();
        while(fillSelect(selectKeys)){
            seekStudent(selectKeys);
        }
        stopAndQuit();
    }
}