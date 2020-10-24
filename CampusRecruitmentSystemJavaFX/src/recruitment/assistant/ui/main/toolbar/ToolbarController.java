package recruitment.assistant.ui.main.toolbar;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import recruitment.assistant.ui.callback.CompanyReturnCallback;
import recruitment.assistant.ui.visitedlist.VisitedListController;
import recruitment.assistant.util.RecruitmentAssistantUtil;

public class ToolbarController implements Initializable {

    private CompanyReturnCallback callback;

    public void setCompanyReturnCallback(CompanyReturnCallback callback) {
        this.callback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void loadAddMember(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/addmember/member_add.fxml"), "Add New Member", null);
    }

    @FXML
    private void loadAddCompany(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/addcompany/add_company.fxml"), "Add New Company", null);
    }

    @FXML
    private void loadMemberTable(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/listmember/member_list.fxml"), "Member List", null);
    }

    @FXML
    private void loadCompanyTable(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/listcompany/company_list.fxml"), "Company List", null);
    }

    @FXML
    private void loadSettings(ActionEvent event) {
        RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/settings/settings.fxml"), "Settings", null);
    }

    @FXML
    private void loadVisitedCompanyList(ActionEvent event) {
        Object controller = RecruitmentAssistantUtil.loadWindow(getClass().getResource("/recruitment/assistant/ui/visitedlist/visited_list.fxml"), "Visited Company List", null);
        if (controller != null) {
            VisitedListController cont = (VisitedListController) controller;
            cont.setCompanyReturnCallback(callback);
        }
    }

}
