package project_pet_backEnd.groomer.petgroomer.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import project_pet_backEnd.groomer.petgroomer.dto.GetAllGroomers;
import project_pet_backEnd.groomer.petgroomer.dto.PGQueryParameter;
import project_pet_backEnd.groomer.petgroomer.dto.response.GetAllGroomerListSortResForUser;
import project_pet_backEnd.groomer.petgroomer.dto.response.ManagerGetByFunctionIdRes;
import project_pet_backEnd.groomer.petgroomer.dto.response.GetAllGroomerListSortRes;
import project_pet_backEnd.groomer.petgroomer.vo.PetGroomer;
import project_pet_backEnd.groomer.petgroomer.dao.PetGroomerDao;
import project_pet_backEnd.groomer.petgroomer.dto.request.PGInsertReq;
import project_pet_backEnd.groomer.petgroomer.dto.response.GetAllGroomerListRes;
import project_pet_backEnd.groomer.petgroomer.service.PetGroomerService;
import project_pet_backEnd.user.dto.ResultResponse;
import project_pet_backEnd.utils.AllDogCatUtils;
import project_pet_backEnd.utils.commonDto.Page;

import java.util.ArrayList;
import java.util.List;

@Service
public class PetGroomerServiceImp implements PetGroomerService {

    @Autowired
    PetGroomerDao petGroomerDao;

    /**
     * 獲取擁有美容師個人管理權限的管理員列表，供新增美容師使用。 for 管理員
     *
     * @param functionId 美容師個人管理功能的ID
     * @return rs 包含結果的回應對象
     * @throws ResponseStatusException 如果找不到擁有美容師個人管理權限之管理員，將拋出此異常
     */
    @Override
    public ResultResponse getManagerByFunctionId(Integer functionId) {
        ResultResponse rs = new ResultResponse();
        List<ManagerGetByFunctionIdRes> managerGetByFunctionIdResList = petGroomerDao.getManagerByFunctionId(functionId);
        if (managerGetByFunctionIdResList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到擁有美容師個人管理權限之管理員，請至權限管理新增擁有美容師個人管理權限之管理員");
        }
        rs.setMessage(managerGetByFunctionIdResList);
        return rs;
    }

