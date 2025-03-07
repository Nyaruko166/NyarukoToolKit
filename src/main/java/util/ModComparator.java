package util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModComparator {

    public static void main(String[] args) throws IOException {
        ModComparator mc = new ModComparator();
        String firstDir = "E:\\Games\\maicac\\Instances\\Craft to Exile 2\\mods";
        String secondDir = "E:\\Games\\maicac\\Instances\\CTE2 Nya\\mods";
        String thirdDir = "E:\\Games\\maicac\\Instances\\Craft to Exile 2 (1)\\mods";
        String stagingFolder = "E:\\Games\\maicac\\Instances\\Mod Staging";
//        List<File> lstFile = mc.compareFileListsByName(secondDir, thirdDir);

        mc.seg(firstDir, thirdDir);

//        lstFile.forEach(file -> System.out.println(file.getName()));
//        System.out.println(lstFile.size());

//        FileUtils.copyToDirectory(lstFile, new File(stagingFolder));
    }

    public List<File> compareFileListsByName(String firstDir, String secondDir) {
        File[] firstDirFileArr = new File(firstDir).listFiles((dir, name) -> name.endsWith(".jar"));
        File[] secondDirFileArr = new File(secondDir).listFiles((dir, name) -> name.endsWith(".jar"));
//        File[] thirdDirFileArr = thirdDirFile.listFiles((dir, name) -> name.endsWith(".jar"));

        assert firstDirFileArr != null;
        List<File> list1 = Arrays.asList(firstDirFileArr);
        assert secondDirFileArr != null;
        List<File> list2 = Arrays.asList(secondDirFileArr);

        // Convert list2 to a set of file names for fast lookup
        var namesInList2 = list2.stream().map(File::getName).collect(Collectors.toSet());

        // Collect files from list1 that are not in list2
        List<File> uniqueInList1 = list1.stream()
                .filter(file -> !namesInList2.contains(file.getName()))
                .toList();

        // Convert list1 to a set of file names for fast lookup
        var namesInList1 = list1.stream().map(File::getName).collect(Collectors.toSet());

        // Collect files from list2 that are not in list1
        List<File> uniqueInList2 = list2.stream()
                .filter(file -> !namesInList1.contains(file.getName()))
                .toList();

        // Combine both lists of unique files
        List<File> differences = new ArrayList<>(uniqueInList1);
        differences.addAll(uniqueInList2);
        differences.sort(Comparator.comparing(File::getName));
        return differences;
    }

    public List<String> seg(String firstDir, String secondDir) {
        String thirdDir = "C:\\Users\\ADMIN\\curseforge\\minecraft\\Instances\\CTE2";

        File[] firstDirFileArr = new File(firstDir).listFiles((dir, name) -> name.endsWith(".jar"));
        File[] secondDirFileArr = new File(secondDir).listFiles((dir, name) -> name.endsWith(".jar"));
//        File[] thirdDirFileArr = thirdDirFile.listFiles((dir, name) -> name.endsWith(".jar"));

        List<File> lst1 = Arrays.asList(firstDirFileArr);
        List<File> lst2 = Arrays.asList(secondDirFileArr);

        Set<String> set1 = Arrays.stream(firstDirFileArr).map(File::getName).collect(Collectors.toSet());
        Set<String> set2 = Arrays.stream(secondDirFileArr).map(File::getName).collect(Collectors.toSet());
//        Set<String> lst3 = Arrays.stream(thirdDirFileArr).map(File::getName).collect(Collectors.toSet());

        List<File> lstResult = new ArrayList<>(lst1);
        lstResult.addAll(lst2);

        lstResult.removeIf(file -> set1.contains(file.getName()) && set2.contains(file.getName()));

//        lstResult.addAll(lst3);
//
//        lstResult.removeIf(lst3::contains);
        lstResult.sort(Comparator.comparing(File::getName));
        lstResult.forEach(file -> System.out.println(file.getName()));
        System.out.printf("There are %d different mods%n", lstResult.size());
//        System.out.println(lst3.size());

        return null;
    }

}
