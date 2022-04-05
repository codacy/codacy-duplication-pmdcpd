package pages;

import org.codacy.BasePage;
import org.codacy.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.print.DocFlavor;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

public class Autenthication {

    private static final String NAV_BAR_LOGIN_LINK = "login_link";
    private static final String VALIDATE_LOGIN_PAGE = "Welcome_Back_login";
    private static final String GOOGLE_BUTTON = "login_google";
    private static final String SELECT_USER_NAME = "identifierId";
    private static final String NEXT_BTN = "identifierNext";
    private static final String SELECT_PASSWORD_TXT = "//*[@type=\"password\"]";
    private static final String PASSWORD_NEXT_BTN = "passwordNext";
    private static final String BITBUCKET_BTN = "login_bitbucket";
    private static final String LOGIN_FORM = "login-form";
    private static final String LOG_IN_WITH_GOOGLE = "//*[@class='google-login']";
    private static final String GITHUB_BTN = "login_github";
    private static final String GITHUB_LOGIN_FORM = "class=\"auth-form-body mt-3\"";
    private static final String GITHUB_LOGIN_TXT = "login_field";
    private static final String GITHUB_PASSWORD_TXT = "password";
    private static final String GITHUB_SUBMIT = "commit";
    private static final String SELECT_ACCOUNT = "account-dropdown";
    private static final String SIGN_OUT_BTN = "signout-form";
    private static final String TESTE = "//*[@class='ANuIbb IdAqtf']";

}

