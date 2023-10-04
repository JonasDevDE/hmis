package com.divudi.bean.common;

import com.divudi.bean.clinical.PatientEncounterController;
import com.divudi.bean.clinical.PracticeBookingController;
import com.divudi.bean.inward.AdmissionController;
import com.divudi.bean.opd.OpdBillController;
import com.divudi.bean.pharmacy.PharmacySaleController;
import com.divudi.bean.web.CaptureComponentController;
import com.divudi.data.PaymentMethod;
import com.divudi.data.Sex;
import com.divudi.data.Title;
import com.divudi.data.dataStructure.YearMonthDay;
import com.divudi.data.hr.ReportKeyWord;
import com.divudi.data.inward.PatientEncounterType;
import com.divudi.ejb.BillNumberGenerator;
import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.Family;
import com.divudi.entity.FamilyMember;
import com.divudi.entity.Institution;
import com.divudi.entity.Patient;
import com.divudi.entity.PatientEncounter;
import com.divudi.entity.Person;
import com.divudi.entity.Relation;
import com.divudi.entity.WebUser;
import com.divudi.entity.lab.PatientSample;
import com.divudi.entity.membership.MembershipScheme;
import com.divudi.facade.BillFacade;
import com.divudi.facade.FamilyFacade;
import com.divudi.facade.FamilyMemberFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.WebUserFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class PatientController implements Serializable {

    /**
     *
     * EJBs
     *
     *
     */
    @EJB
    private PatientFacade ejbFacade;
    @EJB
    FamilyFacade familyFacade;
    @EJB
    FamilyMemberFacade familyMemberFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    BillNumberGenerator billNumberBean;
    @EJB
    CommonFunctions commonFunctions;
    @EJB
    BillFacade billFacade;
    @EJB
    private WebUserFacade webUserFacade;
    /**
     *
     * Controllers
     *
     *
     */
    @Inject
    SessionController sessionController;
    @Inject
    PracticeBookingController practiceBookingController;
    @Inject
    PatientEncounterController patientEncounterController;
    @Inject
    private CommonController commonController;
    @Inject
    private SecurityController securityController;
    @Inject
    ApplicationController applicationController;
    @Inject
    BillController billController;
    @Inject
    PharmacySaleController pharmacySaleController;
    @Inject
    CaptureComponentController captureComponentController;
    @Inject
    OpdBillController opdBillController;
    @Inject
    AdmissionController admissionController;
    @Inject
    AppointmentController appointmentController;
    /**
     *
     * Class Variables
     *
     *
     */
    private static final long serialVersionUID = 1L;
    private Patient current;
    Long patientId;
    private Person familyMember;
    private List<Person> familyMembers;
    Family currentFamily;
    private List<Family> families;
    FamilyMember currentFamilyMember;
    Patient addingPatientToFamily;
    FamilyMember removingFamilyMember;
    Relation currentRelation;
    private String password;

    private List<Patient> items = null;
    private List<Patient> selectedItems = null;

    private MembershipScheme membershipScheme;

    private Date dob;
    private String membershipTypeListner = "1";

    StreamedContent barcode;
    ReportKeyWord reportKeyWord;

    private String searchText;

    private String searchName;
    private String searchPhone;
    private String searchNic;
    private String searchPhn;
    private String searchPatientCode;
    private String searchPatientId;
    private String searchBillId;
    private String searchSampleId;
    private List<Patient> searchedPatients;

    private Integer ageYearComponant;
    private Integer ageMonthComponant;
    private Integer ageDateComponant;

    public void calculateAgeComponantsFromDob(Patient p) {
        if (p == null || p.getPerson() == null || p.getPerson().getDob() == null) {
            return;
        }

        Date dob = p.getPerson().getDob();
        Calendar today = Calendar.getInstance();
        Calendar birthdate = Calendar.getInstance();
        birthdate.setTime(dob);

        int years = today.get(Calendar.YEAR) - birthdate.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - birthdate.get(Calendar.MONTH);
        int days = today.get(Calendar.DATE) - birthdate.get(Calendar.DATE);

        if (months < 0 || (months == 0 && days < 0)) {
            years--;
            months += 12;
            if (days < 0) {
                months--;
                days += birthdate.getActualMaximum(Calendar.DATE);
            }
        }

        ageYearComponant = years;
        ageMonthComponant = months;
        ageDateComponant = days;
    }

    public void calculateDobFromAgeComponants(Patient p) {
        if (p == null) {
            return;
        }
        if (p.getPerson() == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Integer currentYear = calendar.get(Calendar.YEAR);
        Integer currentMonth = calendar.get(Calendar.MONTH);
        Integer currentDate = calendar.get(Calendar.DATE);

        if (ageYearComponant == null) {
            ageYearComponant = 0;
        }
        if (ageMonthComponant == null) {
            ageMonthComponant = 0;
        }
        if (ageDateComponant == null) {
            ageDateComponant = 0;
        }

        Integer birthYear = currentYear - ageYearComponant;
        Integer birthMonth = currentMonth - ageMonthComponant;
        Integer birthDate = currentDate - ageDateComponant;

        // If the birth date is in the future, subtract a year from the birth year
        if (birthMonth > 0 || (birthMonth == 0 && birthDate > 0)) {
            birthYear--;
        }

        calendar.set(Calendar.YEAR, birthYear);
        calendar.set(Calendar.MONTH, birthMonth);
        calendar.set(Calendar.DATE, birthDate);

        Date calculatetDob = calendar.getTime();
        p.getPerson().setDob(calculatetDob);
    }

    public String navigateToNewOpdVisitFromSearch() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing");
            return "";
        }
        PatientEncounter opdVisit;
        opdVisit = new PatientEncounter();
        opdVisit.setCreatedAt(Calendar.getInstance().getTime());
        opdVisit.setCreater(getSessionController().getLoggedUser());
        opdVisit.setPatient(current);
        opdVisit.setInstitution(sessionController.getInstitution());
        opdVisit.setDepartment(sessionController.getDepartment());
        opdVisit.setPatientEncounterType(PatientEncounterType.OpdVisit);
        getPatientEncounterController().setCurrent(opdVisit);
        getPatientEncounterController().setStartedEncounter(opdVisit);
        getPatientEncounterController().fillCurrentPatientLists(current);
        getPatientEncounterController().fillCurrentEncounterLists(opdVisit);
        getPatientEncounterController().generateDocumentsFromDocumentTemplates(opdVisit);
        getPatientEncounterController().saveSelected();
        return "/emr/opd_visit";
    }

    public String navigateToNewDataEntryForm() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing");
            return "";
        }
        captureComponentController.setDataEntryForms(captureComponentController.listDataEntryForms());
        PatientEncounter opdVisit;
        opdVisit = new PatientEncounter();
        opdVisit.setCreatedAt(Calendar.getInstance().getTime());
        opdVisit.setCreater(getSessionController().getLoggedUser());
        opdVisit.setPatient(current);
        opdVisit.setInstitution(sessionController.getInstitution());
        opdVisit.setDepartment(sessionController.getDepartment());
        opdVisit.setPatientEncounterType(PatientEncounterType.OpdVisit);
        getPatientEncounterController().setCurrent(opdVisit);
        getPatientEncounterController().setStartedEncounter(opdVisit);
        getPatientEncounterController().fillCurrentPatientLists(current);
        getPatientEncounterController().fillCurrentEncounterLists(opdVisit);
        getPatientEncounterController().generateDocumentsFromDocumentTemplates(opdVisit);
        getPatientEncounterController().saveSelected();
        return "/emr/select_data_entry_form";
    }

    public void generateNewPhn() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient");
            return;
        }
        if (sessionController.getLoggedUser() == null) {
            return;
        }
        if (sessionController.getLoggedUser().getInstitution() == null) {
            return;
        }
        Institution ins = sessionController.getLoggedUser().getInstitution();
        current.setPhn(applicationController.createNewPersonalHealthNumber(ins));
        current.setCreatedInstitution(ins);
    }

    public String toOpdBilling() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        billController.prepareNewBill();
        billController.setPatientSearchTab(1);
        billController.setSearchedPatient(current);
        return billController.toOpdBilling();
    }

    public String toPharmacyBilling() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        pharmacySaleController.prepareForNewPharmacyRetailBill();
        pharmacySaleController.setSearchedPatient(current);
        pharmacySaleController.setPatientSearchTab(1);
        return pharmacySaleController.toPharmacyRetailSale();
    }

    public String toEmrPatientProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        patientEncounterController.setPatient(current);
        patientEncounterController.fillCurrentPatientLists(current);
        return "/emr/patient_profile";
    }

    public String navigateToOpdPatientProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        return "/opd/patient";
    }

    public String navigateToAdmitFromPatientProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        admissionController.prepereToAdmitNewPatient();
        admissionController.getCurrent().setPatient(current);
        return "/inward/inward_admission";
    }

    public String navigateToInwardAppointmentFromPatientProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        appointmentController.prepereForInwardAppointPatient();
        appointmentController.setSearchedPatient(getCurrent());
        appointmentController.getCurrentAppointment().setPatient(getCurrent());
        appointmentController.getCurrentBill().setPatient(getCurrent());
        return "/inward/inward_appointment";
    }

    public String navigateToOpdPatientEdit() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        return "/opd/patient_edit";
    }

    public String navigateToOpdPatientEditFromId() {
        if (patientId == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        current = getFacade().find(patientId);
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        return "/opd/patient_edit";
    }

    public String navigateToOpdBillFromOpdPatient() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        return opdBillController.navigateToNewOpdBill(current);
    }

    public String navigateToOpdBillForCashier() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient selected");
            return "";
        }
        return "/opd/opd_bill";
    }

    public String toChannelling() {
        return "";
    }

    public String toQueue() {
        return "";
    }

    public String toAdmit() {
        return "";
    }

    public String toRecords() {
        return "";
    }

    public String toSearchPatient() {
        return "/emr/patient_search";
    }

    public void generateNewCode() {
        if (current == null) {
            JsfUtil.addErrorMessage("No patient");
            return;
        }
        current.setCode(getCountPatientCode());
    }

    public String toChangeMembershipOfSelectedPersons() {
        items = new ArrayList<>();
        return "/membership/change_membership";
    }

    public String toAddToQueueFromSearchPatients() {
        if (current == null) {
            JsfUtil.addErrorMessage("No Patient Selected");
            return "";
        }
        patientSelected();
        return "/emr/patient_add_to_queue";
    }

    public void patientSelected() {
        getPatientEncounterController().fillCurrentPatientLists(current);
    }

    public String searchPatient() {
        if (searchBillId != null && !searchBillId.trim().equals("")) {
            searchByBill();
        } else if (searchSampleId != null && !searchSampleId.trim().equals("")) {
            searchBySample();
        } else if (searchPatientId != null && !searchPatientId.trim().equals("")) {
            searchByPatientId();
        } else {
            searchPatientByDetails();
        }
        if (searchedPatients == null) {
            JsfUtil.addErrorMessage("No Matches. Please use different criteria.");
            return "";
        }
        clearSearchDetails();
        return "";
    }

    public String searchPatientForOpd() {
        if (searchBillId != null && !searchBillId.trim().equals("")) {
            searchByBill();
        } else if (searchSampleId != null && !searchSampleId.trim().equals("")) {
            searchBySample();
        } else if (searchPatientId != null && !searchPatientId.trim().equals("")) {
            searchByPatientId();
        } else {
            searchPatientByDetails();
        }
        if (searchedPatients == null) {
            JsfUtil.addErrorMessage("No Matches. Please use different criteria.");
            return "";
        }
        clearSearchDetails();
        return "";
    }

    public void clearSearchDetails() {
        searchName = null;
        searchPhone = null;
        searchNic = null;
        searchPatientCode = null;
        searchPatientId = null;
        searchBillId = null;
        searchSampleId = null;
    }

    public void searchByBill() {
        String j;
        j = "select b.patient from Bill b where b.retired=false ";
        Map m = new HashMap();
        Long temId;
//        if(false){
//            Bill temP = new Bill();
//            temP.getPerson().getName();
//            temP.setRetired(true);
//            temP.getIdStr();
//            temP.getInsId();
//        }
        if (StringUtils.isNumeric(searchBillId)) {
            try {
                temId = Long.parseLong(searchBillId);
                j += " and b.id=:id ";
                m.put("id", temId);
            } catch (NumberFormatException e) {
                temId = 0l;
                j += " and b.id=:id ";
                m.put("id", temId);
            }
        } else {
            j += " and b.insId=:insid ";
            m.put("insid", searchBillId);
            temId = 0l;
        }
        j += " order by b.patient.person.name";
        searchedPatients = getFacade().findByJpql(j, m);
    }

    public void searchBySample() {
        String j;
        j = "select ps.patientInvestigation.billItem.bill.patient from PatientSample ps where ps.retired=false ";
        Map m = new HashMap();
        Long temId;
        if (false) {
            PatientSample ps = new PatientSample();
            ps.getId();
            ps.getIdStr();
            ps.getPatientInvestigation().getBillItem().getBill().getPatient();
        }
        if (StringUtils.isNumeric(searchBillId)) {
            try {
                temId = Long.parseLong(searchSampleId);
                j += " and ps.id=:id ";
                m.put("id", temId);
            } catch (Exception e) {
                temId = 0l;
                j += " and ps.id=:id ";
                m.put("id", temId);
                searchedPatients = new ArrayList<>();
            }
        }
        j += " order by ps.patientInvestigation.billItem.bill.patient.person.name";
        searchedPatients = getFacade().findByJpql(j, m);
    }

    public void searchPatientByDetails() {
        boolean atLeastOneCriteriaIsGiven = false;
        String j;
        Map m = new HashMap();
        if (false) {
            Patient temP = new Patient();
            temP.getPerson().getName();
            temP.setRetired(true);
        }

        j = "select p "
                + " from Patient p "
                + " where p.retired=false and ";

        if (searchName != null && !searchName.trim().equals("")) {
            j += " (p.person.name) like :name ";
            m.put("name", "%" + searchName.toLowerCase() + "%");
            atLeastOneCriteriaIsGiven = true;
        }

        if (searchPatientCode != null && !searchPatientCode.trim().equals("")) {
            j += " (p.code) like :name ";
            m.put("name", "%" + searchPatientCode.toLowerCase() + "%");
            atLeastOneCriteriaIsGiven = true;
        }

        if (searchPhone != null && !searchPhone.trim().equals("")) {
            j += " (p.person.phone =:phone or p.person.mobile =:phone)";
            m.put("phone", searchPhone);
            atLeastOneCriteriaIsGiven = true;
        }

        if (searchNic != null && !searchNic.trim().equals("")) {
            j += " p.person.nic =:nic";
            m.put("nic", searchNic);
            atLeastOneCriteriaIsGiven = true;
        }

        if (searchPhn != null && !searchPhn.trim().equals("")) {
            j += " p.phn =:phn";
            m.put("phn", searchPhn);
            atLeastOneCriteriaIsGiven = true;
        }

        j += " order by p.person.name";

        if (!atLeastOneCriteriaIsGiven) {
            JsfUtil.addErrorMessage("Ät least one search criteria should be given");
            return;
        }

        searchedPatients = getFacade().findByJpql(j, m);

    }

    public void searchByPatientId() {
        String j;
        Map m = new HashMap();
        j = "select p from Patient p where p.retired=false and p.id=:id";
        Long ptId = 0l;
        try {
            ptId = Long.parseLong(searchPatientId);
        } catch (Exception e) {

        }
        m.put("id", ptId);
        searchedPatients = getFacade().findByJpql(j, m);

    }

    public void listAllPatients() {
        String j = "select p from Patient p where p.retired=false order by p.person.name";
        items = getFacade().findByJpql(j);
    }

    public void listAllMembers() {
        String j = "select p from Patient p where p.retired=false and p.person.membershipScheme is not null order by p.person.name";
        items = getFacade().findByJpql(j);
    }

    public void changeMembershipOfSelectedPersons() {
        for (Patient p : getSelectedItems()) {
            if (p.getPerson() != null) {
                p.getPerson().setMembershipScheme(membershipScheme);
//                p.getPerson().setEditedAt(new Date());
//                p.getPerson().setEditer(sessionController.getLoggedUser());
                getFacade().edit(p);
                getPersonFacade().edit(p.getPerson());
            }
        }
        JsfUtil.addSuccessMessage("Membership Updated");
    }

    public String toAddAFamily() {
        currentFamily = new Family();
        return "/membership/add_family";
    }

    public String searchFamily() {
        families = null;
        String j = "Select f from Family f where f.retired=false and f.phoneNo = :pn or f.membershipCardNo = :mcn";
        Map m = new HashMap();
        Long mcn;
        try {
            mcn = Long.parseLong(searchText);
        } catch (Exception e) {
            mcn = 0L;
        }
        m.put("pn", searchText);
        m.put("mcn", mcn);
        List<Family> fs = getFamilyFacade().findByJpql(j, m);
        if (fs == null) {
            JsfUtil.addErrorMessage("No matches");
            return "";
        } else if (fs.size() == 1) {
            currentFamily = fs.get(0);
            searchText = "";
            return "/membership/add_family";
        } else {
            families = fs;
            searchText = "";
            return "/membership/search_family";
        }
    }

    public void saveFamily() {
        if (currentFamily == null) {
            JsfUtil.addErrorMessage("No Family Selected to Save or Update");
            return;
        }
        if (currentFamily.getId() == null) {
            currentFamily.setCreatedAt(new Date());
            currentFamily.setCreater(getSessionController().getLoggedUser());
            getFamilyFacade().create(currentFamily);
            JsfUtil.addSuccessMessage("Family Added");
        } else {
            currentFamily.setEditedAt(new Date());
            currentFamily.setEditer(getSessionController().getLoggedUser());
            getFamilyFacade().edit(currentFamily);
            JsfUtil.addSuccessMessage("Family Updated");
        }

    }

    public String saveAndClearForNewFamily() {
        saveFamily();
        currentFamily = new Family();
        return toFamily();
    }

    public String toAddNewFamily() {
        currentFamily = new Family();
        return toFamily();
    }

    public String toFamily() {
        return "/membership/add_family";
    }

    public String toNewPatient() {
        prepareAdd();
        return "/membership/patient";
    }

    public void addNewMemberToFamily() {
        saveFamily();
        if (currentFamily == null) {
            JsfUtil.addErrorMessage("No Family Selected.");
            return;
        }
        if (current == null) {
            JsfUtil.addErrorMessage("No Member is selected to add to family.");
            return;
        }
        if (current.getPerson().getMembershipScheme() == null) {
            current.getPerson().setMembershipScheme(currentFamily.getMembershipScheme());
            getPersonFacade().edit(current.getPerson());
        }
        FamilyMember tfm = new FamilyMember();
        tfm.setPatient(current);
        tfm.setFamily(currentFamily);
        tfm.setCreatedAt(new Date());
        tfm.setCreater(sessionController.getLoggedUser());
        tfm.setRelationToChh(currentRelation);
        getFamilyMemberFacade().create(tfm);
        currentFamily.getFamilyMembers().add(tfm);
        saveFamily();
        JsfUtil.addSuccessMessage("Family Member Added to Family");
        current = null;
        currentRelation = null;
    }

    public void removeFamilyMember() {
        if (currentFamily == null) {
            JsfUtil.addErrorMessage("No Family Selected.");
            return;
        }
        if (removingFamilyMember == null) {
            JsfUtil.addErrorMessage("No Member is selected to remove.");
            return;
        }
        try {
            currentFamily.getFamilyMembers().remove(removingFamilyMember);
            getFamilyMemberFacade().remove(removingFamilyMember);
            JsfUtil.addSuccessMessage("Removed");
        } catch (Error e) {
            JsfUtil.addErrorMessage("Error in removing. " + e.getMessage());
        }
    }

    public void removeFamily() {
        if (currentFamily == null) {
            JsfUtil.addErrorMessage("No user");
            return;
        }
        if (currentFamily.getId() == null) {
            JsfUtil.addErrorMessage("User Not yet Added to system to remove");
        } else {
            currentFamily.setRetired(true);
            currentFamily.setRetiredAt(new Date());
            currentFamily.setRetirer(getSessionController().getLoggedUser());
            JsfUtil.addSuccessMessage("Family Removed. But the family members remain in the system.");
        }

    }

    public void removeFamilyAndMembers() {
        if (currentFamily == null) {
            JsfUtil.addErrorMessage("No user");
            return;
        }
        if (currentFamily.getId() == null) {
            JsfUtil.addErrorMessage("User Not yet Added to system to remove");
        } else {
            for (FamilyMember fm : currentFamily.getFamilyMembers()) {
                Patient pt = fm.getPatient();
                pt.setRetired(true);
                pt.setRetiredAt(new Date());
                pt.setRetirer(getSessionController().getLoggedUser());
                getFacade().edit(pt);

                Person p = pt.getPerson();
                p.setRetired(true);
                p.setRetirer(getSessionController().getLoggedUser());
                p.setRetiredAt(new Date());
                getPersonFacade().edit(p);

                WebUser u = p.getWebUser();
                if (u != null) {
                    u.setActivated(false);
                    u.setRetired(true);
                    u.setRetiredAt(new Date());
                    u.setRetirer(getSessionController().getLoggedUser());
                }

            }
            currentFamily.setRetired(true);
            currentFamily.setRetiredAt(new Date());
            currentFamily.setRetirer(getSessionController().getLoggedUser());
            JsfUtil.addSuccessMessage("Family Members and all user details removed.");
        }

    }

    public String toPatientFromSearchPatients() {
        if (current == null) {
            JsfUtil.addErrorMessage("No Patient Selected");
            return "";
        }
        patientSelected();
        return "/emr/patient_basic_info";
    }

    public String toPatientFromSearchPatientsProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("No Patient Selected");
            return "";
        }
        patientSelected();
        return "/emr/patient_profile";
    }

    public void createPatientBarcode() {
        File barcodeFile = new File("ptbarcode");
        if (current != null && current.getCode() != null && !current.getCode().trim().equals("")) {
            try {
                BarcodeImageHandler.saveJPEG(BarcodeFactory.createCode128(getCurrent().getCode()), barcodeFile);
                InputStream targetStream = new FileInputStream(barcodeFile);
                StreamedContent str = DefaultStreamedContent.builder().contentType("image/jpeg").name(barcodeFile.getName()).stream(() -> targetStream).build();
                barcode = str;
//                return str;

            } catch (Exception ex) {
                //   ////System.out.println("ex = " + ex.getMessage());
            }
        } else {
            //   ////System.out.println("else = ");
            try {
                Barcode bc = BarcodeFactory.createCode128A("0000");
                bc.setBarHeight(5);
                bc.setBarWidth(3);
                bc.setDrawingText(true);
                BarcodeImageHandler.saveJPEG(bc, barcodeFile);
                //   ////System.out.println("12");
                InputStream targetStream = new FileInputStream(barcodeFile);
                StreamedContent str = DefaultStreamedContent.builder().contentType("image/jpeg").name(barcodeFile.getName()).stream(() -> targetStream).build();
                barcode = str;
            } catch (Exception ex) {
                //   ////System.out.println("ex = " + ex.getMessage());
            }
        }
    }

    public void createFamilymembers(ActionEvent event) {
        FacesMessage message = null;
        boolean loggedIn;

        if (familyMember.getFullName() == null || familyMember.getFullName().equals("")) {
            loggedIn = false;
            UtilityController.addErrorMessage("Please enter full name");
            return;

        }
        if (familyMember.getSex() == null) {
            loggedIn = false;
            UtilityController.addErrorMessage("Please enter gender");
            return;

        }
        if (familyMember.getNic() == null || familyMember.getNic().equals("")) {
            loggedIn = false;
            UtilityController.addErrorMessage("Please enter NIC no");
            return;
        }
        if (familyMember.getDob() == null) {
            loggedIn = false;
            UtilityController.addErrorMessage("Please enter Date Of Birth");
            return;
        }
        familyMember.setSerealNumber(familyMembers.size());
        familyMembers.add(familyMember);
        loggedIn = true;

        familyMember = null;

//        context.addCallbackParam("loggedIn", loggedIn);
        PrimeRequestContext.getCurrentInstance().getCallbackParams().put("loggedIn", loggedIn);
    }

    public void removeFamilyMember(Person p) {

        familyMembers.remove(p.getSerealNumber());
        int i = 0;
        for (Person familyMember1 : familyMembers) {
            familyMember1.setSerealNumber(i);
            i++;
        }
    }

    public void listnerFamilyMember() {
        familyMember = null;

    }

    public void listnerMembershipType() {
        membershipTypeListner = null;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    private YearMonthDay yearMonthDay;

    public YearMonthDay getYearMonthDay() {
        if (yearMonthDay == null) {
            yearMonthDay = new YearMonthDay();
        }
        return yearMonthDay;
    }

    public void setYearMonthDay(YearMonthDay yearMonthDay) {
        this.yearMonthDay = yearMonthDay;
    }

    public void dateChangeListen() {
        getCurrent().getPerson().setDob(getCommonFunctions().guessDob(yearMonthDay));
    }

    public void dobChangeListen() {
        yearMonthDay = getCommonFunctions().guessAge(getCurrent().getPerson().getDob());
    }

    public StreamedContent getPhoto(Patient p) {
        //////System.out.println("p is " + p);
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            return new DefaultStreamedContent();
        } else if (p == null) {
            return new DefaultStreamedContent();
        } else {
            if (p.getId() != null && p.getBaImage() != null) {
                //////System.out.println("giving image");
                InputStream targetStream = new ByteArrayInputStream(p.getBaImage());
                StreamedContent str = DefaultStreamedContent.builder().contentType(p.getFileType()).name(p.getFileName()).stream(() -> targetStream).build();
                return str;
//                return new DefaultStreamedContent(new ByteArrayInputStream(p.getBaImage()), p.getFileType(), p.getFileName());
            } else {
                return new DefaultStreamedContent();
            }
        }

    }

    public Title[] getTitles() {
        return Title.values();
    }

    public Sex[] getSexs() {
        return Sex.values();
    }

    public void prepareAddReg() {
        prepareAdd();
        current.setCode(null);
    }

    public void prepareAdd() {
        current = null;
        yearMonthDay = null;
        //familyMember=null;
        familyMembers = new ArrayList<>();
        reportKeyWord = new ReportKeyWord();
        getCurrent();

        getYearMonthDay();
    }

    public String toAddNewPatient() {
        current = null;
        yearMonthDay = null;
        getCurrent();
        getYearMonthDay();
        return "/emr/patient";
    }

    public String navigateToAddNewPatientForOpd() {
        current = null;
        getCurrent();
        return "/opd/patient_edit";
    }

    public String toViewPatient() {
        current = null;
        return "/emr/patient_profile";
    }

    public String savePatientAndThenNavigateToPatientProfile() {
        saveSelectedPatient();
        return toViewPatient();
    }

    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfull");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    private void recreateModel() {
        items = null;
    }

    public void createRandomPatient(String ptName) {
        Person person = new Person();
        Patient pt = new Patient();
        person.setName(ptName);
        pt.setPerson(person);
        getPersonFacade().create(person);
        getFacade().create(pt);
    }

    public List<Patient> completePatient(String query) {
        List<Patient> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select p from Patient p where p.retired=false "
                    + " and (p.person.name) like :q "
                    + " or (p.code) like :q "
                    + " or (p.person.nic) like :q"
                    + " or (p.person.mobile) like :q "
                    + "  order by p.person.name";
            hm.put("q", "%" + query.toUpperCase() + "%");
            //////System.out.println(sql);
            suggestions = getFacade().findByJpql(sql, hm, 20);
        }
        return suggestions;
    }

    List<Patient> patientList;

    public List<Patient> completePatientByNameOrCode(String query) {
        if (query == null) {
            return null;
        }
        Date startTime = new Date();
        String sql;
        HashMap hm = new HashMap();
        sql = "select p from Patient p where p.retired=false "
                + " and ((p.person.name) like  :q "
                + " or (p.code) like :q "
                + " or (p.person.nic) like :q "
                + " or (p.person.mobile) like :q "
                + " or (p.person.phone) like :q "
                + " or (p.phn) like :q) ";
        sql += " order by p.person.name";
        hm.put("q", "%" + query.toUpperCase() + "%");
        patientList = getFacade().findByJpql(sql, hm, 20);
        commonController.printReportDetails(null, null, startTime, "Autocomplet Patient Search");
        return patientList;
    }

    public void saveAndUpdateQueue() {
        saveSelected();
        getPracticeBookingController().listBillSessions();
    }

    public String getCountPatientCode() {

        String sql;

        sql = "select count(p) FROM Patient p where p.code is not null";

        long lng = getEjbFacade().countByJpql(sql);
        lng++;
        String str = "";
        str += lng;
        return str;
    }

    public void saveSelected() {
        saveSelected(current);
    }

    public String saveSelectedAndToFamily() {
        saveSelected(current);
        return "/membership/add_family";
    }

    public String saveAndNavigateToOpdPatientProfile() {
        if (current == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        saveSelected(current);
        return "/opd/patient";
    }

    public void saveSelected(Patient p) {
        if (p == null) {
            UtilityController.addErrorMessage("No Current. Error. NOT SAVED");
            return;
        }
        if (p.getPerson() == null) {
            UtilityController.addErrorMessage("No Person. Not Saved");
            return;
        }
        if (p.getPerson().getName().trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a name");
            return;
        }
        if (p.getPerson().getId() == null) {
            p.getPerson().setCreatedAt(Calendar.getInstance().getTime());
            p.getPerson().setCreater(getSessionController().getLoggedUser());
            getPersonFacade().create(p.getPerson());
        } else {
            getPersonFacade().edit(p.getPerson());
        }
        if (p.getId() == null) {
            p.setCreatedAt(new Date());
            p.setCreater(getSessionController().getLoggedUser());
            p.setCreatedInstitution(getSessionController().getInstitution());
            getFacade().create(p);
        } else {
            getFacade().edit(p);
        }
    }

    public String saveAndNavigateToProfile() {
        if (current == null) {
            UtilityController.addErrorMessage("No Current. Error. NOT SAVED");
            return "";
        }
        if (current.getPerson() == null) {
            UtilityController.addErrorMessage("No Person. Not Saved");
            return "";
        }
        if (current.getPerson().getName().trim().equals("")) {
            UtilityController.addErrorMessage("Please enter a name");
            return "";
        }
        if (current.getPerson().getId() == null) {
            current.getPerson().setCreatedAt(Calendar.getInstance().getTime());
            current.getPerson().setCreater(getSessionController().getLoggedUser());
            getPersonFacade().create(current.getPerson());
        } else {
            getPersonFacade().edit(current.getPerson());
        }
        if (current.getId() == null) {
            current.setCreatedAt(new Date());
            current.setCreater(getSessionController().getLoggedUser());
            current.setCreatedInstitution(getSessionController().getInstitution());
            getFacade().create(current);
        } else {
            getFacade().edit(current);
        }

        if (current.getCreatedAt() == null) {
            current.setCreatedAt(new Date());
            getFacade().edit(current);
        }

        if (current.getPerson().getCreatedAt() == null) {
            current.getPerson().setCreatedAt(new Date());
            getPersonFacade().edit(current.getPerson());
        }

        return toEmrPatientProfile();
    }

    public void saveSelectedPatient() {
        if (getCurrent().getPerson().getId() == null) {
            getCurrent().getPerson().setCreatedAt(Calendar.getInstance().getTime());
            getCurrent().getPerson().setCreater(getSessionController().getLoggedUser());
            getPersonFacade().create(getCurrent().getPerson());
        } else {
            getPersonFacade().edit(getCurrent().getPerson());
        }
        if (getCurrent().getId() == null) {
            getCurrent().setCreatedAt(new Date());
            getCurrent().setCreater(getSessionController().getLoggedUser());
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved as a new patient successfully.");
        } else {
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("Updated the patient details successfully.");
        }
//        getPersonFacade().flush();
//        getFacade().flush();
    }

    public void createPatientList() {
        String sql;
        Map m = new HashMap();
        sql = " select p from Patient p ";

        if (getReportKeyWord().isAdditionalDetails()) {
            sql += " where ( p.code is not null "
                    + " or p.code=:code ) ";
            if (getReportKeyWord().getMembershipScheme() != null) {
                sql += " and p.person.membershipScheme=:mem ";
                m.put("mem", getReportKeyWord().getMembershipScheme());
            }
            if (getReportKeyWord().getString().equals("0")) {
            }
            if (getReportKeyWord().getString().equals("1")) {
                sql += " and p.retired=false ";
            }
            if (getReportKeyWord().getString().equals("2")) {
                sql += " and p.retired=true ";
            }
            m.put("code", "");
            sql += " order by p.code ";
            patientList = getFacade().findByJpql(sql, m, getReportKeyWord().getNumOfRows());
            for (Patient p : patientList) {
                if (p.getCreatedAt() != null) {
                    m = new HashMap();
                    sql = "select b from Bill b where b.retired=false "
                            + " and b.billDate=:d "
                            + " and b.patient.id=:p "
                            + " and b.paymentMethod=:pm ";
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(p.getCreatedAt());
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    m.put("pm", PaymentMethod.OnlineSettlement);
                    m.put("d", cal.getTime());
                    m.put("p", p.getId());
                    Bill b = getBillFacade().findFirstByJpql(sql, m);
                    if (b != null) {
                        p.setBill(b);
                    }
                }
            }
        } else {
            if (getReportKeyWord().getString().equals("0")) {
            }
            if (getReportKeyWord().getString().equals("1")) {
                sql += " where p.retired=false ";
            }
            if (getReportKeyWord().getString().equals("2")) {
                sql += " where p.retired=true ";
            }
            sql += " order by p.createdAt desc ";
            patientList = getFacade().findByJpql(sql, getReportKeyWord().getNumOfRows());
        }

    }

    public void activePatient(Patient p) {
//        p.setEditedAt(new Date());
//        p.setEditer(getSessionController().getLoggedUser());
        p.setRetired(false);
        p.setRetireComments("Re-Activated");
        getFacade().edit(p);

//        p.getPerson().setEditedAt(new Date());
//        p.getPerson().setEditer(getSessionController().getLoggedUser());
        p.getPerson().setRetired(false);
        p.getPerson().setRetireComments("Re-Activated");
        getPersonFacade().edit(p.getPerson());
        createPatientList();
        JsfUtil.addSuccessMessage("Re-Activated");
    }

    public void deActivePatient(Patient p) {
//        p.setEditedAt(new Date());
//        p.setEditer(getSessionController().getLoggedUser());
        p.setRetired(true);
        p.setRetireComments("De-Activated");
        getFacade().edit(p);

//        p.getPerson().setEditedAt(new Date());
//        p.getPerson().setEditer(getSessionController().getLoggedUser());
        p.getPerson().setRetired(true);
        p.getPerson().setRetireComments("De-Activated");
        getPersonFacade().edit(p.getPerson());
        createPatientList();
        JsfUtil.addSuccessMessage("De-Activated");
    }

    public PatientFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(PatientFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public PatientController() {
    }

    public Patient findPatientByPatientId(Long pid) {
        String j = "select p "
                + " from Patient p "
                + " where p.patientId=:pid";
        Map m = new HashMap();
        m.put("pid", pid);
        return getFacade().findFirstByJpql(j, m);
    }

    public Patient getCurrent() {
        if (current == null) {
            Person p = new Person();
            current = new Patient();
            current.setPerson(p);
        }
        return current;
    }

    public void setCurrent(Patient current) {
        this.current = current;
        getYearMonthDay();
        if (current == null) {
            yearMonthDay.setDay("0");
            yearMonthDay.setMonth("0");
            yearMonthDay.setYear("0");
        } else {
            yearMonthDay.setDay(current.getAgeDays() + "");
            yearMonthDay.setMonth(current.getAgeMonths() + "");
            yearMonthDay.setYear(current.getAgeYears() + "");
        }
        getPatientEncounterController().fillCurrentPatientLists(current);
    }

    private PatientFacade getFacade() {
        return ejbFacade;
    }

    public List<Patient> getItems() {
        return items;
    }

    public void fillAllPatients() {
        String sql;
        sql = "select p from Patient p where p.retired = false order by p.person.name";
        items = getFacade().findByJpql(sql);
    }

    public List<Patient> getItemsByDob() {
        String sql;
        Map m = new HashMap();
        m.put("dob", dob);
        sql = "select p from Patient p where p.retired = false and p.person.dob=:dob order by p.person.name";
        return getFacade().findByJpql(sql, m);
    }

    public void membershipChangeListner() {
        if (getCurrent().getPerson().getMembershipScheme() == null) {
            getCurrent().setCode(null);
            return;
        }
        if (getCurrent().getPerson().getMembershipScheme().getCode() == null
                || getCurrent().getPerson().getMembershipScheme().getCode().equals("")) {
            getCurrent().setCode(null);
            JsfUtil.addErrorMessage("Please Select Membership Scheme Code Correctly");
            return;
        }
        if (getCurrent().getId() == null) {
            getCurrent().setCode(getCountPatientCode(getCurrent().getPerson().getMembershipScheme().getCode()));
        } else {
            Patient p = getEjbFacade().find(getCurrent().getId());
            getCurrent().setCode(p.getCode());
        }
    }

    public String getCountPatientCode(String s) {

        String sql;
        Map m = new HashMap();
        sql = "select p FROM Patient p "
                + " where p.code is not null"
                + " and p.retired=false "
                + " and (p.code) like :q "
                + " order by p.code desc ";
        m.put("q", "%" + s.toUpperCase() + "%");

        Patient p = getEjbFacade().findFirstByJpql(sql, m);
        DecimalFormat df = new DecimalFormat("000000");
        String st = "";
        if (p != null) {
            String str = p.getCode();
//        //System.out.println("str.substring(0,1) = " + str.substring(0, 1));
//        //System.out.println("str.substring(0,2) = " + str.substring(0, 2));
//        //System.out.println("str.substring(2) = " + str.substring(2));
//        //System.out.println("str.substring(3) = " + str.substring(3));
//        //System.out.println("str.substring(3,7) = " + str.substring(3, 7));
            long l = Long.parseLong(str.substring(2));
            l++;
            st += s;
            st += df.format(l);
            return st;
        } else {
            st += s;
            st += df.format(1l);
            return st;
        }

    }

    private boolean errorCheck(Patient p) {
        if (p == null) {
            UtilityController.addErrorMessage("No Current. Error. NOT SAVED");
            return true;
        }
        if (p.getPerson() == null) {
            UtilityController.addErrorMessage("No Person. Not Saved");
            return true;
        }
        if (p.getPerson().getName().trim().equals("")) {
            UtilityController.addErrorMessage("Please Enter a Name");
            return true;
        }
        if (p.getPerson().getSex() == null) {
            UtilityController.addErrorMessage("Please Select Sex");
            return true;
        }
        if (p.getPerson().getDob() == null) {
            UtilityController.addErrorMessage("Please Pic a Birth Day");
            return true;
        }
        if (p.getPerson().getAddress() == null || p.getPerson().getAddress().equals("")) {
            UtilityController.addErrorMessage("Please Enter a Address");
            return true;
        }
        if (sessionController.getApplicationPreference().isNeedAreaForPatientRegistration()) {
            if (p.getPerson().getArea() == null) {
                UtilityController.addErrorMessage("Please Enter a Area");
                return true;
            }
        }
        if (sessionController.getApplicationPreference().isNeedPhoneNumberForPatientRegistration()) {
            if (p.getPerson().getPhone() == null || p.getPerson().getPhone().equals("")) {
                UtilityController.addErrorMessage("Please Enter a Phone Number");
                return true;
            }
        }
        if (sessionController.getApplicationPreference().isNeedNicForPatientRegistration()) {
            if (p.getPerson().getNic() == null || p.getPerson().getNic().equals("")) {
                UtilityController.addErrorMessage("Please Enter a Nic No");
                return true;
            }
        }
//        if (getCurrent().getPhn().equals("")) {
//            UtilityController.addErrorMessage("Please Enter PHN number");
//            return;
//        }
        return false;
    }

    private boolean checkCodeNull(Patient pt) {
        Patient p = null;
        if (pt.getId() != null) {
            p = getEjbFacade().find(pt.getId());
        }
        if (p != null) {
            if (pt.getCode() == null || pt.getCode().equals("")) {
                JsfUtil.addErrorMessage("Please Enter a Code");
                return true;
            } else {
                String sql;
                Map m = new HashMap();
                sql = "select p FROM Patient p "
                        + " where p.code is not null"
                        + " and p.retired=false "
                        + " and p!=:p "
                        + " and (p.code)=:q "
                        + " order by p.code desc ";
                m.put("q", pt.getCode().toUpperCase());
                m.put("p", pt);

                p = getEjbFacade().findFirstByJpql(sql, m);
                if (p != null) {
                    JsfUtil.addErrorMessage("Code Already Exsist.Please Try - " + getCountPatientCode(pt.getPerson().getMembershipScheme().getCode()));
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }

    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public BillNumberGenerator getBillNumberBean() {
        return billNumberBean;
    }

    public void setBillNumberBean(BillNumberGenerator billNumberBean) {
        this.billNumberBean = billNumberBean;
    }

    public StreamedContent getBarcode() {
        return barcode;
    }

    public void setBarcode(StreamedContent barcode) {
        this.barcode = barcode;
    }

    public String getMembershipTypeListner() {
        return membershipTypeListner;
    }

    public void setMembershipTypeListner(String membershipTypeListner) {
        this.membershipTypeListner = membershipTypeListner;
    }

    public Person getFamilyMember() {
        if (familyMember == null) {
            familyMember = new Person();
        }
        return familyMember;
    }

    public void setFamilyMember(Person familyMember) {
        this.familyMember = familyMember;
    }

    public List<Person> getFamilyMembers() {
        if (familyMembers == null) {
            familyMembers = new ArrayList<>();
        }
        return familyMembers;
    }

    public void setFamilyMembers(List<Person> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public ReportKeyWord getReportKeyWord() {
        if (reportKeyWord == null) {
            reportKeyWord = new ReportKeyWord();
        }
        return reportKeyWord;
    }

    public void setReportKeyWord(ReportKeyWord reportKeyWord) {
        this.reportKeyWord = reportKeyWord;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public List<Family> getFamilies() {
        return families;
    }

    public void setFamilies(List<Family> families) {
        this.families = families;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<Patient> getSelectedItems() {
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }
        return selectedItems;
    }

    public void setSelectedItems(List<Patient> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public MembershipScheme getMembershipScheme() {
        return membershipScheme;
    }

    public void setMembershipScheme(MembershipScheme membershipScheme) {
        this.membershipScheme = membershipScheme;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SecurityController getSecurityController() {
        return securityController;
    }

    public WebUserFacade getWebUserFacade() {
        return webUserFacade;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchPhone() {
        return searchPhone;
    }

    public void setSearchPhone(String searchPhone) {
        this.searchPhone = searchPhone;
    }

    public String getSearchNic() {
        return searchNic;
    }

    public void setSearchNic(String searchNic) {
        this.searchNic = searchNic;
    }

    public String getSearchPhn() {
        return searchPhn;
    }

    public void setSearchPhn(String searchPhn) {
        this.searchPhn = searchPhn;
    }

    public String getSearchPatientCode() {
        return searchPatientCode;
    }

    public void setSearchPatientCode(String searchPatientCode) {
        this.searchPatientCode = searchPatientCode;
    }

    public String getSearchPatientId() {
        return searchPatientId;
    }

    public void setSearchPatientId(String searchPatientId) {
        this.searchPatientId = searchPatientId;
    }

    public String getSearchBillId() {
        return searchBillId;
    }

    public void setSearchBillId(String searchBillId) {
        this.searchBillId = searchBillId;
    }

    public String getSearchSampleId() {
        return searchSampleId;
    }

    public void setSearchSampleId(String searchSampleId) {
        this.searchSampleId = searchSampleId;
    }

    public List<Patient> getSearchedPatients() {
        return searchedPatients;
    }

    public void setSearchedPatients(List<Patient> searchedPatients) {
        this.searchedPatients = searchedPatients;
    }

    public Integer getAgeYearComponant() {
        return ageYearComponant;
    }

    public void setAgeYearComponant(Integer ageYearComponant) {
        this.ageYearComponant = ageYearComponant;
    }

    public Integer getAgeMonthComponant() {
        return ageMonthComponant;
    }

    public void setAgeMonthComponant(Integer ageMonthComponant) {
        this.ageMonthComponant = ageMonthComponant;
    }

    public Integer getAgeDateComponant() {
        return ageDateComponant;
    }

    public void setAgeDateComponant(Integer ageDateComponant) {
        this.ageDateComponant = ageDateComponant;
    }

    public PracticeBookingController getPracticeBookingController() {
        return practiceBookingController;
    }

    public void setPracticeBookingController(PracticeBookingController practiceBookingController) {
        this.practiceBookingController = practiceBookingController;
    }

    public PatientEncounterController getPatientEncounterController() {
        return patientEncounterController;
    }

    public void setPatientEncounterController(PatientEncounterController PatientEncounterController) {
        this.patientEncounterController = PatientEncounterController;
    }

    public FamilyFacade getFamilyFacade() {
        return familyFacade;
    }

    public FamilyMemberFacade getFamilyMemberFacade() {
        return familyMemberFacade;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Family getCurrentFamily() {
        return currentFamily;
    }

    public void setCurrentFamily(Family currentFamily) {
        this.currentFamily = currentFamily;
    }

    public FamilyMember getCurrentFamilyMember() {
        return currentFamilyMember;
    }

    public void setCurrentFamilyMember(FamilyMember currentFamilyMember) {
        this.currentFamilyMember = currentFamilyMember;
    }

    public Patient getAddingPatientToFamily() {
        return addingPatientToFamily;
    }

    public void setAddingPatientToFamily(Patient addingPatientToFamily) {
        this.addingPatientToFamily = addingPatientToFamily;
    }

    public FamilyMember getRemovingFamilyMember() {
        return removingFamilyMember;
    }

    public void setRemovingFamilyMember(FamilyMember removingFamilyMember) {
        this.removingFamilyMember = removingFamilyMember;
    }

    public Relation getCurrentRelation() {
        return currentRelation;
    }

    public void setCurrentRelation(Relation currentRelation) {
        this.currentRelation = currentRelation;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    /**
     *
     * Set all Patients to null
     *
     */
    /**
     *
     */
    /**
     *
     * Delete the current Patient
     *
     */
    /**
     *
     */
    @FacesConverter(forClass = Patient.class)
    public static class PatientControllerConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PatientController controller = (PatientController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "patientController");
            //////System.out.println("value at converter getAsObject is " + value);
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            //////System.out.println(value);
            if (value == null || value.equals("null") || value.trim().equals("")) {
                key = 0l;
            } else {
                key = Long.valueOf(value);
            }
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Patient) {
                Patient o = (Patient) object;
                return getStringKey(o.getId());
            } else {
                String error = "object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + PatientController.class.getName();
                System.out.println("error = " + error);
                return null;
            }
        }
    }

}
