package com.divudi.light.common;

import com.divudi.entity.Staff;
import java.util.Date;

/**
 *
 * @author Senula Nanayakkara
 */
public class BillLight {

    private Long id;
    private String billNo;
    private Date billDate;
    private String institutionName;
    private String departmentName;
    private String userName;
    private String patientName;
    private String patientPhone;
    private Double grossValue;
    private Double discount;
    private Double netValue;
    private Long patientId;

    private String refDocter;
    private double billCount;
    private double countIncome;

    public BillLight() {
    }

    public BillLight(String refDocter, double billCount, double countIncome) {
        this.refDocter = refDocter;
        this.billCount = billCount;
        this.countIncome = countIncome;
    }

    public BillLight(Long id, String billNo, Date billDate, String institutionName, String departmentName, String userName, String patientName, String patientPhone, Double grossValue, Double discount, Double netValue) {
        this.id = id;
        this.billNo = billNo;
        this.billDate = billDate;
        this.institutionName = institutionName;
        this.departmentName = departmentName;
        this.userName = userName;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.grossValue = grossValue;
        this.discount = discount;
        this.netValue = netValue;
    }

    public BillLight(Long id, String billNo, Date billDate, String institutionName, String departmentName, String userName, String patientName, String patientPhone, Double grossValue, Double discount, Double netValue, Long patientId) {
        this.id = id;
        this.billNo = billNo;
        this.billDate = billDate;
        this.institutionName = institutionName;
        this.departmentName = departmentName;
        this.userName = userName;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.grossValue = grossValue;
        this.discount = discount;
        this.netValue = netValue;
        this.patientId = patientId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public Double getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(Double grossValue) {
        this.grossValue = grossValue;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getNetValue() {
        return netValue;
    }

    public void setNetValue(Double netValue) {
        this.netValue = netValue;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public double getBillCount() {
        return billCount;
    }

    public void setBillCount(double billCount) {
        this.billCount = billCount;
    }

    public double getCountIncome() {
        return countIncome;
    }

    public void setCountIncome(double countIncome) {
        this.countIncome = countIncome;
    }

    public String getRefDocter() {
        return refDocter;
    }

    public void setRefDocter(String refDocter) {
        this.refDocter = refDocter;
    }

}