    /**
     * 新增美容師，for 管理員使用。
     *
     * @param pgInsertReq 美容師的新增請求對象
     * @return rs 包含結果的回應對象
     * @throws ResponseStatusException 如果新增失敗，將拋出此異常
     */
    @Override
    public ResultResponse insertGroomer(PGInsertReq pgInsertReq) {

        List<PetGroomer> allGroomer = petGroomerDao.getAllGroomer();
        for (PetGroomer existingGroomer : allGroomer) {
            if (existingGroomer.getManId() == pgInsertReq.getManId()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新增失敗，管理員ID重複");
            }
        }
        PetGroomer petGroomer = new PetGroomer();
        petGroomer.setManId(pgInsertReq.getManId());
        petGroomer.setPgName(pgInsertReq.getPgName());
        String gender = pgInsertReq.getPgGender();
        switch (gender) {
            case "女性":
                petGroomer.setPgGender(0);
                break;
            case "男性":
                petGroomer.setPgGender(1);
                break;
        }
        petGroomer.setPgPic(AllDogCatUtils.base64Decode(pgInsertReq.getPgPic()));
        petGroomer.setPgEmail(pgInsertReq.getPgEmail());
        petGroomer.setPgPh(pgInsertReq.getPgPh());
        petGroomer.setPgAddress(pgInsertReq.getPgAddress());
        petGroomer.setPgBirthday(AllDogCatUtils.dateFormatToSqlDate(pgInsertReq.getPgBirthday()));

        try {
            petGroomerDao.insertGroomer(petGroomer);
            ResultResponse rs = new ResultResponse();
            rs.setMessage("新增美容師成功");
            return rs;
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "新增失敗，請稍後重試", e);
        }
    }

    /**
     * 取得指定管理員ID的美容師，for 管理員使用。
     *
     * @param manId 管理員ID
     * @return rs 包含結果的回應對象
     * @throws ResponseStatusException 如果找不到對應美容師，將拋出此異常
     */
    @Override
    public ResultResponse getPetGroomerByManId(Integer manId) {
        ResultResponse rs = new ResultResponse();
        PetGroomer petGroomer = petGroomerDao.getPetGroomerByManId(manId);
        if (petGroomer == null) {
            // 沒有找到對應美容師
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此寵物美容師");
        }
        GetAllGroomerListRes getAllGroomerListRes = new GetAllGroomerListRes();
        getAllGroomerListRes.setManId(petGroomer.getManId());
        getAllGroomerListRes.setPgId(petGroomer.getPgId());
        getAllGroomerListRes.setPgName(petGroomer.getPgName());
        int gender = petGroomer.getPgGender();
        switch (gender) {
            case 0:
                getAllGroomerListRes.setPgGender("女性");
                break;
            case 1:
                getAllGroomerListRes.setPgGender("男性");
                break;
        }
        getAllGroomerListRes.setPgPic(AllDogCatUtils.base64Encode(petGroomer.getPgPic()));
        getAllGroomerListRes.setPgEmail(petGroomer.getPgEmail());
        getAllGroomerListRes.setPgPh(petGroomer.getPgPh());
        getAllGroomerListRes.setPgAddress(petGroomer.getPgAddress());
        getAllGroomerListRes.setPgBirthday(AllDogCatUtils.timestampToDateFormat(petGroomer.getPgBirthday()));
        rs.setMessage(getAllGroomerListRes);
        return rs;
    }

    /**
     * 取得美容師列表，for 管理員使用。
     *
     * @param PGQueryParameter 分頁查詢參數
     * @return page 包含分頁結果的對象
     * @throws ResponseStatusException 如果找不到美容師，將拋出此異常
     */
    @Override
    public Page<List<GetAllGroomerListSortRes>> getAllGroomersForMan(PGQueryParameter PGQueryParameter) {
        List<GetAllGroomers> allGroomersList = petGroomerDao.getAllGroomersLimit(PGQueryParameter);
        List<GetAllGroomerListSortRes> rsList = new ArrayList<>();
        if (allGroomersList == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "找不到寵物美容師");
        }
        for (GetAllGroomers groomers : allGroomersList) {
            GetAllGroomerListSortRes getAllGroomerListSortRes = new GetAllGroomerListSortRes();
            getAllGroomerListSortRes.setManId(groomers.getManId());
            getAllGroomerListSortRes.setPgId(groomers.getPgId());
            getAllGroomerListSortRes.setPgName(groomers.getPgName());
            int gender = groomers.getPgGender();
            switch (gender) {
                case 0:
                    getAllGroomerListSortRes.setPgGender("女性");
                    break;
                case 1:
                    getAllGroomerListSortRes.setPgGender("男性");
                    break;
            }
            getAllGroomerListSortRes.setPgPic(AllDogCatUtils.base64Encode(groomers.getPgPic()));
            getAllGroomerListSortRes.setPgEmail(groomers.getPgEmail());
            getAllGroomerListSortRes.setPgPh(groomers.getPgPh());
            getAllGroomerListSortRes.setPgAddress(groomers.getPgAddress());
            getAllGroomerListSortRes.setPgBirthday(AllDogCatUtils.timestampToDateFormat(groomers.getPgBirthday()));
            getAllGroomerListSortRes.setNumAppointments(groomers.getNumAppointments());
            rsList.add(getAllGroomerListSortRes);
        }
        Page page = new Page<>();
        page.setLimit(PGQueryParameter.getLimit());
        page.setOffset(PGQueryParameter.getOffset());
        //得到總筆數，方便實作頁數
        Integer total = petGroomerDao.countPetGroomer(PGQueryParameter);
        page.setTotal(total);
        page.setRs(rsList);
        return page;
    }

    /**
     * 取得美容師列表，for 使用者&訪客使用。
     *
     * @param PGQueryParameter 分頁查詢參數
     * @return page 包含分頁結果的對象
     * @throws ResponseStatusException 如果找不到寵物美容師，將拋出此異常
     */
    @Override
    public Page<List<GetAllGroomerListSortResForUser>> getAllGroomersForUser(PGQueryParameter PGQueryParameter) {
        List<GetAllGroomers> allGroomersList = petGroomerDao.getAllGroomersLimit(PGQueryParameter);
        List<GetAllGroomerListSortResForUser> rsList = new ArrayList<>();
        if (allGroomersList == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "找不到寵物美容師");
        }
        for (GetAllGroomers groomers : allGroomersList) {
            GetAllGroomerListSortResForUser getAllGroomerListSortResForUser = new GetAllGroomerListSortResForUser();
            getAllGroomerListSortResForUser.setPgName(groomers.getPgName());
            int gender = groomers.getPgGender();
            switch (gender) {
                case 0:
                    getAllGroomerListSortResForUser.setPgGender("女性");
                    break;
                case 1:
                    getAllGroomerListSortResForUser.setPgGender("男性");
                    break;
            }
            getAllGroomerListSortResForUser.setPgPic(AllDogCatUtils.base64Encode(groomers.getPgPic()));
            getAllGroomerListSortResForUser.setNumAppointments(groomers.getNumAppointments());
            rsList.add(getAllGroomerListSortResForUser);
        }
        Page page = new Page<>();
        page.setLimit(PGQueryParameter.getLimit());
        page.setOffset(PGQueryParameter.getOffset());
        //得到總筆數，方便實作頁數
        Integer total = petGroomerDao.countPetGroomer(PGQueryParameter);
        page.setTotal(total);
        page.setRs(rsList);
        return page;
    }

    /**
     * 修改美容師資料，for 管理員使用。
     * @param getAllGroomerListRes 包含美容師信息的對象
     * @return rs 包含結果的回應對象
     * @throws ResponseStatusException 如果找不到指定ID的美容師，將拋出此異常
     */
    @Override
    public ResultResponse updateGroomerByIdForMan(GetAllGroomerListRes getAllGroomerListRes) {
        ResultResponse rs = new ResultResponse();
        boolean found = false;
        try {
            // 檢查是否存在該美容師
            List<PetGroomer> allGroomer = petGroomerDao.getAllGroomer();
            for (PetGroomer existingGroomer : allGroomer) {
                if (existingGroomer.getPgId() == getAllGroomerListRes.getPgId()) {
                    PetGroomer petGroomer = new PetGroomer();
                    petGroomer.setManId(getAllGroomerListRes.getManId());
                    petGroomer.setPgId(getAllGroomerListRes.getPgId());
                    petGroomer.setPgName(getAllGroomerListRes.getPgName());

                    String gender = getAllGroomerListRes.getPgGender();
                    switch (gender) {
                        case "女性":
                            petGroomer.setPgGender(0);
                            break;
                        case "男性":
                            petGroomer.setPgGender(1);
                            break;
                    }
                    petGroomer.setPgPic(AllDogCatUtils.base64Decode(getAllGroomerListRes.getPgPic()));
                    petGroomer.setPgEmail(getAllGroomerListRes.getPgEmail());
                    petGroomer.setPgPh(getAllGroomerListRes.getPgPh());
                    petGroomer.setPgAddress(getAllGroomerListRes.getPgAddress());
                    petGroomer.setPgBirthday(AllDogCatUtils.dateFormatToSqlDate(getAllGroomerListRes.getPgBirthday()));
                    petGroomerDao.updateGroomerById(petGroomer);
                    rs.setMessage("美容師信息更新成功");
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到ID為" + getAllGroomerListRes.getPgId() + "的美容師");
            }
            return rs;
        } catch (DataAccessException e) {
            // 出現異常，可以拋出異常或返回錯誤提示信息
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "更新美容師信息失敗，請稍後重試", e);
        }
    }

    /**
     * 取得美容師列表，for 管理員使用。無分頁查詢(已拋棄)
     */
    //    public ResultResponse getAllGroomersForMan() {
