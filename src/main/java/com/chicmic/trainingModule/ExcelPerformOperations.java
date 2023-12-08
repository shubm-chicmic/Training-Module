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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
            Task task = null;

            Iterator<Row> rowIterator = sheet.iterator();
//            rowIterator.next(); // Skip the first row
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
                    phase = new Phase();
                    phase.setName(phaseName);
                    phase.setTasks(new ArrayList<>());
                    phaseList.add(phase);
                    mainTopic = "";
                    task = null; // Reset task when starting a new phase
                }

                if (mainTopicCell != null && !mainTopicCell.getStringCellValue().isEmpty()) {
                    mainTopic = mainTopicCell.getStringCellValue();
                    task = new Task();
                    task.setName(mainTopic);
                    task.setSubTasks(new ArrayList<>());
                    if (phase != null) {
                        phase.getTasks().add(task);
                    }
                }

                if (subTopicCell != null && hoursCell != null && referenceCell != null &&
                        !subTopicCell.getStringCellValue().isEmpty() && hoursCell.getCellType() == CellType.NUMERIC) {
                    SubTask subTask = new SubTask();
                    subTask.setTaskName(subTopicCell.getStringCellValue());
                    subTask.setTime(String.valueOf(hoursCell.getNumericCellValue()));
                    subTask.setUrl(referenceCell.getStringCellValue());

                    if (task != null) {
                        task.getSubTasks().add(subTask);
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
        course.setApprovedBy(new HashSet<>());
        course.setReviewers(new ArrayList<>());
        return course;
    }
}
