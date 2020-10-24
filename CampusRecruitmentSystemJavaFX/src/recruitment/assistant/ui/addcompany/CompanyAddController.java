package recruitment.assistant.ui.addcompany;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import recruitment.assistant.alert.AlertMaker;
import recruitment.assistant.data.wrapper.Company;
import recruitment.assistant.database.DataHelper;
import recruitment.assistant.database.DatabaseHandler;
import recruitment.assistant.ui.listcompany.CompanyListController;

public class CompanyAddController implements Initializable {

    @FXML
    private JFXTextField title;
    @FXML
    private JFXTextField id;
    @FXML
    private JFXTextField campus;
    @FXML
    private JFXTextField headquarter;
    @FXML
    private JFXButton saveButton;
    @FXML
    private JFXButton cancelButton;

    DatabaseHandler databaseHandler;
    @FXML
    private StackPane rootPane;
    private Boolean isInEditMode = Boolean.FALSE;
    @FXML
    private AnchorPane mainContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        databaseHandler = DatabaseHandler.getInstance();
    }

    @FXML
    private void addCompany(ActionEvent event) {
        String companyID = id.getText();
        String companyCampus = campus.getText();
        String companyName = title.getText();
        String companyHeadquarter = headquarter.getText();

        if (companyID.isEmpty() || companyCampus.isEmpty() || companyName.isEmpty() || companyHeadquarter.isEmpty()) {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Insufficient Data", "Please enter data in all fields.");
            return;
        }

        if (DataHelper.isCompanyExists(companyID)) {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Duplicate company id", "Company with same Company ID exists.\nPlease use new ID");
            return;
        }

        if (isInEditMode) {
            handleEditOperation();
            return;
        }

        Company company = new Company(companyID, companyName, companyCampus, companyHeadquarter, Boolean.TRUE);
        boolean result = DataHelper.insertNewCompany(company);
        if (result) {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "New company added", companyName + " has been added");
            clearEntries();
        } else {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed to add new company", "Check all the entries and try again");
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    private void checkData() {
        String qu = "SELECT title FROM COMPANY";
        ResultSet rs = databaseHandler.execQuery(qu);
        try {
            while (rs.next()) {
                String titlex = rs.getString("title");
                System.out.println(titlex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CompanyAddController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void inflateUI(CompanyListController.Company company) {
        title.setText(company.getTitle());
        id.setText(company.getId());
        campus.setText(company.getCampus());
        headquarter.setText(company.getHeadquarter());
        id.setEditable(false);
        isInEditMode = Boolean.TRUE;
    }

    private void clearEntries() {
        title.clear();
        id.clear();
        campus.clear();
        headquarter.clear();
    }

    private void handleEditOperation() {
        CompanyListController.Company company = new CompanyListController.Company(title.getText(), id.getText(), campus.getText(), headquarter.getText(), true);
        if (databaseHandler.updateCompany(company)) {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Success", "Update complete");
        } else {
            AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "Could not update data");
        }
    }
}
