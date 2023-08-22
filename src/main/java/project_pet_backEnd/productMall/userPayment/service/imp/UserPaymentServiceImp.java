package project_pet_backEnd.productMall.userPayment.service.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import project_pet_backEnd.ecpay.payment.integration.AllInOne;
import project_pet_backEnd.ecpay.payment.integration.domain.AioCheckOutALL;
import project_pet_backEnd.productMall.order.dao.OrdersRepository;
import project_pet_backEnd.productMall.order.vo.Orders;
import project_pet_backEnd.productMall.userPayment.service.UserPaymentService;
import project_pet_backEnd.utils.AllDogCatUtils;

import java.util.UUID;

@Service
public class UserPaymentServiceImp implements UserPaymentService {
    @Autowired
    private OrdersRepository ordersRepository;
    private  final  static Logger log= LoggerFactory.getLogger(UserPaymentServiceImp.class);
    @Value("${ecpay-returnHttps}")
    private String returnHttps;
    @Override
    public String getPaymentForm(Integer userId,Integer orderId) {
        //todo 先判斷該使用者有無此訂單編號
        Orders orders= ordersRepository.findById(orderId).orElse(null);
        if(orders==null|| orders.getUserId()!=userId)
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,"您輸入的訂單編號錯誤");

        String form=generateEcpayForm(orderId,"測試商品",orders.getTotalAmount());

        return form;
    }

    @Override
    public void successPayCallBack(String orderId) {
        Orders orders= ordersRepository.findById(Integer.parseInt(orderId)).orElse(null);
        if(orders==null)
            log.warn("orderId : "+orderId+" 回傳異常");
        orders.setOrdPayStatus((byte)1); //修改為1 完成訂單

    }


    public String generateEcpayForm(Integer orderId,String productName,Integer total){

        AllInOne all = new AllInOne("");
        String uuId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        AioCheckOutALL obj = new AioCheckOutALL();
        obj.setMerchantTradeNo(uuId);
        obj.setMerchantTradeDate("2023/12/31 08:05:23");
        obj.setTotalAmount(Integer.toString(total));
        obj.setTradeDesc("test Description");
        obj.setItemName(productName);
        // 交易結果回傳網址，只接受 https 開頭的網站 ;
        obj.setNeedExtraPaidInfo("N");
        // 商店轉跳網址 (Optional)
        obj.setReturnURL(returnHttps+"/successPay/"+orderId);
        String form = all.aioCheckOut(obj, null);
        obj.getMerchantTradeNo();

        return form;
    }
}