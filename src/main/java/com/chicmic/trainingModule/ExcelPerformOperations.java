package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class ExcelPerformOperations {
    public static Course excelPerformOperations(InputStream inputStream, String fileNameWithExtension) {
        List<Phase<Task>> phaseList = new ArrayList<>();
        boolean firstRowSkipped = false;
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            String phaseName = "";
            String mainTask = "";

            Phase<Task> phase = null;
            Task courseTask = null;
            for (Row row : sheet) {
                if (!firstRowSkipped) {
                    firstRowSkipped = true;
                    continue;
                }
                Cell phaseCell = row.getCell(0);
                Cell mainTaskCell = row.getCell(1);
                Cell subTaskCell = row.getCell(2);
                Cell hoursCell = row.getCell(3);
                Cell referenceCell = row.getCell(4);

                if (phaseCell != null && !phaseCell.getStringCellValue().isEmpty()) {
                    phaseName = phaseCell.getStringCellValue();
                    phase = new Phase<>();
                    phase.setTasks(new ArrayList<>());
                    phaseList.add(phase);
                }

                if (mainTaskCell != null && !mainTaskCell.getStringCellValue().isEmpty()) {
                    mainTask = mainTaskCell.getStringCellValue();
                    courseTask = new Task();

                    courseTask.setMainTask(mainTask);
                    courseTask.setSubtasks(new ArrayList<>());

                    if (phase != null) {
                        List<Task> tasks = phase.getTasks();
                        tasks.add(courseTask);
                        phase.setTasks(tasks);
                    }
                }

                if (subTaskCell != null && hoursCell != null && referenceCell != null &&
                        !subTaskCell.getStringCellValue().isEmpty() && hoursCell.getCellType() == CellType.NUMERIC) {
                    SubTask courseSubTask = new SubTask();
                    courseSubTask.setSubTask(subTaskCell.getStringCellValue().trim());
                    courseSubTask.setEstimatedTime(convertToTimeFormat(hoursCell.getNumericCellValue()).trim());

                    if (referenceCell.getHyperlink() != null) {
                        String linkValue = referenceCell.getHyperlink().getAddress().trim();
                        courseSubTask.setLink(linkValue);
                    } else {
                        String linkValue = referenceCell.getStringCellValue().trim();
                        courseSubTask.setLink(linkValue);
                    }

                    if (phase != null && phase.getTasks().size() > 0) {
                        List<SubTask> subTasks = courseTask.getSubtasks();
                        subTasks.add(courseSubTask);
                        courseTask.setSubtasks(subTasks);
                    }
                }
            }

            workbook.close(); // Close the workbook when done
            inputStream.close(); // Close the input stream when done
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String fileNameWithoutExtension = null;
        if (fileNameWithExtension != null) {
            int lastDotIndex = fileNameWithExtension.lastIndexOf('.');
            if (lastDotIndex != -1) {
                fileNameWithoutExtension = fileNameWithExtension.substring(0, lastDotIndex);
            } else {
                fileNameWithoutExtension = fileNameWithExtension;
            }
        }
        System.out.println("file name = " + fileNameWithoutExtension);
        Course course = new Course();
        course.setName(fileNameWithoutExtension);
        course.setFigmaLink("");
        course.setGuidelines("");
        course.setIsDeleted(false);
        course.setIsApproved(true);
        course.setPhases(phaseList);
        course.setApprover(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "5e33ce4411f3a90cf90a0f23"
        )));
        course.setCreatedBy("61fba5d5f4f70d6c0b3eff40");
//        course.setn("Priti Mittal");
        course.setApprovedBy(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "5e33ce4411f3a90cf90a0f23"
        )));

        return course;
    }

    public static Course excelPerformOperations(String excelFilePath) {
        List<Phase<Task>> phaseList = new ArrayList<>();
        boolean firstRowSkipped = false;
        try {
            File excelFile = new File(excelFilePath);
            FileInputStream fis = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            String phaseName = "";
            String mainTask = "";

            Phase<Task> phase = null;
            Task courseTask = null;
            for (Row row : sheet) {
                if (!firstRowSkipped) {
                    firstRowSkipped = true;
                    continue;
                }
                Cell phaseCell = row.getCell(0);
                Cell mainTaskCell = row.getCell(1);
                Cell subTaskCell = row.getCell(2);
                Cell hoursCell = row.getCell(3);
                Cell referenceCell = row.getCell(4);

                if (phaseCell != null && !phaseCell.getStringCellValue().isEmpty()) {
                    phaseName = phaseCell.getStringCellValue();
                    phase = new Phase<>();
                    phase.setTasks(new ArrayList<>());
                    phaseList.add(phase);
                }

                if (mainTaskCell != null && !mainTaskCell.getStringCellValue().isEmpty()) {
                    mainTask = mainTaskCell.getStringCellValue();
                    System.out.println("manTask" + mainTask);
                    courseTask = new Task();

                    courseTask.setMainTask(mainTask);
                    courseTask.setSubtasks(new ArrayList<>());
//                    System.out.println("\u001B[31m Task \u001B[0m" + courseTask);
                    if (phase != null) {
                        System.out.println("im in");
                        List<Task> tasks = phase.getTasks();
                        tasks.add(courseTask);
                        phase.setTasks(tasks);
                    }
                }

                if (subTaskCell != null && hoursCell != null && referenceCell != null &&
                        !subTaskCell.getStringCellValue().isEmpty() && hoursCell.getCellType() == CellType.NUMERIC) {
                    SubTask courseSubTask = new SubTask();
                    courseSubTask.setSubTask(subTaskCell.getStringCellValue().trim());
                    courseSubTask.setEstimatedTime(convertToTimeFormat(hoursCell.getNumericCellValue()).trim());

                    if (referenceCell.getHyperlink() != null) {
                        // Extract the hyperlink address
                        String linkValue = referenceCell.getHyperlink().getAddress().trim();
                        courseSubTask.setLink(linkValue);
                    } else {
                        // If no hyperlink, use the text value
                        String linkValue = referenceCell.getStringCellValue().trim();
                        courseSubTask.setLink(linkValue);
                    }
                    if (phase != null && phase.getTasks().size() > 0) {
                        List<SubTask> subTasks = courseTask.getSubtasks();
                        subTasks.add(courseSubTask);
                        courseTask.setSubtasks(subTasks);
//                        List<Task> tasks = phase.getTasks();
//                        tasks.add(task);
//                        phase.setTasks(tasks);
                    }
                }
//                System.out.println("phase = " + phase);
            }

            fis.close(); // Close the input stream when done
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String fileNameWithoutExtension = Paths.get(excelFilePath).getFileName().toString().replaceFirst("[.][^.]+$", "");
        System.out.println("file name = " + fileNameWithoutExtension);
        Course course = new Course();
        course.setName(fileNameWithoutExtension);
        course.setFigmaLink("");
        course.setGuidelines("");
        course.setIsDeleted(false);
        course.setIsApproved(true);
        course.setPhases(phaseList);
        course.setApprover(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "5e33ce4411f3a90cf90a0f23"
        )));
        course.setCreatedBy("61fba5d5f4f70d6c0b3eff40");
//        course.setn("Priti Mittal");
        course.setApprovedBy(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "5e33ce4411f3a90cf90a0f23"
        )));

        return course;
    }

    private static String convertToTimeFormat(double time) {
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);
        return String.format(Locale.ENGLISH, "%02d:%02d", hours, minutes);
    }
}
