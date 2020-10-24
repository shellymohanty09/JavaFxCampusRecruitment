package recruitment.assistant.ui.visitedlist;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import recruitment.assistant.database.DatabaseHandler;
import recruitment.assistant.settings.Preferences;
import recruitment.assistant.ui.callback.CompanyReturnCallback;
import recruitment.assistant.util.RecruitmentAssistantUtil;

public class VisitedListController implements Initializable {

    private ObservableList<VisitInfo> list = FXCollections.observableArrayList();
    private CompanyReturnCallback callback;

    @FXML
    private TableView<VisitInfo> tableView;
    @FXML
    private TableColumn<VisitInfo, Integer> idCol;
    @FXML
    private TableColumn<VisitInfo, String> companyIDCol;
    @FXML
    private TableColumn<VisitInfo, String> companyNameCol;
    @FXML
    private TableColumn<VisitInfo, String> campusNameCol;
    @FXML
    private TableColumn<VisitInfo, String> visitCol;
    @FXML
    private TableColumn<VisitInfo, Integer> daysCol;
    @FXML
    private TableColumn<VisitInfo, Float> fineCol;
    @FXML
    private StackPane rootPane;
    @FXML
    private AnchorPane contentPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        loadData();
    }

    private void initCol() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        companyIDCol.setCellValueFactory(new PropertyValueFactory<>("companyID"));
        companyNameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        campusNameCol.setCellValueFactory(new PropertyValueFactory<>("campusName"));
        visitCol.setCellValueFactory(new PropertyValueFactory<>("dateOfVisit"));
        tableView.setItems(list);
    }

    public void setCompanyReturnCallback(CompanyReturnCallback callback) {
        this.callback = callback;
    }

    private void loadData() {
        list.clear();
        DatabaseHandler handler = DatabaseHandler.getInstance();
        String qu = "SELECT VISIT.companyID, VISIT.memberID, VISIT.visitTime, MEMBER.name, COMPANY.title FROM VISIT\n"
                + "LEFT OUTER JOIN MEMBER\n"
                + "ON MEMBER.id = VISIT.memberID\n"
                + "LEFT OUTER JOIN COMPANY\n"
                + "ON COMPANY.id = VISIT.companyID";
        ResultSet rs = handler.execQuery(qu);
        Preferences pref = Preferences.getPreferences();
        try {
            int counter = 0;
            while (rs.next()) {
                counter += 1;
                String memberName = rs.getString("name");
                String companyID = rs.getString("companyID");
                String companyTitle = rs.getString("title");
                Timestamp visitTime = rs.getTimestamp("visitTime");
                System.out.println("Visited on " + visitTime);
                Integer days = Math.toIntExact(TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - visitTime.getTime())) + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void exportAsPDF(ActionEvent event) {
        List<List> printData = new ArrayList<>();
        String[] headers = {"SI", "COMPANY ID", "      COMPANY NAME       ", "    HOLDER NAME     ", "VISIT DATE", "DAYS ELAPSED", "FINE"};
        printData.add(Arrays.asList(headers));
        for (VisitInfo info : list) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(info.getId()));
            row.add(info.getCompanyID());
            row.add(info.getCompanyName());
            row.add(info.getCampusName());
            row.add(info.getDateOfVisit());
            printData.add(row);
        }
        RecruitmentAssistantUtil.initPDFExprot(rootPane, contentPane, getStage(), printData);
    }

    @FXML
    private void closeStage(ActionEvent event) {
        getStage().close();
    }

    private Stage getStage() {
        return (Stage) tableView.getScene().getWindow();
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        VisitInfo visitInfo = tableView.getSelectionModel().getSelectedItem();
        if (visitInfo != null) {
            callback.loadCompanyReturn(visitInfo.getCompanyID());
        }
    }

    public static class VisitInfo {

        private final SimpleIntegerProperty id;
        private final SimpleStringProperty companyID;
        private final SimpleStringProperty companyName;
        private final SimpleStringProperty campusName;
        private final SimpleStringProperty dateOfVisit;

        public VisitInfo(int id, String companyID, String companyName, String campusName, String dateOfVisit) {
            this.id = new SimpleIntegerProperty(id);
            this.companyID = new SimpleStringProperty(companyID);
            this.companyName = new SimpleStringProperty(companyName);
            this.campusName = new SimpleStringProperty(campusName);
            this.dateOfVisit = new SimpleStringProperty(dateOfVisit);
        }

        public Integer getId() {
            return id.get();
        }

        public String getCompanyID() {
            return companyID.get();
        }

        public String getCompanyName() {
            return companyName.get();
        }

        public String getCampusName() {
            return campusName.get();
        }

        public String getDateOfVisit() {
            return dateOfVisit.get();
        }
    }
}