//        ResultResponse rs = new ResultResponse();
//        List<PetGroomer> allGroomer;
//        List<PetGroomerInsertRequest> PetGroomerInsertRequestList = new ArrayList<>();
//        try {
//            allGroomer = petGroomerDao.getAllGroomer();
//
//        } catch (DataAccessException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "獲取寵物美容師列表失敗，請稍後重試", e);
//        }
//
//        if (allGroomer == null || allGroomer.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "目前無美容師資料");
//        }
//
//        for (PetGroomer existingGroomer : allGroomer) {
//            PetGroomerInsertRequest petGroomerInsertRequest = new PetGroomerInsertRequest();
//            petGroomerInsertRequest.setManId(existingGroomer.getManId());
//            petGroomerInsertRequest.setPgName(existingGroomer.getPgName());
//            petGroomerInsertRequest.setPgGender(existingGroomer.getPgGender());
//            petGroomerInsertRequest.setPgPic(AllDogCatUtils.base64Encode(existingGroomer.getPgPic()));
//            petGroomerInsertRequest.setPgEmail(existingGroomer.getPgEmail());
//            petGroomerInsertRequest.setPgPh(existingGroomer.getPgPh());
//            petGroomerInsertRequest.setPgAddress(existingGroomer.getPgAddress());
//            petGroomerInsertRequest.setPgBirthday(existingGroomer.getPgBirthday());
//            PetGroomerInsertRequestList.add(petGroomerInsertRequest);
//        }
//
//        rs.setMessage(PetGroomerInsertRequestList);
//        return rs;
//    }

    /**
     * 取得美容師列表，for User 與 訪客 使用。無分頁查詢(已拋棄)
     */
