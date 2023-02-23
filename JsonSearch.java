/*  1 . Дана строка sql-запроса "select * from students where ".
        Сформируйте часть WHERE этого запроса, используя StringBuilder. Данные для фильтрации приведены ниже в виде json строки.
        Если значение null, то параметр не должен попадать в запрос.
        Параметры для фильтрации: {"name":"Ivanov", "country":"Russia", "city":"Moscow", "age":"null"}  //*/
import Lesson_02.Ex005_Logger;

import java.io.*;
import java.util.Scanner;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class JsonSearch {
    static Logger logger = Logger.getLogger(JsonSearch.class.getName());
    static FileHandler fh;
    static Scanner iScanner = new Scanner(System.in);
    static String[] keys = new String[] {"name", "country", "city", "age"};
    static String[] selectKeys = new String[keys.length]; // Сам Json поисковый запрос
    static String[] filesNames = new String[keys.length];
    static boolean[] filesChanged = new boolean[keys.length];
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
                logger.log(Level.WARNING, String.format("Отсутствует параметр %s в файле %s, " +
                        "можно это заложить в файл", "length:", path));
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
                                logger.log(Level.WARNING, String.format("В файле %s отличаются приведённое " +
                                        "количество студентов:%s от фактического:%s", path, lastInt[0], students));
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
            logger.log(Level.WARNING, String.format("Что-то не пошло в файле %s: %s, строка№114", path, e.getMessage()));
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
        //System.out.println(dir.getAbsolutePath ());
        if(dir.mkdir()){
            try{
                if(dir.list().length == 0){
                    fh = new FileHandler(pathDir.concat("/log.txt"), true);
                    logger.addHandler(fh);
                    SimpleFormatter sFormat = new SimpleFormatter();
                    fh.setFormatter(sFormat);

                    String pathFile = pathDir.concat("/students.base");
                    File file = new File(pathFile);
                    if(file.createNewFile()){
                        FileWriter fileWriter = new FileWriter(file, false);
                        fileWriter.write("length:0");
                        fileWriter.append(System.lineSeparator());
                        fileWriter.flush();
                        fileWriter.close();
                        studentsBase = readFile(pathFile);
                        logger.log(Level.INFO, String.format("Файл базы данных студентов создан, он пуст"));
                        File[] files = new File[keys.length];
                        for (String key : keys){
                            int i = getIndex(keys, key);
                            pathFile = pathDir.concat("/" + key + ".keys");
                            files[i] = new File(pathFile);
                            if(files[i].createNewFile()){
                                FileWriter filesWriter = new FileWriter(files[i], false);
                                filesWriter.write("length:0");
                                filesWriter.append(System.lineSeparator());
                                filesWriter.flush();
                                filesWriter.close();
                                logger.log(Level.INFO, String.format("Поисковый Файл %s.keys  базы данных студенитов создан, он пуст", key));
                            }
                            indexesByWord[i] = readFile(pathFile);
                            filesChanged[i] = false;
                        }
                    }
                    else{
                        logger.log(Level.WARNING, String.format( "Файла не может существовать, " +
                                                                 "мы дирректорию-то только что создали, Строка №161"));
                    }
                }
            }
            catch(Exception e){
                logger.log(Level.WARNING, String.format("Что-то не пошло: %s, момент создания файлов, строка№168", e.getMessage()));
            }
        }
        else{
            try{
                fh = new FileHandler(pathDir.concat("/log.txt"), true);
                logger.addHandler(fh);
                SimpleFormatter sFormat = new SimpleFormatter();
                fh.setFormatter(sFormat);

                String pathFile = pathDir.concat("/students.base");
                studentsBase = readFile(pathFile);
                for (String key : keys) {
                    int i = getIndex(keys, key);
                    pathFile = pathDir.concat("/" + key + ".keys");
                    indexesByWord[i] = readFile(pathFile);
                    filesChanged[i] = false;
                }

            }
            catch(Exception e){
                logger.log(Level.WARNING, String.format("Что-то не пошло: %s, момент загрузки файлов, строка№188", e.getMessage()));
            }
        }
        logger.log(Level.INFO, String.format("Прогамма запущена пользователем"));
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
            logger.log(Level.WARNING, String.format("Что-то не пошло: %s, момент ввода ключа пользователем, строка №222%s", e.getMessage(), e.fillInStackTrace().toString()));
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
        for(String key : keys){
            int i = getIndex(keys, key);
            if(fields[i]!= null){
                if(findIndexesByWord[i] != null){
                    if(findStudents == null){
                        findStudents = getSliceInt(findIndexesByWord[i], 0, findIndexesByWord[i].length - 1);
                    }
                    else{
                        if((findStudents = getMatchedOfCoupleIndexes(findStudents, findIndexesByWord[i])) == null)
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
            if(findStudents == null){
                String sNewStudent = getSelectString(fields);
                System.out.println("Студента: {"+ sNewStudent +"} не было в базе и мы его добавили");
                int newStudent = addNewStudentToStudentsBase(sNewStudent);
                if(newStudent > 0){
                    int indexKey;
                    for(String key : keys){
                        indexKey = getIndex(keys, key);
                        filesChanged[indexKey] = addIndexOfNewStudentToDataBase(
                                                                                indexKey,
                                                                                fields[indexKey],
                                                                                newStudent);
                    }
                    logger.log(Level.INFO, String.format("Студента: {%s} добавили в базу данных", sNewStudent));
                }
                else{
                    logger.log(Level.WARNING, String.format("Студента: {%s} НЕ добавили в базу данных", sNewStudent));
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
                System.out.println("Поиск по запросу: {"+getSelectString(fields)+"} не дал результата");
            }
            else{
                System.out.println("Поиск по запросу: {"+getSelectString(fields)+"} дал следующий результат:");
                for(int student: findStudents){
                    System.out.println(studentsBase[student].toString());
                }
            }
        }
    }

    static StringBuilder[] increaseTheSizeStringBuilderArray(StringBuilder[] base, int increaseDelta){
        StringBuilder[] increasedBase = new StringBuilder[base.length + increaseDelta];
        for(int i = 0; i < base.length; i++){
            if(base[i] != null) {
                increasedBase[i] = new StringBuilder().append(base[i].toString());
            }
            else{
                break;
            }
        }
        return increasedBase;
    }

    static int addNewStudentToStudentsBase(String sNewStudent){
        int iNewStudent = -1;

        int[] lastInt = new int[2];
        lastInt[1] = studentsBase[0].indexOf(":");
        int length = (lastInt = getInt(studentsBase[0].toString(), lastInt[1]))[0];
        lastInt[1] = studentsBase[0].indexOf(":", lastInt[1]);
        int students = (lastInt = getInt(studentsBase[0].toString(), lastInt[1]))[0];
        if(students < 0){
            students = 0;
            logger.log(Level.WARNING, String.format("В файле базы данных отсутствует второй параметр: \"students\""));
        }

        for(int i = length + 1; i < studentsBase.length; i++){
            if(studentsBase[i] == null){
                studentsBase[i] = new StringBuilder().append(sNewStudent);
                iNewStudent = i;
                length++;
                students++;
                if(i == studentsBase.length - 1){
                    studentsBase = increaseTheSizeStringBuilderArray(studentsBase, deltaFreeSpase);
                }
                break;
            }
        }
        if(iNewStudent > 0){
            studentsBase[0].delete(0, studentsBase[0].length()).append(String.format("length:%d, students:%d",length, students));
        }
        else{
            logger.log(Level.WARNING, String.format("В оперативной структуре файла базы данных закончилось свободное пространство"));
        }
        return iNewStudent;
    }

    static boolean addIndexOfNewStudentToDataBase(int indexKey, String sWordNewStudent, int iNewStudent){
        boolean success = false;

        int[] lastInt = new int[2];
        lastInt[1] = indexesByWord[indexKey][0].indexOf(":");
        int length = (lastInt = getInt(indexesByWord[indexKey][0].toString(), lastInt[1]))[0];
        lastInt[1] = indexesByWord[indexKey][0].indexOf(":", lastInt[1]);
        int students = (lastInt = getInt(indexesByWord[indexKey][0].toString(), lastInt[1]))[0];
        if(students < 0){
            students = 0;
            logger.log(Level.WARNING, String.format("В поисковом файле базы данных отсутствует второй параметр: \"students\""));
        }

        for(int i = 0; i < indexesByWord[indexKey].length; i++){
            if(indexesByWord[indexKey][i] == null){
                indexesByWord[indexKey][i] = new StringBuilder().append(String.format("%s:%d",sWordNewStudent,iNewStudent));
                success = true;
                length++;
                students++;
                if(i == indexesByWord[indexKey].length - 1){
                    indexesByWord[indexKey] = increaseTheSizeStringBuilderArray(indexesByWord[indexKey], deltaFreeSpase);
                }
                break;
            }
            else{
                if(indexesByWord[indexKey][i].lastIndexOf(sWordNewStudent+":") >= 0){
                    indexesByWord[indexKey][i].append(String.format(",%d",iNewStudent));
                    success = true;
                    students++;
                    break;
                }
            }
        }
        if(success){
            indexesByWord[indexKey][0].delete(0, indexesByWord[indexKey][0].length()).append(String.format("length:%d, students:%d",length, students));
        }
        else{
            logger.log(Level.WARNING, String.format("В оперативной структуре поискового файла базы " +
                    "данных закончилось свободное пространство для ключа %s", sWordNewStudent));
        }
        return success;
    }

    static boolean ifSearchQueryFull(String[] fields){
        boolean bSearchQueryFull = true;
        for(String word: fields){
            if(word == null){
                bSearchQueryFull = false;
                break;
            }
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
                        logger.log(Level.WARNING, String.format("Что-то не пошло c файлом %s во время сохранения: %s, строка №440", pathFile, e.getMessage()));
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
                    logger.log(Level.WARNING, String.format("Что-то не пошло c файлом %s во время сохранения: %s, это можно залогжить в файл",pathFile, e.getMessage()));
                }
            }
        }
        iScanner.close();
        logger.log(Level.INFO, String.format("Прогамма завершена пользователем"));
    }

    public static void main(String[] args){
        startInitialFiles();
        while(fillSelect(selectKeys)){
            seekStudent(selectKeys);
        }
        stopAndQuit();
    }
}