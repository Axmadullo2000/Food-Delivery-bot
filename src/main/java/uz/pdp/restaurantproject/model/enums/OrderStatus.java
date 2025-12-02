package uz.pdp.restaurantproject.model.enums;

public enum OrderStatus {
    IN_CART, // savatga qoshildi
    CREATED,  //  tasdiqlansa (mijoz tomonidan)
    CANCELED, // resoran tomonidan bekor qilingan
    ACCEPTED, // buyurtma qabul qilindi (restoran)
    DONE; // buyurtma yetkazildi

}
