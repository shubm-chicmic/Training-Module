package com.chicmic.trainingModule.Controller.PhaseController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.Constants.DeleteType;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Service.PhaseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/training/phase")
@AllArgsConstructor
public class PhaseCRUD {
    private final PhaseService phaseService;
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable String id, @RequestParam Integer type, HttpServletResponse response) {
        boolean deleted = false;
        String name = "";
        if(type == DeleteType.PHASE){
            deleted = phaseService.deletePhase((Phase<T>) phaseService.getPhaseById(id));
            name = "Phase";
        }else if(type == DeleteType.TASK){
            deleted = phaseService.deleteTask(phaseService.getTaskById(id));
            name = "Task";
        }else if(type == DeleteType.PLAN_TASK){
            deleted = phaseService.deleteTask(phaseService.getPlanTaskById(id));
            name = "Plan Task";
        }else if(type == DeleteType.SUB_TASK){
            deleted = phaseService.deleteSubtask(phaseService.getSubTaskById(id));
            name = "Sub Task";
        }
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), name + " deleted successfully", null, response);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), name +" not found", null, response);
    }
}
