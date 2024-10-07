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

    // Phương thức chính để thực hiện ghép nối
    public void calcMatches() {
        // Xem xét tất cả bệnh nhân để bắt đầu ghép với bác sĩ và bệnh viện
        for (int patientIndex = 0; patientIndex < numberOfEntities; patientIndex++) {
            if (!isPatientMatched[patientIndex]) {
                matchPatientWithDoctorAndHospital(patientIndex);
            }
        }
    }

    // Ghép một bệnh nhân với bác sĩ và bệnh viện
    private void matchPatientWithDoctorAndHospital(int patientIndex) {
        int[] patientDoctorPreferences = preferenceList[0][patientIndex]; // Lấy danh sách ưu tiên bác sĩ của bệnh nhân

        // Duyệt qua danh sách ưu tiên bác sĩ của bệnh nhân
        for (int doctorIndex : patientDoctorPreferences) {
            if (!isDoctorMatched[doctorIndex]) {
                // Nếu bác sĩ chưa được ghép, tạo một newTriplet và thêm bệnh nhân + bác sĩ
                Set<String> newTriplet = new HashSet<>();
                newTriplet.add(patients[patientIndex]);
                newTriplet.add(doctors[doctorIndex]);

                isPatientMatched[patientIndex] = true;
                isDoctorMatched[doctorIndex] = true;

                // Ghép bệnh viện cho cặp bệnh nhân - bác sĩ
                matchWithHospital(newTriplet, patientIndex, doctorIndex);
                break;
            } else {
                // Nếu bác sĩ đã được ghép, so sánh mức độ ưu tiên của bệnh nhân cũ và mới
                int currentPatientIndex = getMatchedPatientIndex(doctorIndex);
                if (isPatientPreferredOverCurrent(patientIndex, currentPatientIndex, doctorIndex)) {
                    // Bệnh nhân mới được ưu tiên hơn, cập nhật ghép cặp với bác sĩ
                    isPatientMatched[currentPatientIndex] = false;  // Hủy ghép với bệnh nhân cũ

                    Set<String> newTriplet = new HashSet<>();
                    newTriplet.add(patients[patientIndex]);
                    newTriplet.add(doctors[doctorIndex]);

                    // Ghép bệnh viện cho cặp mới và cập nhật lại danh sách
                    updateDoctorTriplet(doctorIndex, newTriplet);
                    isPatientMatched[patientIndex] = true;
                    matchWithHospital(newTriplet, patientIndex, doctorIndex);
                    break;
                }
            }
        }
    }

    // Ghép cặp bệnh nhân - bác sĩ với bệnh viện
    private void matchWithHospital(Set<String> triplet, int patientIndex, int doctorIndex) {
        int[] doctorHospitalPreferences = preferenceList[1][doctorIndex]; // Lấy danh sách ưu tiên bệnh viện của bác sĩ

        // Duyệt qua danh sách ưu tiên bệnh viện của bác sĩ
        for (int hospitalIndex : doctorHospitalPreferences) {
            if (!isHospitalMatched[hospitalIndex]) {
                // Nếu bệnh viện chưa được ghép, ghép với bệnh viện này
                triplet.add(hospitals[hospitalIndex]);
                isHospitalMatched[hospitalIndex] = true;
                matchedTriplets.add(triplet);
                break;
            } else {
                // Nếu bệnh viện đã được ghép, so sánh thứ tự ưu tiên của cặp mới và cũ
                Set<String> currentTriplet = getCurrentTriplet(hospitalIndex);
                if (isTripletPreferred(triplet, currentTriplet, hospitalIndex)) {
                    // Ưu tiên cặp mới, thay thế cặp cũ
                    matchedTriplets.remove(currentTriplet);
                    matchedTriplets.add(triplet);
                    break;
                }
            }
        }
    }

    // So sánh mức độ ưu tiên của bệnh nhân mới với bệnh nhân đã ghép
    private boolean isPatientPreferredOverCurrent(int newPatientIndex, int currentPatientIndex, int doctorIndex) {
        int[] doctorPreferenceList = preferenceList[1][doctorIndex];  // Danh sách ưu tiên của bác sĩ
        int newPatientRank = getPreferenceRank(doctorPreferenceList, newPatientIndex);
        int currentPatientRank = getPreferenceRank(doctorPreferenceList, currentPatientIndex);

        return newPatientRank < currentPatientRank;  // Rank càng thấp càng được ưu tiên
    }

    // So sánh mức độ ưu tiên của cặp mới với cặp cũ tại bệnh viện
    private boolean isTripletPreferred(Set<String> newTriplet, Set<String> currentTriplet, int hospitalIndex) {
        // So sánh mức độ ưu tiên giữa hai cặp dựa trên danh sách ưu tiên của bệnh viện
        int newTripletScore = getTripletScore(newTriplet, hospitalIndex);
        int currentTripletScore = getTripletScore(currentTriplet, hospitalIndex);

        return newTripletScore < currentTripletScore;  // Điểm càng thấp càng được ưu tiên
    }

    // Tính điểm ưu tiên của cặp ghép tại bệnh viện
    private int getTripletScore(Set<String> triplet, int hospitalIndex) {
        String patient = triplet.stream().filter(t -> t.startsWith("P")).findFirst().orElse(null);
        String doctor = triplet.stream().filter(t -> t.startsWith("D")).findFirst().orElse(null);

        int patientIndex = getIndexFromName(patient, patients);
        int doctorIndex = getIndexFromName(doctor, doctors);

        int patientRank = getPreferenceRank(preferenceList[0][hospitalIndex], patientIndex);
        int doctorRank = getPreferenceRank(preferenceList[1][hospitalIndex], doctorIndex);

        return patientRank + doctorRank;  // Điểm tổng hợp của cả bệnh nhân và bác sĩ
    }

    private int getPreferenceRank(int[] preferenceList, int entityIndex) {
        return preferenceList[entityIndex];
    }

    private int getIndexFromName(String name, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(name)) {
                return i;
            }
        }
        return -1;
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
    private void updateDoctorTriplet(int doctorIndex, Set<String> newTriplet) {
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
}
