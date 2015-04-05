package util;

import com.opencsv.CSVWriter;
import models.Learner;
import models.User;

import java.io.IOException;
import java.io.Writer;
import java.util.List;


public class CSVUtil {

    public static CSVWriter getCSVWriter(Writer writer) {
        return new CSVWriter(writer);
    }

    public static void writeLearnerCSV(List<Learner> learners, Writer writer) throws IOException {
        CSVWriter csvWriter = getCSVWriter(writer);

        String[] headers = {
                "Last Name",
                "First Name",
                "Email",
                "Type",
                "Coordinator Email"
        };
        csvWriter.writeNext(headers);

        for (Learner learner: learners) {
            String[] fieldsToWrite = {
                    learner.lastName,
                    learner.firstName,
                    learner.email,
                    learner.learnerType,
                    learner.ownerEmail
            };
            csvWriter.writeNext(fieldsToWrite);
        }
        csvWriter.close();
    }

    public static void writeCoordinatorCSV(List<User> coordinators, Writer writer) throws IOException {
        CSVWriter csvWriter = getCSVWriter(writer);

        String[] headers = {
                "Last Name",
                "First Name",
                "Email",
                "Department",
                "Phone Number"
        };
        csvWriter.writeNext(headers);

        for (User coordinator: coordinators) {
            String[] fieldsToWrite = {
                    coordinator.lastName,
                    coordinator.firstName,
                    coordinator.email,
                    coordinator.department,
                    coordinator.phoneNumber
            };

            csvWriter.writeNext(fieldsToWrite);
        }
        csvWriter.close();
    }
}