//    public ResultResponse getAllGroomersForUser() {
//        ResultResponse rs = new ResultResponse();
//        List<PetGroomer> allGroomer;
//        List<GetAllGroomerResponse> allGroomerResponses = new ArrayList<>(); // Create a new list for GetAllGroomerResponse
//
//        try {
//            allGroomer = petGroomerDao.getAllGroomer();
//        } catch (DataAccessException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "獲取寵物美容師列表失敗，請稍後重試", e);
//        }
//
//        if (allGroomer == null || allGroomer.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "目前無美容師資料");
//        }
//
//        for (PetGroomer petGroomer : allGroomer) {
//            GetAllGroomerResponse response = new GetAllGroomerResponse();
//            response.setPgName(petGroomer.getPgName());
//
//            String base64Pic = AllDogCatUtils.base64Encode(petGroomer.getPgPic());
//            response.setPgPic(base64Pic);
//
//            allGroomerResponses.add(response);
//        }
//
//        rs.setMessage(allGroomerResponses);
//        return rs;
//    }

    /**
     * 取得美容師列表 By PgName for 管理員(已拋棄)
     */
//    public ResultResponse getGroomerByPgNameForMan(String PgName) {
//        ResultResponse rs = new ResultResponse();
//        try {
//            List<PetGroomer> groomerByPgNameList = petGroomerDao.getGroomerByPgName(PgName);
//            if (groomerByPgNameList.isEmpty()) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到符合條件的美容師");
//            } else {
//                rs.setMessage(groomerByPgNameList);
//            }
//        } catch (DataAccessException e) {
//            // If there's an exception, set an error status code (e.g., 500 for Internal Server Error)
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "查詢美容師信息失敗，請稍後重試", e);
//        }
//        return rs;
//    }
    /**
     * 取得美容師列表 By PgName for User(已拋棄)
     */
//    public ResultResponse getGroomerByPgNameForUser(String PgName) {
//        ResultResponse rs = new ResultResponse();
//        List<GetAllGroomerResponse> allGroomerResponses = new ArrayList<>();
//        try {
//            List<PetGroomer> groomerByPgNameList = petGroomerDao.getGroomerByPgName(PgName);
//
//            if (groomerByPgNameList.isEmpty()||groomerByPgNameList==null) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到符合條件的美容師");
//            }
//            for(PetGroomer petGroomer:groomerByPgNameList){
//                GetAllGroomerResponse getAllGroomerResponse = new GetAllGroomerResponse();
//                getAllGroomerResponse.setPgName(petGroomer.getPgName());
//                getAllGroomerResponse.setPgPic(AllDogCatUtils.base64Encode(petGroomer.getPgPic()));
//                allGroomerResponses.add(getAllGroomerResponse);
//            }
//            rs.setMessage(allGroomerResponses);
//
//        } catch (DataAccessException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "查詢美容師信息失敗，請稍後重試", e);
//        }
//        return rs;
//    }
}

