package project_pet_backEnd.groomer.appointment.service;

import project_pet_backEnd.groomer.appointment.dto.request.insertAppointmentForUserReq;
import project_pet_backEnd.groomer.appointment.dto.response.GetAllGroomersForAppointmentRes;
import project_pet_backEnd.groomer.petgroomerschedule.vo.PetGroomerSchedule;
import project_pet_backEnd.user.dto.ResultResponse;

import java.util.List;

public interface GroomerAppointmentService {


    List<GetAllGroomersForAppointmentRes> getAllGroomersForAppointment();


    List<PetGroomerSchedule> getGroomerScheduleByPgId(Integer pgId);

}
