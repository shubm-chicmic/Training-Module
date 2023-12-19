package com.chicmic.trainingModule;

import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Course.Phase;
import com.chicmic.trainingModule.Entity.Course.CourseSubTask;
import com.chicmic.trainingModule.Entity.Course.CourseTask;
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
        boolean firstRowSkipped = false;
        try {
            File excelFile = new File(excelFilePath);
            FileInputStream fis = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            String phaseName = "";
            String mainTask = "";

            Phase phase = null;

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
                    phase = new Phase();
                    phase.setTasks(new ArrayList<>());
                    phaseList.add(phase);
                }

                if (mainTaskCell != null && !mainTaskCell.getStringCellValue().isEmpty()) {
                    mainTask = mainTaskCell.getStringCellValue();
                    CourseTask courseTask = new CourseTask();

                    courseTask.setMainTask(mainTask);
                    courseTask.setSubtasks(new ArrayList<>());
                    if (phase != null) {
                        phase.getTasks().add(courseTask);
                    }
                }

                if (subTaskCell != null && hoursCell != null && referenceCell != null &&
                        !subTaskCell.getStringCellValue().isEmpty() && hoursCell.getCellType() == CellType.NUMERIC) {
                    CourseSubTask courseSubTask = new CourseSubTask();
                    courseSubTask.setSubTask(subTaskCell.getStringCellValue());
                    courseSubTask.setEstimatedTime(convertToTimeFormat(hoursCell.getNumericCellValue()));
                    courseSubTask.setLink(referenceCell.getStringCellValue());
                    if (phase != null && phase.getTasks().size() > 0) {
                        phase.getTasks().get(phase.getTasks().size() - 1).getSubtasks().add(courseSubTask);
                    }
                }
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
        course.setReviewers(new HashSet<>(Set.of(
                "61fba5d5f4f70d6c0b3eff40",
                "61fba5d5f4f70d6c0b3eff3d"
        )));
        course.setCreatedBy("61fba5d5f4f70d6c0b3eff40");
        course.setApprovedBy(new HashSet<>());

        return course;
    }

    private static String convertToTimeFormat(double time) {
        int hours = (int) time;
        int minutes = (int) ((time - hours) * 60);
        return String.format(Locale.ENGLISH, "%02d:%02d", hours, minutes);
    }
}
