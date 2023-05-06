package com.android.waterdelivery;

public class OrderModel {
    private Integer COLUMN_ID;
    private String COLUMN_ADDR;
    private byte[] COLUMN_ORDERED_COUNTS;
    private String COLUMN_PHONE;
    private Integer COLUMN_ORDER_STATE;
    private Long COLUMN_ORDER_TIME;
    private String COLUMN_DELIVER_TIME;
    private Integer COLUMN_SMS_STATE;
    private Double DESTINATION;
    private String COLUMN_LATITUDE;
    private String COLUMN_LONGITUDE;

    public OrderModel(Integer COLUMN_ID, String COLUMN_ADDR, byte[] COLUMN_ORDERED_COUNTS, String COLUMN_PHONE, Integer COLUMN_ORDER_STATE, Long COLUMN_ORDER_TIME, String COLUMN_DELIVER_TIME, Integer COLUMN_SMS_STATE, Double DESTINATION, String COLUMN_LATITUDE, String COLUMN_LONGITUDE) {
        this.COLUMN_ID = COLUMN_ID;
        this.COLUMN_ADDR = COLUMN_ADDR;
        this.COLUMN_ORDERED_COUNTS = COLUMN_ORDERED_COUNTS;
        this.COLUMN_PHONE = COLUMN_PHONE;
        this.COLUMN_ORDER_STATE = COLUMN_ORDER_STATE;
        this.COLUMN_ORDER_TIME = COLUMN_ORDER_TIME;
        this.COLUMN_DELIVER_TIME = COLUMN_DELIVER_TIME;
        this.COLUMN_SMS_STATE = COLUMN_SMS_STATE;
        this.DESTINATION = DESTINATION;
        this.COLUMN_LATITUDE = COLUMN_LATITUDE;
        this.COLUMN_LONGITUDE = COLUMN_LONGITUDE;
    }

    public Integer getCOLUMN_ID() {
        return COLUMN_ID;
    }

    public void setCOLUMN_ID(Integer COLUMN_ID) {
        this.COLUMN_ID = COLUMN_ID;
    }

    public String getCOLUMN_ADDR() {
        return COLUMN_ADDR;
    }

    public void setCOLUMN_ADDR(String COLUMN_ADDR) {
        this.COLUMN_ADDR = COLUMN_ADDR;
    }

    public byte[] getCOLUMN_ORDERED_COUNTS() {
        return COLUMN_ORDERED_COUNTS;
    }

    public void setCOLUMN_ORDERED_COUNTS(byte[] COLUMN_ORDERED_COUNTS) {
        this.COLUMN_ORDERED_COUNTS = COLUMN_ORDERED_COUNTS;
    }

    public String getCOLUMN_PHONE() {
        return COLUMN_PHONE;
    }

    public void setCOLUMN_PHONE(String COLUMN_PHONE) {
        this.COLUMN_PHONE = COLUMN_PHONE;
    }

    public Integer getCOLUMN_ORDER_STATE() {
        return COLUMN_ORDER_STATE;
    }

    public void setCOLUMN_ORDER_STATE(Integer COLUMN_ORDER_STATE) {
        this.COLUMN_ORDER_STATE = COLUMN_ORDER_STATE;
    }

    public Long getCOLUMN_ORDER_TIME() {
        return COLUMN_ORDER_TIME;
    }

    public void setCOLUMN_ORDER_TIME(Long COLUMN_ORDER_TIME) {
        this.COLUMN_ORDER_TIME = COLUMN_ORDER_TIME;
    }

    public String getCOLUMN_DELIVER_TIME() {
        return COLUMN_DELIVER_TIME;
    }

    public void setCOLUMN_DELIVER_TIME(String COLUMN_DELIVER_TIME) {
        this.COLUMN_DELIVER_TIME = COLUMN_DELIVER_TIME;
    }

    public Integer getCOLUMN_SMS_STATE() {
        return COLUMN_SMS_STATE;
    }

    public void setCOLUMN_SMS_STATE(Integer COLUMN_SMS_STATE) {
        this.COLUMN_SMS_STATE = COLUMN_SMS_STATE;
    }

    public Double getDESTINATION() {
        return DESTINATION;
    }

    public void setDESTINATION(Double DESTINATION) {
        this.DESTINATION = DESTINATION;
    }

    public String getCOLUMN_LATITUDE() {
        return COLUMN_LATITUDE;
    }

    public void setCOLUMN_LATITUDE(String COLUMN_LATITUDE) {
        this.COLUMN_LATITUDE = COLUMN_LATITUDE;
    }

    public String getCOLUMN_LONGITUDE() {
        return COLUMN_LONGITUDE;
    }

    public void setCOLUMN_LONGITUDE(String COLUMN_LONGITUDE) {
        this.COLUMN_LONGITUDE = COLUMN_LONGITUDE;
    }
}
