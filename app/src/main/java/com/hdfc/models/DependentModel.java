package com.hdfc.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by balamurugan@adstringo.in on 01-01-2016.
 */
public class DependentModel implements Serializable {

    private String strName;
    private String strRelation;
    private String strNotes;
    private String strAddress;
    private String strContacts;
    private String strEmail;
    private String strIllness;
    private String strImagePath;
    private String strImageUrl;
    private String strDob;

    private String strCountry;
    private String strCity;
    private String strState;
    private String strPinCode;

    private int intAge;
    private int intHealthBp;
    private int intHealthHeartRate;

    private String strDependentID;
    private String strCustomerID;

    public DependentModel() {
    }

    public DependentModel(String strName, String strRelation, String strNotes, String strAddress,
                          String strContacts, String strEmail, String strIllness,
                          String strImagePath, String strImageUrl, int intAge, int intHealthBp,
                          int intHealthHeartRate, String strDependentID, String strCustomerID,
                          ArrayList<ServiceModel> serviceModels) {
        this.strName = strName;
        this.strRelation = strRelation;
        this.strNotes = strNotes;
        this.strAddress = strAddress;
        this.strContacts = strContacts;
        this.strEmail = strEmail;
        this.strIllness = strIllness;
        this.strImagePath = strImagePath;
        this.strImageUrl = strImageUrl;
        this.intAge = intAge;
        this.intHealthBp = intHealthBp;
        this.intHealthHeartRate = intHealthHeartRate;
        this.strDependentID = strDependentID;
        this.strCustomerID = strCustomerID;
    }


    public DependentModel(String strName, String strRelation, String strNotes, String strAddress,
                          String strContacts, String strEmail, String strIllness,
                          String strImagePath, String strImageUrl, String strDependentID,
                          String strCustomerID) {
        this.strName = strName;
        this.strRelation = strRelation;
        this.strNotes = strNotes;
        this.strAddress = strAddress;
        this.strContacts = strContacts;
        this.strEmail = strEmail;
        this.strIllness = strIllness;
        this.strImagePath = strImagePath;
        this.strImageUrl = strImageUrl;
        this.strDependentID = strDependentID;
        this.strCustomerID = strCustomerID;
    }

    public String getStrCountry() {
        return strCountry;
    }

    public void setStrCountry(String strCountry) {
        this.strCountry = strCountry;
    }

    public String getStrCity() {
        return strCity;
    }

    public void setStrCity(String strCity) {
        this.strCity = strCity;
    }

    public String getStrState() {
        return strState;
    }

    public void setStrState(String strState) {
        this.strState = strState;
    }

    public String getStrPinCode() {
        return strPinCode;
    }

    public void setStrPinCode(String strPinCode) {
        this.strPinCode = strPinCode;
    }

    public String getStrDob() {
        return strDob;
    }

    public void setStrDob(String strDob) {
        this.strDob = strDob;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public String getStrRelation() {
        return strRelation;
    }

    public void setStrRelation(String strRelation) {
        this.strRelation = strRelation;
    }

    public String getStrNotes() {
        return strNotes;
    }

    public void setStrNotes(String strNotes) {
        this.strNotes = strNotes;
    }

    public String getStrAddress() {
        return strAddress;
    }

    public void setStrAddress(String strAddress) {
        this.strAddress = strAddress;
    }

    public String getStrContacts() {
        return strContacts;
    }

    public void setStrContacts(String strContacts) {
        this.strContacts = strContacts;
    }

    public String getStrEmail() {
        return strEmail;
    }

    public void setStrEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getStrIllness() {
        return strIllness;
    }

    public void setStrIllness(String strIllness) {
        this.strIllness = strIllness;
    }

    public String getStrImagePath() {
        return strImagePath;
    }

    public void setStrImagePath(String strImagePath) {
        this.strImagePath = strImagePath;
    }

    public String getStrImageUrl() {
        return strImageUrl;
    }

    public void setStrImageUrl(String strImageUrl) {
        this.strImageUrl = strImageUrl;
    }

    public int getIntAge() {
        return intAge;
    }

    public void setIntAge(int intAge) {
        this.intAge = intAge;
    }

    public int getIntHealthBp() {
        return intHealthBp;
    }

    public void setIntHealthBp(int intHealthBp) {
        this.intHealthBp = intHealthBp;
    }

    public int getIntHealthHeartRate() {
        return intHealthHeartRate;
    }

    public void setIntHealthHeartRate(int intHealthHeartRate) {
        this.intHealthHeartRate = intHealthHeartRate;
    }

    public String getStrDependentID() {
        return strDependentID;
    }

    public void setStrDependentID(String strDependentID) {
        this.strDependentID = strDependentID;
    }

    public String getStrCustomerID() {
        return strCustomerID;
    }

    public void setStrCustomerID(String strCustomerID) {
        this.strCustomerID = strCustomerID;
    }
}
