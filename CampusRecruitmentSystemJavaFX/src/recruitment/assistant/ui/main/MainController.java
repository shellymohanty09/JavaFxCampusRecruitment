package recruitment.assistant.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import recruitment.assistant.ui.callback.CompanyReturnCallback;
import recruitment.assistant.alert.AlertMaker;
import recruitment.assistant.database.DataHelper;
import recruitment.assistant.database.DatabaseHandler;
import recruitment.assistant.ui.visitedlist.VisitedListController;
import recruitment.assistant.ui.main.toolbar.ToolbarController;
import recruitment.assistant.util.RecruitmentAssistantUtil;

public class MainController implements Initializable, CompanyReturnCallback {

    private static final String COMPANY_NOT_AVAILABLE = "Not Available";
    private static final String NO_SUCH_COMPANY_AVAILABLE = "No Such Company Available";
    private static final String NO_SUCH_MEMBER_AVAILABLE = "No Such Member Available";
    private static final String COMPANY_AVAILABLE = "Available";

    private Boolean isReadyForSubmission = false;
    private DatabaseHandler databaseHandler;
    private PieChart companyChart;
    private PieChart memberChart;

    @FXML
    private HBox company_info;
    @FXML
    private HBox member_info;
    @FXML
    private TextField companyIDInput;
    @FXML
    private Text companyName;
    @FXML
    private Text companyCampus;
    @FXML
    private Text companyStatus;
    @FXML
    private TextField memberIDInput;
    @FXML
    private Text memberName;
    @FXML
    private Text memberMobile;
    @FXML
    private JFXTextField companyID;
    @FXML
    private StackPane rootPane;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private Text memberNameHolder;
    @FXML
    private Text memberEmailHolder;
    @FXML
    private Text memberContactHolder;
    @FXML
    private Text companyNameHolder;
    @FXML
    private Text companyCampusHolder;
    @FXML
    private Text companyPublisherHolder;
    @FXML
    private Text visitDateHolder;
    @FXML
    private Text numberDaysHolder;
    @FXML
    private Text fineInfoHolder;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private JFXButton renewButton;
    @FXML
    private JFXButton submissionButton;
    @FXML
    private HBox submissionDataContainer;
    @FXML
    private StackPane companyInfoContainer;
    @FXML
    private StackPane memberInfoContainer;
    @FXML
    private Tab companyVisitTab;
    @FXML
    private Tab renewTab;
    @FXML
    private JFXTabPane mainTabPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        JFXDepthManager.setDepth(company_info, 1);
        JFXDepthManager.setDepth(member_info, 1);

        databaseHandler = DatabaseHandler.getInstance();

