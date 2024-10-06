package org.example.LMPV4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Matching {
    private int numberOfEntities;
    private int[][][] preferenceList;
    private String[] patients;
    private String[] doctors;
    private String[] hospitals;
    private boolean[] isPatientMatched;
    private boolean[] isDoctorMatched;
    private boolean[] isHospitalMatched;
    private List<Set<String>> matchedTriplets;
    public Matching(String[] patients, String[] doctors, String[] hospitals, int[][][] preferenceList) {
        this.patients = patients;
        this.doctors = doctors;
        this.hospitals = hospitals;
        this.numberOfEntities = patients.length;
        this.preferenceList = preferenceList;

        isPatientMatched = new boolean[numberOfEntities];
        isDoctorMatched = new boolean[numberOfEntities];
        isHospitalMatched = new boolean[numberOfEntities];

        matchedTriplets = new ArrayList<>();
    }

    public void calcMatches() {
        // Gale-Shapley lần 1: Ghép bệnh nhân với bác sĩ
        for (int patientIndex = 0; patientIndex < numberOfEntities; patientIndex++) {
            if (!isPatientMatched[patientIndex]) {
                matchPatientWithDoctor(patientIndex);
            }
        }

        // Gale-Shapley lần 2: Ghép cặp bệnh nhân - bác sĩ với bệnh viện
        for (Set<String> triplet : matchedTriplets) {
            matchTripletWithHospital(triplet);
        }
    }

    private void matchPatientWithDoctor(int patientIndex) {
        for (int doctorIndex = 0; doctorIndex < numberOfEntities; doctorIndex++) {
            if (!isDoctorMatched[doctorIndex]) {
                // Bác sĩ chưa được ghép, ghép bệnh nhân với bác sĩ
                Set<String> newTriplet = new HashSet<>();
                newTriplet.add(patients[patientIndex]);
                newTriplet.add(doctors[doctorIndex]);

                isPatientMatched[patientIndex] = true;
                isDoctorMatched[doctorIndex] = true;

                matchedTriplets.add(newTriplet);
                break;
            } else {
                //bác sĩ đã được ghép với một bệnh nhân khác
                int currentPatientIndex = getMatchedPatientIndex(doctorIndex);
                if (isPatientPreferredOverCurrent(patientIndex, currentPatientIndex, doctorIndex)) {
                    //bệnh nhân mới được ưu tiên hơn bệnh nhân cũ
                    isPatientMatched[currentPatientIndex] = false;  //hủy ghép với bệnh nhân cũ

                    Set<String> newTriplet = new HashSet<>();
                    newTriplet.add(patients[patientIndex]);
                    newTriplet.add(doctors[doctorIndex]);

                    // Thay thế triplet cũ bằng triplet mới
                    updateDoctorTriplet(doctorIndex, newTriplet);

                    isPatientMatched[patientIndex] = true;
                    break;
                }
            }
        }
    }

    private boolean isPatientPreferredOverCurrent(int newPatientIndex, int currentPatientIndex, int doctorIndex) {
        int[] doctorPreferenceList = preferenceList[1][doctorIndex];  // Lấy danh sách ưu tiên của bác sĩ
        int newPatientRank = getPreferenceRank(doctorPreferenceList, newPatientIndex);
        int currentPatientRank = getPreferenceRank(doctorPreferenceList, currentPatientIndex);

        return newPatientRank < currentPatientRank;  // Rank càng thấp càng được ưu tiên
    }

    private void updateDoctorTriplet(int doctorIndex, Set<String> newTriplet) {
        // Xóa triplet cũ và thay thế bằng triplet mới cho bác sĩ
        Set<String> currentTriplet = getCurrentTripletByDoctor(doctorIndex);
        if (currentTriplet != null) {
            matchedTriplets.remove(currentTriplet);
        }
        matchedTriplets.add(newTriplet);
    }

    private Set<String> getCurrentTripletByDoctor(int doctorIndex) {
        String doctorName = doctors[doctorIndex];
        for (Set<String> triplet : matchedTriplets) {
            if (triplet.contains(doctorName)) {
                return triplet;
            }
        }
        return null;
    }
    private int getMatchedPatientIndex(int doctorIndex) {
        String doctorName = doctors[doctorIndex];
        for (int i = 0; i < patients.length; i++) {
            for (Set<String> triplet : matchedTriplets) {
                if (triplet.contains(doctorName) && triplet.contains(patients[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void matchTripletWithHospital(Set<String> triplet) {
        for (int hospitalIndex = 0; hospitalIndex < numberOfEntities; hospitalIndex++) {
            if (!isHospitalMatched[hospitalIndex]) {
                triplet.add(hospitals[hospitalIndex]);
                isHospitalMatched[hospitalIndex] = true;
                break;
            } else {
                compareTripletPriority(triplet, hospitalIndex);
            }
        }
    }

    private void compareTripletPriority(Set<String> newTriplet, int hospitalIndex) {
        Set<String> currentTriplet = getCurrentTriplet(hospitalIndex);
        if (tripletPriority(newTriplet, currentTriplet)) {
            matchedTriplets.remove(currentTriplet);
            matchedTriplets.add(newTriplet);
        }
    }

    private boolean tripletPriority(Set<String> newTriplet, Set<String> currentTriplet) {
        // So sánh độ ưu tiên giữa cặp mới và cặp cũ
        return true;
    }

    private Set<String> getCurrentTriplet(int hospitalIndex) {
        String hospitalName = hospitals[hospitalIndex];
        for (Set<String> triplet : matchedTriplets) {
            if (triplet.contains(hospitalName)) {
                return triplet;
            }
        }
        return null;
    }

    private int getPreferenceRank(int[] preferenceList, int entityIndex) {
        return preferenceList[entityIndex];
    }

}
