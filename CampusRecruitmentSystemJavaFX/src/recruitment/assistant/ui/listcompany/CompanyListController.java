package recruitment.assistant.ui.listcompany;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import recruitment.assistant.alert.AlertMaker;
import recruitment.assistant.database.DatabaseHandler;
import recruitment.assistant.ui.addcompany.CompanyAddController;
import recruitment.assistant.ui.main.MainController;
import recruitment.assistant.util.RecruitmentAssistantUtil;

public class CompanyListController implements Initializable {

    ObservableList<Company> list = FXCollections.observableArrayList();

    @FXML
    private StackPane rootPane;
    @FXML
    private TableView<Company> tableView;
    @FXML
    private TableColumn<Company, String> titleCol;
    @FXML
    private TableColumn<Company, String> idCol;
    @FXML
    private TableColumn<Company, String> campusCol;
    @FXML
    private TableColumn<Company, String> headquarterCol;
    @FXML
    private TableColumn<Company, Boolean> availabilityCol;
    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        loadData();
    }

    private Stage getStage() {
        return (Stage) tableView.getScene().getWindow();
    }

    private void initCol() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        campusCol.setCellValueFactory(new PropertyValueFactory<>("campus"));
        headquarterCol.setCellValueFactory(new PropertyValueFactory<>("headquarter"));
        availabilityCol.setCellValueFactory(new PropertyValueFactory<>("availabilty"));
    }

    private void loadData() {
        list.clear();

        DatabaseHandler handler = DatabaseHandler.getInstance();
        String qu = "SELECT * FROM COMPANY";
        ResultSet rs = handler.execQuery(qu);
        try {
            while (rs.next()) {
                String titlex = rs.getString("title");
                String campus = rs.getString("campus");
                String id = rs.getString("id");
                String headquarter = rs.getString("headquarter");
                Boolean avail = rs.getBoolean("isAvail");

                list.add(new Company(titlex, id, campus, headquarter, avail));

            }
        } catch (SQLException ex) {
            Logger.getLogger(CompanyAddController.class.getName()).log(Level.SEVERE, null, ex);
        }

        tableView.setItems(list);
    }

    @FXML
    private void handleCompanyDeleteOption(ActionEvent event) {
        //Fetch the selected row
        Company selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
        if (selectedForDeletion == null) {
            AlertMaker.showErrorMessage("No Company selected", "Please select a Company for deletion.");
            return;
        }
        if (DatabaseHandler.getInstance().isCompanyAlreadyVisited(selectedForDeletion)) {
            AlertMaker.showErrorMessage("Cant be deleted", "This Company is already Visited and cant be deleted.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting Company");
        alert.setContentText("Are you sure want to delete the Company " + selectedForDeletion.getTitle() + " ?");
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.get() == ButtonType.OK) {
            Boolean result = DatabaseHandler.getInstance().deleteCompany(selectedForDeletion);
            if (result) {
                AlertMaker.showSimpleAlert("Company deleted", selectedForDeletion.getTitle() + " was deleted successfully.");
                list.remove(selectedForDeletion);
            } else {
                AlertMaker.showSimpleAlert("Failed", selectedForDeletion.getTitle() + " could not be deleted");
            }
        } else {
            AlertMaker.showSimpleAlert("Deletion cancelled", "Deletion process cancelled");
        }
    }

    @FXML
    private void handleCompanyEditOption(ActionEvent event) {
        //Fetch the selected row
        Company selectedForEdit = tableView.getSelectionModel().getSelectedItem();
        if (selectedForEdit == null) {
            AlertMaker.showErrorMessage("No Company selected", "Please select a Company for edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recruitment/assistant/ui/addcompany/add_company.fxml"));
            Parent parent = loader.load();

            CompanyAddController controller = (CompanyAddController) loader.getController();
            controller.inflateUI(selectedForEdit);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Edit Company");
            stage.setScene(new Scene(parent));
            stage.show();
            RecruitmentAssistantUtil.setStageIcon(stage);

            stage.setOnCloseRequest((e) -> {
                handleRefresh(new ActionEvent());
            });

        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void exportAsPDF(ActionEvent event) {
        List<List> printData = new ArrayList<>();
        String[] headers = {"   Title   ", "ID", "  Campus  ", "  Headquarters ", "Avail"};
        printData.add(Arrays.asList(headers));
        for (Company company : list) {
            List<String> row = new ArrayList<>();
            row.add(company.getTitle());
            row.add(company.getId());
            row.add(company.getCampus());
            row.add(company.getHeadquarter());
            row.add(company.getAvailabilty());
            printData.add(row);
        }
        RecruitmentAssistantUtil.initPDFExprot(rootPane, contentPane, getStage(), printData);
    }

    @FXML
    private void closeStage(ActionEvent event) {
        getStage().close();
    }

    public static class Company {

        private final SimpleStringProperty title;
        private final SimpleStringProperty id;
        private final SimpleStringProperty campus;
        private final SimpleStringProperty headquarter;
        private final SimpleStringProperty availabilty;

        public Company(String title, String id, String campus, String pub, Boolean avail) {
            this.title = new SimpleStringProperty(title);
            this.id = new SimpleStringProperty(id);
            this.campus = new SimpleStringProperty(campus);
            this.headquarter = new SimpleStringProperty(pub);
            if (avail) {
                this.availabilty = new SimpleStringProperty("Available");
            } else {
                this.availabilty = new SimpleStringProperty("Visited");
            }
        }

        public String getTitle() {
            return title.get();
        }

        public String getId() {
            return id.get();
        }

        public String getCampus() {
            return campus.get();
        }

        public String getHeadquarter() {
            return headquarter.get();
        }

        public String getAvailabilty() {
            return availabilty.get();
        }

    }

}
