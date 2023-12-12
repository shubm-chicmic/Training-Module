package com.chicmic.trainingModule;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.SubTask;

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
        List<Phase> phaseList = new ArrayList<>();
        try {
            File excelFile = new File(excelFilePath);
            FileInputStream fis = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            String phaseName = "";
            String mainTopic = "";

            Phase phase = null;
//            Task task = null;

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Skip the first row
//            rowIterator.next(); // Skip the second row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell phaseCell = row.getCell(0);
                Cell mainTopicCell = row.getCell(1);
                Cell subTopicCell = row.getCell(2);
                Cell hoursCell = row.getCell(3);
                Cell referenceCell = row.getCell(4);

                if (phaseCell != null && !phaseCell.getStringCellValue().isEmpty()) {
                    phaseName = phaseCell.getStringCellValue();
//                    phase = new Phase();
//                    phase.setName(phaseName);
//                    phase.setTasks(new ArrayList<>());
//                    phaseList.add(phase);
//                    mainTopic = "";
//                    task = null; // Reset task when starting a new phase
                }

                if (mainTopicCell != null && !mainTopicCell.getStringCellValue().isEmpty()) {
                    mainTopic = mainTopicCell.getStringCellValue();
                    phase = new Phase();
                    phase.setMainTask(mainTopic);
                    phase.setSubtasks(new ArrayList<>());
                    phaseList.add(phase);
                }

                if (subTopicCell != null && hoursCell != null && referenceCell != null &&
                        !subTopicCell.getStringCellValue().isEmpty() && hoursCell.getCellType() == CellType.NUMERIC) {
                    SubTask subTask = new SubTask();
                    subTask.setSubTask(subTopicCell.getStringCellValue());
                    subTask.setEstimatedTime(convertToTimeFormat(hoursCell.getNumericCellValue()));
                    subTask.setLink(referenceCell.getStringCellValue());
                    if (phase != null) {
                        phase.getSubtasks().add(subTask);
                    }
                }
            }

            fis.close(); // Close the input stream when done
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        for (Phase phase : phaseList) {
//            System.out.println("Phase: " + phase.getName());
//            List<Task> tasks = phase.getTasks();
//            for (Task task : tasks) {
//                System.out.println("  Task: " + task.getName());
//                List<SubTask> subTasks = task.getSubTasks();
//                for (SubTask subTask : subTasks) {
//                    System.out.println("    SubTask: " + subTask.getTaskName());
//                    System.out.println("    Hours: " + subTask.getTime());
//                    System.out.println("    Reference Link: " + subTask.getUrl());
//                }
//            }
//            System.out.println("---------------------------------------------");
//        }
        String fileNameWithoutExtension = Paths.get(excelFilePath).getFileName().toString().replaceFirst("[.][^.]+$", "");
        System.out.println("file name = " + fileNameWithoutExtension);
        Course course = new Course();
        course.setName(fileNameWithoutExtension);
        course.setFigmaLink("https://www.figma.com/file/");
        course.setGuidelines("");
        course.setStatus(1);
        course.setIsDeleted(false);
        course.setIsApproved(false);
        course.setPhases(phaseList);
        course.setCreatedBy("61fba5d5f4f70d6c0b3eff40");
        course.setApprovedBy(new HashSet<>());
        course.setReviewers(new HashSet<>(Collections.singleton("61fba5d5f4f70d6c0b3eff40")));
        return course;
    }
    private static String convertToTimeFormat(double time) {
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);

        return String.format(Locale.ENGLISH, "%02d:%02d", hours, minutes);
    }
}