        initDrawer();
        initGraphs();
    }

    @FXML
    private void loadCompanyInfo(ActionEvent event) {
        clearCompanyCache();
        enableDisableGraph(false);

        String id = companyIDInput.getText();
        ResultSet rs = DataHelper.getCompanyInfoWithVisitData(id);
        Boolean flag = false;
        try {
            if (rs.next()) {
                String bName = rs.getString("title");
                String bCampus = rs.getString("campus");
                Boolean bStatus = rs.getBoolean("isAvail");
                Timestamp visitedOn = rs.getTimestamp("visitTime");

                companyName.setText(bName);
                companyCampus.setText(bCampus);
                String status = (bStatus) ? COMPANY_AVAILABLE : String.format("Visited on %s", RecruitmentAssistantUtil.getDateString(new Date(visitedOn.getTime())));
                if (!bStatus) {
                    companyStatus.getStyleClass().add("not-available");
                } else {
                    companyStatus.getStyleClass().remove("not-available");
                }
                companyStatus.setText(status);

                flag = true;
            }

            if (!flag) {
                companyName.setText(NO_SUCH_COMPANY_AVAILABLE);
            }

        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void clearCompanyCache() {
        companyName.setText("");
        companyCampus.setText("");
        companyStatus.setText("");
    }

    void clearMemberCache() {
        memberName.setText("");
        memberMobile.setText("");
    }

    @FXML
    private void loadMemberInfo(ActionEvent event) {
        clearMemberCache();
        enableDisableGraph(false);

        String id = memberIDInput.getText();
        String qu = "SELECT * FROM MEMBER WHERE id = '" + id + "'";
        ResultSet rs = databaseHandler.execQuery(qu);
        Boolean flag = false;
        try {
            while (rs.next()) {
                String mName = rs.getString("name");
                String mMobile = rs.getString("mobile");

                memberName.setText(mName);
                memberMobile.setText(mMobile);

                flag = true;
            }

            if (!flag) {
                memberName.setText(NO_SUCH_MEMBER_AVAILABLE);
            }

        } catch (SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void loadVisitOperation(ActionEvent event) {
        if (checkForVisitValidity()) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Invalid Input", null);
            return;
        }
        if (companyStatus.getText().equals(COMPANY_NOT_AVAILABLE)) {
            JFXButton btn = new JFXButton("Okay!");
            JFXButton viewDetails = new JFXButton("View Details");
            viewDetails.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
                String companyToBeLoaded = companyIDInput.getText();
                companyID.setText(companyToBeLoaded);
                companyID.fireEvent(new ActionEvent());
                mainTabPane.getSelectionModel().select(renewTab);
            });
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn, viewDetails), "Already visited company", "This company is already visitd. Cant process visit request");
            return;
        }

        String memberID = memberIDInput.getText();
        String companyID = companyIDInput.getText();

        JFXButton yesButton = new JFXButton("YES");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            String str = "INSERT INTO VISIT(memberID,companyID) VALUES ("
                    + "'" + memberID + "',"
                    + "'" + companyID + "')";
            String str2 = "UPDATE COMPANY SET isAvail = false WHERE id = '" + companyID + "'";
            System.out.println(str + " and " + str2);

            if (databaseHandler.execAction(str) && databaseHandler.execAction(str2)) {
                JFXButton button = new JFXButton("Done!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Company Visit Complete", null);
                refreshGraphs();
            } else {
                JFXButton button = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Visit Operation Failed", null);
            }
            clearVisitEntries();
        });
        JFXButton noButton = new JFXButton("NO");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            JFXButton button = new JFXButton("That's Okay");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "Visit Cancelled", null);
            clearVisitEntries();
        });
        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Visit", "Are you sure that the company " + companyName.getText() + " wants to recruit " + memberName.getText() + " ?");
    }

    @FXML
    private void loadCompanyInfo2(ActionEvent event) {
        clearEntries();
        ObservableList<String> visitData = FXCollections.observableArrayList();
        isReadyForSubmission = false;

        try {
            String id = companyID.getText();
            String myQuery = "SELECT VISIT.companyID, VISIT.memberID, VISIT.visitTime, VISIT.renew_count,\n"
                    + "MEMBER.name, MEMBER.mobile, MEMBER.email,\n"
                    + "COMPANY.title, COMPANY.campus, COMPANY.publisher\n"
                    + "FROM VISIT\n"
                    + "LEFT JOIN MEMBER\n"
                    + "ON VISIT.memberID=MEMBER.ID\n"
                    + "LEFT JOIN COMPANY\n"
                    + "ON VISIT.companyID=COMPANY.ID\n"
                    + "WHERE VISIT.companyID='" + id + "'";
            ResultSet rs = databaseHandler.execQuery(myQuery);
            if (rs.next()) {
                memberNameHolder.setText(rs.getString("name"));
                memberContactHolder.setText(rs.getString("mobile"));
                memberEmailHolder.setText(rs.getString("email"));

                companyNameHolder.setText(rs.getString("title"));
                companyCampusHolder.setText(rs.getString("campus"));
                companyPublisherHolder.setText(rs.getString("publisher"));

                Timestamp mVisitTime = rs.getTimestamp("visitTime");
                Date dateOfVisit = new Date(mVisitTime.getTime());
                visitDateHolder.setText(RecruitmentAssistantUtil.formatDateTimeString(dateOfVisit));
                Long timeElapsed = System.currentTimeMillis() - mVisitTime.getTime();
                Long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.MILLISECONDS) + 1;
                String daysElapsed = String.format("Used %d days", days);
                numberDaysHolder.setText(daysElapsed);
                
                isReadyForSubmission = true;
                disableEnableControls(true);
                submissionDataContainer.setOpacity(1);
            } else {
                JFXButton button = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(button), "No such Company Exists in Visit Database", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void loadSubmissionOp(ActionEvent event) {
        if (!isReadyForSubmission) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Please select a Company to submit", "Cant simply submit a null company :-)");
            return;
        }

        JFXButton yesButton = new JFXButton("YES, Please");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
            String id = companyID.getText();
            String ac1 = "DELETE FROM VISIT WHERE COMPANYID = '" + id + "'";
            String ac2 = "UPDATE COMPANY SET ISAVAIL = TRUE WHERE ID = '" + id + "'";

            if (databaseHandler.execAction(ac1) && databaseHandler.execAction(ac2)) {
                JFXButton btn = new JFXButton("Done!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Company has been submitted", null);
                disableEnableControls(false);
                submissionDataContainer.setOpacity(0);
            } else {
                JFXButton btn = new JFXButton("Okay.I'll Check");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Submission Has Been Failed", null);
            }
        });
        JFXButton noButton = new JFXButton("No, Cancel");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent ev) -> {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Submission Operation cancelled", null);
        });

        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Submission Operation", "Are you sure want to return the company ?");
    }

    @FXML
    private void loadRenewOp(ActionEvent event) {
        if (!isReadyForSubmission) {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Please select a company to renew", null);
            return;
        }
        JFXButton yesButton = new JFXButton("YES, Please");
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            String ac = "UPDATE VISIT SET visitTime = CURRENT_TIMESTAMP, renew_count = renew_count+1 WHERE COMPANYID = '" + companyID.getText() + "'";
            System.out.println(ac);
            if (databaseHandler.execAction(ac)) {
                JFXButton btn = new JFXButton("Alright!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Company Has Been Revisited", null);
                disableEnableControls(false);
                submissionDataContainer.setOpacity(0);
            } else {
                JFXButton btn = new JFXButton("Okay!");
                AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "RevisitHas Been Failed", null);
            }
        });
        JFXButton noButton = new JFXButton("No, Don't!");
        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event1) -> {
            JFXButton btn = new JFXButton("Okay!");
            AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(btn), "Revisit Operation cancelled", null);
        });
        AlertMaker.showMaterialDialog(rootPane, rootAnchorPane, Arrays.asList(yesButton, noButton), "Confirm Revisit Operation", "Are you sure you want to revisitthe company ?");
    }

    private Stage getStage() {
        return (Stage) rootPane.getScene().getWindow();
    }

    @FXML
    private void handleMenuClose(ActionEvent event) {
        getStage().close();
    }

    @FXML
    private void handleMenuAddCompany(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/addcompany/add_company.fxml"), "Add New Company", null);
    }

    @FXML
    private void handleMenuAddMember(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/addmember/member_add.fxml"), "Add New Member", null);
    }

    @FXML
    private void handleMenuViewCompany(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/listcompany/company_list.fxml"), "Company List", null);
    }

    @FXML
    private void handleAboutMenu(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/about/about.fxml"), "About Me", null);
    }

    @FXML
    private void handleMenuSettings(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/settings/settings.fxml"), "Settings", null);
    }

    @FXML
    private void handleMenuViewMemberList(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/listmember/member_list.fxml"), "Member List", null);
    }

    @FXML
    private void handleVisitedList(ActionEvent event) {
        Object controller = RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/visitedlist/visited_list.fxml"), "Visited Company List", null);
        if (controller != null) {
            VisitedListController cont = (VisitedListController) controller;
            cont.setCompanyReturnCallback(this);
        }
    }

    @FXML
    private void handleMenuFullScreen(ActionEvent event) {
        Stage stage = getStage();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void initDrawer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recruitment/assistant/ui/main/toolbar/toolbar.fxml"));
            VBox toolbar = loader.load();
            drawer.setSidePane(toolbar);
            ToolbarController controller = loader.getController();
            controller.setCompanyReturnCallback(this);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        HamburgerSlideCloseTransition task = new HamburgerSlideCloseTransition(hamburger);
        task.setRate(-1);
        hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
            drawer.toggle();
        });
        drawer.setOnDrawerOpening((event) -> {
            task.setRate(task.getRate() * -1);
            task.play();
            drawer.toFront();
        });
        drawer.setOnDrawerClosed((event) -> {
            drawer.toBack();
            task.setRate(task.getRate() * -1);
            task.play();
        });
    }

    private void clearEntries() {
        memberNameHolder.setText("");
        memberEmailHolder.setText("");
        memberContactHolder.setText("");

        companyNameHolder.setText("");
        companyCampusHolder.setText("");
        companyPublisherHolder.setText("");

        visitDateHolder.setText("");
        numberDaysHolder.setText("");
        fineInfoHolder.setText("");

        disableEnableControls(false);
        submissionDataContainer.setOpacity(0);
    }

    private void disableEnableControls(Boolean enableFlag) {
        if (enableFlag) {
            renewButton.setDisable(false);
            submissionButton.setDisable(false);
        } else {
            renewButton.setDisable(true);
            submissionButton.setDisable(true);
        }
    }

    private void clearVisitEntries() {
        companyIDInput.clear();
        memberIDInput.clear();
        companyName.setText("");
        companyCampus.setText("");
        companyStatus.setText("");
        memberMobile.setText("");
        memberName.setText("");
        enableDisableGraph(true);
    }

    private void initGraphs() {
        companyChart = new PieChart(databaseHandler.getCompanyGraphStatistics());
        memberChart = new PieChart(databaseHandler.getMemberGraphStatistics());
        companyInfoContainer.getChildren().add(companyChart);
        memberInfoContainer.getChildren().add(memberChart);

        companyVisitTab.setOnSelectionChanged((Event event) -> {
            clearVisitEntries();
            if (companyVisitTab.isSelected()) {
                refreshGraphs();
            }
        });
    }

    private void refreshGraphs() {
        companyChart.setData(databaseHandler.getCompanyGraphStatistics());
        memberChart.setData(databaseHandler.getMemberGraphStatistics());
    }

    private void enableDisableGraph(Boolean status) {
        if (status) {
            companyChart.setOpacity(1);
            memberChart.setOpacity(1);
        } else {
            companyChart.setOpacity(0);
            memberChart.setOpacity(0);
        }
    }

    private boolean checkForVisitValidity() {
        companyIDInput.fireEvent(new ActionEvent());
        memberIDInput.fireEvent(new ActionEvent());
        return companyIDInput.getText().isEmpty() || memberIDInput.getText().isEmpty()
                || memberName.getText().isEmpty() || companyName.getText().isEmpty()
                || companyName.getText().equals(NO_SUCH_COMPANY_AVAILABLE) || memberName.getText().equals(NO_SUCH_MEMBER_AVAILABLE);
    }

    @Override
    public void loadCompanyReturn(String companyID) {
        this.companyID.setText(companyID);
        mainTabPane.getSelectionModel().select(renewTab);
        loadCompanyInfo2(null);
        getStage().toFront();
        if (drawer.isShown()) {
            drawer.close();
        }
    }

}
