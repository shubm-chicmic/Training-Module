package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ExcelPerformOperations {
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
                     Task courseTask = new Task();

                    courseTask.setMainTask(mainTask);
                    courseTask.setSubtasks(new ArrayList<>());
                    System.out.println("\u001B[31m Task \u001B[0m" + courseTask);
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
                    courseSubTask.setSubTask(subTaskCell.getStringCellValue());
                    courseSubTask.setEstimatedTime(convertToTimeFormat(hoursCell.getNumericCellValue()));
                    courseSubTask.setLink(referenceCell.getStringCellValue());
                    if (phase != null && phase.getTasks().size() > 0) {
                        Task task = phase.getTasks().get(phase.getTasks().size() - 1);
                        List<SubTask> subTasks = task.getSubtasks();
                        subTasks.add(courseSubTask);
                        task.setSubtasks(subTasks);
                        List<Task> tasks = phase.getTasks();
                        tasks.add(task);
                        phase.setTasks(tasks);
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
        course.setFigmaLink("https://www.figma.com/file/");
        course.setGuidelines("");
        course.setIsDeleted(false);
        course.setIsApproved(false);
        course.setPhases(phaseList);
        course.setApprover(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "5e33ce4411f3a90cf90a0f23"
        )));
        course.setCreatedBy("61fba5d5f4f70d6c0b3eff40");
//        course.setn("Priti Mittal");
        course.setApprovedBy(new HashSet<>());

        return course;
    }

    private static String convertToTimeFormat(double time) {
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);
        return String.format(Locale.ENGLISH, "%02d:%02d", hours, minutes);
    }
}
