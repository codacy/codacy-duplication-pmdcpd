package pages;

import org.codacy.BasePage;
import org.codacy.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.management.GarbageCollectorMXBean;

public class CommitsPage {
    private static final String COMMIT_TABLE = "//*[@class=\"pull-left col-xs-12 files-wrapper\"]";
    private static final String COLUMN_STATUS = "status_column";
    private static final String COLUMN_AUTHOR = "status_author";
    private static final String COLUMN_COMMIT = "status_commit";
    private static final String COLUMN_MESSAGE = "status_message";
    private static final String COLUMN_CREATED = "status_created";
    private static final String COLUMN_ISSUES = "status_issues";
    private static final String SELECT_COMMIT = "//*[@href=\"/app/qateste/landing-page-2018/commit?bid=28252&cid=8665066\"]";
    private static final String COMMIT_DETAIL_STATUS_BANNER = "commit_status_banner_invite_container";
    private static final String COMMIT_DETAIL_CONTAINER = "commit_detail_container";
    private static final String COMMIT_DETAIL_TIME = "//*[@class=\"commit-time\"]";
    private static final String COMMIT_DETAIL_DESCRIPTION = "commit_description";
    private static final String COMMIT_DETAIL_STATUS = "commit_status_container";
    private static final String COMMIT_METRICS_DETAIL_CONTAINER = "commit_metrics";
    private static final String METRICS_ISSUES_TOOLTIP = "issues_tooltip";
    private static final String METRICS_DUPLICATION_TOOLTIP = "duplication_tooltip";
    private static final String METRICS_COMPLEXITY_TOOLTIP = "complexity-tooltip";
    private static final String METRICS_COVERAGE_TOOLTIP = "coverage_tooltip";
    private static final String COMMIT_DETAIL_TAB_CONTAINER = "commit_detail_tabs";
    private static final String DETAIL_NEW_ISSUE_TAB = "new_issues_tab";
    private static final String DETAIL_NEW_ISSUE_PANEL = "newIssuesView";
    private static final String DETAIL_FIXED_ISSUE_TAB = "fixed_issues_tab" ;
    private static final String DETAIL_FIXED_ISSUE_PANEL = "fixedIssuesView";
    private static final String DETAIL_NEW_DUPLICATION_TAB = "new_duplication_tab";
    private static final String DETAIL_NEW_DUPLICATION_PANEL = "newClonesView";
    private static final String DETAIL_FIXED_DUPLICATION_TAB = "fixed_duplication_tab";
    private static final String DETAIL_FIXED_DUPLICATION_PANEL = "fixedClonesView";
    private static final String DETAIL_FILES_TAB = "files_tab";
    private static final String DETAIL_FILES_PANEL = "filesView";
    private static final String DETAIL_DIFF_TAB = "diffToggle";
    private static final String DETAIL_DIFF_PANEL = "diffView";
    private static final String COMMIT_VIEW_LOGS = "logs_open";


}
